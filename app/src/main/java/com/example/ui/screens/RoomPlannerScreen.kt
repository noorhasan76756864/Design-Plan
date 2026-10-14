package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FurnitureItem
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.components.RoomPlannerCanvas
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPlannerScreen(
    repository: BuildSpaceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val furnitureList by repository.activeRoomFurniture.collectAsState()

    var roomLengthFt by remember { mutableFloatStateOf(20f) }
    var roomWidthFt by remember { mutableFloatStateOf(16f) }
    var roomHeightFt by remember { mutableFloatStateOf(9.5f) }

    var is3DView by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddFurnitureSheet by remember { mutableStateOf(false) }

    val hasCollisions = remember(furnitureList) { furnitureList.any { it.isColliding } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, tint = BlueprintCyanLight, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("3D Room Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Text("2D Floor Grid • 3D Axonometric Projection • Collision Engine", color = Slate400, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { repository.resetFurnitureLayout() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Layout", tint = Slate400)
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("btn_export_planner")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Specs", tint = BlueprintCyanLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = DarkSurface
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // View Mode Toggle (2D Grid vs 3D Isometric)
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { is3DView = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!is3DView) BlueprintCyan else Color.Transparent,
                            contentColor = if (!is3DView) Color.White else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("2D Plan Grid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { is3DView = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (is3DView) BlueprintCyan else Color.Transparent,
                            contentColor = if (is3DView) Color.White else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("3D Isometric", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive Planner Canvas
            item {
                RoomPlannerCanvas(
                    roomLengthFt = roomLengthFt,
                    roomWidthFt = roomWidthFt,
                    roomHeightFt = roomHeightFt,
                    furnitureList = furnitureList,
                    is3DView = is3DView,
                    selectedItemId = selectedItemId,
                    onSelectItem = { selectedItemId = it },
                    onMoveItem = { id, x, y ->
                        repository.updateFurniturePosition(id, x, y, roomLengthFt, roomWidthFt)
                    },
                    onRotateItem = { id ->
                        repository.rotateFurniture(id)
                    },
                    onDeleteItem = { id ->
                        repository.removeFurniture(id)
                        selectedItemId = null
                    }
                )
            }

            // Room Dimension Controls
            item {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ROOM DIMENSIONS & CLEARANCES", color = WarmAmberLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${(roomLengthFt * roomWidthFt).toInt()} SQ FT",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DimensionStepper(
                                label = "Length",
                                value = roomLengthFt,
                                onValueChange = { roomLengthFt = it.coerceIn(10f, 40f) },
                                modifier = Modifier.weight(1f)
                            )
                            DimensionStepper(
                                label = "Width",
                                value = roomWidthFt,
                                onValueChange = { roomWidthFt = it.coerceIn(10f, 30f) },
                                modifier = Modifier.weight(1f)
                            )
                            DimensionStepper(
                                label = "Ceiling",
                                value = roomHeightFt,
                                onValueChange = { roomHeightFt = it.coerceIn(8f, 16f) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Furniture Catalog & Architectural Elements Adder
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ADD ARCHITECTURAL FIXTURES & FURNITURE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${furnitureList.size} objects in room", color = Slate400, fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val catalogPresets = listOf(
                        Triple("Sectional Sofa", "SEATING", "#D4B996"),
                        Triple("Coffee Table", "TABLE", "#EADEC9"),
                        Triple("Dining Table", "TABLE", "#8C4A28"),
                        Triple("King Platform Bed", "BED", "#CBD5E1"),
                        Triple("Media Credenza", "STORAGE", "#78350F"),
                        Triple("Entry Doorway", "DOOR", "#0284C7"),
                        Triple("Curtain Window", "WINDOW", "#38BDF8")
                    )
                    items(catalogPresets) { (name, category, colorHex) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate800,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.clickable {
                                val (w, d, h) = when (category) {
                                    "SEATING" -> Triple(7.5f, 3.5f, 2.8f)
                                    "TABLE" -> Triple(4.0f, 3.0f, 1.5f)
                                    "BED" -> Triple(6.5f, 7.0f, 3.0f)
                                    "STORAGE" -> Triple(5.5f, 1.8f, 2.2f)
                                    "DOOR" -> Triple(3.2f, 0.5f, 8.0f)
                                    else -> Triple(6.0f, 0.4f, 7.0f)
                                }
                                repository.addFurniture(name, w, d, h, category, colorHex)
                                Toast.makeText(context, "Added $name to room canvas", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = when (category) {
                                        "SEATING" -> Icons.Default.Weekend
                                        "TABLE" -> Icons.Default.TableRestaurant
                                        "BED" -> Icons.Default.Bed
                                        "STORAGE" -> Icons.Default.Kitchen
                                        "DOOR" -> Icons.Default.SensorDoor
                                        else -> Icons.Default.Window
                                    },
                                    contentDescription = null,
                                    tint = BlueprintCyanLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Current Items in Room Inventory
            item {
                Text("PLACED OBJECTS & POSITION LOG", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            items(furnitureList) { item ->
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (item.isColliding) DangerRed else Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(android.graphics.Color.parseColor(item.colorHex)))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(item.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${item.widthFt}x${item.depthFt}ft • Pos (${String.format("%.1f", item.xFt)}, ${String.format("%.1f", item.yFt)}) • ${item.rotationDeg.toInt()}°",
                                    color = if (item.isColliding) DangerRed else Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { repository.rotateFurniture(item.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = BlueprintCyanLight, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { repository.removeFurniture(item.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = BlueprintCyanLight)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Architectural 3D Specs", color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ready to generate PDF and CAD spatial bundle for subcontractors:",
                        color = Slate200,
                        fontSize = 12.sp
                    )
                    Surface(color = Slate800, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Room Floor: ${(roomLengthFt * roomWidthFt).toInt()} sq ft (${roomLengthFt}x${roomWidthFt} ft)", color = Color.White, fontSize = 11.sp)
                            Text("• Ceiling Height: ${roomHeightFt} ft", color = Color.White, fontSize = 11.sp)
                            Text("• Placed Elements: ${furnitureList.size} objects", color = Color.White, fontSize = 11.sp)
                            Text("• Collision Status: ${if (hasCollisions) "WARNING (Overlap)" else "CLEARED (No collision)"}", color = if (hasCollisions) DangerRed else SageGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "Formats included: Vector PDF, 2D DWG Layout, 3D OBJ Mesh, and Clearance Verification Report.",
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        Toast.makeText(context, "Exported: BuildSpace_Room_Plan_${(roomLengthFt * roomWidthFt).toInt()}sqft.pdf", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Export Specification PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun DimensionStepper(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(label, color = Slate400, fontSize = 10.sp)
            Text("${value.toInt()} ft", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate800,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onValueChange(value - 1f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate800,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onValueChange(value + 1f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
