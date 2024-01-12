package com.typhonh.suikawear.business

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typhonh.suikawear.data.Container
import com.typhonh.suikawear.data.GameState
import com.typhonh.suikawear.data.fruit.Fruit
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
        if (rotationPixels > 0) {
            state.pendingFruit.posX += 0.05f
        } else if (rotationPixels < 0) {
            state.pendingFruit.posX -= 0.05f
        }

        state.pendingFruit.posX = state.pendingFruit.posX
            .coerceIn(
                -state.container.width + state.pendingFruit.radius,
                state.container.width - state.pendingFruit.radius
            )
    }

    fun onDrag(position: Offset, size: IntSize) {
        state.pendingFruit.posX = (position.x / size.width) - 0.5f

        state.pendingFruit.posX = state.pendingFruit.posX
            .coerceIn(
                -state.container.width + state.pendingFruit.radius,
                state.container.width - state.pendingFruit.radius
            )
    }

    private fun update() {
        state.ticks++

        applyFruitPhysics(state.pendingFruit)

        emitLatestState()
    }

    private fun applyFruitPhysics(fruit: Fruit) {
        fruit.velY += GRAVITY
        fruit.posY += fruit.velY

        checkContainerCollision(fruit, state.container)
    }

    private fun checkContainerCollision(fruit: Fruit, container: Container) {
        val containerBottom = (container.height) + container.posY
        if (fruit.posY + fruit.radius >= containerBottom) {
            fruit.posY = containerBottom - fruit.radius
            fruit.velY *= -1 * container.coEfRestitution
        }
    }

    private fun emitLatestState() {
        _uiState.update {
            GameState(
                ticks = state.ticks,
                container = state.container,
                pendingFruit = state.pendingFruit
            )
        }
    }

    companion object {
        private const val FPS = 15
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 0.015f // %s^-2
    }
}
