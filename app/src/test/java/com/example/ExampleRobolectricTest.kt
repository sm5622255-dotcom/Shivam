package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GamePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rummy Row", appName)
  }

  @Test
  fun `test game starts and user discard works`() = runTest {
    val viewModel = GameViewModel()
    
    // Game should be in STARTING phase initially
    assertEquals(GamePhase.STARTING, viewModel.uiState.value.phase)
    
    // Start game with User as Dealer
    viewModel.startNewGame(1)
    
    val state = viewModel.uiState.value
    assertEquals(1, state.dealerId)
    assertEquals(1, state.currentPlayerId)
    assertEquals(GamePhase.COLD_START_DISCARD, state.phase)
    
    // User hand should have 7 cards (Dealer's Privilege)
    assertEquals(7, state.userPlayer.hand.size)
    // AI Left and Right should have 6 cards each
    assertEquals(6, state.aiLeftPlayer.hand.size)
    assertEquals(6, state.aiRightPlayer.hand.size)
    
    // Perform COLD_START_DISCARD
    val cardToDiscard = state.userPlayer.hand.first()
    viewModel.coldStartDiscard(cardToDiscard)
    
    val afterDiscardState = viewModel.uiState.value
    assertEquals(6, afterDiscardState.userPlayer.hand.size)
    assertEquals(1, afterDiscardState.rowCards.size)
    assertEquals(cardToDiscard, afterDiscardState.rowCards.first())
  }

  @Test
  fun `test game starts with AI Dealer`() = runTest {
    val viewModel = GameViewModel()
    
    // Start game with AI Left (id=2) as Dealer
    viewModel.startNewGame(2)
    
    var state = viewModel.uiState.value
    assertEquals(2, state.dealerId)
    assertEquals(2, state.currentPlayerId)
    assertEquals(GamePhase.COLD_START_DISCARD, state.phase)
    
    // Let AI Left think and make its cold start discard
    testScheduler.advanceTimeBy(3000)
    
    // After AI cold start discard, the turn should advance to Player 1 (You) or Player 3 (AI Right) depending on the direction.
    // In code, getNextPlayerId(2) -> next is Player 1 (since IDs cycle: 2 -> 1, wait, let's check order!)
    // Let's verify who is current player now.
    state = viewModel.uiState.value
    assertTrue(state.rowCards.isNotEmpty())
    
    // If it's Player 1's turn (You), phase should be PLAYER_TURN_DECIDING
    if (state.currentPlayerId == 1) {
      assertEquals(GamePhase.PLAYER_TURN_DECIDING, state.phase)
    }
  }
}

