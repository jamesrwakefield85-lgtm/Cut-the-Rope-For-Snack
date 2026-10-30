package com.example.cuttherope.model

enum class OmNomState {
    IDLE,       // Waiting with open mouth, eyes tracking candy
    EATING,     // Got the candy! Munching animation
    SAD         // Missed candy! Crying animation
}

enum class GameStatus {
    PLAYING,
    WON,
    FAILED
}

data class PegData(
    val id: String,
    val position: Vector2
)

data class RopeData(
    val id: String,
    val pegId: String,
    val length: Float = 180f,
    val segments: Int = 10
)

data class StarData(
    val id: String,
    val position: Vector2
)

data class BubbleData(
    val id: String,
    val position: Vector2
)

data class BlowerData(
    val id: String,
    val position: Vector2,
    val directionAngle: Float = 0f, // degrees, 0 = right, 90 = down, 180 = left, 270 = up
    val strength: Float = 400f
)

data class SpikeData(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class LevelData(
    val id: String,
    val name: String,
    val omNomPos: Vector2,
    val candySpawnPos: Vector2,
    val pegs: List<PegData>,
    val ropes: List<RopeData>,
    val stars: List<StarData>,
    val bubbles: List<BubbleData> = emptyList(),
    val blowers: List<BlowerData> = emptyList(),
    val spikes: List<SpikeData> = emptyList(),
    val isCustom: Boolean = false
)

data class Particle(
    var position: Vector2,
    var velocity: Vector2,
    var color: Long,
    var life: Float, // 1.0 down to 0.0
    var size: Float,
    var shape: ParticleShape = ParticleShape.CIRCLE
)

enum class ParticleShape {
    CIRCLE,
    STAR,
    SPARK
}
