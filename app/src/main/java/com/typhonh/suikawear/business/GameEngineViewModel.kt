package com.typhonh.suikawear.business

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typhonh.suikawear.data.GameState
import com.typhonh.suikawear.data.fruit.Fruit
import com.typhonh.suikawear.presentation.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GameEngineViewModel(
    private val state: GameState = GameState(),
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var world: World = World(Vec2(0f, GRAVITY))

    init {
        initContainer()
        viewModelScope.launch {
            while (true) {
                if (state.size != IntSize.Zero && !state.hasEnded ) {
                    update()
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private fun initContainer() {
        val bottom = world.createBody(BodyDef().apply {
            type = BodyType.STATIC
            position = Vec2(state.container.posX, state.container.posY + state.container.height * 2)
        })
        val bottomShape = PolygonShape()
        bottomShape.setAsBox(state.container.width, state.container.height)

        val left = world.createBody(BodyDef().apply {
            type = BodyType.STATIC
            position = Vec2(
                (-state.container.width + state.container.posX - 1),
                state.container.posY
                )
        })
        val leftShape = PolygonShape()
        leftShape.setAsBox(1f, state.container.height * 2)

        val right = world.createBody(BodyDef().apply {
            type = BodyType.STATIC
            position = Vec2(
                (state.container.width - state.container.posX + 1),
                state.container.posY
            )
        })
        val rightShape = PolygonShape()
        rightShape.setAsBox(1f, state.container.height * 2)

        bottom.createFixture(bottomShape, 0f).apply {
            restitution = 0f
            density = 1f
        }
        left.createFixture(leftShape, 0f).apply {
            restitution = 0f
        }
        right.createFixture(rightShape, 0f).apply {
            restitution = 0f
        }
    }


    fun onCanvasSizeChange(newSize: IntSize, isRound: Boolean) {
        state.isRound = isRound
        if (state.size != newSize) {
            state.size = newSize
        }
    }

    fun onRotate(rotationPixels: Float) {
        if(state.pendingFruit.isDropped || state.hasEnded) {
            return
        }
        if (rotationPixels > 0) {
            state.pendingFruit.position(
                state.pendingFruit.position().x + 0.05f,
                Fruit.PENDING_Y
            )
        } else if (rotationPixels < 0) {
            state.pendingFruit.position(
                state.pendingFruit.position().x - 0.05f,
                Fruit.PENDING_Y
            )
        }

        clampPendingFruit()
    }

    fun onDrag(position: Offset, size: IntSize) {
        if(state.pendingFruit.isDropped) {
            return
        }

        state.pendingFruit.position(
            2 * (position.x / size.width) - 1,
            Fruit.PENDING_Y
            )

        clampPendingFruit()
    }

    fun onTap() {
        if(state.pendingFruit.isDropped) {
            return
        }
        state.pendingFruit.isDropped = true

        state.pendingFruit.body(world)

        state.droppedFruits.add(state.pendingFruit)
    }

    private fun checkEndCondition() {
        for(fruit in state.droppedFruits.minus(state.pendingFruit)) {
            if(state.score != 0 && fruit.position().y <= state.container.posY - state.container.imageHeight + state.pendingFruit.radius) {
                state.hasEnded = true
                viewModelScope.launch {
                    updateHighScore()
                }
            }
        }
    }

    private suspend fun updateHighScore() {
        val scoreString = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(MainActivity.SETTINGS_KEY)] ?: ""
        }.first()

        var scores = scoreString.split(",").mapNotNull { s -> s.toIntOrNull() }.toMutableList()

        scores.add(state.score)
        scores.sortDescending()
        scores = scores.subList(0,min(3, scores.size))
        val commaSeparatedString = StringBuilder()
        scores.forEachIndexed { index, number ->
            commaSeparatedString.append(number)
            if (index != scores.lastIndex) { // Add comma except for the last element
                commaSeparatedString.append(",")
            }
        }

        dataStore.edit { currentPreferences ->
            currentPreferences[stringPreferencesKey(MainActivity.SETTINGS_KEY)] = commaSeparatedString.toString()
        }
    }

    fun resetGame() {
        state.score = 0
        clearFruit()
        state.hasEnded = false
        state.pendingFruit.isDropped = false
        state.pendingFruit = Fruit.getPendingCandidate()
        state.nextFruit = Fruit.getPendingCandidate()
    }

    private fun checkDroppedFruit() {
        if(!state.pendingFruit.isDropped) {
            return
        }
        var fallen = state.pendingFruit.position().y >= state.container.posY + state.container.height * 2 - state.container.imageHeight - state.pendingFruit.radius

        for(fruit in state.droppedFruits.minus(state.pendingFruit)) {
            if(fruit.isTouching(state.pendingFruit) || fallen) {
                fallen = true
                break
            }
        }

        if(fallen) {
            val oldX = state.pendingFruit.position().x
            state.pendingFruit = state.nextFruit
            state.nextFruit = Fruit.getPendingCandidate()
            state.pendingFruit.position(oldX, Fruit.PENDING_Y)
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
                if(f1.canMergeWith(f2)) {
                    val nextFruit = Fruit.getNextFruit(f1::class)
                    largestFruit = largestFruit?.takeIf { f1.radius > it.radius } ?: f1

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
        nextFruit.position(
            (f1.position().x + f2.position().x) / 2,
            (f1.position().y + f2.position().y) / 2
        )

        state.droppedFruits.add(nextFruit)
        nextFruit.body(world)
        state.droppedFruits.remove(f1)
        state.droppedFruits.remove(f2)

        world.destroyBody(f1.body(world))
        world.destroyBody(f2.body(world))
    }

    private fun clearFruit() {
        while (state.droppedFruits.isNotEmpty()) {
            world.destroyBody(state.droppedFruits.first().body(world))
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
                score = state.score,
                hasEnded = state.hasEnded,
            )
        }
    }

    private fun clampPendingFruit() {
        state.pendingFruit.position(
            state.pendingFruit.position().x
                .coerceIn(
                    -state.container.width + state.pendingFruit.radius,
                    state.container.width - state.pendingFruit.radius
                ),
            Fruit.PENDING_Y
        )
    }

    private fun update() {
        state.ticks++
        world.step(1f / FPS, 6, 2)
        checkDroppedFruit()
        tryMergeFruit()
        checkEndCondition()
        emitLatestState()
    }

    companion object {
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 6f // %s^-2
    }
}
