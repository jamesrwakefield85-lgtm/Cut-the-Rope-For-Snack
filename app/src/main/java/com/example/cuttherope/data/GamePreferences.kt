package com.example.cuttherope.data

import android.content.Context
import android.content.SharedPreferences
import com.example.cuttherope.model.BlowerData
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.CandyType
import com.example.cuttherope.model.LevelData
import com.example.cuttherope.model.PegData
import com.example.cuttherope.model.RopeData
import com.example.cuttherope.model.SpikeData
import com.example.cuttherope.model.StarData
import com.example.cuttherope.model.Vector2
import org.json.JSONArray
import org.json.JSONObject

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cut_the_rope_snack_prefs", Context.MODE_PRIVATE)

    fun getStarsForLevel(levelId: String): Int {
        return prefs.getInt("stars_$levelId", 0)
    }

    fun setStarsForLevel(levelId: String, stars: Int) {
        val current = getStarsForLevel(levelId)
        if (stars > current) {
            prefs.edit().putInt("stars_$levelId", stars).apply()
        }
    }

    fun getTotalStars(totalCampaignLevels: Int): Int {
        var total = 0
        for (i in 1..totalCampaignLevels) {
            total += getStarsForLevel("level_$i")
        }
        return total
    }

    fun isLevelUnlocked(levelNumber: Int): Boolean {
        if (levelNumber <= 1) return true
        // Level N unlocked if previous level has at least 1 star
        return getStarsForLevel("level_${levelNumber - 1}") > 0
    }

    fun getSelectedCandySkin(): String {
        return prefs.getString("selected_candy_skin", CandyType.CLASSIC.id) ?: CandyType.CLASSIC.id
    }

    fun setSelectedCandySkin(skinId: String) {
        prefs.edit().putString("selected_candy_skin", skinId).apply()
    }

    fun getCustomLevels(): List<LevelData> {
        val jsonStr = prefs.getString("custom_levels_list", "[]") ?: "[]"
        val list = mutableListOf<LevelData>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(deserializeLevel(obj))
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveCustomLevel(level: LevelData) {
        val current = getCustomLevels().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == level.id }
        if (existingIndex >= 0) {
            current[existingIndex] = level
        } else {
            current.add(level)
        }
        val arr = JSONArray()
        for (lvl in current) {
            arr.put(serializeLevel(lvl))
        }
        prefs.edit().putString("custom_levels_list", arr.toString()).apply()
    }

    fun deleteCustomLevel(levelId: String) {
        val current = getCustomLevels().filter { it.id != levelId }
        val arr = JSONArray()
        for (lvl in current) {
            arr.put(serializeLevel(lvl))
        }
        prefs.edit().putString("custom_levels_list", arr.toString()).apply()
    }

    private fun serializeLevel(level: LevelData): JSONObject {
        val obj = JSONObject()
        obj.put("id", level.id)
        obj.put("name", level.name)
        obj.put("omNomX", level.omNomPos.x.toDouble())
        obj.put("omNomY", level.omNomPos.y.toDouble())
        obj.put("candyX", level.candySpawnPos.x.toDouble())
        obj.put("candyY", level.candySpawnPos.y.toDouble())

        val pegsArr = JSONArray()
        for (p in level.pegs) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("x", p.position.x.toDouble())
            pObj.put("y", p.position.y.toDouble())
            pegsArr.put(pObj)
        }
        obj.put("pegs", pegsArr)

        val ropesArr = JSONArray()
        for (r in level.ropes) {
            val rObj = JSONObject()
            rObj.put("id", r.id)
            rObj.put("pegId", r.pegId)
            rObj.put("length", r.length.toDouble())
            rObj.put("segments", r.segments)
            ropesArr.put(rObj)
        }
        obj.put("ropes", ropesArr)

        val starsArr = JSONArray()
        for (s in level.stars) {
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("x", s.position.x.toDouble())
            sObj.put("y", s.position.y.toDouble())
            starsArr.put(sObj)
        }
        obj.put("stars", starsArr)

        val bubblesArr = JSONArray()
        for (b in level.bubbles) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("x", b.position.x.toDouble())
            bObj.put("y", b.position.y.toDouble())
            bubblesArr.put(bObj)
        }
        obj.put("bubbles", bubblesArr)

        val blowersArr = JSONArray()
        for (bl in level.blowers) {
            val blObj = JSONObject()
            blObj.put("id", bl.id)
            blObj.put("x", bl.position.x.toDouble())
            blObj.put("y", bl.position.y.toDouble())
            blObj.put("angle", bl.directionAngle.toDouble())
            blObj.put("strength", bl.strength.toDouble())
            blowersArr.put(blObj)
        }
        obj.put("blowers", blowersArr)

        val spikesArr = JSONArray()
        for (sp in level.spikes) {
            val spObj = JSONObject()
            spObj.put("id", sp.id)
            spObj.put("x", sp.x.toDouble())
            spObj.put("y", sp.y.toDouble())
            spObj.put("w", sp.width.toDouble())
            spObj.put("h", sp.height.toDouble())
            spikesArr.put(spObj)
        }
        obj.put("spikes", spikesArr)
        obj.put("isCustom", true)

        return obj
    }

    private fun deserializeLevel(obj: JSONObject): LevelData {
        val id = obj.optString("id", "custom_${System.currentTimeMillis()}")
        val name = obj.optString("name", "Custom Level")
        val omNomPos = Vector2(obj.optDouble("omNomX", 200.0).toFloat(), obj.optDouble("omNomY", 580.0).toFloat())
        val candySpawnPos = Vector2(obj.optDouble("candyX", 200.0).toFloat(), obj.optDouble("candyY", 240.0).toFloat())

        val pegs = mutableListOf<PegData>()
        val pArr = obj.optJSONArray("pegs")
        if (pArr != null) {
            for (i in 0 until pArr.length()) {
                val pObj = pArr.getJSONObject(i)
                pegs.add(
                    PegData(
                        id = pObj.optString("id", "peg_$i"),
                        position = Vector2(pObj.optDouble("x", 200.0).toFloat(), pObj.optDouble("y", 100.0).toFloat())
                    )
                )
            }
        }

        val ropes = mutableListOf<RopeData>()
        val rArr = obj.optJSONArray("ropes")
        if (rArr != null) {
            for (i in 0 until rArr.length()) {
                val rObj = rArr.getJSONObject(i)
                ropes.add(
                    RopeData(
                        id = rObj.optString("id", "rope_$i"),
                        pegId = rObj.optString("pegId", pegs.firstOrNull()?.id ?: "peg_0"),
                        length = rObj.optDouble("length", 160.0).toFloat(),
                        segments = rObj.optInt("segments", 10)
                    )
                )
            }
        }

        val stars = mutableListOf<StarData>()
        val sArr = obj.optJSONArray("stars")
        if (sArr != null) {
            for (i in 0 until sArr.length()) {
                val sObj = sArr.getJSONObject(i)
                stars.add(
                    StarData(
                        id = sObj.optString("id", "star_$i"),
                        position = Vector2(sObj.optDouble("x", 200.0).toFloat(), sObj.optDouble("y", 350.0).toFloat())
                    )
                )
            }
        }

        val bubbles = mutableListOf<BubbleData>()
        val bArr = obj.optJSONArray("bubbles")
        if (bArr != null) {
            for (i in 0 until bArr.length()) {
                val bObj = bArr.getJSONObject(i)
                bubbles.add(
                    BubbleData(
                        id = bObj.optString("id", "bubble_$i"),
                        position = Vector2(bObj.optDouble("x", 200.0).toFloat(), bObj.optDouble("y", 400.0).toFloat())
                    )
                )
            }
        }

        val blowers = mutableListOf<BlowerData>()
        val blArr = obj.optJSONArray("blowers")
        if (blArr != null) {
            for (i in 0 until blArr.length()) {
                val blObj = blArr.getJSONObject(i)
                blowers.add(
                    BlowerData(
                        id = blObj.optString("id", "blower_$i"),
                        position = Vector2(blObj.optDouble("x", 60.0).toFloat(), blObj.optDouble("y", 400.0).toFloat()),
                        directionAngle = blObj.optDouble("angle", 0.0).toFloat(),
                        strength = blObj.optDouble("strength", 400.0).toFloat()
                    )
                )
            }
        }

        val spikes = mutableListOf<SpikeData>()
        val spArr = obj.optJSONArray("spikes")
        if (spArr != null) {
            for (i in 0 until spArr.length()) {
                val spObj = spArr.getJSONObject(i)
                spikes.add(
                    SpikeData(
                        id = spObj.optString("id", "spike_$i"),
                        x = spObj.optDouble("x", 100.0).toFloat(),
                        y = spObj.optDouble("y", 500.0).toFloat(),
                        width = spObj.optDouble("w", 200.0).toFloat(),
                        height = spObj.optDouble("h", 30.0).toFloat()
                    )
                )
            }
        }

        return LevelData(
            id = id,
            name = name,
            omNomPos = omNomPos,
            candySpawnPos = candySpawnPos,
            pegs = pegs,
            ropes = ropes,
            stars = stars,
            bubbles = bubbles,
            blowers = blowers,
            spikes = spikes,
            isCustom = true
        )
    }
}
