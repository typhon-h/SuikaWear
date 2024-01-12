package com.typhonh.suikawear.business

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

interface GameController {
    fun onRotateEvent(rotationPixels: Float)
    fun onDragEvent(position: Offset, size: IntSize)
}