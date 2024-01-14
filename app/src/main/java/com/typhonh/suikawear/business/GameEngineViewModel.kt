package com.typhonh.suikawear.business

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typhonh.suikawear.data.GameState
import com.typhonh.suikawear.data.fruit.Fruit
import de.chaffic.dynamics.Body
import de.chaffic.dynamics.World
import de.chaffic.geometry.Polygon
import de.chaffic.math.Vec2
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

    private val world = World(Vec2(0.0, GRAVITY))

    private val borders = listOf(
        Body( // Bottom
            Polygon(state.container.width.toDouble(), 1.0),
            state.container.posX.toDouble(),
            state.container.height + state.container.posY.toDouble() + 1.0
        ),
        Body( // Left
            Polygon(0.5, state.container.height.toDouble() * 2),
            -state.container.width + state.container.posX.toDouble() - 0.5,
            state.container.posY.toDouble()
        ),
        Body( // Right
            Polygon(0.5, state.container.height.toDouble() * 2),
            state.container.width - state.container.posX.toDouble() + 0.5,
            state.container.posY.toDouble()
        ),
    )


    init {
        borders.forEach {
            it.density = 0.0
            it.affectedByGravity = false
            it.restitution = 0.0
            world.addBody(it)
        }

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
            state.pendingFruit.body.position.x += 0.05f
        } else if (rotationPixels < 0) {
            state.pendingFruit.body.position.x -= 0.05f
        }

        state.pendingFruit.body.position.x = state.pendingFruit.body.position.x
            .coerceIn(
                -state.container.width + state.pendingFruit.radius,
                state.container.width - state.pendingFruit.radius
            )
    }

    fun onDrag(position: Offset, size: IntSize) {
        state.pendingFruit.body.position.x = (position.x / size.width) - 0.5

        state.pendingFruit.body.position.x = state.pendingFruit.body.position.x
            .coerceIn(
                -state.container.width + state.pendingFruit.radius,
                state.container.width - state.pendingFruit.radius
            )
    }

    fun onTap() {
        state.pendingFruit.isDropped = true
        val oldX = state.pendingFruit.body.position.x
        world.addBody(state.pendingFruit.body)
        state.droppedFruits.add(state.pendingFruit)
        state.pendingFruit = Fruit.getPendingCandidate()
        state.pendingFruit.body.position.x = oldX
    }

    private fun update() {
        state.ticks++
        world.step(UPDATE_INTERVAL.toDouble() / 2 )
        world.step(UPDATE_INTERVAL.toDouble() / 2 )
        emitLatestState()
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
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 0.000005 // %s^-2
    }
}
