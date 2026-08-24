package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PremiumUpgradeModal
import com.example.ui.screens.*
import kotlinx.coroutines.flow.collectLatest

data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun StudyMateApp(
    viewModel: StudyViewModel = viewModel()
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val showPremiumDialog by viewModel.showPremiumDialog.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    val navItems = listOf(
        BottomNavItem(
            destination = AppDestination.HOME,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            destination = AppDestination.STUDY,
            label = "Study",
            selectedIcon = Icons.Filled.School,
            unselectedIcon = Icons.Outlined.School
        ),
        BottomNavItem(
            destination = AppDestination.QUIZ,
            label = "Quiz",
            selectedIcon = Icons.Filled.Quiz,
            unselectedIcon = Icons.Outlined.Quiz
        ),
        BottomNavItem(
            destination = AppDestination.SAVED,
            label = "Saved",
            selectedIcon = Icons.Filled.Bookmark,
            unselectedIcon = Icons.Outlined.BookmarkBorder
        ),
        BottomNavItem(
            destination = AppDestination.PROFILE,
            label = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = currentDestination == item.destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.destination) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        modifier = Modifier.testTag("nav_item_${item.label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { destination ->
                when (destination) {
                    AppDestination.HOME -> HomeScreen(viewModel = viewModel)
                    AppDestination.STUDY -> StudyScreen(viewModel = viewModel)
                    AppDestination.QUIZ -> QuizScreen(viewModel = viewModel)
                    AppDestination.SAVED -> SavedScreen(viewModel = viewModel)
                    AppDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showPremiumDialog) {
        PremiumUpgradeModal(
            onDismiss = { viewModel.showPremiumUpgrade(false) },
            onActivatePro = { isPro -> viewModel.toggleProSubscription(isPro) },
            isCurrentlyPro = profile.isPremium
        )
    }
}
