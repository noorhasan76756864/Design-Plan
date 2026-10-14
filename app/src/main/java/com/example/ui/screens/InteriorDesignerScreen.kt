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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.R
import com.example.data.model.*
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteriorDesignerScreen(
    repository: BuildSpaceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    var selectedStyle by remember { mutableStateOf(InteriorStyle.JAPANDI) }
    var selectedTier by remember { mutableStateOf(BudgetTier.LUXURY) }
    var selectedRoomType by remember { mutableStateOf("Living Lounge & Salon") }
    val roomTemplates = listOf("Living Lounge & Salon", "Master Bedroom Suite", "Scandinavian Kitchen", "Executive Studio")

    var isGenerating by remember { mutableStateOf(false) }
    var activeDesign by remember { mutableStateOf<DesignVariant?>(repository.generateAIDesign(InteriorStyle.JAPANDI, BudgetTier.LUXURY)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmAmberLight, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("AI Interior Designer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Text("Generative visual redesign, mood boards & shopping lists", color = Slate400, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Design variant saved to project room vault!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Design", tint = BlueprintCyanLight)
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Share link copied: https://buildspace.design/client/variant-9842", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Design", tint = Slate200)
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
            // Room Template Selector
            item {
                Spacer(Modifier.height(4.dp))
                Text("1. SELECT ROOM SPACE OR UPLOAD PHOTO", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(roomTemplates) { template ->
                        val isSelected = selectedRoomType == template
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BlueprintCyan else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) BlueprintCyanLight else Slate700),
                            modifier = Modifier.clickable { selectedRoomType = template }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Slate400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    template,
                                    color = if (isSelected) Color.White else Slate200,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Style Selector Chips
            item {
                Text("2. CHOOSE INTERIOR ARCHITECTURAL STYLE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(InteriorStyle.values().toList()) { style ->
                        val isSelected = selectedStyle == style
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) WarmAmber.copy(alpha = 0.25f) else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) WarmAmberLight else Slate700),
                            modifier = Modifier.clickable { selectedStyle = style }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    style.displayName,
                                    color = if (isSelected) WarmAmberLight else Slate200,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Budget Tier Selector
            item {
                Text("3. BUDGET TIER & FINISH GRADE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetTier.values().forEach { tier ->
                        val isSelected = selectedTier == tier
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) BlueprintCyanLight else Slate700),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTier = tier }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    tier.label,
                                    color = if (isSelected) BlueprintCyanLight else Slate200,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (tier) {
                                        BudgetTier.ECONOMY -> "Value / DIY"
                                        BudgetTier.MID_RANGE -> "Premium Studio"
                                        BudgetTier.LUXURY -> "Bespoke Luxury"
                                    },
                                    color = Slate400,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Generate Button
            item {
                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            delay(1200) // realistic synthesis delay
                            activeDesign = repository.generateAIDesign(selectedStyle, selectedTier)
                            isGenerating = false
                            Toast.makeText(context, "AI Redesign synthesized successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_generate_redesign")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Synthesizing Spatial Materials & Render...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generate AI Room Redesign", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Active Design Showcase: Before / After Slider
            activeDesign?.let { design ->
                item {
                    Text("BEFORE / AFTER SLIDER COMPARISON", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    BeforeAfterSlider(
                        beforeImageRes = design.beforeImageRes ?: R.drawable.img_room_before,
                        afterImageRes = design.afterImageRes ?: R.drawable.img_room_after
                    )
                }

                // Mood Board Section
                item {
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "CURATED MOOD BOARD & PALETTE",
                                    color = WarmAmberLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    design.style.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            // Color swatches row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                design.moodBoard.palette.forEach { swatch ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(swatch.hex)))
                                                .border(2.dp, Slate600, CircleShape)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(swatch.name, color = Slate200, fontSize = 8.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                                        Text(swatch.hex, color = Slate400, fontSize = 8.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Slate700)
                            Spacer(Modifier.height(12.dp))

                            // Materials & Textures
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("KEY MATERIALS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    design.moodBoard.materials.forEach { mat ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BlueprintCyanLight))
                                            Spacer(Modifier.width(6.dp))
                                            Text(mat, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TEXTURES & LAYERS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    design.moodBoard.textures.forEach { tex ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(WarmAmberLight))
                                            Spacer(Modifier.width(6.dp))
                                            Text(tex, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Design Rationale: ${design.moodBoard.designRationale}",
                                color = Slate300,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Shopping List Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CURATED SHOPPING LIST WITH REAL RETAIL LINKS", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            currencyFormat.format(design.shoppingList.sumOf { it.price }),
                            color = SageGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(design.shoppingList) { item ->
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${item.category} • Sold by ${item.store}", color = Slate400, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    currencyFormat.format(item.price),
                                    color = BlueprintCyanLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Opening retail link for ${item.name}...", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("View Item", color = WarmAmberLight, fontSize = 10.sp)
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = WarmAmberLight, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}
