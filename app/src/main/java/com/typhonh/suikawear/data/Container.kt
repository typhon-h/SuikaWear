package com.typhonh.suikawear.data

data class Container(
    val height: Float = 0.72f,
    val width: Float = 0.6f,
    override var posY: Float = 0.05f,
    override var posX: Float = 0f
) : UiObject
