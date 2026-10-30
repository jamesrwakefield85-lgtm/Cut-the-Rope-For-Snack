package com.example.cuttherope.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttherope.data.GamePreferences
import com.example.cuttherope.data.LevelRepository
import com.example.cuttherope.model.AirBlower
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.CandyType
import com.example.cuttherope.model.GameStatus
import com.example.cuttherope.model.LevelData
import com.example.cuttherope.model.OmNomState
import com.example.cuttherope.model.Vector2
import com.example.cuttherope.physics.PhysicsEngine
import com.example.cuttherope.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class AppScreen {
    LEVEL_SELECT,
    GAMEPLAY,
    LEVEL_EDITOR,
    CANDY_SHOP
}

data class SlicePoint(
    val point: Vector2,
    val timeMs: Long
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.LEVEL_SELECT,
    val currentLevel: LevelData = LevelRepository.campaignLevels.first(),
    val currentLevelIndex: Int = 1,
    val isTestingCustom: Boolean = false,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val omNomState: OmNomState = OmNomState.IDLE,
    val omNomAnimationProgress: Float = 0f,
    val collectedStarIds: Set<String> = emptySet(),
    val activeBubbles: List<BubbleData> = emptyList(),
    val blowers: List<AirBlower> = emptyList(),
    val totalStars: Int = 0,
    val selectedSkin: CandyType = CandyType.CLASSIC,
    val unlockedSkins: Set<String> = setOf(CandyType.CLASSIC.id),
    val customLevels: List<LevelData> = emptyList(),
    val isMuted: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = GamePreferences(application)
    val soundManager = SoundManager(application)
    val physics = PhysicsEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val sliceTrail = mutableListOf<SlicePoint>()

    private var gameLoopJob: Job? = null
    private var lastFrameTime = 0L

    init {
        refreshProgression()
    }

    fun refreshProgression() {
        val total = prefs.getTotalStars(LevelRepository.campaignLevels.size)
        val selectedId = prefs.getSelectedCandySkin()
        val skin = CandyType.values().find { it.id == selectedId } ?: CandyType.CLASSIC
        val unlocked = CandyType.values().filter { it.requiredStars <= total }.map { it.id }.toSet()
        val custom = prefs.getCustomLevels()

        _uiState.update {
            it.copy(
                totalStars = total,
                selectedSkin = skin,
                unlockedSkins = unlocked,
                customLevels = custom,
                isMuted = soundManager.isMuted
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.GAMEPLAY) {
            stopGameLoop()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun startCampaignLevel(index: Int) {
        val lvl = LevelRepository.campaignLevels.getOrNull(index - 1) ?: LevelRepository.campaignLevels.first()
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAMEPLAY,
                currentLevel = lvl,
                currentLevelIndex = index,
                isTestingCustom = false
            )
        }
        loadLevel(lvl)
    }

    fun playCustomLevel(level: LevelData) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAMEPLAY,
                currentLevel = level,
                currentLevelIndex = -1,
                isTestingCustom = true
            )
        }
        loadLevel(level)
    }

    fun testFromEditor(level: LevelData) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAMEPLAY,
                currentLevel = level,
                currentLevelIndex = -1,
                isTestingCustom = true
            )
        }
        loadLevel(level)
    }

    fun restartCurrentLevel() {
        loadLevel(_uiState.value.currentLevel)
    }

    fun nextLevel() {
        val nextIdx = _uiState.value.currentLevelIndex + 1
        if (nextIdx <= LevelRepository.campaignLevels.size) {
            startCampaignLevel(nextIdx)
        } else {
            navigateTo(AppScreen.LEVEL_SELECT)
        }
    }

    private fun loadLevel(level: LevelData) {
        stopGameLoop()
        physics.initLevel(level)

        val activeB = level.bubbles.toMutableList()
        val blowersList = level.blowers.map {
            val rad = Math.toRadians(it.directionAngle.toDouble()).toFloat()
            AirBlower(
                id = it.id,
                position = it.position.copy(),
                direction = Vector2(cos(rad), sin(rad)),
                strength = it.strength
            )
        }

        _uiState.update {
            it.copy(
                gameStatus = GameStatus.PLAYING,
                omNomState = OmNomState.IDLE,
                omNomAnimationProgress = 0f,
                collectedStarIds = emptySet(),
                activeBubbles = activeB,
                blowers = blowersList
            )
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        lastFrameTime = System.nanoTime()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastFrameTime = now

                updateGameStep(dt)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun updateGameStep(dt: Float) {
        val state = _uiState.value
        val level = state.currentLevel

        // Update animation progress for Om Nom states
        val newAnimProgress = state.omNomAnimationProgress + dt * 3.5f

        // Physics step (standard virtual resolution 400x700)
        physics.update(
            dt = dt,
            screenWidth = 400f,
            screenHeight = 700f,
            blowers = state.blowers
        )

        // Clean old slice trail points
        val curTime = System.currentTimeMillis()
        val iter = sliceTrail.iterator()
        while (iter.hasNext()) {
            if (curTime - iter.next().timeMs > 220) {
                iter.remove()
            }
        }

        if (state.gameStatus != GameStatus.PLAYING) {
            _uiState.update { it.copy(omNomAnimationProgress = newAnimProgress) }
            return
        }

        // Check bubble capture: if candy is inside an active bubble, capture it!
        val activeBubbles = state.activeBubbles.toMutableList()
        val bIter = activeBubbles.iterator()
        while (bIter.hasNext()) {
            val b = bIter.next()
            if (physics.candyPos.distanceTo(b.position) < 36f && !physics.candyInBubble) {
                physics.candyInBubble = true
                bIter.remove()
                soundManager.playBubblePop()
                physics.spawnBubblePopParticles(b.position)
                break
            }
        }

        // Check star collection
        val collected = state.collectedStarIds.toMutableSet()
        for (star in level.stars) {
            if (!collected.contains(star.id)) {
                if (physics.candyPos.distanceTo(star.position) < 32f) {
                    collected.add(star.id)
                    soundManager.playStarCollect(collected.size - 1)
                    physics.spawnStarSparkles(star.position)
                }
            }
        }

        // Check spike collision
        for (spike in level.spikes) {
            val cx = physics.candyPos.x
            val cy = physics.candyPos.y
            val r = physics.candyRadius
            if (cx + r >= spike.x && cx - r <= spike.x + spike.width &&
                cy + r >= spike.y && cy - r <= spike.y + spike.height
            ) {
                // Hit spike!
                physics.candyIsDestroyed = true
                physics.spawnCutSparks(physics.candyPos)
                soundManager.playOmNomSad()
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.FAILED,
                        omNomState = OmNomState.SAD,
                        omNomAnimationProgress = 0f,
                        collectedStarIds = collected,
                        activeBubbles = activeBubbles
                    )
                }
                return
            }
        }

        // Check Om Nom mouth catch (mouth is near top-center of Om Nom body)
        val mouthPos = level.omNomPos + Vector2(0f, -12f)
        val distToMouth = physics.candyPos.distanceTo(mouthPos)
        if (distToMouth < 42f) {
            // Eaten!
            physics.candyIsEaten = true
            physics.spawnChompHearts(mouthPos)
            soundManager.playOmNomChomp()
            soundManager.playWinFanfare()

            val starCount = collected.size
            if (!state.isTestingCustom) {
                prefs.setStarsForLevel(level.id, starCount)
            }
            refreshProgression()

            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.WON,
                    omNomState = OmNomState.EATING,
                    omNomAnimationProgress = 0f,
                    collectedStarIds = collected,
                    activeBubbles = activeBubbles
                )
            }
            return
        }

        // Check fall off bottom of screen
        if (physics.candyPos.y > 750f) {
            physics.candyIsDestroyed = true
            soundManager.playOmNomSad()
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.FAILED,
                    omNomState = OmNomState.SAD,
                    omNomAnimationProgress = 0f,
                    collectedStarIds = collected,
                    activeBubbles = activeBubbles
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                omNomAnimationProgress = newAnimProgress,
                collectedStarIds = collected,
                activeBubbles = activeBubbles
            )
        }
    }

    fun onSlice(p1: Vector2, p2: Vector2) {
        if (_uiState.value.gameStatus != GameStatus.PLAYING) return
        sliceTrail.add(SlicePoint(p2.copy(), System.currentTimeMillis()))
        if (physics.checkSlice(p1, p2)) {
            soundManager.playSlice()
        }
    }

    fun popBubbleUnderFinger(tapPos: Vector2) {
        if (_uiState.value.gameStatus != GameStatus.PLAYING) return

        // If candy is in bubble and user taps near candy, pop it!
        if (physics.candyInBubble && physics.candyPos.distanceTo(tapPos) < 48f) {
            physics.candyInBubble = false
            physics.spawnBubblePopParticles(physics.candyPos)
            soundManager.playBubblePop()
            return
        }

        // Or if user taps an uncollected bubble
        val active = _uiState.value.activeBubbles.toMutableList()
        val iter = active.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            if (b.position.distanceTo(tapPos) < 40f) {
                iter.remove()
                physics.spawnBubblePopParticles(b.position)
                soundManager.playBubblePop()
                _uiState.update { it.copy(activeBubbles = active) }
                break
            }
        }
    }

    fun tapBlower(blowerId: String) {
        if (_uiState.value.gameStatus != GameStatus.PLAYING) return
        val blowers = _uiState.value.blowers.map {
            if (it.id == blowerId) {
                // Apply instant strong wind push to candy
                val toCandy = physics.candyPos - it.position
                val dist = toCandy.length()
                if (dist < 340f) {
                    val dot = (toCandy.x * it.direction.x + toCandy.y * it.direction.y) / (dist.coerceAtLeast(1f))
                    if (dot > 0.4f) {
                        physics.candyPos += it.direction * 18f
                        physics.candyOldPos -= it.direction * 18f
                    }
                }
                soundManager.playBlowerPuff()
                it.copy(puffAnim = 1f)
            } else it
        }
        _uiState.update { it.copy(blowers = blowers) }
    }

    fun selectSkin(skin: CandyType) {
        if (_uiState.value.unlockedSkins.contains(skin.id)) {
            prefs.setSelectedCandySkin(skin.id)
            _uiState.update { it.copy(selectedSkin = skin) }
        }
    }

    fun saveCustomLevel(level: LevelData) {
        prefs.saveCustomLevel(level)
        refreshProgression()
    }

    fun deleteCustomLevel(levelId: String) {
        prefs.deleteCustomLevel(levelId)
        refreshProgression()
    }

    fun toggleMute() {
        soundManager.isMuted = !soundManager.isMuted
        _uiState.update { it.copy(isMuted = soundManager.isMuted) }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
