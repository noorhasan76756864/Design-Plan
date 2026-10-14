package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: BuildSpaceRepository,
    onNavigateToDesigner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by repository.projects.collectAsState()
    val selectedProjectId by repository.selectedProjectId.collectAsState()
    val activeProject = projects.firstOrNull { it.id == selectedProjectId } ?: projects.first()

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Kanban", "Gantt Timeline", "Budget Tracker", "Document Vault")
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewTaskDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BuildSpace",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = BlueprintCyanLight
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate800,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = activeProject.type.label.uppercase(),
                                    color = WarmAmberLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${activeProject.name} • ${activeProject.location}",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.testTag("btn_new_project")
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "New Project", tint = BlueprintCyanLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                )
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
            // Project Selector Chips
            item {
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(projects) { proj ->
                        val isSelected = proj.id == activeProject.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { repository.selectProject(proj.id) },
                            label = {
                                Text(
                                    proj.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueprintCyan,
                                selectedLabelColor = Color.White,
                                containerColor = Slate800,
                                labelColor = Slate200
                            )
                        )
                    }
                }
            }

            // Project Key Metrics Summary Banner
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
                            Column {
                                Text("TOTAL BUDGET ALLOCATION", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    currencyFormat.format(activeProject.totalBudget),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (activeProject.status) {
                                    ProjectStatus.PLANNING -> Slate700
                                    ProjectStatus.DESIGN -> BlueprintCyan
                                    ProjectStatus.CONSTRUCTION -> Terracotta
                                    ProjectStatus.FINISHING -> WarmAmber
                                    ProjectStatus.DONE -> SageGreen
                                    else -> BlueprintCyan
                                }
                            ) {
                                Text(
                                    text = activeProject.status.label.uppercase(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Spent: ${currencyFormat.format(activeProject.spentBudget)}", color = Slate400, fontSize = 11.sp)
                            Text("Overall Progress: ${activeProject.progressPercent}%", color = BlueprintCyanLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { activeProject.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BlueprintCyanLight,
                            trackColor = Slate700
                        )
                    }
                }
            }

            // Tabs Row
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Slate900,
                    contentColor = BlueprintCyanLight,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                    // Kanban Board
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CONSTRUCTION WORKFLOW BOARD", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showNewTaskDialog = true }) {
                                Icon(Icons.Default.AddTask, contentDescription = "Add Task", tint = BlueprintCyanLight)
                            }
                        }
                    }

                    // Render Kanban Columns
                    items(ProjectStatus.values().toList()) { status ->
                        val tasksInColumn = activeProject.tasks.filter { it.status == status }
                        Surface(
                            color = Slate800.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (status) {
                                                        ProjectStatus.PLANNING -> Slate400
                                                        ProjectStatus.DESIGN -> BlueprintCyanLight
                                                        ProjectStatus.PERMITS -> WarmAmberLight
                                                        ProjectStatus.CONSTRUCTION -> Terracotta
                                                        ProjectStatus.FINISHING -> SageGreenLight
                                                        ProjectStatus.DONE -> SageGreen
                                                    }
                                                )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = status.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Slate900
                                    ) {
                                        Text(
                                            text = "${tasksInColumn.size}",
                                            color = Slate400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (tasksInColumn.isEmpty()) {
                                    Text(
                                        "No tasks in ${status.label.lowercase()}",
                                        color = Slate600,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Spacer(Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        tasksInColumn.forEach { task ->
                                            KanbanTaskCard(
                                                task = task,
                                                onMoveForward = {
                                                    val next = ProjectStatus.values().getOrNull(status.ordinal + 1)
                                                    if (next != null) {
                                                        repository.updateTaskStatus(activeProject.id, task.id, next)
                                                    }
                                                },
                                                onMoveBackward = {
                                                    val prev = ProjectStatus.values().getOrNull(status.ordinal - 1)
                                                    if (prev != null) {
                                                        repository.updateTaskStatus(activeProject.id, task.id, prev)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Gantt Timeline View
                    item {
                        Text("SCHEDULE & DEPENDENCY TIMELINE", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    items(activeProject.tasks) { task ->
                        GanttTimelineItem(task = task)
                    }
                }
                2 -> {
                    // Budget Tracker View
                    item {
                        Text("CATEGORY ESTIMATED VS ACTUAL SPEND", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    items(activeProject.budgetItems) { budget ->
                        BudgetItemCard(item = budget, currencyFormat = currencyFormat)
                    }
                }
                3 -> {
                    // Document Vault View
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PROJECT CONTRACTS & BLUEPRINTS", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Upload Document", tint = BlueprintCyanLight)
                            }
                        }
                    }
                    items(activeProject.documents) { doc ->
                        DocumentVaultCard(doc = doc)
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        var projName by remember { mutableStateOf("") }
        var clientName by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var budgetStr by remember { mutableStateOf("150000") }
        var selectedType by remember { mutableStateOf(ProjectType.RESIDENTIAL) }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Project Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("Target Budget ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projName.isNotBlank()) {
                            val b = budgetStr.toDoubleOrNull() ?: 100000.0
                            repository.addProject(projName, selectedType, clientName, location, b)
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Create Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel") }
            },
            containerColor = Slate900
        )
    }

    // New Task Dialog
    if (showNewTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var assignee by remember { mutableStateOf("Architect / GC") }
        var dueDate by remember { mutableStateOf("Nov 20") }
        var priority by remember { mutableStateOf("High") }

        AlertDialog(
            onDismissRequest = { showNewTaskDialog = false },
            title = { Text("Add Construction Task", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignee,
                        onValueChange = { assignee = it },
                        label = { Text("Assigned Subcontractor / Person") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Target Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            repository.addTask(activeProject.id, taskTitle, ProjectStatus.PLANNING, assignee, dueDate, priority)
                            showNewTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan)
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTaskDialog = false }) { Text("Cancel") }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun KanbanTaskCard(
    task: TaskItem,
    onMoveForward: () -> Unit,
    onMoveBackward: () -> Unit
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (task.priority == "High") Terracotta.copy(alpha = 0.2f) else Slate800
                ) {
                    Text(
                        text = task.priority,
                        color = if (task.priority == "High") Terracotta else Slate400,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(task.assignedTo, color = Slate400, fontSize = 10.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Slate400, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(task.dueDate, color = Slate400, fontSize = 10.sp)
                }

                Row {
                    if (task.status.ordinal > 0) {
                        IconButton(onClick = onMoveBackward, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ArrowBackIos, contentDescription = "Move Left", tint = Slate400, modifier = Modifier.size(12.dp))
                        }
                    }
                    if (task.status.ordinal < ProjectStatus.values().size - 1) {
                        IconButton(onClick = onMoveForward, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Move Right", tint = BlueprintCyanLight, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GanttTimelineItem(task: TaskItem) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(task.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Assigned: ${task.assignedTo} • Due ${task.dueDate}", color = Slate400, fontSize = 10.sp)
                if (task.dependency != null) {
                    Text("↳ Depends on ${task.dependency}", color = WarmAmberLight, fontSize = 9.sp)
                }
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (task.status) {
                        ProjectStatus.DONE -> SageGreen.copy(alpha = 0.2f)
                        ProjectStatus.CONSTRUCTION -> Terracotta.copy(alpha = 0.2f)
                        else -> BlueprintCyan.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = task.status.label,
                        color = when (task.status) {
                            ProjectStatus.DONE -> SageGreen
                            ProjectStatus.CONSTRUCTION -> Terracotta
                            else -> BlueprintCyanLight
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetItemCard(item: BudgetItem, currencyFormat: NumberFormat) {
    val variance = item.actualAmount - item.estimatedAmount
    val isOver = variance > 0

    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = if (item.actualAmount == 0.0) "Unbilled" else if (isOver) "+${currencyFormat.format(variance)} Over" else "On Budget",
                    color = if (isOver) DangerRed else SageGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ESTIMATED", color = Slate400, fontSize = 9.sp)
                    Text(currencyFormat.format(item.estimatedAmount), color = Slate200, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ACTUAL SPENT", color = Slate400, fontSize = 9.sp)
                    Text(currencyFormat.format(item.actualAmount), color = if (isOver) DangerRed else Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))
            val ratio = if (item.estimatedAmount > 0) (item.actualAmount / item.estimatedAmount).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { ratio },
                color = if (isOver) DangerRed else BlueprintCyanLight,
                trackColor = Slate700,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.height(4.dp))
            Text(item.notes, color = Slate400, fontSize = 10.sp)
        }
    }
}

@Composable
fun DocumentVaultCard(doc: DocumentItem) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (doc.type) {
                    "CONTRACT" -> WarmAmber.copy(alpha = 0.2f)
                    "BLUEPRINT" -> BlueprintCyan.copy(alpha = 0.2f)
                    "PERMIT" -> SageGreen.copy(alpha = 0.2f)
                    else -> Slate700
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (doc.type) {
                            "CONTRACT" -> Icons.Default.Description
                            "BLUEPRINT" -> Icons.Default.Architecture
                            "PERMIT" -> Icons.Default.VerifiedUser
                            else -> Icons.Default.ReceiptLong
                        },
                        contentDescription = doc.type,
                        tint = when (doc.type) {
                            "CONTRACT" -> WarmAmberLight
                            "BLUEPRINT" -> BlueprintCyanLight
                            "PERMIT" -> SageGreenLight
                            else -> Slate200
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(doc.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                Text("${doc.type} • ${doc.fileSize} • Modified ${doc.dateModified}", color = Slate400, fontSize = 10.sp)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (doc.status == "Approved") SageGreen.copy(alpha = 0.15f) else Slate700
            ) {
                Text(
                    text = doc.status,
                    color = if (doc.status == "Approved") SageGreen else WarmAmberLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
