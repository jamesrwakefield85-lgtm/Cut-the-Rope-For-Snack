package com.example.cuttherope.data

import com.example.cuttherope.model.BlowerData
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.LevelData
import com.example.cuttherope.model.PegData
import com.example.cuttherope.model.RopeData
import com.example.cuttherope.model.SpikeData
import com.example.cuttherope.model.StarData
import com.example.cuttherope.model.Vector2

object LevelRepository {
    val campaignLevels: List<LevelData> = listOf(
        // Level 1: First Cut (Tutorial)
        LevelData(
            id = "level_1",
            name = "First Snack",
            omNomPos = Vector2(200f, 590f),
            candySpawnPos = Vector2(200f, 230f),
            pegs = listOf(
                PegData("peg_1", Vector2(200f, 90f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 150f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(200f, 320f)),
                StarData("s2", Vector2(200f, 410f)),
                StarData("s3", Vector2(200f, 500f))
            )
        ),

        // Level 2: Three Pegs & A Swing (as in the screenshot)
        LevelData(
            id = "level_2",
            name = "Three Pegs",
            omNomPos = Vector2(255f, 620f),
            candySpawnPos = Vector2(150f, 290f),
            pegs = listOf(
                PegData("peg_1", Vector2(150f, 100f)),
                PegData("peg_2", Vector2(200f, 100f)),
                PegData("peg_3", Vector2(255f, 100f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 190f, segments = 10),
                RopeData("rope_2", "peg_2", length = 205f, segments = 10),
                RopeData("rope_3", "peg_3", length = 265f, segments = 12)
            ),
            stars = listOf(
                StarData("s1", Vector2(150f, 360f)),
                StarData("s2", Vector2(150f, 530f)),
                StarData("s3", Vector2(255f, 360f))
            )
        ),

        // Level 3: Bubble Up
        LevelData(
            id = "level_3",
            name = "Bubble Flight",
            omNomPos = Vector2(200f, 160f),
            candySpawnPos = Vector2(200f, 480f),
            pegs = listOf(
                PegData("peg_1", Vector2(200f, 340f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 140f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(200f, 400f)),
                StarData("s2", Vector2(200f, 290f)),
                StarData("s3", Vector2(200f, 220f))
            ),
            bubbles = listOf(
                BubbleData("b1", Vector2(200f, 480f))
            )
        ),

        // Level 4: Spike Canyon
        LevelData(
            id = "level_4",
            name = "Spike Canyon",
            omNomPos = Vector2(320f, 590f),
            candySpawnPos = Vector2(100f, 310f),
            pegs = listOf(
                PegData("peg_1", Vector2(100f, 120f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 190f, segments = 9)
            ),
            stars = listOf(
                StarData("s1", Vector2(160f, 410f)),
                StarData("s2", Vector2(240f, 470f)),
                StarData("s3", Vector2(310f, 520f))
            ),
            spikes = listOf(
                SpikeData("sp1", 40f, 580f, 150f, 25f)
            )
        ),

        // Level 5: The Blower
        LevelData(
            id = "level_5",
            name = "Windy Day",
            omNomPos = Vector2(330f, 580f),
            candySpawnPos = Vector2(90f, 250f),
            pegs = listOf(
                PegData("peg_1", Vector2(90f, 100f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 160f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(160f, 320f)),
                StarData("s2", Vector2(250f, 420f)),
                StarData("s3", Vector2(320f, 500f))
            ),
            blowers = listOf(
                BlowerData("bl1", Vector2(40f, 340f), directionAngle = 0f, strength = 450f)
            )
        ),

        // Level 6: Triple Web
        LevelData(
            id = "level_6",
            name = "Triple Web",
            omNomPos = Vector2(200f, 610f),
            candySpawnPos = Vector2(200f, 250f),
            pegs = listOf(
                PegData("peg_1", Vector2(70f, 120f)),
                PegData("peg_2", Vector2(200f, 90f)),
                PegData("peg_3", Vector2(330f, 120f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 170f, segments = 8),
                RopeData("rope_2", "peg_2", length = 160f, segments = 8),
                RopeData("rope_3", "peg_3", length = 170f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(120f, 360f)),
                StarData("s2", Vector2(280f, 360f)),
                StarData("s3", Vector2(200f, 490f))
            )
        ),

        // Level 7: Pop & Drop
        LevelData(
            id = "level_7",
            name = "Pop & Drop",
            omNomPos = Vector2(310f, 590f),
            candySpawnPos = Vector2(100f, 430f),
            pegs = listOf(
                PegData("peg_1", Vector2(100f, 290f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 140f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(100f, 210f)),
                StarData("s2", Vector2(200f, 140f)),
                StarData("s3", Vector2(310f, 380f))
            ),
            bubbles = listOf(
                BubbleData("b1", Vector2(100f, 430f))
            ),
            spikes = listOf(
                SpikeData("sp1", 60f, 40f, 180f, 20f)
            )
        ),

        // Level 8: Crossfire
        LevelData(
            id = "level_8",
            name = "Crossfire",
            omNomPos = Vector2(200f, 600f),
            candySpawnPos = Vector2(200f, 220f),
            pegs = listOf(
                PegData("peg_1", Vector2(200f, 80f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 150f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(120f, 340f)),
                StarData("s2", Vector2(280f, 340f)),
                StarData("s3", Vector2(200f, 480f))
            ),
            blowers = listOf(
                BlowerData("bl1", Vector2(40f, 300f), directionAngle = 0f, strength = 380f),
                BlowerData("bl2", Vector2(360f, 380f), directionAngle = 180f, strength = 380f)
            )
        ),

        // Level 9: Bubble Express
        LevelData(
            id = "level_9",
            name = "Bubble Express",
            omNomPos = Vector2(200f, 160f),
            candySpawnPos = Vector2(90f, 520f),
            pegs = listOf(
                PegData("peg_1", Vector2(90f, 380f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 140f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(90f, 300f)),
                StarData("s2", Vector2(200f, 280f)),
                StarData("s3", Vector2(200f, 220f))
            ),
            bubbles = listOf(
                BubbleData("b1", Vector2(90f, 520f))
            ),
            blowers = listOf(
                BlowerData("bl1", Vector2(40f, 260f), directionAngle = 0f, strength = 420f)
            )
        ),

        // Level 10: Spike Trap
        LevelData(
            id = "level_10",
            name = "Spike Trap",
            omNomPos = Vector2(200f, 610f),
            candySpawnPos = Vector2(200f, 230f),
            pegs = listOf(
                PegData("peg_1", Vector2(70f, 140f)),
                PegData("peg_2", Vector2(330f, 140f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 170f, segments = 8),
                RopeData("rope_2", "peg_2", length = 170f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(130f, 380f)),
                StarData("s2", Vector2(270f, 380f)),
                StarData("s3", Vector2(200f, 490f))
            ),
            spikes = listOf(
                SpikeData("sp1", 120f, 410f, 160f, 20f)
            )
        ),

        // Level 11: Sky High
        LevelData(
            id = "level_11",
            name = "Sky High",
            omNomPos = Vector2(320f, 180f),
            candySpawnPos = Vector2(80f, 480f),
            pegs = listOf(
                PegData("peg_1", Vector2(80f, 320f)),
                PegData("peg_2", Vector2(280f, 320f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 160f, segments = 8),
                RopeData("rope_2", "peg_2", length = 220f, segments = 9)
            ),
            stars = listOf(
                StarData("s1", Vector2(140f, 390f)),
                StarData("s2", Vector2(220f, 270f)),
                StarData("s3", Vector2(300f, 230f))
            ),
            bubbles = listOf(
                BubbleData("b1", Vector2(80f, 480f))
            )
        ),

        // Level 12: Master Feeder
        LevelData(
            id = "level_12",
            name = "Master Feeder",
            omNomPos = Vector2(200f, 610f),
            candySpawnPos = Vector2(200f, 240f),
            pegs = listOf(
                PegData("peg_1", Vector2(60f, 120f)),
                PegData("peg_2", Vector2(200f, 80f)),
                PegData("peg_3", Vector2(340f, 120f))
            ),
            ropes = listOf(
                RopeData("rope_1", "peg_1", length = 180f, segments = 8),
                RopeData("rope_2", "peg_2", length = 160f, segments = 8),
                RopeData("rope_3", "peg_3", length = 180f, segments = 8)
            ),
            stars = listOf(
                StarData("s1", Vector2(100f, 330f)),
                StarData("s2", Vector2(300f, 330f)),
                StarData("s3", Vector2(200f, 460f))
            ),
            spikes = listOf(
                SpikeData("sp1", 140f, 510f, 120f, 20f)
            ),
            blowers = listOf(
                BlowerData("bl1", Vector2(40f, 420f), directionAngle = 0f, strength = 450f),
                BlowerData("bl2", Vector2(360f, 420f), directionAngle = 180f, strength = 450f)
            )
        )
    )
}
