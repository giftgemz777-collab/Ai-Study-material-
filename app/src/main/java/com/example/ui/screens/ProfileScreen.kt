package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val totalMinutes by viewModel.totalStudyMinutes.collectAsState()
    val totalSessions by viewModel.totalCompletedSessions.collectAsState()
    val flashcards by viewModel.allFlashcards.collectAsState()
    val quizResults by viewModel.allQuizResults.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val streakDays by viewModel.studyStreakDays.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    val masteredCards = remember(flashcards) { flashcards.count { it.isKnown } }
    val avgQuizScore = remember(quizResults) {
        if (quizResults.isNotEmpty()) quizResults.map { it.percentage }.average().toInt() else 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(IndigoPrimary, PurpleAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profile.major} • ${profile.semester}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = profile.university,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Student Details")
                    }
                }
            }
        }

        // Monetization & Subscription Plan Status Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (profile.isPremium) AmberTertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_plan_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (profile.isPremium) Icons.Filled.Star else Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (profile.isPremium) AmberTertiary else IndigoPrimary
                            )
                            Text(
                                text = if (profile.isPremium) "AI StudyMate Pro (Active)" else "Current Plan: Free Tier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.isPremium) AmberOnTertiaryContainer else IndigoOnPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (profile.isPremium) AmberTertiary else IndigoPrimary
                        ) {
                            Text(
                                text = if (profile.isPremium) "PRO" else "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Daily AI Usage Progress Bar
                    val remainingAi = (profile.dailyAiLimit - profile.dailyAiUsed).coerceAtLeast(0)
                    val aiProgress = if (profile.isPremium) 1f else (profile.dailyAiUsed.toFloat() / profile.dailyAiLimit)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (profile.isPremium) "Unlimited AI generation active" else "Daily AI Generations ($remainingAi left today)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (profile.isPremium) AmberOnTertiaryContainer else IndigoOnPrimaryContainer
                            )
                            if (!profile.isPremium) {
                                Text(
                                    text = "${profile.dailyAiUsed}/${profile.dailyAiLimit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!profile.isPremium) {
                            LinearProgressIndicator(
                                progress = { aiProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = IndigoPrimary,
                                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showPremiumUpgrade(true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("upgrade_plan_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (profile.isPremium) IndigoPrimary else AmberTertiary
                            )
                        ) {
                            Text(if (profile.isPremium) "Manage Plan" else "Upgrade to Pro")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetAiDailyQuota() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("reset_quota_button")
                        ) {
                            Text("Reset AI Quota (Test)")
                        }
                    }
                }
            }
        }

        // Study Statistics Overview
        item {
            SectionHeader(title = "Study Analytics")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Focus Time",
                        value = "${totalMinutes ?: 0}m",
                        icon = Icons.Filled.Timelapse,
                        iconColor = IndigoPrimary,
                        subtitle = "Goal: ${profile.dailyStudyGoalMinutes}m/day",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pomodoro Sessions",
                        value = "$totalSessions",
                        icon = Icons.Filled.Timer,
                        iconColor = SlateSecondary,
                        subtitle = "25m deep focus",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Flashcards Mastered",
                        value = "$masteredCards",
                        icon = Icons.Filled.Style,
                        iconColor = SuccessGreen,
                        subtitle = "Out of ${flashcards.size} cards",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Quizzes Completed",
                        value = "${quizResults.size}",
                        icon = Icons.Filled.Quiz,
                        iconColor = AmberTertiary,
                        subtitle = "Avg Score: $avgQuizScore%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // App Settings & Actions
        item {
            SectionHeader(title = "Preferences & Diagnostics")

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Theme Mode Selector
                    ListItem(
                        headlineContent = { Text("App Theme", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Text(
                                text = when (themeMode) {
                                    AppThemeMode.SYSTEM -> "Follows system setting"
                                    AppThemeMode.LIGHT -> "Light mode active"
                                    AppThemeMode.DARK -> "Dark mode active"
                                }
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = when (themeMode) {
                                    AppThemeMode.DARK -> Icons.Filled.DarkMode
                                    AppThemeMode.LIGHT -> Icons.Filled.LightMode
                                    AppThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = themeMode == AppThemeMode.LIGHT,
                                    onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                                    label = { Text("Light", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = themeMode == AppThemeMode.DARK,
                                    onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                                    label = { Text("Dark", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = themeMode == AppThemeMode.SYSTEM,
                                    onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                                    label = { Text("Auto", fontSize = 11.sp) }
                                )
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    ListItem(
                        headlineContent = { Text("Study Reminders") },
                        supportingContent = { Text("Daily Pomodoro & quiz practice notifications") },
                        leadingContent = {
                            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            var enabled by remember { mutableStateOf(true) }
                            Switch(checked = enabled, onCheckedChange = { enabled = it })
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    ListItem(
                        headlineContent = { Text("Vibration Feedback") },
                        supportingContent = { Text("Haptic alert on timer completion") },
                        leadingContent = {
                            Icon(imageVector = Icons.Outlined.Vibration, contentDescription = null, tint = SlateSecondary)
                        },
                        trailingContent = {
                            var enabled by remember { mutableStateOf(true) }
                            Switch(checked = enabled, onCheckedChange = { enabled = it })
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    ListItem(
                        headlineContent = { Text("Reset Sample Study Data", color = ErrorRed, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Restores default decks, notes, and quiz history") },
                        leadingContent = {
                            Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = null, tint = ErrorRed)
                        },
                        modifier = Modifier
                            .clickable { viewModel.resetAllDataToDefault() }
                            .testTag("reset_sample_data_item")
                    )
                }
            }
        }

        // Version Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI StudyMate v1.0 • College Study Companion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Powered by Google AI Studio & Gemini",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.name) }
        var editMajor by remember { mutableStateOf(profile.major) }
        var editGoal by remember { mutableStateOf(profile.dailyStudyGoalMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMajor,
                        onValueChange = { editMajor = it },
                        label = { Text("Major / Course") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGoal,
                        onValueChange = { editGoal = it },
                        label = { Text("Daily Study Goal (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val goal = editGoal.toIntOrNull() ?: 60
                        viewModel.updateStudentProfile(editName, editMajor, goal)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
