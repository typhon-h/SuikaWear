package com.typhonh.suikawear.business

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typhonh.suikawear.data.Container
import com.typhonh.suikawear.data.Fruit
import com.typhonh.suikawear.data.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GameEngineViewModel(
    private val state: GameState = GameState()
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                if (state.size != IntSize.Zero ) {
                    update()
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    fun onCanvasSizeChange(newSize: IntSize, isRound: Boolean) {
        state.isRound = isRound
        if (state.size != newSize) {
            state.size = newSize
        }
    }

    fun onRotate(rotationPixels: Float) {
        state.pendingFruit.posX += rotationPixels
    }

    fun onDrag(dragAmount: Offset) {
        state.pendingFruit.posX += dragAmount.x
    }

    private fun update() {
        state.ticks++

        emitLatestState()
    }

    private fun emitLatestState() {
        _uiState.update {
            GameState(
                ticks = state.ticks,
                container = Container(
                    width = state.container.width,
                    height = state.container.height,
                    posX = state.container.posX,
                    posY = state.container.posY,
                ),
                pendingFruit = Fruit(
                    posX = state.pendingFruit.posX,
                    posY = state.pendingFruit.posY,
                    radius = state.pendingFruit.radius
                )
            )
        }
    }

    companion object {
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
    }
}
