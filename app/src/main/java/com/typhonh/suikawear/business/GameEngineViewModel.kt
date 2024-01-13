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
import kotlin.math.abs
import kotlin.math.sqrt

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

        state.droppedFruits.forEach {
            advanceFruitPosition(it)
            checkContainerCollision(it, state.container)
        }
        for(i in 0 until state.droppedFruits.size) {
            for(j in i+1 until state.droppedFruits.size) {
                checkFruitCollision(state.droppedFruits[i], state.droppedFruits[j])
                checkContainerCollision(state.droppedFruits[i], state.container)
                resolvePenetration(state.droppedFruits[i], state.droppedFruits[j])
            }
        }

        emitLatestState()
    }

    private fun advanceFruitPosition(fruit: Fruit) {
        val containerBottom = (state.container.height) + state.container.posY
        if (fruit.posY + fruit.radius >= containerBottom) {
            fruit.velY = 0f
        } else {
            fruit.velY += GRAVITY
        }
        fruit.posY += fruit.velY

        fruit.posX += fruit.velX
    }

    private fun checkFruitCollision(fruit: Fruit, otherFruit: Fruit) {

        // Calculate the distance between the two balls
        val dx = otherFruit.posX - fruit.posX
        val dy = otherFruit.posY - fruit.posY
        val dist = sqrt(dx*dx + dy*dy)

        // Check for collision
        if (dist < fruit.radius + otherFruit.radius) {
            // Calculate normal vector
            val normalX = dx / dist
            val normalY = dy / dist

            // Calculate relative velocity
            val relVelX = otherFruit.velX - fruit.velX
            val relVelY = otherFruit.velY - fruit.velY

            // Calculate relative velocity in normal direction
            val relVelNormal = relVelX * normalX + relVelY * normalY

            // Calculate impulse
            val impulse = (2 / (fruit.radius + otherFruit.radius)) * relVelNormal

            // Update velocities after collision with energy loss
            fruit.velX += impulse * otherFruit.radius * normalX
            fruit.velY += impulse * otherFruit.radius * normalY
            otherFruit.velX -= impulse * fruit.radius * normalX
            otherFruit.velY -= impulse * fruit.radius * normalY

            // Apply energy loss
            fruit.velX *= Fruit.CO_EF_RESTITUTION
            fruit.velY *= Fruit.CO_EF_RESTITUTION
            otherFruit.velX *= Fruit.CO_EF_RESTITUTION
            otherFruit.velY *= Fruit.CO_EF_RESTITUTION
        }
    }

    private fun resolvePenetration(fruit: Fruit, otherFruit: Fruit) {
        // Calculate the distance between the two balls
        val dx = otherFruit.posX - fruit.posX
        val dy = otherFruit.posY - fruit.posY
        val distance = sqrt(dx*dx + dy*dy)

        // Calculate penetration depth
        val penetrationAmount = (fruit.radius + otherFruit.radius) - distance

        // Move the balls away from each other along the collision normal only if they are overlapping
        if( penetrationAmount > 0){
        val normalX = dx / distance
        val normalY = dy / distance

        // Move the balls along the collision normal
        val moveX = (penetrationAmount / 2) * normalX
        val moveY = (penetrationAmount / 2) * normalY

        fruit.posX -= moveX
        fruit.posY -= moveY
        otherFruit.posX += moveX
        otherFruit.posY += moveY }
    }

    private fun checkContainerCollision(fruit: Fruit, container: Container) {
        val containerBottom = (container.height) + container.posY
        if (fruit.posY + fruit.radius >= containerBottom) {
            fruit.posY = containerBottom - fruit.radius
            fruit.velY = -abs(fruit.velY) * container.coEfRestitution
        }

        val containerLeft = -container.width + container.posX
        if (fruit.posX - fruit.radius <= containerLeft) {
            fruit.posX = containerLeft + fruit.radius
            fruit.velX = abs(fruit.velX) * container.coEfRestitution
        }

        val containerRight = container.width - container.posX
        if (fruit.posX + fruit.radius >= containerRight) {
            fruit.posX = containerRight - fruit.radius
            fruit.velX = -abs(fruit.velX) * container.coEfRestitution
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
