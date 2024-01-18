package com.typhonh.suikawear.business

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typhonh.suikawear.data.GameState
import com.typhonh.suikawear.data.fruit.Fruit
import de.chaffic.dynamics.World
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

    init {
        listOf(state.container.bottom, state.container.left, state.container.right).forEach {
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
        if(state.pendingFruit.isDropped) {
            return
        }
        if (rotationPixels > 0) {
            state.pendingFruit.body.position.x += 0.05f
        } else if (rotationPixels < 0) {
            state.pendingFruit.body.position.x -= 0.05f
        }

        clampPendingFruit()
    }

    fun onDrag(position: Offset, size: IntSize) {
        if(state.pendingFruit.isDropped) {
            return
        }
        state.pendingFruit.body.position.x = 2 * (position.x.toDouble() / size.width) - 1

        clampPendingFruit()
    }

    fun onTap() {
        if(state.pendingFruit.isDropped) {
            return
        }
        state.pendingFruit.isDropped = true
        world.addBody(state.pendingFruit.body)
        state.droppedFruits.add(state.pendingFruit)
    }

    private fun update() {
        state.ticks++
        world.step(UPDATE_INTERVAL.toDouble())
        checkDroppedFruit()
        tryMergeFruit()
        emitLatestState()
    }

    private fun checkDroppedFruit() {
        if(!state.pendingFruit.isDropped) {
            return
        }
        var fallen = state.pendingFruit.body.position.y >= state.container.bottom.position.y - state.container.height - state.pendingFruit.radius
        for(fruit in state.droppedFruits.minus(state.pendingFruit)) {
            if(fruit.isTouching(state.pendingFruit) || fallen) {
                fallen = true
                break
            }
        }

        if(fallen) {
            val oldX = state.pendingFruit.body.position.x
            state.pendingFruit = state.nextFruit
            state.nextFruit = Fruit.getPendingCandidate()
            state.pendingFruit.body.position.x = oldX
            clampPendingFruit()
        }
    }

    private fun tryMergeFruit() {
        var i = 0
        var largestFruit: Fruit? = null
        while (i < state.droppedFruits.size) {
            val f1 = state.droppedFruits[i]

            var j = i + 1
            while (j < state.droppedFruits.size) {
                val f2 = state.droppedFruits[j]
                if(f1::class == f2::class && f1.isTouching(f2)) {
                    val nextFruit = Fruit.getNextFruit(f1::class)
                    largestFruit =  if (largestFruit == null || f1.radius > largestFruit.radius) f1 else largestFruit

                    if(nextFruit != null) {
                        combineFruit(f1, f2, nextFruit)
                    } else {
                        clearFruit()
                    }

                    break
                }
                j++
            }

            i++
        }

        state.score += largestFruit?.points ?: 0
    }

    private fun combineFruit(f1: Fruit, f2: Fruit, nextFruit:Fruit) {
        nextFruit.body.position.x = (f1.body.position.x + f2.body.position.x) / 2
        nextFruit.body.position.y = (f1.body.position.y + f2.body.position.y) / 2
        state.droppedFruits.add(nextFruit)
        world.addBody(nextFruit.body)
        state.droppedFruits.remove(f1)
        state.droppedFruits.remove(f2)

        world.removeBody(f1.body)
        world.removeBody(f2.body)
    }
    private fun clearFruit() {
        while (state.droppedFruits.isNotEmpty()) {
            world.removeBody(state.droppedFruits.first().body)
            state.droppedFruits.remove(state.droppedFruits.first())
        }
    }

    private fun emitLatestState() {
        _uiState.update {
            GameState(
                ticks = state.ticks,
                container = state.container,
                pendingFruit = state.pendingFruit,
                nextFruit = state.nextFruit,
                droppedFruits = state.droppedFruits,
                score = state.score
            )
        }
    }

    private fun clampPendingFruit() {
        state.pendingFruit.body.position.x = state.pendingFruit.body.position.x
            .coerceIn(
                -state.container.width + state.pendingFruit.radius,
                state.container.width - state.pendingFruit.radius
            )
    }

    companion object {
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 0.000004 // %s^-2
    }
}
