package com.typhonh.suikawear.data

import com.typhonh.suikawear.R

data class Container(
    val height: Float = 0.69f,
    val width: Float = 0.57f,
    val imageHeight: Float = 0.72f,
    val imageWidth:  Float = 0.6f,
    val image: Int = R.drawable.container,
    val imageTop: Int = R.drawable.container_top,
    override var posY: Float = 0.05f,
    override var posX: Float = 0f,
) : UiObject
