package com.typhonh.suikawear.data.fruit

import com.typhonh.suikawear.data.UiObject

interface Fruit : UiObject {
    val radius: Float
    var velY: Float

    companion object {
        const val PENDING_X_POS = 0f
        const val PENDING_Y_POS = -0.8f
    }
}
