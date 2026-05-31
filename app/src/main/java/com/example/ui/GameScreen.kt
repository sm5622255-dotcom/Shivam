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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
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
    val haptic = LocalHapticFeedback.current
    
    var showCustomizeDialog by remember { mutableStateOf(false) }

    // Dynamic procedural backgrounds depending on the active state.tableStyle
    val backgroundModifier = when (state.tableStyle) {
        TableStyle.FELT_GREEN -> {
            val tableGradient = Brush.radialGradient(
                colors = listOf(TableGreenLight, TableGreenDark),
                radius = 1600f
            )
            Modifier.background(tableGradient)
        }
        TableStyle.WALNUT_WOOD -> {
            Modifier.drawBehind {
                if (size.width > 0f && size.height > 0f) {
                    // Procedural wood boards canvas drawing
                    drawRect(color = Color(0xFF5D4037)) // Base Walnut
                    
                    // Draw panel wood planks
                    val plankCount = 6
                    val plankWidth = size.width / plankCount
                    for (i in 0..plankCount) {
                        val x = i * plankWidth
                        drawLine(
                            color = Color(0xFF3E2723), // Deep board crease
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF795548).copy(alpha = 0.35f), // Plank bevel highlights
                            start = androidx.compose.ui.geometry.Offset(x + 1.dp.toPx(), 0f),
                            end = androidx.compose.ui.geometry.Offset(x + 1.dp.toPx(), size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Procedural vertical natural wood waves/grains
                    val grainCount = 20
                    val step = size.width / grainCount
                    for (i in 0..grainCount) {
                        val grainX = i * step + (Math.sin(i.toDouble() * 1.5) * 12f).toFloat()
                        drawLine(
                            color = Color(0xFF3E2723).copy(alpha = 0.12f),
                            start = androidx.compose.ui.geometry.Offset(grainX, 0f),
                            end = androidx.compose.ui.geometry.Offset(grainX, size.height),
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
        TableStyle.WINTER_FOREST -> {
            Modifier.drawBehind {
                if (size.width > 0f && size.height > 0f) {
                    // Background Sky Haze Gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8BA5C9), // Cold blue top
                                Color(0xFFBACADB), // Haze middle
                                Color(0xFFDCE6F2)  // Crisp white snow bottom
                            )
                        )
                    )

                    // Birch Tree trunks in background
                    val treeCount = 9
                    val segmentWidth = size.width / (treeCount + 1)
                    for (i in 0..treeCount) {
                        val rootX = i * segmentWidth + (Math.sin(i.toDouble() * 2.0) * segmentWidth * 0.25f).toFloat()
                        val trunkWidth = (30.dp.toPx() + (i % 3) * 12.dp.toPx())
                        val shadowAlpha = 0.25f - (i % 3) * 0.05f

                        // Draw trunk base shadow
                        drawRect(
                            color = Color(0xFF2C3E50).copy(alpha = shadowAlpha),
                            topLeft = androidx.compose.ui.geometry.Offset(rootX, 0f),
                            size = androidx.compose.ui.geometry.Size(trunkWidth, size.height)
                        )

                        // Draw snow frost overlay on the left side of trunk
                        drawRect(
                            color = Color.White.copy(alpha = 0.2f),
                            topLeft = androidx.compose.ui.geometry.Offset(rootX, 0f),
                            size = androidx.compose.ui.geometry.Size(trunkWidth * 0.35f, size.height)
                        )
                    }

                    // Bezier curved Snow Hills on the table surface
                    val snowHillPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height * 0.72f)
                        quadraticTo(
                            size.width * 0.35f, size.height * 0.65f,
                            size.width * 0.65f, size.height * 0.74f
                        )
                        quadraticTo(
                            size.width * 0.85f, size.height * 0.69f,
                            size.width, size.height * 0.77f
                        )
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = snowHillPath,
                        color = Color(0xFFEEF3F8).copy(alpha = 0.88f)
                    )

                    // Procedural floating glowing snowflakes
                    val snowCount = 42
                    for (i in 1..snowCount) {
                        val randomX = (i * 157) % size.width
                        val randomY = (i * 269) % size.height
                        val radius = (i % 4 + 1f) * 1.5f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            center = androidx.compose.ui.geometry.Offset(randomX, randomY),
                            radius = radius.dp.toPx()
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- COHESIVE OUTDOOR WINTER CARD TABLE ---
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. TOP MARGIN: Meena.S (Top Player / Host) + Speech bubble card
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val meenaName = when (state.playerCount) {
                    2 -> "Meena.S (Host)"
                    4 -> "Meena.S"
                    else -> "Meena.S (Host)"
                }
                val meenaCardCount = when (state.playerCount) {
                    4 -> state.aiTopPlayer.hand.size
                    2 -> state.deck.size
                    else -> 4
                }
                val meenaCapturedPoints = if (state.playerCount == 4) state.aiTopPlayer.capturedPoints else 35
                val meenaTurn = state.playerCount == 4 && state.currentPlayerId == 4
                val meenaDealer = if (state.playerCount == 4) state.dealerId == 4 else (state.dealerId == 3 || state.playerCount == 2)

                val topOpen = state.players.find { it.id == 4 }?.openCard
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TableSeat(
                        name = meenaName,
                        avatarEmoji = "👱‍♀️",
                        flagEmoji = "🇮🇳",
                        cardCount = meenaCardCount,
                        capturedPoints = meenaCapturedPoints,
                        isCurrentTurn = meenaTurn,
                        isDealer = meenaDealer,
                        modifier = Modifier.testTag("seat_meena")
                    )

                    if (topOpen != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Revealed", fontSize = 8.sp, color = CasinoGold, fontWeight = FontWeight.Bold)
                            GameCard(
                                card = topOpen,
                                isSelected = false,
                                width = 38.dp,
                                height = 54.dp,
                                deckStyle = state.deckStyle
                            )
                        }
                    }
                }

                // Translucent Speech Bubble with Meena's helpful tips or logs of game!
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)
                    ),
                    border = BorderStroke(1.dp, CasinoGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .heightIn(min = 44.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = state.lastActionDescription.ifEmpty { 
                                state.logs.firstOrNull() ?: "Ready to play? Show off your best runs!"
                            },
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // 2. LEFT MARGIN: AI Left (Lily34)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TableSeat(
                    name = if (state.playerCount == 2) "Lily34 (Opponent)" else "Lily34",
                    avatarEmoji = "👩‍🦰",
                    flagEmoji = "🇨🇦",
                    cardCount = state.aiLeftPlayer.hand.size,
                    capturedPoints = state.aiLeftPlayer.capturedPoints,
                    isCurrentTurn = state.currentPlayerId == 2,
                    isDealer = state.dealerId == 2,
                    modifier = Modifier.testTag("seat_lily34")
                )

                val leftOpen = state.players.find { it.id == 2 }?.openCard
                if (leftOpen != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Revealed", fontSize = 8.sp, color = CasinoGold, fontWeight = FontWeight.Bold)
                        GameCard(
                             card = leftOpen,
                             isSelected = false,
                             width = 38.dp,
                             height = 54.dp,
                             deckStyle = state.deckStyle
                        )
                    }
                }
            }

            // 3. RIGHT MARGIN: AI Right (Dannie) - Only visible in 3-player or 4-player mode
            if (state.playerCount >= 3) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rightOpen = state.players.find { it.id == 3 }?.openCard
                    if (rightOpen != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Revealed", fontSize = 8.sp, color = CasinoGold, fontWeight = FontWeight.Bold)
                            GameCard(
                                 card = rightOpen,
                                 isSelected = false,
                                 width = 38.dp,
                                 height = 54.dp,
                                 deckStyle = state.deckStyle
                            )
                        }
                    }

                    TableSeat(
                        name = "Dannie",
                        avatarEmoji = "🧔",
                        flagEmoji = "🇬🇧",
                        cardCount = state.aiRightPlayer.hand.size,
                        capturedPoints = state.aiRightPlayer.capturedPoints,
                        isCurrentTurn = state.currentPlayerId == 3,
                        isDealer = state.dealerId == 3,
                        modifier = Modifier.testTag("seat_dannie")
                    )
                }
            }

            // 4. CENTRAL AREA: Cascading Card Row, Draw Deck, Last Match Preview
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 110.dp) // Safely leaves space for left/right players
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot A: Draw Deck view
                DrawDeckView(
                    deckSize = state.deck.size,
                    onClick = {
                        if (state.currentPlayerId == 1 && state.phase == GamePhase.PLAYER_TURN_DECIDING) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.drawCardFromDeck()
                        }
                    },
                    enabled = state.currentPlayerId == 1 && state.phase == GamePhase.PLAYER_TURN_DECIDING,
                    deckStyle = state.deckStyle
                )

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.7f)
                        .background(Color.White.copy(alpha = 0.12f))
                )

                // Slot B: Scrollable Central Cascading Column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.rowCards.isEmpty()) {
                        Text(
                            text = "Central Table is empty.\nPlace or discard a card.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    } else {
                        val rowScrollState = rememberScrollState()
                        
                        LaunchedEffect(state.rowCards.size) {
                            rowScrollState.animateScrollTo(rowScrollState.maxValue)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rowScrollState),
                            horizontalArrangement = Arrangement.spacedBy((-38).dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.rowCards.forEachIndexed { index, card ->
                                val isSelected = state.selectedRowCard == card && state.selectedRowCardIndex == index
                                val clickEnabled = state.currentPlayerId == 1 && state.phase == GamePhase.PLAYER_TURN_DECIDING
                                
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            val rotation = if (index % 2 == 0) 2.5f else -2.5f
                                            rotationZ = rotation
                                            translationY = if (isSelected) (-12).dp.toPx() else 0f
                                        }
                                        .clickable(enabled = clickEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.selectRowCard(card, index)
                                        }
                                ) {
                                    GameCard(
                                        card = card,
                                        isSelected = isSelected,
                                        width = 48.dp,
                                        height = 70.dp,
                                        deckStyle = state.deckStyle,
                                        modifier = Modifier.testTag("row_card_${index}")
                                    )
                                }
                            }
                        }
                    }
                }

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.7f)
                        .background(Color.White.copy(alpha = 0.12f))
                )

                // Slot C: Last match combination fanned on center
                LastShownMatchView(
                    lastMatch = state.lastShownMatch,
                    makerName = state.players.find { it.id == state.lastShownMatchPlayerId }?.name,
                    deckStyle = state.deckStyle
                )
            }

            // 5. BOTTOM MARGIN: User (YOU) metadata, and fanned cards
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Details Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (state.currentPlayerId == 1) Color(0xFF22C55E) else Color.White.copy(alpha = 0.3f))
                    )

                    Text(
                        text = "👤 YOU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.currentPlayerId == 1) CasinoGold else Color.White
                    )

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🏆captured: ${state.userPlayer.capturedPoints} pts",
                            fontSize = 9.sp,
                            color = CasinoGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Penalty: -${state.userPlayer.handPenaltyPoints} pts",
                            fontSize = 9.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val youOpen = state.userPlayer.openCard
                    if (youOpen != null) {
                        Box(
                            modifier = Modifier
                                .background(CasinoGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, CasinoGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Revealed:",
                                    fontSize = 8.5.sp,
                                    color = CasinoGold,
                                    fontWeight = FontWeight.Bold
                                )
                                GameCard(
                                    card = youOpen,
                                    isSelected = false,
                                    width = 24.dp,
                                    height = 34.dp,
                                    deckStyle = state.deckStyle
                                )
                            }
                        }
                    }
                }

                // Horizontal fanned deck
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(98.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val prevId = viewModel.getPreviousPlayerId(1)
                    val prevPlayer = state.players.find { it.id == prevId }
                    val refCard = prevPlayer?.openCard
                    val consecutiveCards = if (refCard != null) {
                        state.userPlayer.hand.filter { viewModel.isConsecutiveCard(it, refCard) }
                    } else {
                        emptyList()
                    }

                    FannedHandCardRow(
                        cards = state.userPlayer.hand,
                        selectedCards = state.selectedHandCards,
                        deckStyle = state.deckStyle,
                        onCardClick = { card ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleHandCardSelection(card)
                        },
                        testTagPrefix = "hand_card",
                        consecutiveCards = consecutiveCards
                    )
                }
            }

            // 6. BOTTOM LEFT CORNER OVERLAYS: Customizer and Organizers
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customize 🎨
                    IconButton(
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.2.dp, CasinoGold.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text("🎨", fontSize = 15.sp)
                    }

                    // Sort 🃏
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.manualSortUserHand()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Text("🃏", fontSize = 15.sp)
                    }

                    // Smart Arrange 🤖
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.smartArrangeUserHand()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.2.dp, CasinoGold.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text("🤖", fontSize = 15.sp)
                    }

                    // Reset 🔄
                    IconButton(
                        onClick = { viewModel.resetGame() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Game",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Arrange Selected Single Card Left/Right controls
                if (state.selectedHandCards.size == 1) {
                    val singleCard = state.selectedHandCards.first()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.moveCardInHand(singleCard, true)
                            },
                            modifier = Modifier.size(30.dp).background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Text("◀", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.moveCardInHand(singleCard, false)
                            },
                            modifier = Modifier.size(30.dp).background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Text("▶", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 7. BOTTOM RIGHT CORNER OVERLAYS: Gameplay Active Actions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 10.dp)
                    .width(130.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                val isActiveTurn = state.currentPlayerId == 1
                val selectedCount = state.selectedHandCards.size
                val selectedRowCard = state.selectedRowCard
                val selectedRowCardIndex = state.selectedRowCardIndex
                val lastShownMatch = state.lastShownMatch

                if (isActiveTurn) {
                    if (state.phase == GamePhase.PLAYER_TURN_DECIDING) {
                        val isMeldValid = selectedRowCard != null && selectedCount >= 2 && 
                                RummyRules.checkMatch(selectedRowCard, state.selectedHandCards)

                        // Action 1: Meld Match
                        Button(
                            onClick = {
                                if (selectedRowCardIndex != null && isMeldValid) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.matchRowCard(selectedRowCardIndex, state.selectedHandCards)
                                }
                            },
                            enabled = isMeldValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoGold,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth().height(33.dp).testTag("meld_match_button")
                        ) {
                            Text("Meld Match", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Chain Card Add
                        val previousMatchExist = lastShownMatch != null && lastShownMatch.isNotEmpty()
                        val canChainMatch = previousMatchExist && selectedCount == 1 && 
                                viewModel.isCardMatchingCombination(state.selectedHandCards.first(), lastShownMatch ?: emptyList())

                        Button(
                            onClick = {
                                if (canChainMatch) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.chainMatchCard(state.selectedHandCards.first())
                                }
                            },
                            enabled = canChainMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth().height(33.dp).testTag("chain_match_button")
                        ) {
                            Text("Chain Card", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 3: Show Trail from hand
                        val canMeldFromHand = selectedCount >= 3 && 
                                (RummyRules.isValidSet(state.selectedHandCards) || 
                                 RummyRules.isValidSequence(state.selectedHandCards))

                        Button(
                            onClick = {
                                if (canMeldFromHand) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.meldHandCards(state.selectedHandCards)
                                }
                            },
                            enabled = canMeldFromHand,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth().height(33.dp).testTag("meld_from_hand_button")
                        ) {
                            Text("Show Trail", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (state.phase == GamePhase.PLAYER_TURN_DISCARDING || state.phase == GamePhase.COLD_START_DISCARD) {
                        val canDiscard = selectedCount == 1
                        val discardCard = state.selectedHandCards.firstOrNull()

                        Button(
                            onClick = {
                                if (discardCard != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (state.phase == GamePhase.COLD_START_DISCARD) {
                                        viewModel.coldStartDiscard(discardCard)
                                    } else {
                                        viewModel.discardCard(discardCard)
                                    }
                                }
                            },
                            enabled = canDiscard,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth().height(35.dp).testTag("discard_button")
                        ) {
                            Text("📤 Discard Card", fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${state.currentPlayer?.name ?: "Opponent"} plays...",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                        )
                    }
                }
            }
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
                onDealerSelect = { selectedId, selectedCount -> viewModel.startNewGame(selectedId, selectedCount) }
            )
        }

        // Game Finished Final Score Screen (Phase = GAME_OVER)
        if (state.phase == GamePhase.GAME_OVER) {
            GameOverOverviewDialog(
                state = state,
                onRestart = { viewModel.resetGame() }
            )
        }

        // Customize Deck/Felt Dialog
        if (showCustomizeDialog) {
            CustomizeYourDeckDialog(
                currentDeckStyle = state.deckStyle,
                currentTableStyle = state.tableStyle,
                onDeckSelect = { viewModel.selectDeckStyle(it) },
                onTableSelect = { viewModel.selectTableStyle(it) },
                onDismiss = { showCustomizeDialog = false }
            )
        }
    }
}

@Composable
fun TableSeat(
    name: String,
    avatarEmoji: String,
    flagEmoji: String,
    cardCount: Int,
    capturedPoints: Int,
    isCurrentTurn: Boolean,
    isDealer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Player Avatar with pulsing active turn border
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(
                    width = if (isCurrentTurn) 3.dp else 1.5.dp,
                    color = if (isCurrentTurn) CasinoGold else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
                )
        ) {
            Text(
                text = avatarEmoji,
                fontSize = 24.sp
            )

            // Country Flag badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                    .padding(2.dp)
            ) {
                Text(
                    text = flagEmoji,
                    fontSize = 9.sp
                )
            }

            // Crown for Dealer indicator
            if (isDealer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-4).dp)
                ) {
                    Text(
                        text = "👑",
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Username and Info Pill
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF1E293B).copy(alpha = 0.8f), 
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isCurrentTurn) CasinoGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (isCurrentTurn) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                }
                Text(
                    text = name,
                    fontSize = 9.sp,
                    color = if (isCurrentTurn) CasinoGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Card count/Points representation
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hand size pill (brownish)
            Box(
                modifier = Modifier
                    .background(Color(0xFF5D4037).copy(alpha = 0.85f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "🎴 $cardCount",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Trophy pile
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFB300).copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                    .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.25f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "🏆 $capturedPoints",
                    color = CasinoGold,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun GameTopHeader(
    deckCount: Int,
    phase: GamePhase,
    onResetClick: () -> Unit,
    onCustomizeClick: () -> Unit
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customize Deck / Felt Button
                IconButton(
                    onClick = onCustomizeClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("customize_deck_button")
                ) {
                    Text(
                        text = "🎨",
                        fontSize = 15.sp
                    )
                }

                if (phase != GamePhase.STARTING) {
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
fun CapturedCardsPreview(
    capturedCards: List<Card>,
    isMini: Boolean,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC,
    modifier: Modifier = Modifier
) {
    if (capturedCards.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(if (isMini) 4.dp else 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (isMini) "🏆 CAPTURED" else "🏆 CAPTURED TASH (MELDS)",
                style = MaterialTheme.typography.labelSmall,
                color = CaptionGreen,
                fontWeight = FontWeight.Bold,
                fontSize = if (isMini) 8.sp else 9.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(if (isMini) 2.dp else 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(if (isMini) (-12).dp else (-18).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                capturedCards.forEach { card ->
                    GameCard(
                        card = card,
                        isSelected = false,
                        width = if (isMini) 24.dp else 36.dp,
                        height = if (isMini) 36.dp else 52.dp,
                        deckStyle = deckStyle
                    )
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
    alignRight: Boolean,
    viewModel: GameViewModel
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) CasinoGold.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(135.dp)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) CasinoGold else Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = if (isCurrent) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isDealer) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("👑", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🎴 ${player.hand.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🏆 ${player.capturedPoints}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CasinoGold,
                        fontSize = 9.sp
                    )
                }
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
    currentPlayerId: Int,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎰 CENTRAL CASCADING COLUMN",
                    style = MaterialTheme.typography.labelSmall,
                    color = CasinoGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (rowCards.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${rowCards.size} Cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (rowCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Column is empty. Discard a card to start the cascading pile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                val verticalScrollState = rememberScrollState()
                
                LaunchedEffect(rowCards.size) {
                    if (rowCards.isNotEmpty()) {
                        try {
                            verticalScrollState.animateScrollTo(verticalScrollState.maxValue)
                        } catch (e: Exception) {
                            // Suppress gracefully
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(verticalScrollState)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy((-54).dp), // Cascading overlap vertically
                    horizontalAlignment = Alignment.CenterHorizontally
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
                                deckStyle = deckStyle,
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
    enabled: Boolean,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
) {
    val clickModifier = if (enabled) Modifier.clickable { onClick() } else Modifier
    
    // Pulse animation when enabled
    val pulseScale = if (enabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "deck_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "deck_scale"
        )
        scale
    } else {
        1.0f
    }

    val glowAlpha = if (enabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "deck_glow")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "deck_glow"
        )
        alpha
    } else {
        0.15f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // High visibility title labels
        if (enabled) {
            Text(
                text = "👉 TOUCH TO DRAW!",
                style = MaterialTheme.typography.labelSmall,
                color = CasinoGold,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .graphicsLayer {
                        // Tiny vertical bounce matching the pulse
                        translationY = (pulseScale - 1f) * -50f
                    }
            )
        } else {
            Text(
                text = "DRAW DECK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .width(76.dp) // Sized slightly wider for stack layered offsets
                .height(108.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
            contentAlignment = Alignment.Center
        ) {
            if (deckSize > 0) {
                // Render the stacked visual background cards (depth levels)
                val maxVisibleOffset = minOf(3, (deckSize + 5) / 10).coerceAtLeast(1)
                
                for (i in (maxVisibleOffset - 1) downTo 0) {
                    val shiftX = (i * 2).dp
                    val shiftY = (-i * 2).dp
                    Box(
                        modifier = Modifier
                            .offset(x = shiftX, y = shiftY)
                            .width(72.dp)
                            .height(104.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .border(
                                width = if (enabled && i == 0) 2.5.dp else 1.dp,
                                color = if (enabled && i == 0) CasinoGold.copy(alpha = glowAlpha) else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        GameCardBack(
                            width = 72.dp,
                            height = 104.dp,
                            deckStyle = deckStyle
                        )
                        
                        // Show the card count badge on top-most card
                        if (i == 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .align(Alignment.Center)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "🎴",
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "$deckSize",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CasinoGold,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Clicking is applied on a master transparent overlay of the whole stack box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(clickModifier)
                        .testTag("draw_deck")
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(104.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
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
}

@Composable
fun LastShownMatchView(
    lastMatch: List<Card>?,
    makerName: String?,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
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
                .background(Color.Black.copy(alpha = 0.4f)),
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
                                width = 38.dp,
                                height = 56.dp,
                                deckStyle = deckStyle
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

fun getMatchedCardsInHand(hand: List<Card>): List<Card> {
    val matched = mutableSetOf<Card>()
    
    // Group by rank to find Sets
    val rankGroups = hand.groupBy { it.rank }
    for ((_, cardsOfRank) in rankGroups) {
        val uniqueSuits = cardsOfRank.distinctBy { it.suit }
        if (uniqueSuits.size >= 3) {
            matched.addAll(uniqueSuits)
        }
    }
    
    // Group by suit to find sequences
    val suitGroups = hand.groupBy { it.suit }
    for ((_, cardsOfSuit) in suitGroups) {
        val n = cardsOfSuit.size
        if (n >= 3) {
            // Check low Ace order
            val sortedLow = cardsOfSuit.sortedBy { it.rank.order }
            for (len in 3..n) {
                for (start in 0..n - len) {
                    val sub = sortedLow.subList(start, start + len)
                    if (RummyRules.isValidSequence(sub)) {
                        matched.addAll(sub)
                    }
                }
            }
            // Check high Ace order (Ace counts as 14)
            val sortedHigh = cardsOfSuit.sortedBy { if (it.rank == Rank.ACE) 14 else it.rank.order }
            for (len in 3..n) {
                for (start in 0..n - len) {
                    val sub = sortedHigh.subList(start, start + len)
                    if (RummyRules.isValidSequence(sub)) {
                        matched.addAll(sub)
                    }
                }
            }
        }
    }
    
    return hand.filter { it in matched }
}

@Composable
fun UserInteractionPanel(
    state: GameUIState,
    viewModel: GameViewModel
) {
    val userPlayer = state.userPlayer
    val hand = userPlayer.hand
    val isActiveTurn = state.currentPlayerId == 1

    Surface(
        color = Color.Black.copy(alpha = 0.75f),
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
            // 💡 AI ASSISTANT COACH & RECOMMENDATION ENGINE
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, CasinoGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Column {
                            Text("AI COACH ASSISTANT", fontSize = 8.sp, color = CasinoGold, fontWeight = FontWeight.ExtraBold)
                            val hintText = viewModel.getAiHint()
                            Text(
                                text = hintText,
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    
                    val canAutoPlay = state.currentPlayerId == 1 && !state.isAiThinking && state.phase != GamePhase.GAME_OVER
                    if (canAutoPlay) {
                        Button(
                            onClick = { viewModel.autoPlayRecommendedMove() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoGold,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp).testTag("ai_autoplay_button")
                        ) {
                            Text("🤖 Auto-Play", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // DETAILED POINTS DASHBOARD HUD (make poin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Captured points (positive)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CAPTURED TASH",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${userPlayer.capturedPoints} pts",
                        fontSize = 14.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Black
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

                // Hand penalty count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HAND PENALTY",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "-${userPlayer.handPenaltyPoints} pts",
                        fontSize = 14.sp,
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Black
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

                // Net score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NET SCORE",
                        fontSize = 8.sp,
                        color = CasinoGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${userPlayer.netScore} pts",
                        fontSize = 15.sp,
                        color = CasinoGold,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // USER'S SHOW/CAPTURED CARDS PREVIEW (showing card display own side)
            if (userPlayer.captured.isNotEmpty()) {
                CapturedCardsPreview(
                    capturedCards = userPlayer.captured,
                    isMini = false,
                    deckStyle = state.deckStyle,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            // SINGLE CARD ARRANGE AND PLACE TOOLKIT
            if (state.selectedHandCards.size == 1) {
                val singleCard = state.selectedHandCards.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SINGLE CARD TOOLKIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CasinoGold,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${singleCard.rank.representation}${singleCard.suit.symbol} Selected",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Move left button
                        IconButton(
                            onClick = { viewModel.moveCardInHand(singleCard, true) },
                            modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Text(
                                text = "◀",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Move right button
                        IconButton(
                            onClick = { viewModel.moveCardInHand(singleCard, false) },
                            modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Text(
                                text = "▶",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Place card onto row button
                        val isActiveTurn = state.currentPlayerId == 1 && (state.phase == GamePhase.PLAYER_TURN_DECIDING || state.phase == GamePhase.PLAYER_TURN_DISCARDING)
                        Button(
                            onClick = { viewModel.placeSingleCardOnRow(singleCard) },
                            enabled = isActiveTurn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E3A8A),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "📤",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Place On Row",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

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
                        val prevId = viewModel.getPreviousPlayerId(1)
                        val prevPlayer = state.players.find { it.id == prevId }
                        val refCard = prevPlayer?.openCard
                        val consecutivePlayCard = if (refCard != null) {
                            state.userPlayer.hand.find { viewModel.isConsecutiveCard(it, refCard) }
                        } else null

                        if (consecutivePlayCard != null) {
                            Button(
                                onClick = { viewModel.playConsecutiveOnSide(consecutivePlayCard) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD97706),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .padding(horizontal = 4.dp)
                                    .testTag("play_consecutive_button")
                            ) {
                                Text(
                                    text = "✨ consecutive: ${consecutivePlayCard.rank.representation}${consecutivePlayCard.suit.symbol}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

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
                                viewModel.isCardMatchingCombination(state.selectedHandCards.first(), state.lastShownMatch ?: emptyList())

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
                                .weight(1.1f)
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
                                text = "Chain",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Meld purely from hand
                        val canMeldFromHand = state.selectedHandCards.size >= 3 && 
                                (RummyRules.isValidSet(state.selectedHandCards) || 
                                 RummyRules.isValidSequence(state.selectedHandCards))

                        Button(
                            onClick = {
                                if (canMeldFromHand) {
                                    viewModel.meldHandCards(state.selectedHandCards)
                                }
                            },
                            enabled = canMeldFromHand,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .padding(horizontal = 4.dp)
                                .testTag("meld_from_hand_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Show Trail / Run",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Show Trail / Run",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (state.phase == GamePhase.PLAYER_TURN_DISCARDING) {
                        val selectedCnt = state.selectedHandCards.size
                        val canDiscard = selectedCnt == 1
                        val canMeldFromHand = state.selectedHandCards.size >= 3 && 
                                (RummyRules.isValidSet(state.selectedHandCards) || 
                                 RummyRules.isValidSequence(state.selectedHandCards))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    .weight(1.5f)
                                    .padding(horizontal = 4.dp)
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

                            if (canMeldFromHand) {
                                Button(
                                    onClick = {
                                        viewModel.meldHandCards(state.selectedHandCards)
                                    },
                                    enabled = true,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("meld_from_hand_button_discarding")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Show Trail / Run",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Show Trail / Run",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.dealerId == 1) "YOUR HAND 👑" else "YOUR HAND",
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Manual Auto-Sort Button
                    TextButton(
                        onClick = { viewModel.manualSortUserHand() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto Sort",
                            style = MaterialTheme.typography.labelSmall,
                            color = CasinoGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Captured: ${userPlayer.capturedPoints} pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(82.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B5E20))
                                .border(2.dp, CasinoGold, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Cleared Hand",
                                tint = CasinoGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "No cards left in hand! You have Completed!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CaptionGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val matchedCards = getMatchedCardsInHand(hand)
                val unmatchedCards = hand.filter { it !in matchedCards }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Matched combinations
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 MATCHED SETS & RUNS",
                                style = MaterialTheme.typography.labelSmall,
                                color = CaptionGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (matchedCards.isNotEmpty()) "(${matchedCards.size} cards)" else "(No matched sets/runs yet)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        }
                        
                        if (matchedCards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Matched combinations appear here when you form 3+ card sets or suit sequences.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        } else {
                            FannedHandCardRow(
                                cards = matchedCards,
                                selectedCards = state.selectedHandCards,
                                deckStyle = state.deckStyle,
                                onCardClick = { card -> viewModel.toggleHandCardSelection(card) },
                                testTagPrefix = "hand_card_matched"
                            )
                        }
                    }
                    
                    // 2. Unmatched Penalty Cards
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔴 UNMATCHED CARDS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE57373),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "(${unmatchedCards.size} cards)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        }
                        
                        if (unmatchedCards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All cards in hand are matched!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CaptionGreen
                                )
                            }
                        } else {
                            FannedHandCardRow(
                                cards = unmatchedCards,
                                selectedCards = state.selectedHandCards,
                                deckStyle = state.deckStyle,
                                onCardClick = { card -> viewModel.toggleHandCardSelection(card) },
                                testTagPrefix = "hand_card_unmatched"
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
fun FannedHandCardRow(
    cards: List<Card>,
    selectedCards: List<Card>,
    deckStyle: DeckStyle,
    onCardClick: (Card) -> Unit,
    testTagPrefix: String,
    consecutiveCards: List<Card> = emptyList()
) {
    val count = cards.size
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp) // Height allows space for vertical arching and selection offset
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        cards.forEachIndexed { index, card ->
            val isSelected = card in selectedCards
            val isConsecutive = card in consecutiveCards
            
            // Calculate fanning rotation and translation
            val mid = (count - 1) / 2.0f
            val relPos = if (count > 1) (index - mid) else 0f
            
            // Rotation angle: e.g. -12 to +12 degrees maximum to avoid illegibility
            val rotationAngle = relPos * (if (count > 6) 24f / count else 5.5f)
            
            // Horizontal overlap offset to fit multiple cards fanned beautifully on compact devices
            val overlapOffset = if (count > 1) {
                val baseShift = if (count > 8) 18.dp else if (count > 5) 26.dp else 36.dp
                (baseShift * relPos)
            } else {
                0.dp
            }
            
            // Vertical offset to form a curved arch (grows with squared distance from center)
            val arcCurveHeight = (if (count > 6) 1.5.dp else 2.6.dp) * (relPos * relPos)

            Box(
                modifier = Modifier
                    .offset(x = overlapOffset, y = arcCurveHeight)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                        // Scale up active fanned card slightly for superior feel
                        scaleX = if (isSelected) 1.08f else 1.0f
                        scaleY = if (isSelected) 1.08f else 1.0f
                    }
                    .clickable { onCardClick(card) }
            ) {
                GameCard(
                    card = card,
                    isSelected = isSelected || isConsecutive, // Glows gold like selected items!
                    width = 58.dp,
                    height = 84.dp,
                    deckStyle = deckStyle,
                    modifier = Modifier.testTag("${testTagPrefix}_${card.rank.representation}_${card.suit.name}")
                )

                if (isConsecutive) {
                    // Golden Indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-4).dp)
                            .background(CasinoGold, CircleShape)
                            .border(1.2.dp, Color.White, CircleShape)
                            .size(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 6.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    card: Card,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 82.dp,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
) {
    // Custom colors depending on the deck style
    val cardBackground = when (deckStyle) {
        DeckStyle.GREEN_MODERN -> Color(0xFFFAFAFA)
        DeckStyle.BLUE_CLASSIC -> CardCream
        DeckStyle.RED_ORNATE -> Color(0xFFFFFDF5)
    }

    val cardColor = if (card.suit.isRed) {
        when (deckStyle) {
            DeckStyle.GREEN_MODERN -> Color(0xFFE53935)
            DeckStyle.BLUE_CLASSIC -> CardRed
            DeckStyle.RED_ORNATE -> Color(0xFF8D0014)
        }
    } else {
        when (deckStyle) {
            DeckStyle.GREEN_MODERN -> Color(0xFF1B5E20) // Custom stylish dark green suits for modern
            DeckStyle.BLUE_CLASSIC -> CardBlack
            DeckStyle.RED_ORNATE -> Color(0xFF1A1A1A)
        }
    }

    val customBorderColor = if (isSelected) {
        CasinoGold
    } else {
        when (deckStyle) {
            DeckStyle.GREEN_MODERN -> Color(0xFF81C784).copy(alpha = 0.5f)
            DeckStyle.BLUE_CLASSIC -> if (card.suit.isRed) CardRed.copy(alpha = 0.25f) else CardBlack.copy(alpha = 0.25f)
            DeckStyle.RED_ORNATE -> CasinoGold.copy(alpha = 0.45f)
        }
    }
    
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-12).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val isMini = height <= 45.dp
    val textPadding = if (isMini) 1.dp else 4.dp
    val rankSize = if (isMini) 7.sp else 13.sp
    val suitSize = if (isMini) 6.sp else 11.sp
    val centerSuitSize = if (isMini) 12.sp else 24.sp
    val pointsSize = if (isMini) 5.sp else 8.sp
    val cornerRadius = if (isMini) 4.dp else 8.dp
    val strokeWidth = if (isSelected) (if (isMini) 1.5.dp else 3.dp) else (if (isMini) 0.5.dp else 1.dp)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(
            width = strokeWidth,
            color = customBorderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) (if (isMini) 4.dp else 10.dp) else (if (isMini) 1.dp else 2.dp)
        ),
        modifier = modifier
            .width(width)
            .height(height)
            .offset(y = offsetY)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(textPadding)
        ) {
            // Ornate decorative watermark for Red/Gold Luxury style
            if (deckStyle == DeckStyle.RED_ORNATE && !isMini) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = CasinoGold.copy(alpha = 0.08f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    // Draw subtle diagonal lines
                    drawLine(
                        color = CasinoGold.copy(alpha = 0.05f),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }

            // Modern green subtle framing lines for Green style
            if (deckStyle == DeckStyle.GREEN_MODERN && !isMini) {
                Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                    drawRect(
                        color = Color(0xFFC8E6C9).copy(alpha = 0.3f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.representation,
                    color = cardColor,
                    fontSize = rankSize,
                    fontWeight = FontWeight.Black,
                    lineHeight = rankSize
                )
                if (!isMini) {
                    Text(
                        text = card.suit.symbol,
                        color = cardColor,
                        fontSize = suitSize,
                        lineHeight = suitSize
                    )
                }
            }

            Text(
                text = card.suit.symbol,
                color = cardColor.copy(alpha = 0.85f),
                fontSize = centerSuitSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = if (isMini) 2.dp else 10.dp)
            )

            if (!isMini) {
                Text(
                    text = "${card.points}p",
                    color = Color.Gray,
                    fontSize = pointsSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            if (isSelected && !isMini) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun GameCardBack(
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 82.dp,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
) {
    val isMini = height <= 45.dp
    val cornerRadius = if (isMini) 4.dp else 8.dp
    
    val backColor = when (deckStyle) {
        DeckStyle.GREEN_MODERN -> Color(0xFF2E7D32) // Soft forest green
        DeckStyle.BLUE_CLASSIC -> Color(0xFF1E3A8A) // Nice casino royal blue
        DeckStyle.RED_ORNATE -> Color(0xFF8D0014) // Rich deep red
    }
    
    val patternColor = when (deckStyle) {
        DeckStyle.GREEN_MODERN -> Color(0xFF81C784).copy(alpha = 0.25f)
        DeckStyle.BLUE_CLASSIC -> Color.White.copy(alpha = 0.15f)
        DeckStyle.RED_ORNATE -> CasinoGold.copy(alpha = 0.35f)
    }

    val borderColor = when (deckStyle) {
        DeckStyle.GREEN_MODERN -> Color(0xFF81C784).copy(alpha = 0.6f)
        DeckStyle.BLUE_CLASSIC -> Color.White.copy(alpha = 0.3f)
        DeckStyle.RED_ORNATE -> CasinoGold.copy(alpha = 0.6f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backColor),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMini) 1.dp else 3.dp),
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isMini) 2.dp else 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Outer inner border
                drawRect(
                    color = patternColor,
                    style = Stroke(width = 1.dp.toPx())
                )
                
                when (deckStyle) {
                    DeckStyle.GREEN_MODERN -> {
                        drawCircle(
                            color = patternColor,
                            radius = size.width / 4,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawLine(
                            color = patternColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = patternColor,
                            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    DeckStyle.BLUE_CLASSIC -> {
                        val step = maxOf(8.dp.toPx(), 1f)
                        var diagonal = 0f
                        while (diagonal < size.width + size.height) {
                            drawLine(
                                color = patternColor,
                                start = androidx.compose.ui.geometry.Offset(diagonal, 0f),
                                end = androidx.compose.ui.geometry.Offset(0f, diagonal),
                                strokeWidth = 0.5.dp.toPx()
                            )
                            drawLine(
                                color = patternColor,
                                start = androidx.compose.ui.geometry.Offset(size.width - diagonal, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, diagonal),
                                strokeWidth = 0.5.dp.toPx()
                            )
                            diagonal += step
                        }
                    }
                    DeckStyle.RED_ORNATE -> {
                        drawCircle(
                            color = patternColor,
                            radius = size.width / 3.5f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = patternColor,
                            radius = size.width / 5f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawLine(color = patternColor, start = androidx.compose.ui.geometry.Offset(cx, cy - 10.dp.toPx()), end = androidx.compose.ui.geometry.Offset(cx, cy + 10.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                        drawLine(color = patternColor, start = androidx.compose.ui.geometry.Offset(cx - 10.dp.toPx(), cy), end = androidx.compose.ui.geometry.Offset(cx + 10.dp.toPx(), cy), strokeWidth = 1.5.dp.toPx())
                    }
                }
            }
            
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                when (deckStyle) {
                    DeckStyle.GREEN_MODERN -> {
                        Text(
                            text = "✦",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = if (isMini) 10.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DeckStyle.BLUE_CLASSIC -> {
                        Text(
                            text = "★",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = if (isMini) 9.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DeckStyle.RED_ORNATE -> {
                        Text(
                            text = "👑",
                            color = CasinoGold.copy(alpha = 0.9f),
                            fontSize = if (isMini) 10.sp else 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeYourDeckDialog(
    currentDeckStyle: DeckStyle,
    currentTableStyle: TableStyle,
    onDeckSelect: (DeckStyle) -> Unit,
    onTableSelect: (TableStyle) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF131A26)
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, CasinoGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CUSTOMIZE YOUR DECK",
                    style = MaterialTheme.typography.titleMedium,
                    color = CasinoGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Select a card skin & table background",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // DECK OPTIONS SHOWCASE SECTION
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Green Modernist
                    DeckShowcaseOption(
                        title = "🟢 GREEN MODERNIST DECK",
                        cards = listOf(
                            Card("g1", Rank.JACK, Suit.SPADES),
                            Card("g2", Rank.TEN, Suit.CLUBS),
                            Card("g3", Rank.JACK, Suit.HEARTS),
                            Card("g4", Rank.ACE, Suit.HEARTS),
                            Card("g5", Rank.TWO, Suit.DIAMONDS)
                        ),
                        deckStyle = DeckStyle.GREEN_MODERN,
                        isSelected = currentDeckStyle == DeckStyle.GREEN_MODERN,
                        onClick = { onDeckSelect(DeckStyle.GREEN_MODERN) }
                    )

                    // Option 2: Blue Classic
                    DeckShowcaseOption(
                        title = "🔵 BLUE ROYAL CLASSIC DECK",
                        cards = listOf(
                            Card("b1", Rank.TEN, Suit.DIAMONDS),
                            Card("b2", Rank.JACK, Suit.SPADES),
                            Card("b3", Rank.QUEEN, Suit.HEARTS),
                            Card("b4", Rank.TEN, Suit.CLUBS),
                            Card("b5", Rank.JACK, Suit.DIAMONDS)
                        ),
                        deckStyle = DeckStyle.BLUE_CLASSIC,
                        isSelected = currentDeckStyle == DeckStyle.BLUE_CLASSIC,
                        onClick = { onDeckSelect(DeckStyle.BLUE_CLASSIC) }
                    )

                    // Option 3: Red Ornate
                    DeckShowcaseOption(
                        title = "🔴 RED GOLD ORNATE DECK",
                        cards = listOf(
                            Card("r1", Rank.QUEEN, Suit.CLUBS),
                            Card("r2", Rank.TEN, Suit.DIAMONDS),
                            Card("r3", Rank.QUEEN, Suit.HEARTS),
                            Card("r4", Rank.NINE, Suit.DIAMONDS),
                            Card("r5", Rank.KING, Suit.CLUBS)
                        ),
                        deckStyle = DeckStyle.RED_ORNATE,
                        isSelected = currentDeckStyle == DeckStyle.RED_ORNATE,
                        onClick = { onDeckSelect(DeckStyle.RED_ORNATE) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TABLE BACKGROUND STYLE SELECTOR SECTION
                Text(
                    text = "SELECT BOARD TABLE FELT",
                    style = MaterialTheme.typography.labelSmall,
                    color = CasinoGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Table Style 1: Felt Green
                    OutlinedCard(
                        onClick = { onTableSelect(TableStyle.FELT_GREEN) },
                        border = BorderStroke(
                            width = if (currentTableStyle == TableStyle.FELT_GREEN) 2.dp else 1.dp,
                            color = if (currentTableStyle == TableStyle.FELT_GREEN) CasinoGold else Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.radialGradient(listOf(TableGreenLight, TableGreenDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Casino Felt Green",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTableStyle == TableStyle.FELT_GREEN) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Table Style 2: Walnut Wood
                    OutlinedCard(
                        onClick = { onTableSelect(TableStyle.WALNUT_WOOD) },
                        border = BorderStroke(
                            width = if (currentTableStyle == TableStyle.WALNUT_WOOD) 2.dp else 1.dp,
                            color = if (currentTableStyle == TableStyle.WALNUT_WOOD) CasinoGold else Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF5D4037)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Walnut Wood",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTableStyle == TableStyle.WALNUT_WOOD) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Table Style 3: Winter Forest
                    OutlinedCard(
                        onClick = { onTableSelect(TableStyle.WINTER_FOREST) },
                        border = BorderStroke(
                            width = if (currentTableStyle == TableStyle.WINTER_FOREST) 2.dp else 1.dp,
                            color = if (currentTableStyle == TableStyle.WINTER_FOREST) CasinoGold else Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF8BA5C9), Color(0xFFBACADB)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Winter Forest",
                                color = Color(0xFF0F172A),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTableStyle == TableStyle.WINTER_FOREST) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CasinoGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_and_play_button")
                ) {
                    Text(
                        text = "SAVE & CONTINUE PLAYING",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DeckShowcaseOption(
    title: String,
    cards: List<Card>,
    deckStyle: DeckStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) CasinoGold else Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_option_${deckStyle.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) CasinoGold else Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (isSelected) {
                    Text(
                        text = "✓ SELECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = CasinoGold,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // Previews fanned card style row
            FannedHandCardRow(
                cards = cards,
                selectedCards = emptyList(),
                deckStyle = deckStyle,
                onCardClick = { onClick() },
                testTagPrefix = "dialog_preview_${deckStyle.name}"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerChoiceDialog(
    onDealerSelect: (Int, Int) -> Unit
) {
    var selectedPlayerCount by remember { mutableStateOf(3) }

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

                Spacer(modifier = Modifier.height(16.dp))

                // PLAYER COUNT SELECTOR SECTION
                Text(
                    text = "SELECT PLAYER COUNT",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 2 Players Option
                    Button(
                        onClick = { selectedPlayerCount = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPlayerCount == 2) CasinoGold else Color.White.copy(alpha = 0.1f),
                            contentColor = if (selectedPlayerCount == 2) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("👥 2 Players", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }

                    // 3 Players Option
                    Button(
                        onClick = { selectedPlayerCount = 3 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPlayerCount == 3) CasinoGold else Color.White.copy(alpha = 0.1f),
                            contentColor = if (selectedPlayerCount == 3) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("👥 3 Players", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }

                    // 4 Players Option
                    Button(
                        onClick = { selectedPlayerCount = 4 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPlayerCount == 4) CasinoGold else Color.White.copy(alpha = 0.1f),
                            contentColor = if (selectedPlayerCount == 4) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("👥 4 Players", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        onClick = { onDealerSelect(1, selectedPlayerCount) },
                        tag = "dealer_choice_user"
                    )

                    DealerSelectionChoice(
                        name = "Roll Random",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            val rolled = (1..selectedPlayerCount).random()
                            onDealerSelect(rolled, selectedPlayerCount)
                        },
                        tag = "dealer_choice_random"
                    )

                    DealerSelectionChoice(
                        name = if (selectedPlayerCount == 2) "AI Opponent" else "AI Left",
                        icon = Icons.Default.Person,
                        onClick = { onDealerSelect(2, selectedPlayerCount) },
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
                        RowScoreCard(player = player, deckStyle = state.deckStyle)
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
fun RowScoreCard(
    player: PlayerState,
    deckStyle: DeckStyle = DeckStyle.BLUE_CLASSIC
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Player identity and total points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (player.id == 1) CasinoGold else Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (player.id == 1) "👤" else "🤖",
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = player.name + if (player.id == 1) " (You)" else "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (player.id == 1) CasinoGold else Color.White
                    )
                }
                
                Surface(
                    color = if (player.netScore >= 0) CaptionGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (player.netScore >= 0) CaptionGreen else Color.Red)
                ) {
                    Text(
                        text = "${player.netScore} pts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (player.netScore >= 0) CaptionGreen else Color.LightGray,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Melded / Captured Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 CAPTURED TASH (${player.captured.size} cards)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CaptionGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "+${player.capturedPoints}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaptionGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (player.captured.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        player.captured.forEach { card ->
                            GameCard(
                                card = card,
                                isSelected = false,
                                width = 44.dp,
                                height = 64.dp,
                                deckStyle = deckStyle
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No cards captured yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section 2: Unmatched leftover hand cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❌ UNMATCHED TASH (${player.hand.size} leftover)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "-${player.handPenaltyPoints}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (player.hand.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        player.hand.forEach { card ->
                            GameCard(
                                card = card,
                                isSelected = false,
                                width = 44.dp,
                                height = 64.dp,
                                deckStyle = deckStyle
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B5E20)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = CasinoGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Clean match! 0 Penalty",
                            style = MaterialTheme.typography.labelSmall,
                            color = CaptionGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
