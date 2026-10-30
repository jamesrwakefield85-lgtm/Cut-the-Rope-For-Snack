package com.example.cuttherope.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuttherope.model.BlowerData
import com.example.cuttherope.model.BubbleData
import com.example.cuttherope.model.LevelData
import com.example.cuttherope.model.PegData
import com.example.cuttherope.model.RopeData
import com.example.cuttherope.model.SpikeData
import com.example.cuttherope.model.StarData
import com.example.cuttherope.model.Vector2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class EditorTool(val label: String, val icon: ImageVector) {
    MOVE("Move", Icons.Default.Edit),
    ADD_PEG("Peg & Rope", Icons.Default.AddCircle),
    ADD_STAR("Star", Icons.Default.Star),
    ADD_BUBBLE("Bubble", Icons.Default.BubbleChart),
    ADD_BLOWER("Air Blower", Icons.Default.Navigation),
    ADD_SPIKE("Spikes", Icons.Default.Warning),
    DELETE("Delete", Icons.Default.Delete)
}

@Composable
fun LevelEditorScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assets = rememberGameAssets()

    var levelTitle by remember { mutableStateOf("My Custom Snack") }
    var selectedTool by remember { mutableStateOf(EditorTool.MOVE) }

    var omNomPos by remember { mutableStateOf(Vector2(200f, 580f)) }
    var candyPos by remember { mutableStateOf(Vector2(200f, 220f)) }
    var pegs by remember {
        mutableStateOf(
            listOf(
                PegData("peg_1", Vector2(100f, 100f)),
                PegData("peg_2", Vector2(300f, 100f))
            )
        )
    }
    var ropes by remember {
        mutableStateOf(
            listOf(
                RopeData("rope_1", "peg_1", 160f, 8),
                RopeData("rope_2", "peg_2", 160f, 8)
            )
        )
    }
    var stars by remember {
        mutableStateOf(
            listOf(
                StarData("s1", Vector2(150f, 320f)),
                StarData("s2", Vector2(250f, 320f)),
                StarData("s3", Vector2(200f, 450f))
            )
        )
    }
    var bubbles by remember { mutableStateOf(emptyList<BubbleData>()) }
    var blowers by remember { mutableStateOf(emptyList<BlowerData>()) }
    var spikes by remember { mutableStateOf(emptyList<SpikeData>()) }

    var draggedItemType by remember { mutableStateOf<String?>(null) }
    var draggedItemId by remember { mutableStateOf<String?>(null) }

    fun buildCurrentLevel(): LevelData {
        return LevelData(
            id = "custom_${System.currentTimeMillis()}",
            name = levelTitle.ifBlank { "Custom Level" },
            omNomPos = omNomPos,
            candySpawnPos = candyPos,
            pegs = pegs,
            ropes = ropes,
            stars = stars,
            bubbles = bubbles,
            blowers = blowers,
            spikes = spikes,
            isCustom = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF211510))
    ) {
        // Top Toolbar
        Surface(
            color = Color(0xFF3E2723),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 40.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    OutlinedTextField(
                        value = levelTitle,
                        onValueChange = { levelTitle = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .height(52.dp)
                            .testTag("level_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD54F),
                            unfocusedBorderColor = Color(0x99BCAAA4)
                        ),
                        placeholder = { Text("Level Name", color = Color(0x99BCAAA4), fontSize = 13.sp) }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val lvl = buildCurrentLevel()
                                viewModel.testFromEditor(lvl)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.height(40.dp).testTag("editor_test_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val lvl = buildCurrentLevel()
                                viewModel.saveCustomLevel(lvl)
                                Toast.makeText(context, "Level '${lvl.name}' saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color(0xFF3E2723)),
                            modifier = Modifier.height(40.dp).testTag("editor_save_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tool Selector Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorTool.values().forEach { tool ->
                        val isSelected = selectedTool == tool
                        Card(
                            modifier = Modifier
                                .clickable { selectedTool = tool }
                                .testTag("tool_${tool.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFD54F) else Color(0xFF4E342E)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.label,
                                    tint = if (isSelected) Color(0xFF3E2723) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tool.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF3E2723) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Editor Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val virtualWidth = 400f
            val virtualHeight = 700f

            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()

            val scale = minOf(screenWidthPx / virtualWidth, screenHeightPx / virtualHeight)
            val offsetX = (screenWidthPx - virtualWidth * scale) / 2f
            val offsetY = (screenHeightPx - virtualHeight * scale) / 2f

            val toVirtual = { screenX: Float, screenY: Float ->
                Vector2((screenX - offsetX) / scale, (screenY - offsetY) / scale)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool) {
                        detectTapGestures { offset ->
                            val v = toVirtual(offset.x, offset.y)
                            when (selectedTool) {
                                EditorTool.ADD_PEG -> {
                                    val newId = "peg_${System.currentTimeMillis()}"
                                    val newPeg = PegData(newId, v.copy())
                                    pegs = pegs + newPeg
                                    val dist = v.distanceTo(candyPos)
                                    val newRope = RopeData("rope_${System.currentTimeMillis()}", newId, length = dist.coerceAtLeast(100f), segments = 8)
                                    ropes = ropes + newRope
                                }
                                EditorTool.ADD_STAR -> {
                                    if (stars.size < 3) {
                                        stars = stars + StarData("s_${System.currentTimeMillis()}", v.copy())
                                    } else {
                                        Toast.makeText(context, "Max 3 stars per level!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                EditorTool.ADD_BUBBLE -> {
                                    bubbles = bubbles + BubbleData("b_${System.currentTimeMillis()}", v.copy())
                                }
                                EditorTool.ADD_BLOWER -> {
                                    blowers = blowers + BlowerData("bl_${System.currentTimeMillis()}", v.copy(), directionAngle = 0f)
                                }
                                EditorTool.ADD_SPIKE -> {
                                    spikes = spikes + SpikeData("sp_${System.currentTimeMillis()}", v.x - 60f, v.y, 120f, 25f)
                                }
                                EditorTool.DELETE -> {
                                    // Remove nearest element clicked
                                    val pegHit = pegs.find { it.position.distanceTo(v) < 30f }
                                    if (pegHit != null) {
                                        pegs = pegs.filter { it.id != pegHit.id }
                                        ropes = ropes.filter { it.pegId != pegHit.id }
                                        return@detectTapGestures
                                    }
                                    val starHit = stars.find { it.position.distanceTo(v) < 30f }
                                    if (starHit != null) {
                                        stars = stars.filter { it.id != starHit.id }
                                        return@detectTapGestures
                                    }
                                    val bubbleHit = bubbles.find { it.position.distanceTo(v) < 30f }
                                    if (bubbleHit != null) {
                                        bubbles = bubbles.filter { it.id != bubbleHit.id }
                                        return@detectTapGestures
                                    }
                                    val blowerHit = blowers.find { it.position.distanceTo(v) < 35f }
                                    if (blowerHit != null) {
                                        blowers = blowers.filter { it.id != blowerHit.id }
                                        return@detectTapGestures
                                    }
                                    val spikeHit = spikes.find { v.x >= it.x && v.x <= it.x + it.width && v.y >= it.y - 10f && v.y <= it.y + it.height + 10f }
                                    if (spikeHit != null) {
                                        spikes = spikes.filter { it.id != spikeHit.id }
                                        return@detectTapGestures
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    .pointerInput(selectedTool) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val v = toVirtual(offset.x, offset.y)
                                if (v.distanceTo(omNomPos) < 45f) {
                                    draggedItemType = "omnom"
                                } else if (v.distanceTo(candyPos) < 35f) {
                                    draggedItemType = "candy"
                                } else {
                                    val pegHit = pegs.find { it.position.distanceTo(v) < 30f }
                                    if (pegHit != null) {
                                        draggedItemType = "peg"
                                        draggedItemId = pegHit.id
                                    } else {
                                        val starHit = stars.find { it.position.distanceTo(v) < 30f }
                                        if (starHit != null) {
                                            draggedItemType = "star"
                                            draggedItemId = starHit.id
                                        } else {
                                            val bubbleHit = bubbles.find { it.position.distanceTo(v) < 30f }
                                            if (bubbleHit != null) {
                                                draggedItemType = "bubble"
                                                draggedItemId = bubbleHit.id
                                            } else {
                                                val blowerHit = blowers.find { it.position.distanceTo(v) < 35f }
                                                if (blowerHit != null) {
                                                    draggedItemType = "blower"
                                                    draggedItemId = blowerHit.id
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val v = toVirtual(change.position.x, change.position.y)
                                when (draggedItemType) {
                                    "omnom" -> omNomPos = v
                                    "candy" -> candyPos = v
                                    "peg" -> {
                                        pegs = pegs.map { if (it.id == draggedItemId) it.copy(position = v) else it }
                                    }
                                    "star" -> {
                                        stars = stars.map { if (it.id == draggedItemId) it.copy(position = v) else it }
                                    }
                                    "bubble" -> {
                                        bubbles = bubbles.map { if (it.id == draggedItemId) it.copy(position = v) else it }
                                    }
                                    "blower" -> {
                                        blowers = blowers.map { if (it.id == draggedItemId) it.copy(position = v) else it }
                                    }
                                }
                            },
                            onDragEnd = {
                                draggedItemType = null
                                draggedItemId = null
                            },
                            onDragCancel = {
                                draggedItemType = null
                                draggedItemId = null
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Grid background
                    drawRect(Color(0xFFB58047), size = size)
                    val gridColor = Color(0x22FFFFFF)
                    var x = offsetX
                    while (x <= offsetX + virtualWidth * scale) {
                        drawLine(gridColor, Offset(x, offsetY), Offset(x, offsetY + virtualHeight * scale))
                        x += 40f * scale
                    }
                    var y = offsetY
                    while (y <= offsetY + virtualHeight * scale) {
                        drawLine(gridColor, Offset(offsetX, y), Offset(offsetX + virtualWidth * scale, y))
                        y += 40f * scale
                    }

                    val toScreen = { v: Vector2 ->
                        Offset(offsetX + v.x * scale, offsetY + v.y * scale)
                    }

                    // Draw Spikes
                    for (sp in spikes) {
                        val sx = offsetX + sp.x * scale
                        val sy = offsetY + sp.y * scale
                        drawRect(Color(0xFF37474F), Offset(sx, sy), Size(sp.width * scale, sp.height * scale))
                    }

                    // Draw Ropes connecting Pegs to Candy
                    for (rope in ropes) {
                        val peg = pegs.find { it.id == rope.pegId } ?: continue
                        drawLine(
                            color = Color(0xFFD7CCC8),
                            start = toScreen(peg.position),
                            end = toScreen(candyPos),
                            strokeWidth = 4f * scale,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw Pegs
                    for (peg in pegs) {
                        val p = toScreen(peg.position)
                        drawCircle(Color(0xFF5D4037), radius = 10f * scale, center = p)
                        drawCircle(Color(0xFFFFD54F), radius = 4f * scale, center = p)
                    }

                    // Draw Stars
                    for (star in stars) {
                        val sp = toScreen(star.position)
                        drawCircle(Color(0xFFFFD700), radius = 14f * scale, center = sp)
                    }

                    // Draw Bubbles
                    for (bubble in bubbles) {
                        val bp = toScreen(bubble.position)
                        drawCircle(Color(0x664FC3F7), radius = 24f * scale, center = bp)
                        drawCircle(Color(0xFFB2EBF2), radius = 24f * scale, center = bp, style = Stroke(width = 2f))
                    }

                    // Draw Blowers
                    for (blower in blowers) {
                        val blp = toScreen(blower.position)
                        drawCircle(Color(0xFF8D6E63), radius = 22f * scale, center = blp)
                        drawLine(Color(0xFFFFD54F), blp, blp + Offset(25f, 0f), strokeWidth = 5f)
                    }

                    // Draw Candy (image 6)
                    val cp = toScreen(candyPos)
                    val candySize = (44f * scale).toInt()
                    drawImage(
                        image = assets.candyClassic,
                        dstOffset = IntOffset((cp.x - candySize / 2).toInt(), (cp.y - candySize / 2).toInt()),
                        dstSize = IntSize(candySize, candySize)
                    )

                    // Draw Om Nom (image 1)
                    val onp = toScreen(omNomPos)
                    val onSize = (90f * scale).toInt()
                    drawImage(
                        image = assets.omnomIdle,
                        dstOffset = IntOffset((onp.x - onSize / 2).toInt(), (onp.y - onSize / 2).toInt()),
                        dstSize = IntSize(onSize, onSize)
                    )
                }
            }
        }

        // Bottom Editor Help bar
        Surface(
            color = Color(0xFF3E2723),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedTool) {
                        EditorTool.MOVE -> "Drag Om Nom, Candy, Pegs, Stars to reposition"
                        EditorTool.ADD_PEG -> "Tap canvas to place Peg & Rope to Candy"
                        EditorTool.ADD_STAR -> "Tap canvas to place Star (${stars.size}/3)"
                        EditorTool.ADD_BUBBLE -> "Tap to place a floating bubble"
                        EditorTool.ADD_BLOWER -> "Tap to place air blower"
                        EditorTool.ADD_SPIKE -> "Tap to place danger spikes"
                        EditorTool.DELETE -> "Tap any element on canvas to remove"
                    },
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = {
                        pegs = listOf(PegData("peg_1", Vector2(200f, 100f)))
                        ropes = listOf(RopeData("rope_1", "peg_1", 160f, 8))
                        stars = emptyList()
                        bubbles = emptyList()
                        blowers = emptyList()
                        spikes = emptyList()
                        candyPos = Vector2(200f, 240f)
                        omNomPos = Vector2(200f, 580f)
                    },
                    modifier = Modifier.size(36.dp).testTag("editor_clear_button")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFEF5350))
                }
            }
        }
    }
}
