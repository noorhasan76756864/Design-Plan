package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.model.CostEstimate
import com.example.data.model.RegionPricing
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimatorScreen(
    repository: BuildSpaceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val regions = listOf(
        RegionPricing("NYC", "New York, USA", "$", 125.0, 1.25),
        RegionPricing("SFO", "San Francisco, USA", "$", 140.0, 1.35),
        RegionPricing("LON", "London, UK", "£", 95.0, 1.15),
        RegionPricing("BER", "Berlin, Germany", "€", 85.0, 1.05),
        RegionPricing("TKO", "Tokyo, Japan", "¥", 110.0, 1.20),
        RegionPricing("ATX", "Austin, USA", "$", 95.0, 1.00),
        RegionPricing("DXB", "Dubai, UAE", "$", 115.0, 1.18)
    )

    var selectedRegion by remember { mutableStateOf(regions[0]) }

    var lengthFt by remember { mutableFloatStateOf(20f) }
    var widthFt by remember { mutableFloatStateOf(16f) }
    var heightFt by remember { mutableFloatStateOf(10f) }

    val flooringOptions = listOf(
        "Italian Porcelain Tile",
        "Engineered White Oak",
        "Solid Hardwood Herringbone",
        "Luxury Vinyl Plank (LVP)"
    )
    var selectedFlooring by remember { mutableStateOf(flooringOptions[1]) }

    val wallOptions = listOf(
        "Architectural Matte Paint (2 Coats)",
        "Venetian Plaster",
        "Textured Grasscloth Wallpaper",
        "Acoustic Wood Slat Paneling"
    )
    var selectedWallFinish by remember { mutableStateOf(wallOptions[1]) }

    val fixtureOptions = listOf("Economy Trade", "Premium Designer", "Luxury Architectural")
    var selectedFixture by remember { mutableStateOf(fixtureOptions[1]) }

    val estimate = remember(lengthFt, widthFt, heightFt, selectedFlooring, selectedWallFinish, selectedFixture, selectedRegion) {
        repository.calculateCostEstimate(
            lengthFt = lengthFt.toDouble(),
            widthFt = widthFt.toDouble(),
            heightFt = heightFt.toDouble(),
            flooringType = selectedFlooring,
            wallFinish = selectedWallFinish,
            fixturesQuality = selectedFixture,
            region = selectedRegion
        )
    }

    var showQuotationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = SageGreenLight, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Material & Cost Estimator", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Text("Regional Labor Rates • Accurate Bill of Materials • 10% Contingency", color = Slate400, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuotationDialog = true },
                        modifier = Modifier.testTag("btn_generate_quote")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Generate Quote", tint = BlueprintCyanLight)
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
            // Regional Pricing Selector
            item {
                Spacer(Modifier.height(4.dp))
                Text("1. REGIONAL COST INDEX & LABOR MARKET", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(regions) { reg ->
                        val isSelected = reg.code == selectedRegion.code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BlueprintCyan else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) BlueprintCyanLight else Slate700),
                            modifier = Modifier.clickable { selectedRegion = reg }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(reg.name, color = if (isSelected) Color.White else Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Labor: $${reg.laborRatePerHour.toInt()}/hr • ${reg.materialMultiplier}x index", color = if (isSelected) Color(0xFFE0F2FE) else Slate400, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Room Area Dimensions
            item {
                Text("2. ROOM DIMENSIONS FOR TAKEOFF", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Floor: ${(lengthFt * widthFt).toInt()} sq ft", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Wall Surface: ${estimate.wallAreaSqFt.toInt()} sq ft", color = WarmAmberLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DimensionStepper(label = "Length", value = lengthFt, onValueChange = { lengthFt = it }, modifier = Modifier.weight(1f))
                            DimensionStepper(label = "Width", value = widthFt, onValueChange = { widthFt = it }, modifier = Modifier.weight(1f))
                            DimensionStepper(label = "Ceiling", value = heightFt, onValueChange = { heightFt = it }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Material Selections
            item {
                Text("3. MATERIAL FINISHES SPECIFICATION", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                // Flooring selection
                Text("Flooring Selection", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(flooringOptions) { option ->
                        val isSelected = selectedFlooring == option
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) WarmAmber.copy(alpha = 0.25f) else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) WarmAmberLight else Slate700),
                            modifier = Modifier.clickable { selectedFlooring = option }
                        ) {
                            Text(
                                option,
                                color = if (isSelected) WarmAmberLight else Slate200,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                // Wall Finish selection
                Text("Wall Treatment", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallOptions) { option ->
                        val isSelected = selectedWallFinish == option
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BlueprintCyan.copy(alpha = 0.25f) else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) BlueprintCyanLight else Slate700),
                            modifier = Modifier.clickable { selectedWallFinish = option }
                        ) {
                            Text(
                                option,
                                color = if (isSelected) BlueprintCyanLight else Slate200,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Total Cost Summary Header Banner
            item {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SageGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ESTIMATED PROJECT GRAND TOTAL", color = SageGreenLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    currencyFormat.format(estimate.grandTotal),
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Button(
                                onClick = { showQuotationDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Client Quote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Slate700)
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Materials Subtotal", color = Slate400, fontSize = 10.sp)
                                Text(currencyFormat.format(estimate.subtotal - estimate.laborCost), color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Skilled Labor", color = Slate400, fontSize = 10.sp)
                                Text(currencyFormat.format(estimate.laborCost), color = BlueprintCyanLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("10% Contingency", color = Slate400, fontSize = 10.sp)
                                Text(currencyFormat.format(estimate.contingency), color = WarmAmberLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Itemized Bill of Materials
            item {
                Text("4. ITEMIZED BREAKDOWN & QUANTITY TAKEOFF", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            items(estimate.items) { item ->
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
                            Text(
                                "${String.format("%.1f", item.quantity)} ${item.unit} @ ${currencyFormat.format(item.unitPrice)}/${item.unit}",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            currencyFormat.format(item.total),
                            color = BlueprintCyanLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }

    // Client Quotation Dialog
    if (showQuotationDialog) {
        AlertDialog(
            onDismissRequest = { showQuotationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BlueprintCyanLight)
                    Spacer(Modifier.width(8.dp))
                    Text("Client Quotation Ready", color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Official estimate prepared for client approval:", color = Slate200, fontSize = 12.sp)
                    Surface(color = Slate800, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Region: ${selectedRegion.name}", color = Slate400, fontSize = 11.sp)
                            Text("Room Size: ${(lengthFt * widthFt).toInt()} sq ft (${lengthFt}x${widthFt} ft)", color = Color.White, fontSize = 11.sp)
                            Text("Flooring: $selectedFlooring", color = Color.White, fontSize = 11.sp)
                            Text("Wall Treatment: $selectedWallFinish", color = Color.White, fontSize = 11.sp)
                            HorizontalDivider(color = Slate700, modifier = Modifier.padding(vertical = 4.dp))
                            Text("Subtotal: ${currencyFormat.format(estimate.subtotal)}", color = Slate200, fontSize = 11.sp)
                            Text("Contingency (10%): ${currencyFormat.format(estimate.contingency)}", color = WarmAmberLight, fontSize = 11.sp)
                            Text("Total Quote: ${currencyFormat.format(estimate.grandTotal)}", color = SageGreenLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Includes itemized materials, labor guarantees, and permit allowance.", color = Slate400, fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuotationDialog = false
                        Toast.makeText(context, "Quotation generated & exported to Document Vault!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Export Client PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuotationDialog = false }) { Text("Close") }
            },
            containerColor = Slate900
        )
    }
}
