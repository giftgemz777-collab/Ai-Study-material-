package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppDestination
import com.example.ui.StudyTool
import com.example.ui.StudyViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val totalMinutes by viewModel.totalStudyMinutes.collectAsState()
    val totalSessions by viewModel.totalCompletedSessions.collectAsState()
    val flashcards by viewModel.allFlashcards.collectAsState()
    val quizResults by viewModel.allQuizResults.collectAsState()
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isBreakMode by viewModel.isBreakMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val streakDays by viewModel.studyStreakDays.collectAsState()

    var quickQuery by remember { mutableStateOf("") }
    val masteredCardsCount = remember(flashcards) { flashcards.count { it.isKnown } }

    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val promptSuggestions = listOf(
        "📝 Summarize OS Virtual Memory",
        "🎯 5 MCQs on Binary Trees",
        "🎓 Viva questions for DBMS",
        "⚡ Explain Big-O notation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Profile & Status Row
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Student Avatar + Name + Major
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppDestination.PROFILE) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (profile.isPremium) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AmberTertiaryContainer
                                    ) {
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = AmberOnTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${profile.major} • ${profile.semester}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Theme Toggle + Quota Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeToggleIconButton(
                            themeMode = themeMode,
                            onToggle = { viewModel.toggleThemeMode() }
                        )

                        AiQuotaBadge(
                            profile = profile,
                            onClick = { viewModel.showPremiumUpgrade(true) }
                        )
                    }
                }
            }
        }

        // 2. Study Streak & Motivational Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakBadge(streakDays = streakDays)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = profile.university,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Hero Card: AI Study Assistant & Quick Input
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_study_banner")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Greeting & Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$greetingText, ${profile.name.split(" ").firstOrNull() ?: "Student"}! ✨",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Ask anything, summarize complex notes, or practice exam MCQs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick AI Input Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickQuery,
                            onValueChange = { quickQuery = it },
                            placeholder = {
                                Text(
                                    "Ask AI Study Assistant...",
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_quick_ai_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        FilledIconButton(
                            onClick = {
                                if (quickQuery.isNotBlank()) {
                                    val q = quickQuery
                                    quickQuery = ""
                                    viewModel.openStudyTool(StudyTool.ASSISTANT)
                                    viewModel.sendChatMessage(q)
                                } else {
                                    viewModel.openStudyTool(StudyTool.ASSISTANT)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("home_ask_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Prompt suggestion pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(promptSuggestions) { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    val cleanPrompt = suggestion.substringAfter(" ")
                                    viewModel.openStudyTool(StudyTool.ASSISTANT)
                                    viewModel.sendChatMessage(cleanPrompt)
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Study Tools Section (6 Core Student Features)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Study Tools",
                    subtitle = "AI-powered exam & syllabus preparation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeToolCard(
                        title = "AI Study\nAssistant",
                        subtitle = "Instant Q&A",
                        icon = Icons.Filled.AutoAwesome,
                        iconTint = IndigoPrimary,
                        containerColor = IndigoPrimaryContainer.copy(alpha = 0.55f),
                        borderColor = IndigoPrimary.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_assistant"),
                        onClick = { viewModel.openStudyTool(StudyTool.ASSISTANT) }
                    )
                    HomeToolCard(
                        title = "Summarize\nNotes",
                        subtitle = "Key Points",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconTint = SlateSecondary,
                        containerColor = SlateSecondaryContainer.copy(alpha = 0.55f),
                        borderColor = SlateSecondary.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_summarize"),
                        onClick = { viewModel.openStudyTool(StudyTool.SUMMARIZER) }
                    )
                    HomeToolCard(
                        title = "Generate\nMCQs",
                        subtitle = "Exam practice",
                        icon = Icons.Filled.Quiz,
                        iconTint = AmberTertiary,
                        containerColor = AmberTertiaryContainer.copy(alpha = 0.55f),
                        borderColor = AmberTertiary.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_mcqs"),
                        onClick = {
                            viewModel.navigateTo(AppDestination.QUIZ)
                            viewModel.resetQuiz()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeToolCard(
                        title = "Viva\nPreparation",
                        subtitle = "Oral exams",
                        icon = Icons.Filled.School,
                        iconTint = PurpleAccent,
                        containerColor = PurpleAccentContainer.copy(alpha = 0.55f),
                        borderColor = PurpleAccent.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_viva"),
                        onClick = { viewModel.openStudyTool(StudyTool.VIVA) }
                    )
                    HomeToolCard(
                        title = "Explain\nTopic",
                        subtitle = "Analogies",
                        icon = Icons.Filled.Lightbulb,
                        iconTint = SuccessGreen,
                        containerColor = SuccessGreenContainer.copy(alpha = 0.6f),
                        borderColor = SuccessGreen.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_explain"),
                        onClick = { viewModel.openStudyTool(StudyTool.EXPLAIN) }
                    )
                    HomeToolCard(
                        title = "Interactive\nFlashcards",
                        subtitle = "${flashcards.size} cards",
                        icon = Icons.Filled.Style,
                        iconTint = PinkAccent,
                        containerColor = PinkAccent.copy(alpha = 0.15f),
                        borderColor = PinkAccent.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_flashcards"),
                        onClick = { viewModel.openStudyTool(StudyTool.FLASHCARDS) }
                    )
                }
            }
        }

        // 5. Pomodoro Focus Timer Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openStudyTool(StudyTool.POMODORO) }
                    .testTag("home_pomodoro_widget"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTimerRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isBreakMode) SuccessGreen.copy(alpha = 0.18f)
                                    else if (isTimerRunning) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBreakMode) Icons.Filled.Coffee else Icons.Filled.Timer,
                                contentDescription = null,
                                tint = if (isBreakMode) SuccessGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val minutes = timerRemaining / 60
                            val seconds = timerRemaining % 60
                            val formattedTime = String.format("%02d:%02d", minutes, seconds)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isBreakMode) "Break Time • $formattedTime" else "Focus Timer • $formattedTime",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isTimerRunning) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                }
                            }
                            Text(
                                text = if (isTimerRunning) "Session active • Tap for fullscreen timer" else "Completed today: $totalSessions focus blocks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = { viewModel.toggleTimer() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("home_pomodoro_toggle")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 6. Today's Study Progress & Statistics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Today's Progress",
                    subtitle = "Your daily learning performance",
                    actionText = "Analytics",
                    onActionClick = { viewModel.navigateTo(AppDestination.PROFILE) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val minutesStudied = totalMinutes ?: 0
                    val goalMinutes = profile.dailyStudyGoalMinutes
                    val progressPercent = if (goalMinutes > 0) ((minutesStudied.toFloat() / goalMinutes) * 100).toInt() else 0

                    StatCard(
                        title = "Study Time",
                        value = "${minutesStudied}m",
                        icon = Icons.Filled.Timelapse,
                        iconColor = IndigoPrimary,
                        subtitle = "Goal: ${goalMinutes}m ($progressPercent%)",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_study_time")
                    )

                    StatCard(
                        title = "Quizzes Taken",
                        value = "${quizResults.size}",
                        icon = Icons.Filled.CheckCircle,
                        iconColor = AmberTertiary,
                        subtitle = "Avg: ${if (quizResults.isNotEmpty()) quizResults.map { it.percentage }.average().toInt() else 0}%",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_quizzes_taken")
                    )

                    StatCard(
                        title = "Mastered",
                        value = "$masteredCardsCount",
                        icon = Icons.Filled.Bolt,
                        iconColor = SuccessGreen,
                        subtitle = "Of ${flashcards.size} cards",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_mastered_cards")
                    )
                }
            }
        }

        // 7. Monetization / Campus Perks Banner
        item {
            AdBannerCard(
                isPremium = profile.isPremium,
                onUpgradeClick = { viewModel.showPremiumUpgrade(true) }
            )
        }
    }
}

@Composable
fun HomeToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(136.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
