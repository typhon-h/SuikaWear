package com.typhonh.suikawear.data.fruit

import de.chaffic.dynamics.Body
import de.chaffic.geometry.Circle
import kotlin.random.Random

abstract class Fruit(var radius: Double) {

    var body: Body = Body(Circle(radius), 0.0, -0.8)

    var isDropped: Boolean = false

    init {
        this.body.restitution = CO_EF_RESTITUTION
    }

    companion object {
        const val CO_EF_RESTITUTION = 0.8

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

