package com.example.cuttherope.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.cuttherope.model.AirBlower
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.CandyType
import com.example.cuttherope.model.GameStatus
import com.example.cuttherope.model.OmNomState
import com.example.cuttherope.model.ParticleShape
import com.example.cuttherope.model.SpikeData
import com.example.cuttherope.model.StarData
import com.example.cuttherope.model.Vector2
import com.example.cuttherope.physics.ActiveRope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvasScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val assets = rememberGameAssets()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF261C14))
    ) {
        val virtualWidth = 400f
        val virtualHeight = 700f

        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        val scale = minOf(screenWidthPx / virtualWidth, screenHeightPx / virtualHeight)
        val offsetX = (screenWidthPx - virtualWidth * scale) / 2f
        val offsetY = (screenHeightPx - virtualHeight * scale) / 2f

        val toScreen = { v: Vector2 ->
            Offset(offsetX + v.x * scale, offsetY + v.y * scale)
        }
        val toVirtual = { px: Float, py: Float ->
            Vector2((px - offsetX) / scale, (py - offsetY) / scale)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startV = toVirtual(down.position.x, down.position.y)
                        var lastV = startV
                        var totalMovement = 0f

                        // Immediate slice test at touch-down
                        viewModel.onSlice(startV, startV)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                // Finger released
                                if (totalMovement < 16f) {
                                    // Tap interaction (pop bubble or hit air blower)
                                    viewModel.popBubbleUnderFinger(startV)
                                    for (bl in uiState.blowers) {
                                        if (bl.position.distanceTo(startV) < 45f) {
                                            viewModel.tapBlower(bl.id)
                                            break
                                        }
                                    }
                                }
                                break
                            }

                            val curV = toVirtual(change.position.x, change.position.y)
                            val moveDist = lastV.distanceTo(curV)
                            totalMovement += moveDist
                            if (moveDist > 2f) {
                                viewModel.onSlice(lastV, curV)
                                lastV = curV
                            }
                            change.consume()
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Cardboard Pattern
                drawCardboardBackground(size.width, size.height)

                // 1. Spikes
                for (spike in uiState.currentLevel.spikes) {
                    drawSpikes(spike, scale, Offset(offsetX, offsetY))
                }

                // 2. Air Blowers
                for (blower in uiState.blowers) {
                    drawAirBlower(blower, toScreen)
                }

                // 3. Stars (3 stars per level)
                for (star in uiState.currentLevel.stars) {
                    val isCollected = uiState.collectedStarIds.contains(star.id)
                    drawStar(star, isCollected, toScreen)
                }

                // 4. Bubbles (floating or placed)
                for (bubble in uiState.activeBubbles) {
                    drawBubble(toScreen(bubble.position), radius = 34f * scale)
                }

                // 5. Pegs & Ropes
                for (peg in uiState.currentLevel.pegs) {
                    drawPeg(toScreen(peg.position), radius = 10f * scale)
                }

                for (rope in viewModel.physics.ropes) {
                    drawVerletRope(rope, toScreen, scale)
                }

                // 6. Candy
                if (!viewModel.physics.candyIsEaten && !viewModel.physics.candyIsDestroyed) {
                    drawCandy(
                        center = toScreen(viewModel.physics.candyPos),
                        radius = viewModel.physics.candyRadius * scale,
                        rotation = viewModel.physics.candyRotation,
                        skin = uiState.selectedSkin,
                        assets = assets,
                        inBubble = viewModel.physics.candyInBubble
                    )
                }

                // 7. Om Nom
                drawOmNom(
                    omNomPos = toScreen(uiState.currentLevel.omNomPos),
                    state = uiState.omNomState,
                    animProgress = uiState.omNomAnimationProgress,
                    assets = assets,
                    sizePx = 96f * scale
                )

                // 8. Sparks and particles
                for (p in viewModel.physics.particles) {
                    drawParticle(p, toScreen, scale)
                }

                // 9. Slicing blade trail
                drawSliceTrail(viewModel.sliceTrail, toScreen, scale)
            }

            // Top HUD Overlay
            GameTopHud(
                levelName = if (uiState.isTestingCustom) uiState.currentLevel.name else "Level ${uiState.currentLevelIndex}",
                totalStars = uiState.currentLevel.stars.size,
                collectedStars = uiState.collectedStarIds.size,
                isMuted = uiState.isMuted,
                onRestart = { viewModel.restartCurrentLevel() },
                onMenu = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) },
                onToggleMute = { viewModel.toggleMute() }
            )

            // Victory Dialog Overlay
            AnimatedVisibility(
                visible = uiState.gameStatus == GameStatus.WON,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameResultCard(
                    isVictory = true,
                    starsEarned = uiState.collectedStarIds.size,
                    onNext = { viewModel.nextLevel() },
                    onRetry = { viewModel.restartCurrentLevel() },
                    onMenu = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) },
                    isCustom = uiState.isTestingCustom
                )
            }

            // Failure Dialog Overlay
            AnimatedVisibility(
                visible = uiState.gameStatus == GameStatus.FAILED,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameResultCard(
                    isVictory = false,
                    starsEarned = 0,
                    onNext = { /* not used */ },
                    onRetry = { viewModel.restartCurrentLevel() },
                    onMenu = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) },
                    isCustom = uiState.isTestingCustom
                )
            }
        }
    }
}

@Composable
fun GameTopHud(
    levelName: String,
    totalStars: Int,
    collectedStars: Int,
    isMuted: Boolean,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onToggleMute: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 3 Stars Counter (like top-left in the screenshot)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0x55000000), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            for (i in 0 until totalStars) {
                val isEarned = i < collectedStars
                Icon(
                    imageVector = if (isEarned) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Star",
                    tint = if (isEarned) Color(0xFFFFEB3B) else Color(0x99FFFFFF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Right Actions (Music, Restart, MENU button - like top-right in screenshot)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound button (musical note)
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x88000000), CircleShape)
                    .testTag("sound_toggle")
            ) {
                Icon(
                    if (isMuted) Icons.Default.VolumeMute else Icons.Default.MusicNote,
                    contentDescription = "Toggle Audio",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Restart button (circular arrow)
            IconButton(
                onClick = onRestart,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x88000000), CircleShape)
                    .testTag("restart_button")
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // MENU Pill Button (bold white text)
            Surface(
                onClick = onMenu,
                shape = RoundedCornerShape(20.dp),
                color = Color(0x88000000),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("menu_button")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MENU",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameResultCard(
    isVictory: Boolean,
    starsEarned: Int,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onMenu: () -> Unit,
    isCustom: Boolean
) {
    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(0.85f)
            .shadow(16.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVictory) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        if (isVictory) listOf(Color(0xFF388E3C), Color(0xFF1B5E20))
                        else listOf(Color(0xFFD32F2F), Color(0xFF8E0000))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isVictory) "DELICIOUS!" else "OOPS!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isVictory) "Om Nom enjoyed the sweet snack!" else "Om Nom is still hungry! Try again!",
                color = Color(0xFFFFF9C4),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isVictory) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val earned = i <= starsEarned
                        val animScale by animateFloatAsState(
                            targetValue = if (earned) 1.2f else 0.9f,
                            animationSpec = tween(durationMillis = 400, delayMillis = i * 150, easing = FastOutSlowInEasing)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (earned) Color(0xFFFFEB3B) else Color(0x66FFFFFF),
                            modifier = Modifier.size((28 * animScale).dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$starsEarned / 3 Stars",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).testTag("result_retry_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }

                if (isVictory && !isCustom) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1.2f).testTag("result_next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color(0xFF3E2723))
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Button(
                        onClick = onMenu,
                        modifier = Modifier.weight(1.2f).testTag("result_menu_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color(0xFF3E2723))
                    ) {
                        Text("Levels", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Canvas Drawing functions
private fun DrawScope.drawCardboardBackground(width: Float, height: Float) {
    // 1. Warm cardboard box gradient base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFC99458), Color(0xFFAA733C)),
            startY = 0f,
            endY = height
        ),
        size = Size(width, height)
    )

    // 2. Layered Taped Cardboard Patches (as seen in the screenshot!)
    // Patch A: Top-center corrugated patch behind top pegs
    drawCardboardPatch(
        topLeft = Offset(width * 0.32f, height * 0.10f),
        size = Size(width * 0.28f, height * 0.18f),
        color = Color(0xFFBD864B),
        hasVerticalRidges = true
    )

    // Patch B: Left paper sheet patch
    drawCardboardPatch(
        topLeft = Offset(width * 0.08f, height * 0.24f),
        size = Size(width * 0.28f, height * 0.22f),
        color = Color(0xFFD4A772),
        hasVerticalRidges = false
    )

    // Patch C: Center-middle wide cardboard sheet
    drawCardboardPatch(
        topLeft = Offset(width * 0.35f, height * 0.36f),
        size = Size(width * 0.32f, height * 0.24f),
        color = Color(0xFFD3A46D),
        hasVerticalRidges = false
    )

    // Patch D: Right corrugated panel
    drawCardboardPatch(
        topLeft = Offset(width * 0.62f, height * 0.40f),
        size = Size(width * 0.32f, height * 0.38f),
        color = Color(0xFFBF884E),
        hasVerticalRidges = true
    )

    // 3. Om Nom's cardboard platform mat in bottom right
    val matX = width * 0.52f
    val matY = height * 0.88f
    val matW = width * 0.26f
    val matH = 14f
    drawRect(
        color = Color(0x33000000),
        topLeft = Offset(matX + 2f, matY + 4f),
        size = Size(matW, matH)
    )
    drawRect(
        color = Color(0xFFB07A43),
        topLeft = Offset(matX, matY),
        size = Size(matW, matH)
    )
    drawRect(
        color = Color(0xFF795548),
        topLeft = Offset(matX, matY),
        size = Size(matW, 2.5f)
    )

    // 4. Subtle ambient vignette around box edges
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x38000000)),
            center = Offset(width / 2f, height / 2f),
            radius = width.coerceAtLeast(height) * 0.72f
        ),
        size = Size(width, height)
    )

    // 5. Box corner creases & frame borders
    drawRect(
        color = Color(0x22000000),
        topLeft = Offset.Zero,
        size = Size(width, 10f)
    )
    drawRect(
        color = Color(0x22000000),
        topLeft = Offset(0f, height - 10f),
        size = Size(width, 10f)
    )
    drawRect(
        color = Color(0x22000000),
        topLeft = Offset.Zero,
        size = Size(10f, height)
    )
    drawRect(
        color = Color(0x22000000),
        topLeft = Offset(width - 10f, 0f),
        size = Size(10f, height)
    )
}

private fun DrawScope.drawCardboardPatch(
    topLeft: Offset,
    size: Size,
    color: Color,
    hasVerticalRidges: Boolean
) {
    // Drop shadow
    drawRect(
        color = Color(0x25000000),
        topLeft = topLeft + Offset(3f, 4f),
        size = size
    )
    // Patch body
    drawRect(
        color = color,
        topLeft = topLeft,
        size = size
    )
    // Paper edge border
    drawRect(
        color = Color(0x30FFFFFF),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = 1.5f)
    )

    // Corrugated vertical ridges if true
    if (hasVerticalRidges) {
        val ridgeSpacing = 16f
        var rx = topLeft.x + 8f
        while (rx < topLeft.x + size.width - 4f) {
            drawLine(
                color = Color(0x20000000),
                start = Offset(rx, topLeft.y),
                end = Offset(rx, topLeft.y + size.height),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0x20FFFFFF),
                start = Offset(rx + 2f, topLeft.y),
                end = Offset(rx + 2f, topLeft.y + size.height),
                strokeWidth = 1.5f
            )
            rx += ridgeSpacing
        }
    }
}

private fun DrawScope.drawPeg(pos: Offset, radius: Float) {
    // Cut the Rope Bright Blue Peg (as in the screenshot!)
    // Outer drop shadow
    drawCircle(Color(0x55000000), radius = radius * 1.35f, center = pos + Offset(2f, 3.5f))
    // Outer dark blue rim
    drawCircle(Color(0xFF01579B), radius = radius, center = pos)
    // Bright cyan/blue circular ring
    drawCircle(Color(0xFF0288D1), radius = radius * 0.88f, center = pos)
    drawCircle(Color(0xFF29B6F6), radius = radius * 0.72f, center = pos)
    // Inner metallic silver socket
    drawCircle(Color(0xFFCFD8DC), radius = radius * 0.48f, center = pos)
    // Center dark metal rivet pin
    drawCircle(Color(0xFF263238), radius = radius * 0.28f, center = pos)
    // Top-left specular glint
    drawCircle(Color.White.copy(alpha = 0.85f), radius = radius * 0.16f, center = pos - Offset(radius * 0.35f, radius * 0.35f))
}

private fun DrawScope.drawVerletRope(
    rope: ActiveRope,
    toScreen: (Vector2) -> Offset,
    densityScale: Float
) {
    val particles = rope.particles
    if (particles.size < 2) return

    val maxIdx = if (rope.isCut) rope.cutIndex else particles.size - 1

    // Draw shadow first
    val shadowOffset = Offset(2f, 4f)
    for (i in 0 until maxIdx) {
        val p1 = toScreen(particles[i].pos) + shadowOffset
        val p2 = toScreen(particles[i + 1].pos) + shadowOffset
        drawLine(
            color = Color(0x44000000),
            start = p1,
            end = p2,
            strokeWidth = 5f * densityScale,
            cap = StrokeCap.Round
        )
    }

    // Braided rope strands
    for (i in 0 until maxIdx) {
        val p1 = toScreen(particles[i].pos)
        val p2 = toScreen(particles[i + 1].pos)

        // Main rope cord
        drawLine(
            color = Color(0xFFD7CCC8),
            start = p1,
            end = p2,
            strokeWidth = 4.5f * densityScale,
            cap = StrokeCap.Round
        )

        // Rope twisted weave pattern
        if (i % 2 == 0) {
            drawLine(
                color = Color(0xFF8D6E63),
                start = p1,
                end = p2,
                strokeWidth = 2.5f * densityScale,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawCandy(
    center: Offset,
    radius: Float,
    rotation: Float,
    skin: CandyType,
    assets: GameAssets,
    inBubble: Boolean
) {
    // Shadow
    drawCircle(
        color = Color(0x40000000),
        radius = radius * 1.05f,
        center = center + Offset(3f, 5f)
    )

    // Candy Rotation
    rotate(degrees = rotation, pivot = center) {
        if (skin == CandyType.CLASSIC) {
            // Draw image 6 (candy_classic.png)!
            val size = (radius * 2.2f).toInt()
            drawImage(
                image = assets.candyClassic,
                dstOffset = IntOffset((center.x - size / 2).toInt(), (center.y - size / 2).toInt()),
                dstSize = IntSize(size, size)
            )
        } else {
            // Procedural unlocked custom candy skins!
            drawCircle(skin.primaryColor, radius = radius, center = center)

            // Pinwheel / spiral stripes
            val path = Path()
            val blades = 6
            for (i in 0 until blades) {
                val angleStart = (i * 2f * PI.toFloat() / blades)
                val angleEnd = angleStart + (PI.toFloat() / blades)
                path.reset()
                path.moveTo(center.x, center.y)
                path.lineTo(
                    center.x + cos(angleStart) * radius,
                    center.y + sin(angleStart) * radius
                )
                path.arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        center.x - radius, center.y - radius,
                        center.x + radius, center.y + radius
                    ),
                    startAngleDegrees = Math.toDegrees(angleStart.toDouble()).toFloat(),
                    sweepAngleDegrees = 180f / blades,
                    forceMoveTo = false
                )
                path.close()
                drawPath(path, color = skin.secondaryColor)
            }

            // Outer glossy sweet ring
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = radius * 0.88f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Glossy highlight shine
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.85f), Color.Transparent),
                    center = center - Offset(radius * 0.35f, radius * 0.35f),
                    radius = radius * 0.6f
                ),
                radius = radius * 0.55f,
                center = center - Offset(radius * 0.25f, radius * 0.25f)
            )
        }
    }

    // If candy is currently in a bubble, draw enclosing bubble
    if (inBubble) {
        drawBubble(center, radius * 1.55f)
    }
}

private fun DrawScope.drawBubble(center: Offset, radius: Float) {
    // Outer glow
    drawCircle(
        color = Color(0x334FC3F7),
        radius = radius * 1.15f,
        center = center
    )
    // Iridescent bubble fill
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33B3E5FC), Color(0x660288D1)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    // Rim stroke
    drawCircle(
        color = Color(0xCCB2EBF2),
        radius = radius,
        center = center,
        style = Stroke(width = 2.5f)
    )
    // Specular highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = radius * 0.28f,
        center = center - Offset(radius * 0.38f, radius * 0.38f)
    )
}

private fun DrawScope.drawStar(
    star: StarData,
    isCollected: Boolean,
    toScreen: (Vector2) -> Offset
) {
    if (isCollected) return
    val center = toScreen(star.position)
    val outerR = 18f
    val innerR = 8f

    // Star shadow
    drawStarShape(center + Offset(2f, 3f), outerR, innerR, Color(0x44000000))
    // Outer golden glow
    drawCircle(Color(0x33FFEB3B), radius = outerR * 1.6f, center = center)
    // Golden fill
    drawStarShape(center, outerR, innerR, Color(0xFFFFD700))
    // Inner bright center
    drawStarShape(center, outerR * 0.6f, innerR * 0.6f, Color(0xFFFFF9C4))
}

private fun DrawScope.drawStarShape(center: Offset, outerR: Float, innerR: Float, color: Color) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = (i * PI.toFloat() / points) - (PI.toFloat() / 2f)
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

private fun DrawScope.drawAirBlower(
    blower: AirBlower,
    toScreen: (Vector2) -> Offset
) {
    val center = toScreen(blower.position)
    val r = 26f

    // Shadow
    drawCircle(Color(0x44000000), radius = r * 1.1f, center = center + Offset(2f, 3f))

    // Blower cushion body
    drawCircle(Color(0xFF8D6E63), radius = r, center = center)
    drawCircle(Color(0xFFBCAAA4), radius = r * 0.7f, center = center)

    // Nozzle / Direction arrow
    val tip = center + Offset(blower.direction.x * (r + 10f), blower.direction.y * (r + 10f))
    drawLine(
        color = Color(0xFFFFD54F),
        start = center,
        end = tip,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Wind gust lines in direction of blower
    val angle = Math.atan2(blower.direction.y.toDouble(), blower.direction.x.toDouble()).toFloat()
    for (i in -1..1) {
        val lateral = Offset(-sin(angle) * i * 14f, cos(angle) * i * 14f)
        val startGust = center + Offset(blower.direction.x * 32f, blower.direction.y * 32f) + lateral
        val endGust = startGust + Offset(blower.direction.x * 65f, blower.direction.y * 65f)
        drawLine(
            color = Color(0x66FFFFFF),
            start = startGust,
            end = endGust,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSpikes(
    spike: SpikeData,
    scale: Float,
    origin: Offset
) {
    val startX = origin.x + spike.x * scale
    val startY = origin.y + spike.y * scale
    val w = spike.width * scale
    val h = spike.height * scale

    // Hazard base
    drawRect(
        color = Color(0xFF37474F),
        topLeft = Offset(startX, startY),
        size = Size(w, h)
    )

    // Triangle teeth
    val toothWidth = 16f * scale
    var tx = startX
    val path = Path()
    while (tx < startX + w) {
        path.reset()
        path.moveTo(tx, startY)
        path.lineTo(tx + toothWidth / 2f, startY - h * 0.9f)
        path.lineTo(tx + toothWidth, startY)
        path.close()
        drawPath(path, color = Color(0xFFCFD8DC))
        drawPath(path, color = Color(0xFF90A4AE), style = Stroke(width = 1.5f))
        tx += toothWidth
    }
}

private fun DrawScope.drawOmNom(
    omNomPos: Offset,
    state: OmNomState,
    animProgress: Float,
    assets: GameAssets,
    sizePx: Float
) {
    // Determine which image to render based on user instructions:
    // 1st image: omnom_idle.png (default look)
    // 2nd & 3rd images: omnom_sad_1.png and omnom_sad_2.png (candy fails to get to mouth)
    // 4th & 5th images: omnom_eat_1.png and omnom_eat_2.png (candy in mouth)

    val imageToDraw = when (state) {
        OmNomState.IDLE -> {
            assets.omnomIdle
        }
        OmNomState.SAD -> {
            // Animate between 2nd (omnomSad1) and 3rd (omnomSad2)
            val frame = (animProgress.toInt()) % 2
            if (frame == 0) assets.omnomSad1 else assets.omnomSad2
        }
        OmNomState.EATING -> {
            // Animate between 4th (omnomEat1) and 5th (omnomEat2)
            val frame = (animProgress.toInt()) % 2
            if (frame == 0) assets.omnomEat1 else assets.omnomEat2
        }
    }

    val size = sizePx.toInt()
    val dstOffset = IntOffset(
        (omNomPos.x - size / 2).toInt(),
        (omNomPos.y - size / 2).toInt()
    )

    // Ground shadow beneath Om Nom
    drawOval(
        color = Color(0x44000000),
        topLeft = Offset(omNomPos.x - size * 0.42f, omNomPos.y + size * 0.32f),
        size = Size(size * 0.84f, size * 0.22f)
    )

    drawImage(
        image = imageToDraw,
        dstOffset = dstOffset,
        dstSize = IntSize(size, size)
    )

    // Extra crying tear animation if SAD
    if (state == OmNomState.SAD) {
        val tearDropOffset = (animProgress * 15f) % 25f
        drawCircle(
            color = Color(0xCC00B0FF),
            radius = 3.5f,
            center = Offset(omNomPos.x - size * 0.16f, omNomPos.y + size * 0.05f + tearDropOffset)
        )
        drawCircle(
            color = Color(0xCC00B0FF),
            radius = 3.5f,
            center = Offset(omNomPos.x + size * 0.16f, omNomPos.y + size * 0.05f + tearDropOffset)
        )
    }
}

private fun DrawScope.drawParticle(
    p: com.example.cuttherope.model.Particle,
    toScreen: (Vector2) -> Offset,
    densityScale: Float
) {
    val pos = toScreen(p.position)
    val color = Color(p.color).copy(alpha = p.life.coerceIn(0f, 1f))
    val size = p.size * densityScale * p.life

    when (p.shape) {
        ParticleShape.CIRCLE -> {
            drawCircle(color, radius = size, center = pos)
        }
        ParticleShape.STAR -> {
            drawStarShape(pos, size * 1.5f, size * 0.6f, color)
        }
        ParticleShape.SPARK -> {
            drawLine(
                color = color,
                start = pos,
                end = pos + Offset(p.velocity.x * 0.04f, p.velocity.y * 0.04f),
                strokeWidth = size
            )
        }
    }
}

private fun DrawScope.drawSliceTrail(
    trail: List<SlicePoint>,
    toScreen: (Vector2) -> Offset,
    densityScale: Float
) {
    if (trail.size < 2) return
    val curTime = System.currentTimeMillis()

    for (i in 0 until trail.size - 1) {
        val p1 = trail[i]
        val p2 = trail[i + 1]
        val age = (curTime - p2.timeMs).toFloat()
        val alpha = (1f - (age / 220f)).coerceIn(0f, 1f)
        if (alpha <= 0f) continue

        val s1 = toScreen(p1.point)
        val s2 = toScreen(p2.point)

        // Outer glow
        drawLine(
            color = Color(0xFFFFEB3B).copy(alpha = alpha * 0.5f),
            start = s1,
            end = s2,
            strokeWidth = 10f * densityScale * alpha,
            cap = StrokeCap.Round
        )

        // Inner sharp laser blade
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = s1,
            end = s2,
            strokeWidth = 4f * densityScale * alpha,
            cap = StrokeCap.Round
        )
    }
}
