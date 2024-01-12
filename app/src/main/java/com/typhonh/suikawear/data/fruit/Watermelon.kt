package com.typhonh.suikawear.data.fruit

data class Watermelon(
    override var posX: Float = Fruit.PENDING_X_POS,
    override var posY: Float = Fruit.PENDING_Y_POS,
    override val radius: Float = 0.343f,
    override var velY: Float = 0f,
    override var isDropped: Boolean = false
) : Fruit