package com.typhonh.suikawear.data

import androidx.compose.ui.unit.IntSize
import com.typhonh.suikawear.data.fruit.Fruit
import org.jbox2d.dynamics.Fixture

data class GameState(
    var ticks: Long = 0L,
    var size: IntSize = IntSize.Zero,
    var isRound: Boolean = true,
    val container: Container = Container(),
    var pendingFruit: Fruit = Fruit.getPendingCandidate(),
    var nextFruit: Fruit = Fruit.getPendingCandidate(),
    val droppedFruits: MutableMap<Fixture, Fruit> = mutableMapOf(),
    var score: Int = 0,
    var hasEnded: Boolean = false
    )
