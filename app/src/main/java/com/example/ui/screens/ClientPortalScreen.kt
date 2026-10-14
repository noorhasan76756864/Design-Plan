package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.components.SignaturePad
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPortalScreen(
    repository: BuildSpaceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    val projects by repository.projects.collectAsState()
    val selectedProjectId by repository.selectedProjectId.collectAsState()
    val activeProject = projects.firstOrNull { it.id == selectedProjectId } ?: projects.first()
    val contracts by repository.contracts.collectAsState()
    val invoices by repository.invoices.collectAsState()

    var isClientMode by remember { mutableStateOf(true) }
    var showPinCommentDialog by remember { mutableStateOf(false) }
    var pendingPinX by remember { mutableFloatStateOf(0.5f) }
    var pendingPinY by remember { mutableFloatStateOf(0.5f) }
    var newCommentText by remember { mutableStateOf("") }
    var showNewInvoiceDialog by remember { mutableStateOf(false) }

    val activeRoom = activeProject.rooms.firstOrNull()
    val activeDesign = activeRoom?.designs?.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = BlueprintCyanLight, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Client Collaboration Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Text("${activeProject.clientName} • ${activeProject.name}", color = Slate400, fontSize = 11.sp)
                    }
                },
                actions = {
                    // Mode Switcher: Designer vs Client View
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isClientMode) SageGreen.copy(alpha = 0.2f) else BlueprintCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (isClientMode) SageGreen else BlueprintCyanLight),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { isClientMode = !isClientMode }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isClientMode) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isClientMode) SageGreenLight else BlueprintCyanLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isClientMode) "Client View" else "Designer Mode",
                                color = if (isClientMode) SageGreenLight else BlueprintCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            // Mode Banner
            item {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isClientMode) Icons.Default.VerifiedUser else Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (isClientMode) SageGreenLight else BlueprintCyanLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isClientMode) "Viewing as Client: Elena Vance" else "Designer Studio Workspace",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isClientMode) "You can review design proposals, tap anywhere on images to pin feedback, e-sign contracts, and pay invoices." else "Review client feedback pins, generate invoices, and track signed contracts.",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Design Version Review & Approval Card
            activeDesign?.let { design ->
                item {
                    Text("1. DESIGN VERSION REVIEW & APPROVAL", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (design.approved) SageGreen else Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Version ${design.version}: ${design.style.displayName} Proposal",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text("Estimated Budget: ${currencyFormat.format(design.costEstimate)}", color = Slate400, fontSize = 11.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (design.approved) SageGreen else WarmAmber
                                ) {
                                    Text(
                                        text = if (design.approved) "APPROVED BY CLIENT" else "PENDING REVIEW",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Interactive Rendering with Pin Markers Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate900)
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            pendingPinX = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                                            pendingPinY = (offset.y / size.height).coerceIn(0.05f, 0.95f)
                                            showPinCommentDialog = true
                                        }
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = design.afterImageRes ?: R.drawable.img_room_after),
                                    contentDescription = "Design Rendering",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Overlay Existing Pin Markers
                                design.comments.forEachIndexed { index, pin ->
                                    Surface(
                                        shape = CircleShape,
                                        color = WarmAmberLight,
                                        border = BorderStroke(2.dp, Color.White),
                                        shadowElevation = 6.dp,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopStart)
                                            .offset(
                                                x = (pin.xPercent * 340).dp - 12.dp,
                                                y = (pin.yPercent * 200).dp - 12.dp
                                            )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Slate900,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate900.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "Tap anywhere on rendering to drop feedback pin",
                                        color = Slate200,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Action buttons: Approve vs Request Revision
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        activeRoom?.let { room ->
                                            repository.toggleDesignApproval(activeProject.id, room.id, design.id)
                                        }
                                        Toast.makeText(context, if (!design.approved) "Design approved! Notification sent to architect." else "Approval retracted.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (design.approved) Slate700 else SageGreen
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (design.approved) Icons.Default.Close else Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (design.approved) "Revoke Approval" else "Approve Design",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Revision requested with pinned notes.", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmAmberLight),
                                    border = BorderStroke(1.dp, WarmAmber),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Request Revisions", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Pinned Discussion Threads
                item {
                    Text("PINNED CLIENT & DESIGNER FEEDBACK (${design.comments.size})", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                items(design.comments) { comment ->
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = WarmAmberLight,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PushPin, contentDescription = null, tint = Slate900, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${comment.author} (${comment.role})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(comment.timestamp, color = Slate400, fontSize = 9.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(comment.text, color = Slate200, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // E-Signature Section
            item {
                Text("2. LEGAL CONTRACTS & DIGITAL E-SIGNATURE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                val contract = contracts.firstOrNull { it.projectId == activeProject.id } ?: contracts.first()
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (contract.status == "SIGNED") SageGreen else Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contract.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Contract Value: ${currencyFormat.format(contract.totalFee)} • Client: ${contract.clientName}", color = Slate400, fontSize = 10.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (contract.status == "SIGNED") SageGreen else Terracotta
                            ) {
                                Text(
                                    text = contract.status,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (contract.status == "SIGNED") {
                            Surface(
                                color = SageGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SageGreenLight, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Digitally signed by ${contract.signerName} on ${contract.signedDate}",
                                        color = SageGreenLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            SignaturePad(
                                onSignComplete = { hasSig ->
                                    if (hasSig) {
                                        repository.signContract(contract.id, activeProject.clientName)
                                        Toast.makeText(context, "Contract verified and signed!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Invoices & Payments Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. INVOICES & STRIPE PAYMENT LINKS", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    if (!isClientMode) {
                        IconButton(onClick = { showNewInvoiceDialog = true }) {
                            Icon(Icons.Default.AddCard, contentDescription = "New Invoice", tint = BlueprintCyanLight)
                        }
                    }
                }
            }

            items(invoices.filter { it.clientName.contains(activeProject.clientName) || it.projectName.contains(activeProject.name) }) { inv ->
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
                            Text(inv.invoiceNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Due ${inv.dueDate} • ${currencyFormat.format(inv.amount)}", color = Slate400, fontSize = 11.sp)
                        }

                        if (inv.status == "PAID") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SageGreen.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("PAID", color = SageGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    repository.payInvoice(inv.id)
                                    Toast.makeText(context, "Stripe payment simulated: $${inv.amount.toInt()} paid successfully!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pay via Stripe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }

    // Pin Comment Dialog
    if (showPinCommentDialog) {
        AlertDialog(
            onDismissRequest = { showPinCommentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = WarmAmberLight)
                    Spacer(Modifier.width(8.dp))
                    Text("Pin Comment on Rendering", color = Color.White, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add precise visual feedback at this spatial coordinate:", color = Slate300, fontSize = 11.sp)
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("e.g. Can we change this wall to lime plaster or walnut slat?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            activeRoom?.let { room ->
                                activeDesign?.let { des ->
                                    repository.addDesignPinComment(
                                        activeProject.id,
                                        room.id,
                                        des.id,
                                        pendingPinX,
                                        pendingPinY,
                                        newCommentText,
                                        if (isClientMode) "Elena Vance (Client)" else "Marc Architect (Designer)"
                                    )
                                }
                            }
                            newCommentText = ""
                            showPinCommentDialog = false
                            Toast.makeText(context, "Comment pin saved to design variant!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Drop Pin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinCommentDialog = false }) { Text("Cancel") }
            },
            containerColor = Slate900
        )
    }

    // New Invoice Dialog
    if (showNewInvoiceDialog) {
        var invAmount by remember { mutableStateOf("15000") }
        var invDueDate by remember { mutableStateOf("Nov 15, 2026") }

        AlertDialog(
            onDismissRequest = { showNewInvoiceDialog = false },
            title = { Text("Create Client Invoice", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = invAmount,
                        onValueChange = { invAmount = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invDueDate,
                        onValueChange = { invDueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = invAmount.toDoubleOrNull() ?: 10000.0
                        repository.createInvoice(activeProject.clientName, activeProject.name, amt, invDueDate)
                        showNewInvoiceDialog = false
                        Toast.makeText(context, "Invoice created & Stripe payment link generated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Generate Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewInvoiceDialog = false }) { Text("Cancel") }
            },
            containerColor = Slate900
        )
    }
}
