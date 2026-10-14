package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FurnitureItem
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun RoomPlannerCanvas(
    roomLengthFt: Float,
    roomWidthFt: Float,
    roomHeightFt: Float,
    furnitureList: List<FurnitureItem>,
    is3DView: Boolean,
    selectedItemId: String?,
    onSelectItem: (String?) -> Unit,
    onMoveItem: (id: String, newX: Float, newY: Float) -> Unit,
    onRotateItem: (id: String) -> Unit,
    onDeleteItem: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val hasCollisions = remember(furnitureList) {
        furnitureList.any { it.isColliding }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (is3DView) Color(0xFF0D1527) else Slate900)
            .border(1.dp, if (hasCollisions) DangerRed else Slate700, RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(is3DView, furnitureList) {
                    if (!is3DView) {
                        detectTapGestures { tapOffset ->
                            val scaleX = (canvasSize.width * 0.85f) / roomLengthFt
                            val scaleY = (canvasSize.height * 0.80f) / roomWidthFt
                            val offsetX = canvasSize.width * 0.075f
                            val offsetY = canvasSize.height * 0.10f

                            val tapXFt = (tapOffset.x - offsetX) / scaleX
                            val tapYFt = (tapOffset.y - offsetY) / scaleY

                            val tapped = furnitureList.findLast { item ->
                                val halfW = item.widthFt / 2f
                                val halfD = item.depthFt / 2f
                                tapXFt in (item.xFt - halfW)..(item.xFt + halfW) &&
                                        tapYFt in (item.yFt - halfD)..(item.yFt + halfD)
                            }
                            onSelectItem(tapped?.id)
                        }
                    }
                }
                .pointerInput(is3DView, furnitureList, selectedItemId) {
                    if (!is3DView) {
                        var draggingId: String? = null
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val scaleX = (canvasSize.width * 0.85f) / roomLengthFt
                                val scaleY = (canvasSize.height * 0.80f) / roomWidthFt
                                val offsetX = canvasSize.width * 0.075f
                                val offsetY = canvasSize.height * 0.10f

                                val startXFt = (startOffset.x - offsetX) / scaleX
                                val startYFt = (startOffset.y - offsetY) / scaleY

                                val hit = furnitureList.findLast { item ->
                                    val halfW = item.widthFt / 2f
                                    val halfD = item.depthFt / 2f
                                    startXFt in (item.xFt - halfW)..(item.xFt + halfW) &&
                                            startYFt in (item.yFt - halfD)..(item.yFt + halfD)
                                }
                                draggingId = hit?.id
                                if (hit != null) onSelectItem(hit.id)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val targetId = draggingId ?: selectedItemId
                                if (targetId != null) {
                                    val item = furnitureList.find { it.id == targetId }
                                    if (item != null) {
                                        val scaleX = (canvasSize.width * 0.85f) / roomLengthFt
                                        val scaleY = (canvasSize.height * 0.80f) / roomWidthFt
                                        val newX = item.xFt + (dragAmount.x / scaleX)
                                        val newY = item.yFt + (dragAmount.y / scaleY)
                                        onMoveItem(item.id, newX, newY)
                                    }
                                }
                            },
                            onDragEnd = { draggingId = null },
                            onDragCancel = { draggingId = null }
                        )
                    }
                }
        ) {
            canvasSize = size

            if (is3DView) {
                drawIsometric3DRoom(
                    roomLengthFt = roomLengthFt,
                    roomWidthFt = roomWidthFt,
                    roomHeightFt = roomHeightFt,
                    furnitureList = furnitureList,
                    textMeasurer = textMeasurer
                )
            } else {
                draw2DBlueprintGrid(
                    roomLengthFt = roomLengthFt,
                    roomWidthFt = roomWidthFt,
                    furnitureList = furnitureList,
                    selectedItemId = selectedItemId,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Collision Warning Banner
        if (hasCollisions) {
            Surface(
                color = DangerRed.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Collision Warning",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Collision detected! Furniture items overlap",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // View Mode Badge
        Surface(
            color = Slate800.copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = if (is3DView) "3D ISOMETRIC RENDER" else "2D BLUEPRINT PLAN",
                color = BlueprintCyanLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Quick Selected Item Action Bar
        if (!is3DView && selectedItemId != null) {
            val selectedItem = furnitureList.find { it.id == selectedItemId }
            if (selectedItem != null) {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedItem.name} (${selectedItem.widthFt}x${selectedItem.depthFt}ft)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(
                            onClick = { onRotateItem(selectedItem.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate 90 degrees",
                                tint = BlueprintCyanLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteItem(selectedItem.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove furniture",
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw2DBlueprintGrid(
    roomLengthFt: Float,
    roomWidthFt: Float,
    furnitureList: List<FurnitureItem>,
    selectedItemId: String?,
    textMeasurer: TextMeasurer
) {
    val scaleX = (size.width * 0.85f) / roomLengthFt
    val scaleY = (size.height * 0.80f) / roomWidthFt
    val offsetX = size.width * 0.075f
    val offsetY = size.height * 0.10f

    val roomWidthPx = roomLengthFt * scaleX
    val roomHeightPx = roomWidthFt * scaleY

    // Draw blueprint 1ft grid lines
    val gridColor = Color(0xFF1E293B)
    for (x in 0..roomLengthFt.toInt()) {
        val px = offsetX + x * scaleX
        drawLine(gridColor, Offset(px, offsetY), Offset(px, offsetY + roomHeightPx), strokeWidth = 1f)
    }
    for (y in 0..roomWidthFt.toInt()) {
        val py = offsetY + y * scaleY
        drawLine(gridColor, Offset(offsetX, py), Offset(offsetX + roomWidthPx, py), strokeWidth = 1f)
    }

    // Room Outer Walls
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(offsetX, offsetY),
        size = Size(roomWidthPx, roomHeightPx),
        style = Stroke(width = 6f)
    )

    // Dimension labels
    val lengthText = "${roomLengthFt.toInt()} ft"
    val widthText = "${roomWidthFt.toInt()} ft"
    drawText(
        textMeasurer = textMeasurer,
        text = lengthText,
        topLeft = Offset(offsetX + roomWidthPx / 2f - 20f, offsetY - 25f),
        style = TextStyle(color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = widthText,
        topLeft = Offset(offsetX - 45f, offsetY + roomHeightPx / 2f - 10f),
        style = TextStyle(color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )

    // Draw Furniture items
    for (item in furnitureList) {
        val itemPxW = item.widthFt * scaleX
        val itemPxD = item.depthFt * scaleY
        val centerPxX = offsetX + item.xFt * scaleX
        val centerPxY = offsetY + item.yFt * scaleY
        val isSelected = item.id == selectedItemId

        rotate(item.rotationDeg, pivot = Offset(centerPxX, centerPxY)) {
            val topLeft = Offset(centerPxX - itemPxW / 2f, centerPxY - itemPxD / 2f)
            val rectSize = Size(itemPxW, itemPxD)

            val baseColor = when {
                item.isColliding -> DangerRed
                item.category == "DOOR" -> BlueprintCyan
                item.category == "WINDOW" -> BlueprintCyanLight
                else -> Color(android.graphics.Color.parseColor(item.colorHex))
            }

            // Fill
            drawRoundRect(
                color = baseColor.copy(alpha = if (item.isColliding) 0.85f else 0.70f),
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Outline
            drawRoundRect(
                color = when {
                    item.isColliding -> Color.White
                    isSelected -> BlueprintCyanLight
                    else -> baseColor
                },
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = if (isSelected || item.isColliding) 4f else 2f)
            )

            // Category specific icon / pattern
            if (item.category == "DOOR") {
                // Door swing arc
                drawArc(
                    color = BlueprintCyanLight,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(itemPxW * 2f, itemPxW * 2f),
                    style = Stroke(width = 2f)
                )
            } else if (item.category == "WINDOW") {
                // Glass lines
                drawLine(
                    color = Color.White,
                    start = Offset(topLeft.x, centerPxY),
                    end = Offset(topLeft.x + itemPxW, centerPxY),
                    strokeWidth = 2f
                )
            }

            // Label
            val shortLabel = item.name.take(12)
            drawText(
                textMeasurer = textMeasurer,
                text = shortLabel,
                topLeft = Offset(centerPxX - (itemPxW * 0.42f), centerPxY - 8f),
                style = TextStyle(
                    color = if (item.isColliding) Color.White else Color(0xFF0F172A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawIsometric3DRoom(
    roomLengthFt: Float,
    roomWidthFt: Float,
    roomHeightFt: Float,
    furnitureList: List<FurnitureItem>,
    textMeasurer: TextMeasurer
) {
    // Axonometric isometric projection:
    // Screen X = (x - y) * cos(30°)
    // Screen Y = (x + y) * sin(30°) - z
    val angle = 30.0 * Math.PI / 180.0
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()

    val centerX = size.width / 2f
    val centerY = size.height * 0.72f
    val unitScale = (size.width * 0.38f) / (roomLengthFt + roomWidthFt)

    fun toIso(x: Float, y: Float, z: Float): Offset {
        val px = (x - y) * cosA * unitScale + centerX
        val py = centerY + (x + y) * sinA * unitScale - (z * unitScale * 1.5f)
        return Offset(px, py)
    }

    // Floor vertices
    val p000 = toIso(0f, 0f, 0f) // Back Corner
    val pL00 = toIso(roomLengthFt, 0f, 0f) // Right Corner
    val pLW0 = toIso(roomLengthFt, roomWidthFt, 0f) // Front Bottom
    val p0W0 = toIso(0f, roomWidthFt, 0f) // Left Corner

    // Back Left Wall (0,0 to 0,W)
    val p00H = toIso(0f, 0f, roomHeightFt)
    val p0WH = toIso(0f, roomWidthFt, roomHeightFt)
    val wallLeft = Path().apply {
        moveTo(p000.x, p000.y)
        lineTo(p0W0.x, p0W0.y)
        lineTo(p0WH.x, p0WH.y)
        lineTo(p00H.x, p00H.y)
        close()
    }
    drawPath(wallLeft, color = Color(0xFF1E293B))
    drawPath(wallLeft, color = Color(0xFF334155), style = Stroke(width = 2f))

    // Back Right Wall (0,0 to L,0)
    val pL0H = toIso(roomLengthFt, 0f, roomHeightFt)
    val wallRight = Path().apply {
        moveTo(p000.x, p000.y)
        lineTo(pL00.x, pL00.y)
        lineTo(pL0H.x, pL0H.y)
        lineTo(p00H.x, p00H.y)
        close()
    }
    drawPath(wallRight, color = Color(0xFF162032))
    drawPath(wallRight, color = Color(0xFF334155), style = Stroke(width = 2f))

    // Floor Base (Oak Wood Planks / Concrete)
    val floorPath = Path().apply {
        moveTo(p000.x, p000.y)
        lineTo(pL00.x, pL00.y)
        lineTo(pLW0.x, pLW0.y)
        lineTo(p0W0.x, p0W0.y)
        close()
    }
    drawPath(floorPath, color = Color(0xFF28241E)) // Warm natural oak shade
    drawPath(floorPath, color = Color(0xFF5B4636), style = Stroke(width = 3f))

    // Floor Planks Hatching
    var fy = 2f
    while (fy < roomWidthFt) {
        val startP = toIso(0f, fy, 0f)
        val endP = toIso(roomLengthFt, fy, 0f)
        drawLine(Color(0xFF3A342B), startP, endP, strokeWidth = 1f)
        fy += 2f
    }

    // Sort furniture by (x + y) so back items render before front items (depth sort)
    val sortedItems = furnitureList.sortedBy { it.xFt + it.yFt }

    for (item in sortedItems) {
        if (item.category == "WINDOW") continue

        val halfW = item.widthFt / 2f
        val halfD = item.depthFt / 2f
        val minX = item.xFt - halfW
        val maxX = item.xFt + halfW
        val minY = item.yFt - halfD
        val maxY = item.yFt + halfD
        val h = item.heightFt

        val c1 = toIso(minX, minY, 0f)
        val c2 = toIso(maxX, minY, 0f)
        val c3 = toIso(maxX, maxY, 0f)
        val c4 = toIso(minX, maxY, 0f)

        val t1 = toIso(minX, minY, h)
        val t2 = toIso(maxX, minY, h)
        val t3 = toIso(maxX, maxY, h)
        val t4 = toIso(minX, maxY, h)

        val color = if (item.isColliding) DangerRed else Color(android.graphics.Color.parseColor(item.colorHex))

        // Side Face 1 (Right Face)
        val faceRight = Path().apply {
            moveTo(c2.x, c2.y)
            lineTo(c3.x, c3.y)
            lineTo(t3.x, t3.y)
            lineTo(t2.x, t2.y)
            close()
        }
        drawPath(faceRight, color = color.copy(alpha = 0.70f))
        drawPath(faceRight, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1f))

        // Side Face 2 (Left/Front Face)
        val faceFront = Path().apply {
            moveTo(c4.x, c4.y)
            lineTo(c3.x, c3.y)
            lineTo(t3.x, t3.y)
            lineTo(t4.x, t4.y)
            close()
        }
        drawPath(faceFront, color = color.copy(alpha = 0.85f))
        drawPath(faceFront, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 1f))

        // Top Face (Lit directly by ambient ceiling cove)
        val topFace = Path().apply {
            moveTo(t1.x, t1.y)
            lineTo(t2.x, t2.y)
            lineTo(t3.x, t3.y)
            lineTo(t4.x, t4.y)
            close()
        }
        drawPath(topFace, color = color)
        drawPath(topFace, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
    }

    // Room Label
    drawText(
        textMeasurer = textMeasurer,
        text = "3D PERSPECTIVE: ${roomLengthFt.toInt()}x${roomWidthFt.toInt()} ft (Ceiling ${roomHeightFt}ft)",
        topLeft = Offset(24f, 20f),
        style = TextStyle(color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    )
}
