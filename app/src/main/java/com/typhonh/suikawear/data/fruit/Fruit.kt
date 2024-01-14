package com.typhonh.suikawear.data.fruit

import de.chaffic.dynamics.Body
import de.chaffic.geometry.Circle
import kotlin.random.Random

abstract class Fruit(var radius: Double) {

    var body: Body = Body(Circle(radius), 0.0, -0.8)

    var isDropped: Boolean = false

    init {
        this.body.restitution = .2
        this.body.mass = Math.PI * radius * radius
    }

    companion object {
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

