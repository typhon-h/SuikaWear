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
import de.chaffic.dynamics.World
import de.chaffic.math.Vec2
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GameEngineViewModel(
    private val state: GameState = GameState(),
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val world = World(Vec2(0.0, GRAVITY))

    init {
        listOf(state.container.bottom, state.container.left, state.container.right).forEach {
            world.addBody(it)
        }

        viewModelScope.launch {
            while (true) {
                if (state.size != IntSize.Zero && !state.hasEnded ) {
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

    private fun checkEndCondition() {
        for(fruit in state.droppedFruits.minus(state.pendingFruit)) {
            if(state.score != 0 && fruit.body.position.y <= state.container.posY - state.container.imageHeight + state.pendingFruit.radius) {
                state.hasEnded = true
                world.step(UPDATE_INTERVAL.toDouble())
                emitLatestState()
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
                score = state.score,
                hasEnded = state.hasEnded,
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

    private fun update() {
        state.ticks++
        world.step(UPDATE_INTERVAL.toDouble())
        checkDroppedFruit()
        tryMergeFruit()
        checkEndCondition()
        emitLatestState()
    }

    companion object {
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
        private const val GRAVITY = 0.000004 // %s^-2
    }
}
