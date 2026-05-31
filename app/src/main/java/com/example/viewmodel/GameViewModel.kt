package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Card
import com.example.model.Rank
import com.example.model.RummyRules
import com.example.model.Suit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

enum class GamePhase {
    STARTING,                // Intro, choosing dealer
    COLD_START_DISCARD,      // Dealer must choose 1 card to discard to start the Row
    PLAYER_TURN_DECIDING,    // Active player deciding to match (Option A), chain match (Option C), or draw (Option B)
    PLAYER_TURN_DISCARDING,  // Active player has drawn or matched, must now discard 1 card to Row
    GAME_OVER                // Deck is empty or a hand finished. Show full score summary
}

data class PlayerState(
    val id: Int,             // 1 = You (User), 2 = AI Left, 3 = AI Right
    val name: String,
    val hand: List<Card> = emptyList(),
    val captured: List<Card> = emptyList(),
    val openCard: Card? = null
) {
    // Points from captured cards
    val capturedPoints: Int get() = captured.sumOf { it.points }
    
    // Penalty points from cards left in hand
    val handPenaltyPoints: Int get() = hand.sumOf { it.points }
    
    // Total net score
    val netScore: Int get() = capturedPoints - handPenaltyPoints
}

enum class DeckStyle {
    GREEN_MODERN,
    BLUE_CLASSIC,
    RED_ORNATE
}

enum class TableStyle {
    FELT_GREEN,
    WALNUT_WOOD,
    WINTER_FOREST
}

data class MatchAction(
    val rowCardIndex: Int,
    val rowCard: Card,
    val handCards: List<Card>
)

data class GameUIState(
    val players: List<PlayerState> = emptyList(),
    val deck: List<Card> = emptyList(),
    val rowCards: List<Card> = emptyList(),
    val dealerId: Int = 1,
    val currentPlayerId: Int = 1,
    val phase: GamePhase = GamePhase.STARTING,
    val lastShownMatch: List<Card>? = null,
    val lastShownMatchPlayerId: Int? = null,
    val selectedRowCard: Card? = null,
    val selectedRowCardIndex: Int? = null,
    val selectedHandCards: List<Card> = emptyList(),
    val logs: List<String> = emptyList(),
    val lastActionDescription: String = "",
    val isAiThinking: Boolean = false,
    val revealedPlayerIds: Set<Int> = emptySet(),
    val playerCount: Int = 3,
    val deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC,
    val tableStyle: TableStyle = TableStyle.WINTER_FOREST
) {
    val currentPlayer: PlayerState? get() = players.find { it.id == currentPlayerId }
    val dealer: PlayerState? get() = players.find { it.id == dealerId }
    val userPlayer: PlayerState get() = players.firstOrNull { it.id == 1 } ?: PlayerState(1, "You")
    val aiLeftPlayer: PlayerState get() = players.firstOrNull { it.id == 2 } ?: PlayerState(2, "AI Left")
    val aiRightPlayer: PlayerState get() = players.firstOrNull { it.id == 3 } ?: PlayerState(3, "AI Right")
    val aiTopPlayer: PlayerState get() = players.firstOrNull { it.id == 4 } ?: PlayerState(4, "Meena.S")
}

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUIState())
    private var aiJob: Job? = null
    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    init {
        resetGame()
    }

    fun resetGame() {
        aiJob?.cancel()
        _uiState.value = GameUIState(
            players = listOf(
                PlayerState(1, "You"),
                PlayerState(2, "AI Left"),
                PlayerState(3, "AI Right"),
                PlayerState(4, "Meena.S")
            ),
            phase = GamePhase.STARTING,
            logs = listOf("Welcome to Rummy Row! Select Player Count and a Dealer to begin."),
            playerCount = 3
        )
    }

    // Set dealer and start dealing cards
    fun startNewGame(selectedDealerId: Int, playerCount: Int = 3) {
        aiJob?.cancel()
        viewModelScope.launch {
            // Keep player count temporarily in local context for name resolution before state is fully built
            val dealerName = when (selectedDealerId) {
                1 -> "You"
                2 -> if (playerCount == 2) "AI Opponent" else "AI Left"
                3 -> "AI Right"
                4 -> "Meena.S"
                else -> "Unknown"
            }
            addLog("The dealer is $dealerName. Dealing cards...")
            
            // Create and shuffle standard 52-card deck
            val fullDeck = createShuffledDeck()
            
            // Deal cards
            // Each player gets 6 cards. Dealer gets 7 (Dealer's Privilege).
            var deckIndex = 0
            val p1Hand = mutableListOf<Card>()
            val p2Hand = mutableListOf<Card>()
            val p3Hand = mutableListOf<Card>()
            val p4Hand = mutableListOf<Card>()

            // Dealer ID mappings:
            // P1 gets 7 if dealer, else 6
            val p1Size = if (selectedDealerId == 1) 7 else 6
            val p2Size = if (selectedDealerId == 2) 7 else 6
            val p3Size = if (selectedDealerId == 3 && playerCount >= 3) 7 else 6
            val p4Size = if (selectedDealerId == 4 && playerCount == 4) 7 else 6

            repeat(p1Size) { p1Hand.add(fullDeck[deckIndex++]) }
            repeat(p2Size) { p2Hand.add(fullDeck[deckIndex++]) }
            if (playerCount >= 3) {
                repeat(p3Size) { p3Hand.add(fullDeck[deckIndex++]) }
            }
            if (playerCount == 4) {
                repeat(p4Size) { p4Hand.add(fullDeck[deckIndex++]) }
            }

            val remainingDeck = fullDeck.subList(deckIndex, fullDeck.size).toList()

            val listPlayers = if (playerCount == 2) {
                listOf(
                    PlayerState(1, "You", sortHand(p1Hand)),
                    PlayerState(2, "AI Opponent", sortHand(p2Hand))
                )
            } else if (playerCount == 3) {
                listOf(
                    PlayerState(1, "You", sortHand(p1Hand)),
                    PlayerState(2, "AI Left", sortHand(p2Hand)),
                    PlayerState(3, "AI Right", sortHand(p3Hand))
                )
            } else {
                listOf(
                    PlayerState(1, "You", sortHand(p1Hand)),
                    PlayerState(2, "AI Left", sortHand(p2Hand)),
                    PlayerState(3, "AI Right", sortHand(p3Hand)),
                    PlayerState(4, "Meena.S", sortHand(p4Hand))
                )
            }

            _uiState.value = _uiState.value.copy(
                players = listPlayers,
                deck = remainingDeck,
                rowCards = emptyList(),
                dealerId = selectedDealerId,
                currentPlayerId = selectedDealerId,
                phase = GamePhase.COLD_START_DISCARD,
                lastShownMatch = null,
                lastShownMatchPlayerId = null,
                selectedRowCard = null,
                selectedRowCardIndex = null,
                selectedHandCards = emptyList(),
                isAiThinking = false,
                playerCount = playerCount
            )

            addLog("$dealerName starts with 7 cards. (Dealer's Privilege!) Other players receive 6 cards.")
            
            // If dealer is AI, trigger AI start discard
            if (selectedDealerId != 1) {
                triggerAiTurn()
            }
        }
    }

    // Sort cards by Rank then Suit to display hand nicely
    private fun sortHand(cards: List<Card>): List<Card> {
        return cards.sortedWith(compareBy({ it.suit }, { it.rank.order }))
    }

    private fun createShuffledDeck(): List<Card> {
        val cards = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                val uniqueId = "${suit.name.first()}_${rank.representation}_${UUID.randomUUID().toString().take(4)}"
                cards.add(Card(uniqueId, rank, suit))
            }
        }
        return cards.shuffled()
    }

    private fun getPlayerName(id: Int): String {
        val count = _uiState.value.playerCount
        return when (id) {
            1 -> "You"
            2 -> if (count == 2) "AI Opponent" else "AI Left"
            3 -> "AI Right"
            4 -> "Meena.S"
            else -> "Unknown"
        }
    }

    private fun addLog(message: String) {
        val currentLogs = _uiState.value.logs.toMutableList()
        currentLogs.add(0, message) // Newest logs first
        _uiState.value = _uiState.value.copy(logs = currentLogs)
    }

    // Helper to move play to the Right-Hand Side (RHS)
    // Order: P1 (You) -> P3 (AI Right) -> P4 (Meena.S) -> P2 (AI Left) -> P1
    private fun getNextPlayerId(currentId: Int): Int {
        val count = _uiState.value.playerCount
        if (count == 2) {
            // 2 Players: alternating 1 and 2
            return if (currentId == 1) 2 else 1
        }
        if (count == 3) {
            return when (currentId) {
                1 -> 3
                3 -> 2
                2 -> 1
                else -> 1
            }
        }
        // 4 Players
        return when (currentId) {
            1 -> 3
            3 -> 4
            4 -> 2
            2 -> 1
            else -> 1
        }
    }

    private fun checkGameEndConditions(): Boolean {
        // Condition 1: Any player runs out of cards
        val outOfCardsPlayer = _uiState.value.players.find { it.hand.isEmpty() }
        if (outOfCardsPlayer != null) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.GAME_OVER)
            addLog("Game ended because ${outOfCardsPlayer.name} cleared their hand!")
            return true
        }

        // Condition 2: Draw deck is completely empty
        if (_uiState.value.deck.isEmpty()) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.GAME_OVER)
            addLog("Game ended because the Draw Deck is empty!")
            return true
        }

        return false
    }

    // Dealer cold-starts the game by discarding 1 card.
    fun coldStartDiscard(card: Card) {
        if (_uiState.value.currentPlayerId != 1 || _uiState.value.isAiThinking || _uiState.value.phase != GamePhase.COLD_START_DISCARD) return

        val p1 = _uiState.value.userPlayer
        if (card !in p1.hand) return

        val newHand = p1.hand - card
        val newRow = listOf(card)

        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players.map { if (it.id == 1) it.copy(hand = newHand, openCard = card) else it },
            rowCards = newRow,
            lastActionDescription = "You started the central Row with ${card.rank.representation}${card.suit.symbol}"
        )

        addLog("You discarded ${card.rank.representation}${card.suit.symbol} to start the Row.")

        // Advance to next player
        advanceTurn()
    }

    // Option A: Active player matches a card from the Row
    fun matchRowCard(rowCardIndex: Int, handCards: List<Card>) {
        if (_uiState.value.currentPlayerId != 1 || _uiState.value.isAiThinking || _uiState.value.phase != GamePhase.PLAYER_TURN_DECIDING) return

        val state = _uiState.value
        if (rowCardIndex !in state.rowCards.indices) return
        val p1 = state.userPlayer
        val rowCard = state.rowCards[rowCardIndex]

        // Validate handCards exist in user's hand
        if (!handCards.all { it in p1.hand }) return

        // Validate minimum 2 cards in hand and valid Rummy match
        if (handCards.size < 2) return
        if (!RummyRules.checkMatch(rowCard, handCards)) return

        // Perform Advanced Row Interaction Capture!
        // Capture rowCard AND all cards after it in the cascading row.
        val capturedFromRow = state.rowCards.subList(rowCardIndex, state.rowCards.size).toList()
        val remainingRow = state.rowCards.subList(0, rowCardIndex).toList()

        // All captured cards (row cards and hand cards) go to user's captured pile
        val allCaptured = capturedFromRow + handCards
        val newHand = p1.hand - handCards
        val newCaptured = p1.captured + allCaptured

        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = newHand, captured = newCaptured) else it },
            rowCards = remainingRow,
            lastShownMatch = allCaptured,
            lastShownMatchPlayerId = 1,
            phase = GamePhase.PLAYER_TURN_DISCARDING,
            selectedRowCard = null,
            selectedRowCardIndex = null,
            selectedHandCards = emptyList(),
            lastActionDescription = "You matched ${rowCard.rank.representation}${rowCard.suit.symbol} from Row and captured ${capturedFromRow.size} card(s)!"
        )

        addLog("You matched ${rowCard.rank.representation}${rowCard.suit.symbol} with: ${handCards.joinToString { it.rank.representation + it.suit.symbol }}. Captured ${capturedFromRow.size} Row cards!")

        if (checkGameEndConditions()) {
            return
        }
    }

    // Option B: Active player draws a card
    fun drawCardFromDeck() {
        if (_uiState.value.currentPlayerId != 1 || _uiState.value.isAiThinking || _uiState.value.phase != GamePhase.PLAYER_TURN_DECIDING) return

        val state = _uiState.value
        if (state.deck.isEmpty()) return

        val drawnCard = state.deck.first()
        val remainingDeck = state.deck.drop(1)
        val p1 = state.userPlayer
        val newHand = p1.hand + drawnCard

        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = newHand) else it },
            deck = remainingDeck,
            phase = GamePhase.PLAYER_TURN_DISCARDING,
            selectedRowCard = null,
            selectedRowCardIndex = null,
            selectedHandCards = emptyList(),
            lastActionDescription = "You drew a card from the deck"
        )

        addLog("You drew a card. Choose one card from your hand to discard.")
    }

    // Option C: Chain Match. Add card to the newly shown combination from previous turn
    fun chainMatchCard(handCard: Card) {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking || state.phase != GamePhase.PLAYER_TURN_DECIDING) return

        val lastMatch = state.lastShownMatch
        if (lastMatch == null || lastMatch.isEmpty()) return

        // Check if handCard matches the last shown combination
        if (!isCardMatchingCombination(handCard, lastMatch)) return

        val p1 = state.userPlayer
        val newHand = p1.hand - handCard
        val newCaptured = p1.captured + handCard

        // Update the last shown match visual so others can continue matching if they want
        val updatedMatch = lastMatch + handCard

        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = newHand, captured = newCaptured) else it },
            lastShownMatch = updatedMatch,
            phase = GamePhase.PLAYER_TURN_DISCARDING,
            lastActionDescription = "You extended the shown match with ${handCard.rank.representation}${handCard.suit.symbol} to score points!"
        )

        addLog("Chain Match! You added ${handCard.rank.representation}${handCard.suit.symbol} to the active board combination.")

        if (checkGameEndConditions()) {
            return
        }
    }

    // Meld cards purely from hand (no Row card requirement)
    fun meldHandCards(cards: List<Card>) {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking) return
        if (state.phase != GamePhase.PLAYER_TURN_DECIDING && state.phase != GamePhase.PLAYER_TURN_DISCARDING) return

        val p1 = state.userPlayer
        if (!cards.all { it in p1.hand }) return

        // Must be at least 3 cards to form a valid set or sequence purely from hand
        if (cards.size < 3) return
        if (!RummyRules.isValidSet(cards) && !RummyRules.isValidSequence(cards)) return

        val newHand = p1.hand - cards
        val newCaptured = p1.captured + cards

        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = newHand, captured = newCaptured) else it },
            selectedHandCards = emptyList(),
            lastShownMatch = cards,
            lastShownMatchPlayerId = 1,
            lastActionDescription = "You melded ${cards.joinToString { it.rank.representation + it.suit.symbol }} purely from hand!"
        )

        addLog("You melded ${cards.joinToString { it.rank.representation + it.suit.symbol }} purely from hand!")

        if (checkGameEndConditions()) {
            return
        }
    }

    // Helper for AI to find a pure set or sequence of 3+ cards from their hand
    fun scanPureMeldInHand(hand: List<Card>): List<Card> {
        if (hand.size < 3) return emptyList()
        // 1. Check Sets (Same Rank, Unique Suits)
        val rankGroups = hand.groupBy { it.rank }
        for ((_, cards) in rankGroups) {
            if (cards.size >= 3) {
                val uniqueSuits = cards.distinctBy { it.suit }
                if (uniqueSuits.size >= 3) {
                    return uniqueSuits.take(4)
                }
            }
        }

        // 2. Check Sequences (Consecutive orders, same suit)
        val suitGroups = hand.groupBy { it.suit }
        for ((_, cards) in suitGroups) {
            if (cards.size >= 3) {
                val sorted = cards.distinctBy { it.rank.order }.sortedBy { it.rank.order }
                var start = 0
                while (start < sorted.size - 2) {
                    var end = start
                    while (end < sorted.size - 1 && sorted[end + 1].rank.order == sorted[end].rank.order + 1) {
                        end++
                    }
                    if (end - start + 1 >= 3) {
                        return sorted.subList(start, end + 1)
                    }
                    start++
                }
            }
        }
        return emptyList()
    }

    private fun performAiMeldPurely(aiId: Int, cards: List<Card>) {
        val aiState = _uiState.value.players.firstOrNull { it.id == aiId } ?: return
        val newHand = aiState.hand - cards
        val newCaptured = aiState.captured + cards

        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), captured = newCaptured) else it },
            lastShownMatch = cards,
            lastShownMatchPlayerId = aiId,
            lastActionDescription = "${aiState.name} melded ${cards.joinToString { it.rank.representation + it.suit.symbol }} from hand!"
        )

        addLog("${aiState.name} melded ${cards.joinToString { it.rank.representation + it.suit.symbol }} purely from hand!")
    }

    // Helper to check if a single card can be added to an existing Rummy set or sequence
    fun isCardMatchingCombination(card: Card, combination: List<Card>): Boolean {
        if (combination.isEmpty()) return false
        
        // 1. Is it a Set?
        val firstItem = combination.first()
        val isAllSameRank = combination.all { it.rank == firstItem.rank }
        if (isAllSameRank) {
            // Can add if it has the same rank and we don't already have this suit in the combination
            return card.rank == firstItem.rank && combination.none { it.suit == card.suit }
        }

        // 2. Is it a Sequence?
        val isAllSameSuit = combination.all { it.suit == firstItem.suit }
        if (isAllSameSuit) {
            if (card.suit != firstItem.suit) return false

            // Check if adding it forms a valid sequence
            val testGroup = combination + card
            return RummyRules.isValidSequence(testGroup)
        }

        return false
    }

    // Finish turn by discarding 1 card to the central Row
    fun discardCard(card: Card) {
        if (_uiState.value.currentPlayerId != 1 || _uiState.value.isAiThinking || _uiState.value.phase != GamePhase.PLAYER_TURN_DISCARDING) return

        val p1 = _uiState.value.userPlayer
        if (card !in p1.hand) return

        val newHand = p1.hand - card
        val newRow = _uiState.value.rowCards + card

        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players.map { if (it.id == 1) it.copy(hand = newHand, openCard = card) else it },
            rowCards = newRow,
            lastActionDescription = "You discarded ${card.rank.representation}${card.suit.symbol} to Row"
        )

        addLog("You discarded ${card.rank.representation}${card.suit.symbol} onto the central Row.")

        advanceTurn()
    }

    // Hand selection helpers
    fun toggleHandCardSelection(card: Card) {
        val state = _uiState.value
        // Only allow selecting cards during the player's own active turn and not during AI thinking
        if (state.currentPlayerId != 1 || state.isAiThinking || state.phase == GamePhase.STARTING || state.phase == GamePhase.GAME_OVER) return

        val currentSelected = state.selectedHandCards.toMutableList()
        if (card in currentSelected) {
            currentSelected.remove(card)
        } else {
            currentSelected.add(card)
        }
        _uiState.value = state.copy(selectedHandCards = currentSelected)
    }

    // Move a card in the player's hand (arrange a single card)
    fun moveCardInHand(card: Card, toLeft: Boolean) {
        val state = _uiState.value
        val p1 = state.userPlayer
        val hand = p1.hand.toMutableList()
        val index = hand.indexOf(card)
        if (index == -1) return
        
        if (toLeft && index > 0) {
            // Swap with previous card
            hand.removeAt(index)
            hand.add(index - 1, card)
        } else if (!toLeft && index < hand.size - 1) {
            // Swap with next card
            hand.removeAt(index)
            hand.add(index + 1, card)
        }
        
        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = hand) else it }
        )
    }

    fun manualSortUserHand() {
        val state = _uiState.value
        val p1 = state.userPlayer
        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = sortHand(p1.hand)) else it }
        )
    }

    fun toggleRevealPlayerCards(playerId: Int) {
        val currentRevealed = _uiState.value.revealedPlayerIds
        val newRevealed = if (playerId in currentRevealed) {
            currentRevealed - playerId
        } else {
            currentRevealed + playerId
        }
        _uiState.value = _uiState.value.copy(revealedPlayerIds = newRevealed)
    }

    fun selectDeckStyle(style: DeckStyle) {
        _uiState.value = _uiState.value.copy(deckStyle = style)
        addLog("Changed playing card design style to ${style.name}.")
    }

    fun selectTableStyle(style: TableStyle) {
        _uiState.value = _uiState.value.copy(tableStyle = style)
        addLog("Changed table felt design to ${style.name}.")
    }

    fun placeSingleCardOnRow(card: Card) {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking) return
        val p1 = state.userPlayer
        if (card !in p1.hand) return

        val newHand = p1.hand - card
        val newRow = state.rowCards + card

        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = newHand, openCard = card) else it },
            rowCards = newRow,
            selectedHandCards = emptyList(),
            lastActionDescription = "You placed ${card.rank.representation}${card.suit.symbol} onto the Row."
        )

        addLog("You placed hand card ${card.rank.representation}${card.suit.symbol} onto the Row.")

        if (state.phase == GamePhase.PLAYER_TURN_DISCARDING) {
            advanceTurn()
        } else {
            if (checkGameEndConditions()) return
        }
    }

    fun selectRowCard(card: Card, index: Int) {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking || state.phase != GamePhase.PLAYER_TURN_DECIDING) return

        if (state.selectedRowCard == card) {
            // Unselect
            _uiState.value = state.copy(selectedRowCard = null, selectedRowCardIndex = null)
        } else {
            // Select
            _uiState.value = state.copy(selectedRowCard = card, selectedRowCardIndex = index)
        }
    }

    private fun advanceTurn() {
        if (checkGameEndConditions()) {
            return
        }

        val nextPlayerId = getNextPlayerId(_uiState.value.currentPlayerId)
        
        _uiState.value = _uiState.value.copy(
            currentPlayerId = nextPlayerId,
            phase = GamePhase.PLAYER_TURN_DECIDING,
            selectedRowCard = null,
            selectedRowCardIndex = null,
            selectedHandCards = emptyList()
        )

        addLog("It is now ${getPlayerName(nextPlayerId)}'s turn.")

        if (nextPlayerId != 1) {
            triggerAiTurn()
        }
    }

    // AI Gameplay Logic
    private fun triggerAiTurn() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiThinking = true)
            // AI simulated thinking delay
            delay(1500)
            
            val aiId = _uiState.value.currentPlayerId
            val aiState = _uiState.value.players.firstOrNull { it.id == aiId } ?: return@launch

            if (_uiState.value.phase == GamePhase.COLD_START_DISCARD) {
                // Cold start discard
                val cardToDiscard = selectWorstCardForAI(aiState.hand)
                val newHand = aiState.hand - cardToDiscard
                val newRow = listOf(cardToDiscard)

                _uiState.value = _uiState.value.copy(
                    players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), openCard = cardToDiscard) else it },
                    rowCards = newRow,
                    lastActionDescription = "${aiState.name} started the Row with ${cardToDiscard.rank.representation}${cardToDiscard.suit.symbol}",
                    isAiThinking = false
                )

                addLog("${aiState.name} discarded ${cardToDiscard.rank.representation}${cardToDiscard.suit.symbol} to start the Row.")
                advanceTurn()
                return@launch
            }

            // Normal turn
            // 00. AI plays a consecutive/subsequent card if they hold it relative to previous player's openCard
            val prevId = getPreviousPlayerId(aiId)
            val prevPlayer = _uiState.value.players.find { it.id == prevId }
            val refCard = prevPlayer?.openCard
            if (refCard != null) {
                val consecutiveCard = aiState.hand.find { isConsecutiveCard(it, refCard) }
                if (consecutiveCard != null) {
                    val newHand = aiState.hand - consecutiveCard
                    val newCaptured = aiState.captured + consecutiveCard
                    _uiState.value = _uiState.value.copy(
                        players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), captured = newCaptured, openCard = consecutiveCard) else it },
                        lastActionDescription = "${aiState.name} played consecutive card ${consecutiveCard.rank.representation}${consecutiveCard.suit.symbol} on their side!",
                        isAiThinking = false
                    )
                    addLog("${aiState.name} showed consecutive card ${consecutiveCard.rank.representation}${consecutiveCard.suit.symbol} next to them (consecutive to ${prevPlayer.name}'s ${refCard.rank.representation}${refCard.suit.symbol})!")
                    delay(1200)
                    if (checkGameEndConditions()) return@launch
                }
            }

            // AI searches for any pure meld in its hand before doing other choices!
            val aiStateAfterConsecutive = _uiState.value.players.firstOrNull { it.id == aiId } ?: return@launch
            val preMeld = scanPureMeldInHand(aiStateAfterConsecutive.hand)
            if (preMeld.isNotEmpty()) {
                performAiMeldPurely(aiId, preMeld)
                if (checkGameEndConditions()) return@launch
            }

            // 1. Check if AI can make an Option A Match from the Row
            var bestMatchRowIndex: Int? = null
            var bestMatchHandCards: List<Card>? = null
            var maxCapturedCount = -1

            // Re-read status after possible pure meld
            val aiStateCur = _uiState.value.players.firstOrNull { it.id == aiId } ?: return@launch

            // Evaluate Row cards starting from the beginning of Row (index 0 gets maximum cascade capture)
            for (i in 0 until _uiState.value.rowCards.size) {
                val rowCard = _uiState.value.rowCards[i]
                val validMatches = RummyRules.findValidMatches(rowCard, aiStateCur.hand)
                if (validMatches.isNotEmpty()) {
                    // Check size of row capture: rowCards.size - i
                    val captureSize = _uiState.value.rowCards.size - i
                    if (captureSize > maxCapturedCount) {
                        maxCapturedCount = captureSize
                        bestMatchRowIndex = i
                        // Pick the first match combination from hand
                        bestMatchHandCards = validMatches.first()
                    }
                }
            }

            if (bestMatchRowIndex != null && bestMatchHandCards != null) {
                // Carry out Option A
                val rowCard = _uiState.value.rowCards[bestMatchRowIndex]
                val capturedFromRow = _uiState.value.rowCards.subList(bestMatchRowIndex, _uiState.value.rowCards.size).toList()
                val remainingRow = _uiState.value.rowCards.subList(0, bestMatchRowIndex).toList()

                val allCaptured = capturedFromRow + bestMatchHandCards
                val newHand = aiStateCur.hand - bestMatchHandCards
                val newCaptured = aiStateCur.captured + allCaptured

                _uiState.value = _uiState.value.copy(
                    players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), captured = newCaptured) else it },
                    rowCards = remainingRow,
                    lastShownMatch = allCaptured,
                    lastShownMatchPlayerId = aiId,
                    lastActionDescription = "${aiStateCur.name} matched ${rowCard.rank.representation}${rowCard.suit.symbol} and captured ${capturedFromRow.size} card(s)!",
                    isAiThinking = false
                )

                addLog("${aiStateCur.name} matched Row card ${rowCard.rank.representation}${rowCard.suit.symbol} with ${bestMatchGroup(bestMatchHandCards)}. Captured ${capturedFromRow.size} Row cards!")

                if (checkGameEndConditions()) return@launch

                // Discard 1 card sequentially in the same coroutine
                performAiDiscardSequential(aiId)
                return@launch
            }

            // 2. Check Option C: Chain Match
            val lastMatch = _uiState.value.lastShownMatch
            if (lastMatch != null && lastMatch.isNotEmpty()) {
                val matchableCard = aiStateCur.hand.find { isCardMatchingCombination(it, lastMatch) }
                if (matchableCard != null) {
                    val newHand = aiStateCur.hand - matchableCard
                    val newCaptured = aiStateCur.captured + matchableCard
                    val updatedMatch = lastMatch + matchableCard

                    _uiState.value = _uiState.value.copy(
                        players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), captured = newCaptured) else it },
                        lastShownMatch = updatedMatch,
                        lastActionDescription = "${aiStateCur.name} performed a Chain Match with ${matchableCard.rank.representation}${matchableCard.suit.symbol}!",
                        isAiThinking = false
                    )

                    addLog("Chain Match! ${aiStateCur.name} added ${matchableCard.rank.representation}${matchableCard.suit.symbol} to the active board combination.")
                    
                    if (checkGameEndConditions()) return@launch

                    performAiDiscardSequential(aiId)
                    return@launch
                }
            }

            // 3. Option B: Draw 1 card from Deck
            if (_uiState.value.deck.isNotEmpty()) {
                val drawn = _uiState.value.deck.first()
                val remainingDeck = _uiState.value.deck.drop(1)
                val newHand = aiStateCur.hand + drawn

                _uiState.value = _uiState.value.copy(
                    players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand)) else it },
                    deck = remainingDeck,
                    lastActionDescription = "${aiStateCur.name} drew a card",
                    isAiThinking = false
                )

                addLog("${aiStateCur.name} drew a card.")

                if (checkGameEndConditions()) return@launch

                // After drawing card, AI checks again if they can form a pure meld from hand!
                val aiStatePostDraw = _uiState.value.players.firstOrNull { it.id == aiId }
                if (aiStatePostDraw != null) {
                    val postMeld = scanPureMeldInHand(aiStatePostDraw.hand)
                    if (postMeld.isNotEmpty()) {
                        performAiMeldPurely(aiId, postMeld)
                        if (checkGameEndConditions()) return@launch
                    }
                }

                performAiDiscardSequential(aiId)
            } else {
                // Fallback (should be covered by checkGameEndConditions)
                _uiState.value = _uiState.value.copy(isAiThinking = false)
                advanceTurn()
            }
        }
    }

    private fun bestMatchGroup(cards: List<Card>): String {
        return cards.joinToString { it.rank.representation + it.suit.symbol }
    }

    private suspend fun performAiDiscardSequential(aiId: Int) {
        _uiState.value = _uiState.value.copy(isAiThinking = true)
        // AI simulated thinking to choose card to discard
        delay(1200)

        val aiState = _uiState.value.players.firstOrNull { it.id == aiId } ?: return
        if (aiState.hand.isNotEmpty()) {
            val cardToDiscard = selectWorstCardForAI(aiState.hand)
            val newHand = aiState.hand - cardToDiscard
            val newRow = _uiState.value.rowCards + cardToDiscard

            _uiState.value = _uiState.value.copy(
                players = _uiState.value.players.map { if (it.id == aiId) it.copy(hand = sortHand(newHand), openCard = cardToDiscard) else it },
                rowCards = newRow.toList(),
                lastActionDescription = "${aiState.name} discarded ${cardToDiscard.rank.representation}${cardToDiscard.suit.symbol}",
                isAiThinking = false
            )

            addLog("${aiState.name} discarded ${cardToDiscard.rank.representation}${cardToDiscard.suit.symbol} onto the Row.")
        }

        _uiState.value = _uiState.value.copy(isAiThinking = false)
        advanceTurn()
    }

    // AI strategy helper to select card with least potential in its hand
    private fun selectWorstCardForAI(hand: List<Card>): Card {
        if (hand.isEmpty()) return Card("dummy", Rank.TWO, Suit.HEARTS)
        if (hand.size == 1) return hand.first()

        // 1. Group cards to understand clusters
        val rankGroups = hand.groupBy { it.rank }
        val suitGroups = hand.groupBy { it.suit }

        // Find cards that are "islands" - not part of any near sequence (same suit, diff of <=2) and not part of a set
        val scores = hand.map { card ->
            var score = 0 // Higher score means we want to KEEP it, lower means we want to DISCARD it

            // Set potential
            val rankCount = rankGroups[card.rank]?.size ?: 0
            if (rankCount >= 3) {
                score += 50 // Already forms a set! Very valuable
            } else if (rankCount == 2) {
                score += 15 // Pair, holds potential
            }

            // Sequence potential
            val suitCards = suitGroups[card.suit] ?: emptyList()
            var sequencePotentialMark = 0
            for (other in suitCards) {
                if (other != card) {
                    val diff = Math.abs(other.rank.order - card.rank.order)
                    if (diff == 1) {
                        sequencePotentialMark += 15 // Consecutive! Keep
                    } else if (diff == 2) {
                        sequencePotentialMark += 5 // One card gap, high potential
                    }
                }
            }
            score += sequencePotentialMark

            // Points value: high point cards like Aces (15) or Kings (10) are good if they match,
            // but if they don't, they are high risk because of the Penalty deduction!
            // So if they have no set/sequence potential, discard high-point cards first!
            if (rankCount < 2 && sequencePotentialMark == 0) {
                // High penalty card without any potential? Discard ASAP!
                score -= card.points // Subtract points to make it more attractive to discard (lower final score)
            } else {
                // Add points as minor secondary tiebreaker for cards of equal potential
                score += card.points / 10
            }

            Pair(card, score)
        }

        // Return the card with the lowest raw score
        return scores.minByOrNull { it.second }?.first ?: hand.first()
    }

    fun getPreviousPlayerId(currentId: Int): Int {
        val count = _uiState.value.playerCount
        if (count == 2) {
            return if (currentId == 1) 2 else 1
        }
        if (count == 3) {
            return when (currentId) {
                1 -> 2
                3 -> 1
                2 -> 3
                else -> 1
            }
        }
        // 4 Players
        return when (currentId) {
            1 -> 2
            3 -> 1
            4 -> 3
            2 -> 4
            else -> 1
        }
    }

    fun isConsecutiveCard(card: Card, refCard: Card): Boolean {
        return card.suit == refCard.suit && (card.rank.order == refCard.rank.order + 1 || card.rank.order == refCard.rank.order - 1)
    }

    fun playConsecutiveOnSide(card: Card) {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking) return
        
        val p1 = state.userPlayer
        if (card !in p1.hand) return
        
        val prevId = getPreviousPlayerId(1)
        val prevPlayer = state.players.find { it.id == prevId } ?: return
        val refCard = prevPlayer.openCard ?: return
        
        if (!isConsecutiveCard(card, refCard)) return
        
        // Play consecutive card on their side:
        // Move from hand to user's captures (to score points) and set as user's openCard
        val newHand = p1.hand - card
        val newCaptured = p1.captured + card
        
        _uiState.value = state.copy(
            players = state.players.map { 
                if (it.id == 1) it.copy(hand = newHand, captured = newCaptured, openCard = card) else it 
            },
            lastActionDescription = "You played ${card.rank.representation}${card.suit.symbol} on your side consecutive to ${prevPlayer.name}'s open card!"
        )
        
        addLog("Consecutive Play! You showed ${card.rank.representation}${card.suit.symbol} next to your avatar (consecutive to ${prevPlayer.name}'s ${refCard.rank.representation}${refCard.suit.symbol}).")
        
        if (checkGameEndConditions()) {
            return
        }
    }

    fun smartArrangeUserHand() {
        val state = _uiState.value
        val p1 = state.userPlayer
        val hand = p1.hand
        val sortedHand = smartArrangeHandCards(hand)
        
        _uiState.value = state.copy(
            players = state.players.map { if (it.id == 1) it.copy(hand = sortedHand) else it },
            lastActionDescription = "AI smart-arranged your hand to group runs and sets!"
        )
        addLog("AI sorted and arranged your hand into smart Rummy groupings.")
    }

    private fun smartArrangeHandCards(hand: List<Card>): List<Card> {
        if (hand.isEmpty()) return emptyList()
        
        // Scan for sets
        val rankGroups = hand.groupBy { it.rank }
        val sets = rankGroups.filter { it.value.size >= 2 }.values.flatten()
        
        // Scan for sequences / potential sequences
        val suitGroups = hand.groupBy { it.suit }
        val sequences = mutableListOf<Card>()
        for ((_, cards) in suitGroups) {
            val sorted = cards.distinctBy { it.rank.order }.sortedBy { it.rank.order }
            for (i in 0 until sorted.size - 1) {
                if (Math.abs(sorted[i+1].rank.order - sorted[i].rank.order) <= 2) {
                    sequences.add(sorted[i])
                    sequences.add(sorted[i+1])
                }
            }
        }
        val sequenceCards = sequences.distinct()
        
        // Prioritize: Sets cards, then Run/Sequence cards, then others
        val meldedCards = (sets + sequenceCards).distinct()
        val otherCards = hand - meldedCards
        
        // Return fully sorted and cleanly grouped hand
        return meldedCards + otherCards.sortedWith(compareBy({ it.suit }, { it.rank.order }))
    }

    fun getAiHint(): String {
        val state = _uiState.value
        if (state.phase == GamePhase.STARTING || state.phase == GamePhase.GAME_OVER) return "The game has not started yet!"
        if (state.currentPlayerId != 1) return "Wait for AI players to finish their turn."
        
        val p1 = state.userPlayer
        
        if (state.phase == GamePhase.COLD_START_DISCARD) {
            val worst = selectWorstCardForAI(p1.hand)
            return "AI Hint: As the Dealer, you have the Dealer's Privilege (7 cards). Tap and discard ${worst.rank.representation}${worst.suit.symbol} to start the Row."
        }
        
        if (state.phase == GamePhase.PLAYER_TURN_DECIDING) {
            // 1. Is there a consecutive play available?
            val prevId = getPreviousPlayerId(1)
            val prevPlayer = state.players.find { it.id == prevId }
            val prevOpen = prevPlayer?.openCard
            if (prevOpen != null) {
                val consecutive = p1.hand.find { isConsecutiveCard(it, prevOpen) }
                if (consecutive != null) {
                    return "AI Hint: Play Consecutive! Show ${consecutive.rank.representation}${consecutive.suit.symbol} next to your avatar to score points without discarding."
                }
            }
            
            // 2. Is there a match from Row?
            var bestMatchRowIndex: Int? = null
            var bestMatchHandCards: List<Card>? = null
            var maxCapturedCount = -1
            
            for (i in 0 until state.rowCards.size) {
                val rowCard = state.rowCards[i]
                val validMatches = RummyRules.findValidMatches(rowCard, p1.hand)
                if (validMatches.isNotEmpty()) {
                    val captureSize = state.rowCards.size - i
                    if (captureSize > maxCapturedCount) {
                        maxCapturedCount = captureSize
                        bestMatchRowIndex = i
                        bestMatchHandCards = validMatches.first()
                    }
                }
            }
            
            if (bestMatchRowIndex != null && bestMatchHandCards != null) {
                val rc = state.rowCards[bestMatchRowIndex]
                return "AI Hint: Match Row Card! Select ${rc.rank.representation}${rc.suit.symbol} from Row and cards ${bestMatchHandCards.joinToString { it.rank.representation + it.suit.symbol }} from your hand, then tap 'Match Card'."
            }
            
            // 3. Chain match extension?
            val lastM = state.lastShownMatch
            if (lastM != null && lastM.isNotEmpty()) {
                val chainable = p1.hand.find { isCardMatchingCombination(it, lastM) }
                if (chainable != null) {
                    return "AI Hint: Chain Match! Select ${chainable.rank.representation}${chainable.suit.symbol} and tap 'Chain Match'."
                }
            }
            
            // 4. Pure meld from hand?
            val pureMeld = scanPureMeldInHand(p1.hand)
            if (pureMeld.isNotEmpty()) {
                return "AI Hint: Hand Meld! Select ${pureMeld.joinToString { it.rank.representation + it.suit.symbol }} from hand and tap 'Meld Hand'."
            }
            
            return "AI Hint: No direct matches available. Tap the Draw Deck to draw a new card."
        }
        
        if (state.phase == GamePhase.PLAYER_TURN_DISCARDING) {
            val worst = selectWorstCardForAI(p1.hand)
            return "AI Hint: Choose card to discard. AI recommends tapping ${worst.rank.representation}${worst.suit.symbol} and clicking 'Discard' to end your turn."
        }
        
        return "Hint: Follow your instincts!"
    }

    fun autoPlayRecommendedMove() {
        val state = _uiState.value
        if (state.currentPlayerId != 1 || state.isAiThinking) return
        
        val p1 = state.userPlayer
        
        if (state.phase == GamePhase.COLD_START_DISCARD) {
            val worst = selectWorstCardForAI(p1.hand)
            coldStartDiscard(worst)
            return
        }
        
        if (state.phase == GamePhase.PLAYER_TURN_DECIDING) {
            // 1. Play Consecutive on Side if available
            val prevId = getPreviousPlayerId(1)
            val prevPlayer = state.players.find { it.id == prevId }
            val prevOpen = prevPlayer?.openCard
            if (prevOpen != null) {
                val consecutive = p1.hand.find { isConsecutiveCard(it, prevOpen) }
                if (consecutive != null) {
                    playConsecutiveOnSide(consecutive)
                    return
                }
            }
            
            // 2. Row Match
            var bestMatchRowIndex: Int? = null
            var bestMatchHandCards: List<Card>? = null
            var maxCapturedCount = -1
            
            for (i in 0 until state.rowCards.size) {
                val rowCard = state.rowCards[i]
                val validMatches = RummyRules.findValidMatches(rowCard, p1.hand)
                if (validMatches.isNotEmpty()) {
                    val captureSize = state.rowCards.size - i
                    if (captureSize > maxCapturedCount) {
                        maxCapturedCount = captureSize
                        bestMatchRowIndex = i
                        bestMatchHandCards = validMatches.first()
                    }
                }
            }
            
            if (bestMatchRowIndex != null && bestMatchHandCards != null) {
                matchRowCard(bestMatchRowIndex, bestMatchHandCards)
                return
            }
            
            // 3. Chain Match extension
            val lastM = state.lastShownMatch
            if (lastM != null && lastM.isNotEmpty()) {
                val chainable = p1.hand.find { isCardMatchingCombination(it, lastM) }
                if (chainable != null) {
                    chainMatchCard(chainable)
                    return
                }
            }
            
            // 4. Pure Meld
            val pureMeld = scanPureMeldInHand(p1.hand)
            if (pureMeld.isNotEmpty()) {
                meldHandCards(pureMeld)
                return
            }
            
            // Draw Deck (Default action)
            drawCardFromDeck()
            return
        }
        
        if (state.phase == GamePhase.PLAYER_TURN_DISCARDING) {
            val worst = selectWorstCardForAI(p1.hand)
            discardCard(worst)
        }
    }
}
