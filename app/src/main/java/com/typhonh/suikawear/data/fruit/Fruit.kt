package com.typhonh.suikawear.data.fruit

import com.typhonh.suikawear.data.UiObject
import kotlin.random.Random

interface Fruit : UiObject {
    val radius: Float
    var velY: Float
    var velX: Float

    var isDropped: Boolean

    companion object {
        const val PENDING_X_POS = 0f
        const val PENDING_Y_POS = -0.8f
        const val CO_EF_RESTITUTION = 0.8f

        fun getPendingCandidate(): Fruit {
            val candidates: List<Fruit> = listOf(
                Cherry(),
                Grape(),
                Satsuma(),
                Persimmon()
            )

            return candidates[Random.nextInt(0, candidates.size)]
        }
    }
}
