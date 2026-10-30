package com.example.cuttherope.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2 = if (scalar != 0f) Vector2(x / scalar, y / scalar) else Vector2(0f, 0f)

    fun length(): Float = sqrt(x * x + y * y)
    fun lengthSquared(): Float = x * x + y * y

    fun distanceTo(other: Vector2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun distanceSquaredTo(other: Vector2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return dx * dx + dy * dy
    }

    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0.0001f) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    fun copy(): Vector2 = Vector2(x, y)

    companion object {
        fun zero() = Vector2(0f, 0f)
        fun fromAngle(radians: Float, length: Float = 1f): Vector2 =
            Vector2(cos(radians) * length, sin(radians) * length)
    }
}
