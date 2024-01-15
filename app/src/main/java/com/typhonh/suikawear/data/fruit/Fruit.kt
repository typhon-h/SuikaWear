package com.typhonh.suikawear.data.fruit

import de.chaffic.dynamics.Body
import de.chaffic.geometry.Circle
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.reflect.KClass

abstract class Fruit(var radius: Double) {
    abstract var image: Int
    var body: Body = Body(Circle(radius), 0.0, -0.8)

    var isDropped: Boolean = false

    init {
        this.body.restitution = .2
        this.body.mass = Math.PI * radius * radius
        this.body.inertia = this.body.mass

    }

    fun isTouching(fruit: Fruit): Boolean {
        val dx = fruit.body.position.x - this.body.position.x
        val dy = fruit.body.position.y - this.body.position.y
        val dist = hypot(dx,dy)
        return dist <= this.radius + fruit.radius
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

        fun getNextFruit(kClass: KClass<out Fruit>): Fruit? {
            return when (kClass) {
                Cherry::class -> Strawberry()
                Strawberry::class -> Grape()
                Grape::class -> Satsuma()
                Satsuma::class -> Persimmon()
                Persimmon::class -> Apple()
                Apple::class -> Pear()
                Pear::class -> Peach()
                Peach::class -> Pineapple()
                Pineapple::class -> Melon()
                Melon::class -> Watermelon()
                else -> null // Watermelon is end of chain
            }
        }
    }
}

