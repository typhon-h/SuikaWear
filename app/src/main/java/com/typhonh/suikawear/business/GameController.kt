package com.typhonh.suikawear.business

import androidx.compose.ui.geometry.Offset

interface GameController {
    fun onRotateEvent(rotationPixels: Float)
    fun onDragEvent(dragAmount: Offset)
}