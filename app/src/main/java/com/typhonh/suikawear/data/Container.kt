package com.typhonh.suikawear.data

import com.typhonh.suikawear.R
import de.chaffic.dynamics.Body
import de.chaffic.geometry.Polygon

data class Container(
    val height: Float = 0.69f,
    val width: Float = 0.57f,
    val imageHeight: Float = 0.72f,
    val imageWidth:  Float = 0.6f,
    val image: Int = R.drawable.container,
    val imageTop: Int = R.drawable.container_top,
    override var posY: Float = 0.05f,
    override var posX: Float = 0f,
    var bottom: Body = Body( // Bottom
        Polygon(width.toDouble(), height.toDouble()),
        posX.toDouble(),
        height*2 + posY.toDouble()
    ),
    var left: Body = Body( // Left
        Polygon(0.5, height.toDouble()),
        -width + posX.toDouble() - 0.5,
        posY.toDouble()
    ),
    var right: Body = Body( // Right
        Polygon(0.5, height.toDouble()),
        width - posX.toDouble() + 0.5,
        posY.toDouble()
    )
) : UiObject
