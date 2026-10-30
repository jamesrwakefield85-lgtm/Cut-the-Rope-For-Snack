package com.example.cuttherope.model

data class AirBlower(
    val id: String,
    val position: Vector2,
    val direction: Vector2,
    val strength: Float = 420f,
    var puffAnim: Float = 0f
)
