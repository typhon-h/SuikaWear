package com.typhonh.suikawear.data

import androidx.compose.ui.unit.IntSize
import com.typhonh.suikawear.data.fruit.Fruit

data class GameState (
    var ticks: Long = 0L,
    var size: IntSize = IntSize.Zero,
    var isRound: Boolean = true,
    val container: Container = Container(),
    var pendingFruit: Fruit = Fruit.getPendingCandidate(),
    var nextFruit: Fruit = Fruit.getPendingCandidate(),
    val droppedFruits: MutableList<Fruit> = mutableListOf(),
    var score: Int = 0,
    var hasEnded: Boolean = false
    )
