package com.example.cuttherope.physics

import com.example.cuttherope.model.AirBlower
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.LevelData
import com.example.cuttherope.model.OmNomState
import com.example.cuttherope.model.Particle
import com.example.cuttherope.model.ParticleShape
import com.example.cuttherope.model.Vector2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class RopeParticle(
    var pos: Vector2,
    var oldPos: Vector2,
    val isPinned: Boolean = false
)

class ActiveRope(
    val id: String,
    val pegId: String,
    val particles: MutableList<RopeParticle>,
    val segmentLength: Float,
    var isCut: Boolean = false,
    var cutIndex: Int = -1 // segment index where cut happened
)

class PhysicsEngine {
    // Gravity & constants
    val gravityNormal = 850f
    val gravityBubble = -320f
    val candyRadius = 24f

    var candyPos = Vector2(0f, 0f)
    var candyOldPos = Vector2(0f, 0f)
    var candyInBubble = false
    var candyIsEaten = false
    var candyIsDestroyed = false
    var candyRotation = 0f

    val ropes = mutableListOf<ActiveRope>()
    val particles = mutableListOf<Particle>()

    fun initLevel(level: LevelData) {
        candyPos = level.candySpawnPos.copy()
        candyOldPos = level.candySpawnPos.copy()
        candyInBubble = false
        candyIsEaten = false
        candyIsDestroyed = false
        candyRotation = 0f

        ropes.clear()
        particles.clear()

        // Create active ropes
        for (rData in level.ropes) {
            val peg = level.pegs.find { it.id == rData.pegId } ?: continue
            val numSegments = max(4, rData.segments)
            val segLen = rData.length / numSegments

            val pList = mutableListOf<RopeParticle>()
            val delta = (candyPos - peg.position)
            val straightDist = delta.length()
            val extraSag = (rData.length - straightDist).coerceAtLeast(0f) * 0.75f
            for (i in 0..numSegments) {
                val t = i.toFloat() / numSegments
                val sagY = sin(t * Math.PI.toFloat()) * extraSag
                val pt = peg.position + (delta * t) + Vector2(0f, sagY)
                pList.add(RopeParticle(pt.copy(), pt.copy(), isPinned = (i == 0)))
            }

            ropes.add(
                ActiveRope(
                    id = rData.id,
                    pegId = peg.id,
                    particles = pList,
                    segmentLength = segLen
                )
            )
        }
    }

    fun update(
        dt: Float,
        screenWidth: Float,
        screenHeight: Float,
        blowers: List<AirBlower>
    ) {
        if (candyIsEaten || candyIsDestroyed) {
            updateParticles(dt)
            return
        }

        val clampedDt = dt.coerceIn(0.001f, 0.033f)

        // 1. Candy Verlet integration
        val cTemp = candyPos.copy()
        val cVel = (candyPos - candyOldPos) * 0.99f // Damping

        val grav = if (candyInBubble) gravityBubble else gravityNormal
        var force = Vector2(0f, grav)

        // Apply air blower force if candy is in airflow
        for (blower in blowers) {
            val toCandy = candyPos - blower.position
            val dist = toCandy.length()
            if (dist < 320f && dist > 10f) {
                val dot = (toCandy.x * blower.direction.x + toCandy.y * blower.direction.y) / dist
                if (dot > 0.65f) { // In cone of airflow
                    val power = (1f - dist / 320f) * blower.strength
                    force += blower.direction * power
                }
            }
        }

        candyPos += cVel + (force * (clampedDt * clampedDt))
        candyOldPos = cTemp

        // Spin candy based on velocity
        candyRotation += cVel.x * 0.08f

        // Bubble floating gentle wobble
        if (candyInBubble) {
            candyPos.x += sin(System.currentTimeMillis() * 0.006f) * 0.4f
        }

        // Screen boundary collision (soft bounce off walls)
        if (candyPos.x - candyRadius < 10f) {
            candyPos.x = 10f + candyRadius
            candyOldPos.x = candyPos.x + cVel.x * 0.4f
        } else if (candyPos.x + candyRadius > screenWidth - 10f) {
            candyPos.x = screenWidth - 10f - candyRadius
            candyOldPos.x = candyPos.x + cVel.x * 0.4f
        }

        // Top boundary bounce if floating in bubble
        if (candyPos.y - candyRadius < 20f && candyInBubble) {
            candyPos.y = 20f + candyRadius
            candyOldPos.y = candyPos.y + 2f
        }

        // 2. Rope physics update
        val ropeGrav = 600f
        for (rope in ropes) {
            for (p in rope.particles) {
                if (p.isPinned) continue
                val temp = p.pos.copy()
                val vel = (p.pos - p.oldPos) * 0.985f
                p.pos += vel + Vector2(0f, ropeGrav * clampedDt * clampedDt)
                p.oldPos = temp
            }
        }

        // 3. Relax distance constraints multiple iterations for realism and stiffness
        val iterations = 12
        for (iter in 0 until iterations) {
            for (rope in ropes) {
                // If cut, we constrain in two separate segments
                val maxIdx = if (rope.isCut) rope.cutIndex else rope.particles.size - 1

                // Peg-side rope segment
                for (i in 0 until maxIdx) {
                    val p1 = rope.particles[i]
                    val p2 = rope.particles[i + 1]
                    val delta = p2.pos - p1.pos
                    val dist = delta.length()
                    if (dist > 0.0001f) {
                        val diff = (dist - rope.segmentLength) / dist
                        val shift = delta * 0.5f * diff
                        if (!p1.isPinned) p1.pos += shift
                        if (!p2.isPinned) p2.pos -= shift
                    }
                }

                // If uncut, the last rope particle is tied to the candy!
                if (!rope.isCut && rope.particles.isNotEmpty()) {
                    val lastP = rope.particles.last()
                    val toCandy = candyPos - lastP.pos
                    val dist = toCandy.length()
                    if (dist > 0.0001f) {
                        val diff = (dist - (rope.segmentLength * 0.5f)) / dist
                        val shift = toCandy * 0.5f * diff
                        lastP.pos += shift * 0.7f
                        candyPos -= shift * 0.3f
                    }
                }
            }
        }

        updateParticles(clampedDt)
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dt * 1.5f
            if (p.life <= 0f) {
                iter.remove()
            } else {
                p.position += p.velocity * dt
                p.velocity.y += 350f * dt // Gravity on sparks
            }
        }
    }

    // Cut rope test using swipe segment (swipeStart -> swipeEnd)
    fun checkSlice(swipeStart: Vector2, swipeEnd: Vector2): Boolean {
        var didCut = false
        val thresholdSq = 18f * 18f
        for (rope in ropes) {
            if (rope.isCut) continue
            for (i in 0 until rope.particles.size - 1) {
                val p1 = rope.particles[i].pos
                val p2 = rope.particles[i + 1].pos
                val isIntersecting = segmentsIntersect(swipeStart, swipeEnd, p1, p2)
                val isNear = distToSegmentSquared(swipeEnd, p1, p2) < thresholdSq ||
                             distToSegmentSquared(swipeStart, p1, p2) < thresholdSq

                if (isIntersecting || isNear) {
                    rope.isCut = true
                    rope.cutIndex = i
                    didCut = true

                    // Spawn cut spark particles
                    val mid = (p1 + p2) * 0.5f
                    spawnCutSparks(mid)
                    break
                }
            }
        }
        return didCut
    }

    private fun distToSegmentSquared(p: Vector2, v: Vector2, w: Vector2): Float {
        val l2 = v.distanceSquaredTo(w)
        if (l2 < 0.0001f) return p.distanceSquaredTo(v)
        val t = (((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2).coerceIn(0f, 1f)
        val projX = v.x + t * (w.x - v.x)
        val projY = v.y + t * (w.y - v.y)
        val dx = p.x - projX
        val dy = p.y - projY
        return dx * dx + dy * dy
    }

    fun spawnCutSparks(pos: Vector2) {
        for (i in 0 until 14) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 160f + 40f
            particles.add(
                Particle(
                    position = pos.copy(),
                    velocity = Vector2(cos(angle) * speed, sin(angle) * speed),
                    color = if (i % 2 == 0) 0xFFFFEE58 else 0xFFFF7043,
                    life = 1f,
                    size = Random.nextFloat() * 5f + 3f,
                    shape = ParticleShape.SPARK
                )
            )
        }
    }

    fun spawnStarSparkles(pos: Vector2) {
        for (i in 0 until 16) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 190f + 60f
            particles.add(
                Particle(
                    position = pos.copy(),
                    velocity = Vector2(cos(angle) * speed, sin(angle) * speed),
                    color = if (i % 3 == 0) 0xFFFFD700 else (if (i % 2 == 0) 0xFFFFF176 else 0xFFFFFFFF),
                    life = 1f,
                    size = Random.nextFloat() * 7f + 4f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    fun spawnBubblePopParticles(pos: Vector2) {
        for (i in 0 until 12) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 120f + 30f
            particles.add(
                Particle(
                    position = pos.copy(),
                    velocity = Vector2(cos(angle) * speed, sin(angle) * speed),
                    color = 0xAA81D4FA,
                    life = 0.8f,
                    size = Random.nextFloat() * 6f + 3f,
                    shape = ParticleShape.CIRCLE
                )
            )
        }
    }

    fun spawnChompHearts(pos: Vector2) {
        for (i in 0 until 12) {
            val angle = -Random.nextFloat() * Math.PI.toFloat() // Upward fountain
            val speed = Random.nextFloat() * 180f + 80f
            particles.add(
                Particle(
                    position = pos.copy(),
                    velocity = Vector2(cos(angle) * speed, sin(angle) * speed),
                    color = if (i % 2 == 0) 0xFFFF4081 else 0xFFFFEB3B,
                    life = 1.2f,
                    size = Random.nextFloat() * 8f + 6f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    companion object {
        fun segmentsIntersect(p1: Vector2, p2: Vector2, p3: Vector2, p4: Vector2): Boolean {
            fun ccw(a: Vector2, b: Vector2, c: Vector2): Boolean {
                return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
            }
            return (ccw(p1, p3, p4) != ccw(p2, p3, p4)) && (ccw(p1, p2, p3) != ccw(p1, p2, p4))
        }
    }
}
