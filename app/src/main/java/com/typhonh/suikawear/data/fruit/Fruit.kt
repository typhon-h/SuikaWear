package com.typhonh.suikawear.data.fruit

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.reflect.KClass


abstract class Fruit(var radius: Float) {
    abstract var image: Int
    abstract var points: Int
    private var body: Body? = null
    private var bodyDef: BodyDef = BodyDef().apply {
        type = BodyType.DYNAMIC
        position = Vec2(PENDING_X, PENDING_Y)
        bullet = true
    }

    var isDropped: Boolean = false

    fun body(world: World): Body {
        if(body != null) {
            return body as Body
        }

        val createdBody = world.createBody(bodyDef)

        val shape = CircleShape()
        shape.radius = radius

        createdBody.createFixture(shape, 1f).apply {
            friction = 0.1f
            restitution = 0f
        }

        body = createdBody
        return createdBody
    }

    fun position(): Vec2 {
        if(body == null) {
            return bodyDef.position
        }

        return body!!.position
    }

    fun position(x: Float, y: Float) {
        if (body == null) {
            bodyDef.position = Vec2(x, y)
        }
    }

    fun orientation(): Float {
        if(body == null) {
            return 0f
        }

        return body!!.angle
    }

    fun isTouching(fruit: Fruit): Boolean {
        if(this.body == null || fruit.body == null) {
            return false
        }
        val dx = fruit.body!!.position.x - this.body!!.position.x
        val dy = fruit.body!!.position.y - this.body!!.position.y
        val dist = hypot(dx,dy)
        return dist <= this.radius + fruit.radius
    }

    fun canMergeWith(fruit: Fruit): Boolean {
        return this::class == fruit::class && this.isTouching(fruit)
    }

    companion object {
        const val NEXT_X = 0.8f
        const val NEXT_Y = 0.0f
        const val PENDING_X = 0.0f
        const val PENDING_Y = -0.8f
        const val NEXT_FRAME_RADIUS = 0.171f
        fun getPendingCandidate(): Fruit {
            val candidates: List<Fruit> = listOf(
                Cherry(),
                Strawberry(),
                Grape(),
                Satsuma(),
                Persimmon(),
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

