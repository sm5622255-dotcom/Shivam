package com.example.model

enum class Suit(val symbol: String, val isRed: Boolean, val displayName: String) {
    HEARTS("♥", true, "Hearts"),
    DIAMONDS("♦", true, "Diamonds"),
    CLUBS("♣", false, "Clubs"),
    SPADES("♠", false, "Spades")
}

enum class Rank(val representation: String, val basePoints: Int, val order: Int) {
    ACE("A", 15, 1),
    TWO("2", 5, 2),
    THREE("3", 5, 3),
    FOUR("4", 5, 4),
    FIVE("5", 5, 5),
    SIX("6", 5, 6),
    SEVEN("7", 5, 7),
    EIGHT("8", 5, 8),
    NINE("9", 5, 9),
    TEN("10", 10, 10),
    JACK("J", 10, 11),
    QUEEN("Q", 10, 12),
    KING("K", 10, 13)
}

data class Card(
    val id: String,
    val rank: Rank,
    val suit: Suit
) {
    val points: Int get() = rank.basePoints
}

object RummyRules {
    
    // Check if the given set of cards forms a valid Rummy Set (same rank, different suits)
    fun isValidSet(cards: List<Card>): Boolean {
        if (cards.size < 3) return false
        val rank = cards.first().rank
        val suits = cards.map { it.suit }
        // All cards must have the same rank and unique suits
        return cards.all { it.rank == rank } && suits.distinct().size == cards.size
    }

    // Check if the given set of cards forms a valid Rummy Sequence (consecutive ranks, same suit)
    fun isValidSequence(cards: List<Card>): Boolean {
        if (cards.size < 3) return false
        val suit = cards.first().suit
        if (!cards.all { it.suit == suit }) return false

        // Check if there is an Ace in the sequence
        val hasAce = cards.any { it.rank == Rank.ACE }
        
        if (hasAce) {
            // Test Ace as 1
            val ordersLow = cards.map { if (it.rank == Rank.ACE) 1 else it.rank.order }.sorted()
            if (isConsecutive(ordersLow)) return true
            
            // Test Ace as 14
            val ordersHigh = cards.map { if (it.rank == Rank.ACE) 14 else it.rank.order }.sorted()
            if (isConsecutive(ordersHigh)) return true
            
            return false
        } else {
            val orders = cards.map { it.rank.order }.sorted()
            return isConsecutive(orders)
        }
    }

    private fun isConsecutive(orders: List<Int>): Boolean {
        for (i in 0 until orders.size - 1) {
            if (orders[i + 1] != orders[i] + 1) return false
        }
        return true
    }

    // Check if a subset of cards plus 1 card from the Row can form a valid Set or Sequence
    fun checkMatch(rowCard: Card, handCards: List<Card>): Boolean {
        val totalCards = handCards + rowCard
        return isValidSet(totalCards) || isValidSequence(totalCards)
    }

    /**
     * Find all combinations of 2 cards from hand that can form a Set or Sequence with a given Row card.
     * Helpful for AI and for showing hints to user.
     */
    fun findValidMatches(rowCard: Card, hand: List<Card>): List<List<Card>> {
        val validPairs = mutableListOf<List<Card>>()
        if (hand.size < 2) return validPairs

        // Generate all pairs (order doesn't matter for sets, but let's keep unique combinations)
        for (i in 0 until hand.size) {
            for (j in i + 1 until hand.size) {
                val pair = listOf(hand[i], hand[j])
                if (checkMatch(rowCard, pair)) {
                    validPairs.add(pair)
                }
            }
        }
        return validPairs
    }
}
