package com.typhonh.suikawear.data

import androidx.compose.ui.unit.IntSize

data class GameState (
    var ticks: Long = 0L,
    var size: IntSize = IntSize.Zero,
    var isRound: Boolean = true,
    val container: Container = Container(),
    val pendingFruit: Fruit = Fruit()
    )