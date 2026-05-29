package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.viewmodel.*

// Themed Colors
val TableGreenDark = Color(0xFF0F4C20)
val TableGreenLight = Color(0xFF135C28)
val CasinoGold = Color(0xFFFFBF00)
val CardCream = Color(0xFFFDFBF7)
val CardRed = Color(0xFFC62828)
val CardBlack = Color(0xFF212121)
val CaptionGreen = Color(0xFF81C784)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    // Gradient background for casino table felt
    val tableGradient = Brush.radialGradient(
        colors = listOf(TableGreenLight, TableGreenDark),
        radius = 1600f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tableGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. GAME TOP BAR (Title & Deck Information)
            GameTopHeader(
                deckCount = state.deck.size,
                phase = state.phase,
                onResetClick = { viewModel.resetGame() }
            )

            // 2. MAIN FELT TABLE AREA (Fills space between header and player deck)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // RENDER PLAYERS CORNERS (AI LEFT AND AI RIGHT)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // AI LEFT (Player 2)
                    AiPlayerPanel(
                        player = state.aiLeftPlayer,
                        isCurrent = state.currentPlayerId == 2,
                        isDealer = state.dealerId == 2,
                        alignRight = false
                    )

                    // AI RIGHT (Player 3)
                    AiPlayerPanel(
                        player = state.aiRightPlayer,
                        isCurrent = state.currentPlayerId == 3,
                        isDealer = state.dealerId == 3,
                        alignRight = true
                    )
                }

                // CENTRAL TABLE AREA (Draw deck, central Row of cascading cards, last match combo)
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ACTIVE ACTIONS BANNER or STATUS LOG
                    TurnProgressBanner(state = state)

                    Spacer(modifier = Modifier.height(12.dp))

                    // central row cascading cards
                    CentralCascadingCardRow(
                        rowCards = state.rowCards,
                        selectedCard = state.selectedRowCard,
                        selectedCardIndex = state.selectedRowCardIndex,
                        onCardSelect = { card, idx -> viewModel.selectRowCard(card, idx) },
                        gamePhase = state.phase,
                        currentPlayerId = state.currentPlayerId
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw Deck visual & Last Meld Combination visually shown
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Draw Deck pile
                        DrawDeckView(
                            deckSize = state.deck.size,
                            onClick = {
                                if (state.currentPlayerId == 1 && state.phase == GamePhase.PLAYER_TURN_DECIDING) {
                                    viewModel.drawCardFromDeck()
                                }
                            },
                            enabled = state.currentPlayerId == 1 && state.phase == GamePhase.PLAYER_TURN_DECIDING
                        )

                        // Newly/Last Showed Match Combination (for Chain Match / informational viewing)
                        LastShownMatchView(
                            lastMatch = state.lastShownMatch,
                            makerName = state.players.find { it.id == state.lastShownMatchPlayerId }?.name
                        )
                    }
                }
            }

            // 3. USER INTERACTION PANEL
            UserInteractionPanel(
                state = state,
                viewModel = viewModel
            )
        }

        // --- OVERLAYS & DIALOGS ---

        // AI Thinking Spinner/Overlay
        if (state.isAiThinking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CasinoGold,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${state.currentPlayer?.name ?: "AI"} is thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Deal Selector Screen (Phase = STARTING)
        if (state.phase == GamePhase.STARTING) {
            DealerChoiceDialog(
                onDealerSelect = { selectedId -> viewModel.startNewGame(selectedId) }
            )
        }

        // Game Finished Final Score Screen (Phase = GAME_OVER)
        if (state.phase == GamePhase.GAME_OVER) {
            GameOverOverviewDialog(
                state = state,
                onRestart = { viewModel.resetGame() }
            )
        }
    }
}

@Composable
fun GameTopHeader(
    deckCount: Int,
    phase: GamePhase,
    onResetClick: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RUMMY ROW",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = CasinoGold
                )
                if (phase != GamePhase.STARTING) {
                    Text(
                        text = "Dealer's Privilege gives 7 cards to start",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (phase != GamePhase.STARTING) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deck info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Deck size",
                            tint = CasinoGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$deckCount left",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("reset_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset game",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiPlayerPanel(
    player: PlayerState,
    isCurrent: Boolean,
    isDealer: Boolean,
    alignRight: Boolean
) {
    val outlineColor = if (isCurrent) CasinoGold else Color.White.copy(alpha = 0.3f)
    val glowingModifier = if (isCurrent) {
        Modifier.drawBehind {
            drawCircle(
                color = CasinoGold,
                radius = size.minDimension / 2f + 4.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    } else Modifier

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, outlineColor),
        modifier = Modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start
        ) {
            // Player avatar/Header info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!alignRight) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) CasinoGold else Color.Gray)
                            .then(glowingModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = player.name,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isDealer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Dealer",
                                tint = CasinoGold,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = "Score: ${player.capturedPoints} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = CasinoGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (alignRight) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) CasinoGold else Color.Gray)
                            .then(glowingModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = player.name,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Facedown Hand cards count preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(player.hand.size) {
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(15.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F2042))
                                )
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                }
                if (player.hand.isEmpty()) {
                    Text(
                        text = "EMPTY HAND",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${player.hand.size} cards left",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Captured sets preview
            if (player.captured.isNotEmpty()) {
                Text(
                    text = "Captured: ${player.captured.size} cards",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Green,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TurnProgressBanner(state: GameUIState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val activeRoundIndicatorColor = if (state.currentPlayerId == 1) CasinoGold else Color.White
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(activeRoundIndicatorColor)
            )
            
            Text(
                text = if (state.phase == GamePhase.COLD_START_DISCARD) {
                    "Dealer ${state.currentPlayer?.name} must discard 1 card to start the Row."
                } else if (state.currentPlayerId == 1) {
                    if (state.phase == GamePhase.PLAYER_TURN_DECIDING) {
                        "Your Turn: Match a Card (A) or Draw from Deck (B)"
                    } else {
                        "Choose 1 card to discard onto Row to end your turn"
                    }
                } else {
                    "${state.currentPlayer?.name}'s Turn"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CentralCascadingCardRow(
    rowCards: List<Card>,
    selectedCard: Card?,
    selectedCardIndex: Int?,
    onCardSelect: (Card, Int) -> Unit,
    gamePhase: GamePhase,
    currentPlayerId: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CENTRAL CASCADING ROW",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )

            if (rowCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Row is empty. Discard a card to start the cascade.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                
                LaunchedEffect(rowCards.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy((-38).dp), // Cascading overlap
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowCards.forEachIndexed { index, card ->
                        val isSelected = selectedCard == card && selectedCardIndex == index
                        val clickEnabled = currentPlayerId == 1 && gamePhase == GamePhase.PLAYER_TURN_DECIDING
                        
                        Box(
                            modifier = Modifier
                                .clickable(enabled = clickEnabled) {
                                    onCardSelect(card, index)
                                }
                        ) {
                            GameCard(
                                card = card,
                                isSelected = isSelected,
                                modifier = Modifier
                                    .testTag("row_card_${index}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawDeckView(
    deckSize: Int,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val clickModifier = if (enabled) Modifier.clickable { onClick() } else Modifier
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "DECK (OPTION B)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .width(72.dp)
                .height(104.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (deckSize > 0) {
                        Brush.verticalGradient(colors = listOf(Color(0xFF3F51B5), Color(0xFF1E293B)))
                    } else {
                        Brush.verticalGradient(colors = listOf(Color.DarkGray, Color.Black))
                    }
                )
                .border(
                    width = if (enabled) 2.dp else 1.dp,
                    color = if (enabled) CasinoGold else Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .then(clickModifier)
                .testTag("draw_deck"),
            contentAlignment = Alignment.Center
        ) {
            if (deckSize > 0) {
                Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Deck Card",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$deckSize",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "EMPTY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun LastShownMatchView(
    lastMatch: List<Card>?,
    makerName: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ACTIVE COMBINATION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .width(180.dp)
                .height(104.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (lastMatch != null && lastMatch.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "By ${makerName ?: "Player"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CasinoGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-18).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        lastMatch.take(4).forEach { card ->
                            GameCard(
                                card = card,
                                isSelected = false,
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(56.dp)
                            )
                        }
                        if (lastMatch.size > 4) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${lastMatch.size - 4}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No meld shown yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun UserInteractionPanel(
    state: GameUIState,
    viewModel: GameViewModel
) {
    val userPlayer = state.userPlayer
    val hand = userPlayer.hand

    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CURRENT ACTIONS
            if (state.currentPlayerId == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.phase == GamePhase.PLAYER_TURN_DECIDING) {
                        val selectedRow = state.selectedRowCard
                        val isMeldValid = selectedRow != null && 
                                state.selectedHandCards.size >= 2 && 
                                RummyRules.checkMatch(selectedRow, state.selectedHandCards)

                        Button(
                            onClick = {
                                if (state.selectedRowCardIndex != null && isMeldValid) {
                                    viewModel.matchRowCard(state.selectedRowCardIndex, state.selectedHandCards)
                                }
                            },
                            enabled = isMeldValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoGold,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .padding(horizontal = 4.dp)
                                .testTag("meld_match_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Meld",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Meld Match",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Option C: Chain Match
                        val selectCount = state.selectedHandCards.size
                        val previousMatchExist = state.lastShownMatch != null && state.lastShownMatch.isNotEmpty()
                        val canChainMatch = previousMatchExist && selectCount == 1 && 
                                viewModel.isCardMatchingCombination(state.selectedHandCards.first(), state.lastShownMatch!!)

                        Button(
                            onClick = {
                                if (canChainMatch) {
                                    viewModel.chainMatchCard(state.selectedHandCards.first())
                                }
                            },
                            enabled = canChainMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(horizontal = 4.dp)
                                .testTag("chain_match_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Chain Match",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chain Match",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (state.phase == GamePhase.PLAYER_TURN_DISCARDING) {
                        val selectedCnt = state.selectedHandCards.size
                        val canDiscard = selectedCnt == 1

                        Button(
                            onClick = {
                                if (canDiscard) {
                                    viewModel.discardCard(state.selectedHandCards.first())
                                }
                            },
                            enabled = canDiscard,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("discard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Discard Icon",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Discard onto Row",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (state.phase == GamePhase.COLD_START_DISCARD) {
                        val selectedCnt = state.selectedHandCards.size
                        val canDiscard = selectedCnt == 1

                        Button(
                            onClick = {
                                if (canDiscard) {
                                    viewModel.coldStartDiscard(state.selectedHandCards.first())
                                }
                            },
                            enabled = canDiscard,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoGold,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("start_discard_button")
                        ) {
                            Text(
                                text = "Start Game: Discard 1st Card",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // USER HAND LIST HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "YOUR HAND",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .background(CasinoGold, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${hand.size} Cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Captured: ${userPlayer.capturedPoints} pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // USER'S INTERACTIVE HAND DISPLAY
            if (hand.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cards left in hand",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    hand.forEach { card ->
                        val isSelected = card in state.selectedHandCards
                        
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.toggleHandCardSelection(card) }
                        ) {
                            GameCard(
                                card = card,
                                isSelected = isSelected,
                                modifier = Modifier
                                    .testTag("hand_card_${card.rank.representation}_${card.suit.name}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RECENT LOG MESSAGE BOX
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Log Info Icon",
                        tint = CasinoGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.logs.firstOrNull() ?: "Game started! Place your discards.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    card: Card,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColor = if (card.suit.isRed) CardRed else CardBlack
    
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-12).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardCream),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) CasinoGold else Color.Gray.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 10.dp else 2.dp
        ),
        modifier = modifier
            .width(56.dp)
            .height(82.dp)
            .offset(y = offsetY)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.representation,
                    color = cardColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 12.sp
                )
                Text(
                    text = card.suit.symbol,
                    color = cardColor,
                    fontSize = 11.sp,
                    lineHeight = 10.sp
                )
            }

            Text(
                text = card.suit.symbol,
                color = cardColor.copy(alpha = 0.85f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp)
            )

            Text(
                text = "${card.points}p",
                color = Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerChoiceDialog(
    onDealerSelect: (Int) -> Unit
) {
    Dialog(onDismissRequest = { /* Force selection */ }) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, CasinoGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Dealer Selection",
                    tint = CasinoGold,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SETUP & DEALING",
                    style = MaterialTheme.typography.titleMedium,
                    color = CasinoGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select a Dealer. The Dealer receives 7 cards to start (Dealer's Privilege), while others receive 6.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DealerSelectionChoice(
                        name = "You",
                        icon = Icons.Default.Person,
                        onClick = { onDealerSelect(1) },
                        tag = "dealer_choice_user"
                    )

                    DealerSelectionChoice(
                        name = "Roll Random",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            val rolled = (1..3).random()
                            onDealerSelect(rolled)
                        },
                        tag = "dealer_choice_random"
                    )

                    DealerSelectionChoice(
                        name = "AI Left",
                        icon = Icons.Default.Person,
                        onClick = { onDealerSelect(2) },
                        tag = "dealer_choice_ai"
                    )
                }
            }
        }
    }
}

@Composable
fun DealerSelectionChoice(
    name: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, CasinoGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = CasinoGold,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameOverOverviewDialog(
    state: GameUIState,
    onRestart: () -> Unit
) {
    Dialog(onDismissRequest = onRestart) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, CasinoGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Game Finished",
                    tint = CasinoGold,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "GAME FINISHED!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CasinoGold,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "FINAL SCORE OVERVIEW",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.players.sortedByDescending { it.netScore }) { player ->
                        RowScoreCard(player = player)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CasinoGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("restart_game_button")
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowScoreCard(player: PlayerState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${player.netScore} pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (player.netScore >= 0) CaptionGreen else Color.Red
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Captured Pile (${player.captured.size} cards):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "+${player.capturedPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CaptionGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            if (player.captured.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    player.captured.forEach { card ->
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(41.dp)
                        ) {
                            GameCard(card = card, isSelected = false)
                        }
                    }
                }
            } else {
                Text(
                    text = "No captured cards",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deducted Hand Leftover (${player.hand.size} cards):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "-${player.handPenaltyPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            if (player.hand.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    player.hand.forEach { card ->
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(41.dp)
                        ) {
                            GameCard(card = card, isSelected = false)
                        }
                    }
                }
            } else {
                Text(
                    text = "Clear hand! (0 penalty)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CaptionGreen,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
