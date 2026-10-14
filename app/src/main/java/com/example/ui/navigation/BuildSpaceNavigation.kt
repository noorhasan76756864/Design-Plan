package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.repository.BuildSpaceRepository
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class BuildSpaceDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    AI_DESIGNER("designer", "AI Designer", Icons.Default.AutoAwesome, "tab_designer"),
    PLANNER("planner", "3D Planner", Icons.Default.ViewInAr, "tab_planner"),
    ESTIMATOR("estimator", "Estimator", Icons.Default.Calculate, "tab_estimator"),
    CLIENT_PORTAL("client", "Client Portal", Icons.Default.Handshake, "tab_client"),
    AI_ASSISTANT("assistant", "AI Copilot", Icons.Default.Psychology, "tab_assistant")
}

@Composable
fun BuildSpaceMainApp(
    repository: BuildSpaceRepository,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(BuildSpaceDestination.DASHBOARD) }

    // Android hardware back handler: return to dashboard if on sub-screens
    BackHandler(enabled = currentDestination != BuildSpaceDestination.DASHBOARD) {
        currentDestination = BuildSpaceDestination.DASHBOARD
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = BlueprintCyanLight,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                BuildSpaceDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                destination.title,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BlueprintCyanLight,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = BlueprintCyan
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        },
        containerColor = DarkSurface
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = modifier.padding(innerPadding)
        ) { target ->
            when (target) {
                BuildSpaceDestination.DASHBOARD -> DashboardScreen(
                    repository = repository,
                    onNavigateToDesigner = { currentDestination = BuildSpaceDestination.AI_DESIGNER }
                )
                BuildSpaceDestination.AI_DESIGNER -> InteriorDesignerScreen(
                    repository = repository
                )
                BuildSpaceDestination.PLANNER -> RoomPlannerScreen(
                    repository = repository
                )
                BuildSpaceDestination.ESTIMATOR -> EstimatorScreen(
                    repository = repository
                )
                BuildSpaceDestination.CLIENT_PORTAL -> ClientPortalScreen(
                    repository = repository
                )
                BuildSpaceDestination.AI_ASSISTANT -> AIAssistantScreen(
                    repository = repository
                )
            }
        }
    }
}
