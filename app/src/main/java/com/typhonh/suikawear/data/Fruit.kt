package com.typhonh.suikawear.data

data class Fruit(
    override var posX: Float = 0f,
    override val posY: Float = -0.85f,
    val radius: Float = 0.1f
) : UiObject
