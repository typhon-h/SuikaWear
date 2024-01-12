package com.typhonh.suikawear.data

data class Fruit(
    override var posX: Float = 0f,
    override val posY: Float = -0.75f,
    val radius: Float = 0.045f
) : UiObject
