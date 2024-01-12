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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

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

    fun onTap() {
        state.pendingFruit.isDropped = true
        val oldX = state.pendingFruit.posX
        val oldY = state.pendingFruit.posY
        state.droppedFruits.add(state.pendingFruit)

        state.pendingFruit = Fruit.getPendingCandidate()
        state.pendingFruit.posX = oldX
        state.pendingFruit.posY = oldY
    }

    private fun update() {
        state.ticks++

        state.droppedFruits.forEach { applyFruitPhysics(it) }

        emitLatestState()
    }

    private fun applyFruitPhysics(fruit: Fruit) {
        fruit.velY += GRAVITY
        fruit.posY += fruit.velY
        checkContainerCollision(fruit, state.container)
        checkFruitCollision(fruit, state.droppedFruits.minus(fruit))
    }

    private fun checkFruitCollision(fruit: Fruit, otherFruits: Set<Fruit>) {
        otherFruits.forEach { f ->
            val dist = hypot(fruit.posX - f.posX, fruit.posY - f.posY)

            if(dist <= fruit.radius + f.radius) {
                val angle = atan2(f.posY - fruit.posY, f.posX - fruit.posX)

                val overlap = (fruit.radius + f.radius) - dist
                val overlapX = overlap * cos(angle) / 2
                val overlapY = overlap * sin(angle) / 2

                fruit.posX -= overlapX
                fruit.posY -= overlapY

                f.posX += overlapX
                f.posY += overlapY

                val tempVelY = fruit.velY
                fruit.velY = f.velY * Fruit.CO_EF_RESTITUTION
                f.velY = tempVelY * Fruit.CO_EF_RESTITUTION
            }
        }
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
                pendingFruit = state.pendingFruit,
                droppedFruits = state.droppedFruits
            )
        }
    }

    companion object {
        private const val FPS = 15
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 0.015f // %s^-2
    }
}
