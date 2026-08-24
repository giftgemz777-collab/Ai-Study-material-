package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.McqQuestion
import com.example.data.model.QuizResultEntity
import com.example.ui.QuizMode
import com.example.ui.StudyViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun QuizScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.quizMode.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_screen")
    ) {
        when (mode) {
            QuizMode.SETUP -> QuizSetupView(viewModel)
            QuizMode.ACTIVE -> ActiveQuizView(viewModel)
            QuizMode.RESULT -> QuizResultScorecardView(viewModel)
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. QUIZ SETUP & HISTORY VIEW
// -----------------------------------------------------------------------------------------
@Composable
fun QuizSetupView(viewModel: StudyViewModel) {
    val subject by viewModel.mcqSubject.collectAsState()
    val topic by viewModel.mcqTopic.collectAsState()
    val count by viewModel.mcqCount.collectAsState()
    val difficulty by viewModel.mcqDifficulty.collectAsState()
    val isGenerating by viewModel.isGeneratingQuiz.collectAsState()
    val quizResults by viewModel.allQuizResults.collectAsState()

    var subjectInput by remember { mutableStateOf(subject) }
    var topicInput by remember { mutableStateOf(topic) }

    val quickTopics = listOf(
        "OS Process Scheduling",
        "Binary Search Trees",
        "SQL Joins & Indexing",
        "Supply & Demand Elasticity",
        "Cellular Respiration ATP",
        "Neural Networks & Backprop"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("quiz_setup_view"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quiz Generator Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberTertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Quiz, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "MCQ Exam Practice Generator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customize subject, difficulty, and question count",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_subject_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Topic or Syllabus Unit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_topic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick topic chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickTopics) { qTopic ->
                            SuggestionChip(
                                onClick = { topicInput = qTopic },
                                label = { Text(qTopic, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Question Count & Difficulty Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(5, 10, 15).forEach { c ->
                                    FilterChip(
                                        selected = count == c,
                                        onClick = { viewModel.setMcqConfig(subjectInput, topicInput, c, difficulty) },
                                        label = { Text("$c") }
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Difficulty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Easy", "Medium", "Hard").forEach { d ->
                                    FilterChip(
                                        selected = difficulty == d,
                                        onClick = { viewModel.setMcqConfig(subjectInput, topicInput, count, d) },
                                        label = { Text(d, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.setMcqConfig(subjectInput, topicInput, count, difficulty)
                            viewModel.startMcqGeneration()
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_quiz_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTertiary)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crafting Practice MCQs...")
                        } else {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Practice Quiz", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quiz Performance Analytics Stats
        item {
            SectionHeader(title = "Quiz Performance")

            val avgScore = if (quizResults.isNotEmpty()) quizResults.map { it.percentage }.average().toInt() else 0
            val bestScore = if (quizResults.isNotEmpty()) quizResults.maxOfOrNull { it.percentage } ?: 0 else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Quizzes",
                    value = "${quizResults.size}",
                    icon = Icons.Filled.Quiz,
                    iconColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Average Score",
                    value = "$avgScore%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconColor = SlateSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Best Score",
                    value = "$bestScore%",
                    icon = Icons.Filled.EmojiEvents,
                    iconColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Past Quiz History List
        item {
            SectionHeader(title = "Recent Quizzes")
        }

        if (quizResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No past quiz history yet. Start your first quiz above!", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            items(quizResults) { result ->
                QuizHistoryItem(result)
            }
        }
    }
}

@Composable
fun QuizHistoryItem(result: QuizResultEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = result.topic.ifBlank { result.subject },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${result.subject} • ${result.difficulty} • ${result.dateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    result.percentage >= 80 -> SuccessGreenContainer
                    result.percentage >= 60 -> AmberTertiaryContainer
                    else -> ErrorRedContainer
                }
            ) {
                Text(
                    text = "${result.score}/${result.totalQuestions} (${result.percentage}%)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        result.percentage >= 80 -> SuccessGreen
                        result.percentage >= 60 -> AmberTertiary
                        else -> ErrorRed
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. ACTIVE QUIZ INTERACTIVE VIEW
// -----------------------------------------------------------------------------------------
@Composable
fun ActiveQuizView(viewModel: StudyViewModel) {
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val currentQuestion = questions.getOrNull(currentIndex)

    if (currentQuestion == null) {
        viewModel.resetQuiz()
        return
    }

    val progress = (currentIndex + 1).toFloat() / questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("active_quiz_view"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header with Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.bookmarkMcqQuestion(currentQuestion) },
                    modifier = Modifier.testTag("bookmark_active_mcq")
                ) {
                    Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = "Bookmark Question", tint = MaterialTheme.colorScheme.primary)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = AmberTertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Question Card & Options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = currentQuestion.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                currentQuestion.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = currentQuestion.selectedIndex == optIndex
                    val isCorrect = optIndex == currentQuestion.correctIndex
                    val hasAnswered = currentQuestion.selectedIndex != -1

                    val cardColor = when {
                        !hasAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                        hasAnswered && isSelected && isCorrect -> SuccessGreenContainer
                        hasAnswered && isSelected && !isCorrect -> ErrorRedContainer
                        hasAnswered && isCorrect -> SuccessGreenContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }

                    Card(
                        onClick = { viewModel.selectMcqOption(currentIndex, optIndex) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("option_${optIndex}")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${('A' + optIndex)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Instant Explanation reveal
                if (currentQuestion.selectedIndex != -1) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💡 Explanation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(currentQuestion.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Navigation Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.previousQuizQuestion() },
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("quiz_prev_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }

            Button(
                onClick = { viewModel.nextQuizQuestion() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("quiz_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AmberTertiary)
            ) {
                Text(if (currentIndex == questions.size - 1) "Finish Quiz" else "Next Question", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. QUIZ SCORECARD RESULT VIEW
// -----------------------------------------------------------------------------------------
@Composable
fun QuizResultScorecardView(viewModel: StudyViewModel) {
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val subject by viewModel.mcqSubject.collectAsState()
    val topic by viewModel.mcqTopic.collectAsState()

    val total = questions.size
    val percentage = if (total > 0) (score * 100) / total else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("quiz_result_view"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scorecard Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    percentage >= 80 -> SuccessGreenContainer
                                    percentage >= 60 -> AmberTertiaryContainer
                                    else -> ErrorRedContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                percentage >= 80 -> SuccessGreen
                                percentage >= 60 -> AmberTertiary
                                else -> ErrorRed
                            }
                        )
                    }

                    Text(
                        text = when {
                            percentage >= 80 -> "🎉 Excellent Mastery!"
                            percentage >= 60 -> "👍 Good Job! Ready for Revision"
                            else -> "📖 Needs More Practice"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "You scored $score out of $total questions on $subject ($topic)",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.resetQuiz() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quiz_retry_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Take Another Quiz")
                        }
                    }
                }
            }
        }

        // Full Review of Questions
        item {
            SectionHeader(title = "Review Questions & Explanations")
        }

        itemsIndexed(questions) { idx, q ->
            val isCorrect = q.selectedIndex == q.correctIndex
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${idx + 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCorrect) SuccessGreenContainer else ErrorRedContainer
                        ) {
                            Text(
                                text = if (isCorrect) "Correct ✅" else "Incorrect ❌",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) SuccessGreen else ErrorRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    q.options.forEachIndexed { oIdx, opt ->
                        val isUserChoice = q.selectedIndex == oIdx
                        val isAns = q.correctIndex == oIdx
                        val rowBg = when {
                            isAns -> SuccessGreenContainer
                            isUserChoice -> ErrorRedContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = rowBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${('A' + oIdx)}) $opt",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Text(
                        text = "💡 ${q.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
