package com.example

import com.example.cuttherope.data.LevelRepository
import com.example.cuttherope.model.CandyType
import com.example.cuttherope.model.Vector2
import com.example.cuttherope.physics.PhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun vectorOperations_areAccurate() {
        val v1 = Vector2(3f, 4f)
        assertEquals(5f, v1.length(), 0.001f)

        val v2 = Vector2(0f, 0f)
        assertEquals(5f, v1.distanceTo(v2), 0.001f)

        val norm = v1.normalized()
        assertEquals(1f, norm.length(), 0.001f)
        assertEquals(0.6f, norm.x, 0.001f)
        assertEquals(0.8f, norm.y, 0.001f)
    }

    @Test
    fun segmentIntersection_detectsCrosses() {
        // Intersecting horizontal and vertical cross
        val s1 = Vector2(0f, 10f)
        val s2 = Vector2(20f, 10f)
        val p1 = Vector2(10f, 0f)
        val p2 = Vector2(10f, 20f)
        assertTrue(PhysicsEngine.segmentsIntersect(s1, s2, p1, p2))

        // Parallel non-intersecting
        val p3 = Vector2(0f, 20f)
        val p4 = Vector2(20f, 20f)
        assertFalse(PhysicsEngine.segmentsIntersect(s1, s2, p3, p4))
    }

    @Test
    fun campaignLevels_have3StarsAndOmNom() {
        assertEquals(12, LevelRepository.campaignLevels.size)
        for (lvl in LevelRepository.campaignLevels) {
            assertTrue("Level ${lvl.name} must have omNomPos", lvl.omNomPos.y > 0)
            assertTrue("Level ${lvl.name} must have candySpawnPos", lvl.candySpawnPos.y > 0)
            assertTrue("Level ${lvl.name} must have ropes", lvl.ropes.isNotEmpty())
            assertEquals("Level ${lvl.name} must have 3 stars", 3, lvl.stars.size)
        }
    }

    @Test
    fun candySkins_haveProgressiveUnlocks() {
        val classic = CandyType.CLASSIC
        assertEquals(0, classic.requiredStars)

        val berry = CandyType.BERRY_ORB
        assertEquals(5, berry.requiredStars)

        val donut = CandyType.COSMIC_DONUT
        assertEquals(30, donut.requiredStars)
    }
}
