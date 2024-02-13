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
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Contact
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GameEngineViewModel(
    private val state: GameState = GameState(),
    private val dataStore: DataStore<Preferences>
) : ViewModel(), ContactListener {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var world: World = World(Vec2(0f, GRAVITY))
    private var mergeCandidates: Pair<Fruit, Fruit>? = null

    init {
        initContainer()
        world.setContactListener(this)
        viewModelScope.launch {
            while (true) {
                if (state.size != IntSize.Zero && !state.hasEnded) {
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
                (-state.container.imageWidth + state.container.posX - 1),
                state.container.posY
            )
        })
        val leftShape = PolygonShape()
        leftShape.setAsBox(1f, state.container.height * 2)

        val right = world.createBody(BodyDef().apply {
            type = BodyType.STATIC
            position = Vec2(
                (state.container.imageWidth - state.container.posX + 1),
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
        if (state.pendingFruit.isDropped || state.hasEnded) {
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
        if (state.pendingFruit.isDropped) {
            return
        }

        state.pendingFruit.position(
            2 * (position.x / size.width) - 1,
            Fruit.PENDING_Y
        )

        clampPendingFruit()
    }

    fun onTap() {
        if (state.pendingFruit.isDropped) {
            return
        }
        state.pendingFruit.isDropped = true

        state.pendingFruit.body(world)

        state.droppedFruits[state.pendingFruit.body(world).fixtureList] = state.pendingFruit
    }

    private fun checkEndCondition() {
        val fruits = if (state.pendingFruit.isDropped) state.droppedFruits.minus(state.pendingFruit.body(world).fixtureList) else state.droppedFruits
        for (fruit in fruits) {
            if (state.score != 0 && fruit.value.position().y <= state.container.posY - state.container.imageHeight + state.pendingFruit.radius) {
                state.hasEnded = true
                viewModelScope.launch {
                    updateHighScore()
                }
                emitLatestState()
                break
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
        scores = scores.subList(0, min(3, scores.size))
        val commaSeparatedString = StringBuilder()
        scores.forEachIndexed { index, number ->
            commaSeparatedString.append(number)
            if (index != scores.lastIndex) { // Add comma except for the last element
                commaSeparatedString.append(",")
            }
        }

        dataStore.edit { currentPreferences ->
            currentPreferences[stringPreferencesKey(MainActivity.SETTINGS_KEY)] =
                commaSeparatedString.toString()
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

    private fun clearFruit() {
        while (state.droppedFruits.isNotEmpty()) {
            val entry = state.droppedFruits.map{e -> e}.first()
            world.destroyBody(entry.value.body(world))
            state.droppedFruits.remove(entry.key)
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
        tryMerge()
        emitLatestState()
        checkEndCondition()
    }

    private fun tryMerge() {
        if (mergeCandidates != null) {
            val f1 = mergeCandidates!!.first
            val f2 = mergeCandidates!!.second

            val nextFruit = Fruit.getNextFruit(f1::class)
            if (nextFruit != null) {
                nextFruit.position(
                    (f1.position().x + f2.position().x) / 2,
                    (f1.position().y + f2.position().y) / 2
                )
                nextFruit.body(world)
                state.droppedFruits[nextFruit.body(world).fixtureList] = nextFruit
                state.droppedFruits.remove(f1.body(world).fixtureList)
                state.droppedFruits.remove(f2.body(world).fixtureList)

                world.destroyBody(f1.body(world))
                world.destroyBody(f2.body(world))
            } else { // Watermelon
                clearFruit()
            }
            state.score += f1.points
            mergeCandidates = null
        }
    }

        companion object {
            private const val FPS = 30
            private const val UPDATE_INTERVAL = 1000L / FPS
            private const val GRAVITY = 4.5f // %s^-2
        }

        override fun beginContact(contact: Contact?) {
            if (contact != null) {
                if (state.pendingFruit.isDropped &&
                    (contact.fixtureA == state.pendingFruit.body(world).fixtureList
                    || contact.fixtureB == state.pendingFruit.body(world).fixtureList)
                ) {
                    val oldX = state.pendingFruit.position().x
                    state.pendingFruit = state.nextFruit
                    state.nextFruit = Fruit.getPendingCandidate()
                    state.pendingFruit.position(oldX, Fruit.PENDING_Y)
                    clampPendingFruit()
                }
                if (state.droppedFruits[contact.fixtureA] != null
                    && state.droppedFruits[contact.fixtureB] != null
                ) {
                    val f1 = state.droppedFruits[contact.fixtureA]!!
                    val f2 = state.droppedFruits[contact.fixtureB]!!

                    if (f1.canMergeWith(f2)
                        && (mergeCandidates == null || f1.points > mergeCandidates!!.first.points)) {
                            mergeCandidates = Pair(f1, f2)
                        }
                    }
                }
            }

    override fun endContact(contact: Contact?) {
        // Do nothing
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
        // Do nothing
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
        // Do nothing
    }
}
