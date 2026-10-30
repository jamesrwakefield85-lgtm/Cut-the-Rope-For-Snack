package com.example.cuttherope.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    var isMuted: Boolean = false

    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<String, Int>()

    init {
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttrs)
            .build()

        // Generate audio files in background and load into SoundPool
        scope.launch {
            try {
                val soundDir = File(context.cacheDir, "sounds")
                if (!soundDir.exists()) soundDir.mkdirs()

                loadWav(soundDir, "slice", generateSlice())
                loadWav(soundDir, "star_0", generateStar(659.25f)) // E5
                loadWav(soundDir, "star_1", generateStar(830.61f)) // G#5
                loadWav(soundDir, "star_2", generateStar(1046.50f)) // C6
                loadWav(soundDir, "bubble", generateBubblePop())
                loadWav(soundDir, "blower", generateBlowerPuff())
                loadWav(soundDir, "chomp", generateChomp())
                loadWav(soundDir, "sad", generateSad())
                loadWav(soundDir, "win", generateWin())
            } catch (_: Exception) {
            }
        }
    }

    private fun loadWav(dir: File, name: String, pcmData: ShortArray) {
        val file = File(dir, "$name.wav")
        FileOutputStream(file).use { out ->
            writeWavHeader(out, pcmData.size * 2)
            val byteBuffer = ByteArray(pcmData.size * 2)
            for (i in pcmData.indices) {
                val s = pcmData[i].toInt()
                byteBuffer[i * 2] = (s and 0xFF).toByte()
                byteBuffer[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
            }
            out.write(byteBuffer)
        }
        val id = soundPool.load(file.absolutePath, 1)
        soundIds[name] = id
    }

    private fun writeWavHeader(out: FileOutputStream, pcmByteSize: Int) {
        val totalSize = pcmByteSize + 36
        val byteRate = sampleRate * 2
        val header = ByteArray(44)

        // "RIFF"
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        // ChunkSize
        header[4] = (totalSize and 0xff).toByte()
        header[5] = ((totalSize shr 8) and 0xff).toByte()
        header[6] = ((totalSize shr 16) and 0xff).toByte()
        header[7] = ((totalSize shr 24) and 0xff).toByte()
        // "WAVE"
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        // "fmt "
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        // Subchunk1Size (16 for PCM)
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        // AudioFormat (1 for PCM)
        header[20] = 1; header[21] = 0
        // NumChannels (1 mono)
        header[22] = 1; header[23] = 0
        // SampleRate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        // ByteRate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        // BlockAlign (2 bytes)
        header[32] = 2; header[33] = 0
        // BitsPerSample (16 bits)
        header[34] = 16; header[35] = 0
        // "data"
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        // Subchunk2Size
        header[40] = (pcmByteSize and 0xff).toByte()
        header[41] = ((pcmByteSize shr 8) and 0xff).toByte()
        header[42] = ((pcmByteSize shr 16) and 0xff).toByte()
        header[43] = ((pcmByteSize shr 24) and 0xff).toByte()

        out.write(header)
    }

    private fun play(name: String, volume: Float = 1.0f) {
        if (isMuted) return
        val id = soundIds[name] ?: return
        soundPool.play(id, volume, volume, 1, 0, 1.0f)
    }

    fun playSlice() {
        play("slice", 0.9f)
    }

    fun playStarCollect(starIndex: Int) {
        val idx = starIndex.coerceIn(0, 2)
        play("star_$idx", 1.0f)
    }

    fun playBubblePop() {
        play("bubble", 0.95f)
    }

    fun playBlowerPuff() {
        play("blower", 0.8f)
    }

    fun playOmNomChomp() {
        play("chomp", 1.0f)
    }

    fun playOmNomSad() {
        play("sad", 0.9f)
    }

    fun playWinFanfare() {
        play("win", 1.0f)
    }

    // Audio generation algorithms
    private fun generateSlice(): ShortArray {
        val numSamples = (sampleRate * 0.09f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 1600f - progress * 1100f
            val env = (1f - progress) * (1f - progress)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.45f
            val sine = sin(2.0 * PI * freq * i / sampleRate).toFloat() * 0.55f
            buffer[i] = ((sine + noise) * env * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateStar(baseFreq: Float): ShortArray {
        val numSamples = (sampleRate * 0.28f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = Math.exp(-progress * 6.5).toFloat()
            val sine1 = sin(2.0 * PI * baseFreq * i / sampleRate).toFloat()
            val sine2 = sin(2.0 * PI * (baseFreq * 2f) * i / sampleRate).toFloat() * 0.45f
            val sine3 = sin(2.0 * PI * (baseFreq * 3f) * i / sampleRate).toFloat() * 0.2f
            buffer[i] = ((sine1 + sine2 + sine3) * env * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBubblePop(): ShortArray {
        val numSamples = (sampleRate * 0.08f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 280f + progress * 800f
            val env = (1f - progress) * (1f - progress)
            val sine = sin(2.0 * PI * freq * i / sampleRate).toFloat()
            buffer[i] = (sine * env * 27000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBlowerPuff(): ShortArray {
        val numSamples = (sampleRate * 0.14f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = sin(progress * PI.toFloat())
            val noise = (Random.nextFloat() * 2f - 1f)
            buffer[i] = (noise * env * 17000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateChomp(): ShortArray {
        val numSamples = (sampleRate * 0.32f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val cycle = (progress * 2.8f) % 1.0f
            val env = (1f - cycle) * (1f - cycle)
            val freq = 480f - cycle * 160f
            val noise = (Random.nextFloat() * 2f - 1f) * 0.35f
            val sine = sin(2.0 * PI * freq * i / sampleRate).toFloat() * 0.65f
            buffer[i] = ((sine + noise) * env * 29000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSad(): ShortArray {
        val numSamples = (sampleRate * 0.45f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 440f - progress * 200f
            val env = sin(progress * PI.toFloat()) * (1f - progress * 0.4f)
            val sine = sin(2.0 * PI * freq * i / sampleRate).toFloat()
            buffer[i] = (sine * env * 21000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWin(): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val noteDur = 0.11f
        val totalMs = noteDur * notes.size + 0.15f
        val numSamples = (sampleRate * totalMs).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceAtMost(notes.lastIndex)
            val freq = notes[noteIdx]
            val noteProgress = (t % noteDur) / noteDur
            val env = if (noteIdx == notes.lastIndex) {
                val endProgress = (t - noteIdx * noteDur) / 0.25f
                Math.exp(-endProgress * 3.5).toFloat().coerceIn(0f, 1f)
            } else {
                (1f - noteProgress * 0.4f)
            }
            val sine = sin(2.0 * PI * freq * i / sampleRate).toFloat()
            val sine2 = sin(2.0 * PI * (freq * 2) * i / sampleRate).toFloat() * 0.35f
            buffer[i] = ((sine + sine2) * env * 25000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }
}
