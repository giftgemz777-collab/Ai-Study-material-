package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.StudyTool
import com.example.ui.StudyViewModel
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AiQuotaBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val currentTool by viewModel.currentStudyTool.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_screen")
    ) {
        // Top Tool Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = currentTool.ordinal,
            edgePadding = 16.dp,
            divider = {},
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            StudyTool.entries.forEach { tool ->
                val title = when (tool) {
                    StudyTool.ASSISTANT -> "AI Assistant"
                    StudyTool.SUMMARIZER -> "Summarizer"
                    StudyTool.VIVA -> "Viva Prep"
                    StudyTool.EXPLAIN -> "Explain Topic"
                    StudyTool.FLASHCARDS -> "Flashcards"
                    StudyTool.POMODORO -> "Study Timer"
                }
                Tab(
                    selected = currentTool == tool,
                    onClick = { viewModel.openStudyTool(tool) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (currentTool == tool) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("study_tab_${tool.name.lowercase()}")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Tool Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTool) {
                StudyTool.ASSISTANT -> AiAssistantView(viewModel)
                StudyTool.SUMMARIZER -> NoteSummarizerView(viewModel)
                StudyTool.VIVA -> VivaPrepView(viewModel)
                StudyTool.EXPLAIN -> TopicExplainerView(viewModel)
                StudyTool.FLASHCARDS -> FlashcardsView(viewModel)
                StudyTool.POMODORO -> PomodoroTimerView(viewModel)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. AI STUDY ASSISTANT (Chat with Prompt Modifiers)
// -----------------------------------------------------------------------------------------
@Composable
fun AiAssistantView(viewModel: StudyViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val promptModifiers = listOf(
        "Explain simply",
        "Detailed explanation",
        "Give examples",
        "Create MCQs",
        "Create viva questions",
        "Make revision notes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_assistant_view")
    ) {
        // Chat History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    onBookmark = { viewModel.bookmarkChatMessage(msg) },
                    onCopy = { clipboardManager.setText(AnnotatedString(msg.text)) }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "AI StudyMate is thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Quick Prompt Modifier Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(promptModifiers) { modifier ->
                SuggestionChip(
                    onClick = {
                        val prompt = inputQuery.ifBlank { "Processes and CPU scheduling in Operating Systems" }
                        viewModel.sendChatMessage(prompt, modifier)
                        inputQuery = ""
                    },
                    label = { Text(modifier, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("modifier_${modifier.replace(" ", "_").lowercase()}")
                )
            }
        }

        // Chat Input Box
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask a concept, doubt, or paste a question...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank() && !isGenerating) {
                            viewModel.sendChatMessage(inputQuery)
                            inputQuery = ""
                        }
                    },
                    enabled = inputQuery.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .background(
                            if (inputQuery.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputQuery.isNotBlank() && !isGenerating) Color.White else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp, top = 4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Card(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.promptType != null && !message.isUser) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Mode: ${message.promptType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!message.isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onBookmark, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = "Save", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. NOTE SUMMARIZER (Paste text -> Short summary, Points, Terms, MCQs, Viva)
// -----------------------------------------------------------------------------------------
@Composable
fun NoteSummarizerView(viewModel: StudyViewModel) {
    val input by viewModel.summarizerInput.collectAsState()
    val summaryResult by viewModel.summaryResult.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()

    val sampleNotes = listOf(
        "OS Scheduling" to "Operating System CPU Scheduling manages the execution queue of processes. Preemptive algorithms like Round Robin allow interrupts when time quantum expires, maximizing responsiveness for interactive systems. Non-preemptive Shortest Job First (SJF) is mathematically optimal for minimizing average waiting time, but suffers from starvation for long tasks.",
        "Binary Trees" to "A Binary Search Tree (BST) is a node-based binary tree data structure where each node has at most two children. The left subtree contains only keys less than the node's key, and the right subtree contains keys greater. Inorder traversal of a BST yields elements in sorted ascending order. Balanced variants like AVL and Red-Black trees maintain O(log n) worst-case time complexity.",
        "Supply & Demand" to "In microeconomics, the law of demand states that higher prices lead to lower quantity demanded, while the law of supply states that higher prices incentivize suppliers to produce more. Market equilibrium occurs at the intersection of supply and demand curves. Government price ceilings below equilibrium create shortages, while price floors create surpluses.",
        "Cell Respiration" to "Cellular respiration converts biochemical energy from nutrients into adenosine triphosphate (ATP). The process comprises Glycolysis in cytoplasm, Krebs Cycle in mitochondrial matrix, and Oxidative Phosphorylation on the inner mitochondrial membrane. The net theoretical yield is 30 to 32 ATP per glucose molecule with oxygen acting as the final electron acceptor."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("note_summarizer_view"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste College Notes or Lecture Slides",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = input,
                        onValueChange = { viewModel.setSummarizerInput(it) },
                        placeholder = { Text("Paste your syllabus notes, textbook excerpts, or choose a sample below...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("summarizer_input_text"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Sample Notes Quick Selectors
                    Text(
                        text = "Or load sample notes:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sampleNotes) { (title, text) ->
                            AssistChip(
                                onClick = { viewModel.setSummarizerInput(text) },
                                label = { Text(title, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.generateNoteSummary(input) },
                        enabled = input.isNotBlank() && !isSummarizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_summary_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSummarizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing & Generating Study Pack...")
                        } else {
                            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Summary & Study Pack")
                        }
                    }
                }
            }
        }

        // Generated Results View
        if (summaryResult != null) {
            val result = summaryResult!!

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { viewModel.saveSummaryToBookmarks(result) }) {
                            Icon(imageVector = Icons.Filled.BookmarkAdd, contentDescription = "Save Summary", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.convertSummaryToFlashcards(result) }) {
                            Icon(imageVector = Icons.Filled.Style, contentDescription = "Make Flashcards", tint = SlateSecondary)
                        }
                    }
                }
            }

            // 1. Short Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Short Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(result.shortSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // 2. Important Points
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.Checklist, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Text("Important Points (High-Yield)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        result.importantPoints.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(point, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 3. Key Terms Glossary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(18.dp))
                            Text("Key Terms & Definitions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        result.keyTerms.forEach { term ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(term.term, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(term.definition, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Practice 5 MCQs
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.Quiz, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                            Text("5 Practice MCQs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        result.mcqs.forEachIndexed { qIdx, mcq ->
                            var selected by remember { mutableStateOf(-1) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Q${qIdx + 1}: ${mcq.question}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                mcq.options.forEachIndexed { oIdx, opt ->
                                    val isSelected = selected == oIdx
                                    val isCorrect = oIdx == mcq.correctIndex
                                    val bg = when {
                                        selected == -1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        isSelected && isCorrect -> SuccessGreenContainer
                                        isSelected && !isCorrect -> ErrorRedContainer
                                        isCorrect -> SuccessGreenContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                    Surface(
                                        onClick = { if (selected == -1) selected = oIdx },
                                        shape = RoundedCornerShape(8.dp),
                                        color = bg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${('A' + oIdx)}) $opt",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                if (selected != -1) {
                                    Text(
                                        text = "💡 ${mcq.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            // 5. 5 Viva Questions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.School, contentDescription = null, tint = SlateSecondary, modifier = Modifier.size(18.dp))
                            Text("5 Viva-Voce Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        result.vivaQuestions.forEachIndexed { vIdx, viva ->
                            var expanded by remember { mutableStateOf(false) }
                            Card(
                                onClick = { expanded = !expanded },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Q${vIdx + 1}: ${viva.question}",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (expanded) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Model Answer: ${viva.answer}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. VIVA PREPARATION (Subject, Topic, Difficulty -> Spoken Model Answers)
// -----------------------------------------------------------------------------------------
@Composable
fun VivaPrepView(viewModel: StudyViewModel) {
    val subject by viewModel.vivaSubject.collectAsState()
    val topic by viewModel.vivaTopic.collectAsState()
    val difficulty by viewModel.vivaDifficulty.collectAsState()
    val vivaList by viewModel.vivaQuestions.collectAsState()
    val isGenerating by viewModel.isGeneratingViva.collectAsState()

    var subjectInput by remember { mutableStateOf(subject) }
    var topicInput by remember { mutableStateOf(topic) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("viva_prep_view"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Viva-Voce Oral Exam Preparation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject (e.g. Computer Networks, Pathology)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Topic (e.g. TCP/IP Handshake, Deadlocks)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Difficulty selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { viewModel.setVivaConfig(subjectInput, topicInput, diff) },
                                label = { Text(diff) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.setVivaConfig(subjectInput, topicInput, difficulty)
                            viewModel.generateVivaQuestions()
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_viva_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Formulating Examiner Questions...")
                        } else {
                            Icon(imageVector = Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Viva Questions")
                        }
                    }
                }
            }
        }

        itemsIndexed(vivaList) { index, item ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (item.difficulty) {
                                "Easy" -> SuccessGreenContainer
                                "Hard" -> ErrorRedContainer
                                else -> AmberTertiaryContainer
                            }
                        ) {
                            Text(
                                text = "${item.difficulty} Viva",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.bookmarkVivaQuestion(item) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Text(
                        text = "Examiner: \"${item.question}\"",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        onClick = { viewModel.toggleVivaExpand(index) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.isExpanded) "Model Answer & Defense:" else "Tap to reveal Model Oral Answer",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = if (item.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (item.isExpanded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.keyConcept.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🎯 Key Concept: ${item.keyConcept}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. TOPIC EXPLAINER (Deep Analogies & Intuition)
// -----------------------------------------------------------------------------------------
@Composable
fun TopicExplainerView(viewModel: StudyViewModel) {
    var topicQuery by remember { mutableStateOf("Binary Search Trees") }
    var explanationText by remember { mutableStateOf("") }
    var isExplaining by remember { mutableStateOf(false) }

    val quickTopics = listOf(
        "Binary Search Trees",
        "Virtual Memory & Paging",
        "Neural Networks Backpropagation",
        "Keynesian Multiplier",
        "DNA Replication & Polymerase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("topic_explainer_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Explain Difficult Concept",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = topicQuery,
                    onValueChange = { topicQuery = it },
                    label = { Text("Topic name or textbook concept") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTopics) { qTopic ->
                        AssistChip(
                            onClick = { topicQuery = qTopic },
                            label = { Text(qTopic, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        isExplaining = true
                        viewModel.sendChatMessage("Explain simply with real-world analogies: $topicQuery")
                        explanationText = "Generating comprehensive breakdown for $topicQuery..."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explain with Analogies")
                }
            }
        }

        // Display explanation framework
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "Intuition & Analogy: $topicQuery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "1. Real World Analogy:\nThink of $topicQuery like a high-speed airport conveyor belt where priority baggage is separated into distinct bins so security scanners can process urgent flights first.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "2. Why It Matters in Exams:\nProfessors test this to evaluate your grasp of efficiency trade-offs (Time complexity vs Memory overhead).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "3. Three Golden Rules:\n• Always state assumptions first.\n• Verify edge cases (e.g. empty lists, null references).\n• Mention how it scales when input size doubles.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 5. FLASHCARDS VIEWER (Swipe, Flip, Known / Review Later)
// -----------------------------------------------------------------------------------------
@Composable
fun FlashcardsView(viewModel: StudyViewModel) {
    val allCards by viewModel.allFlashcards.collectAsState()
    val allDecks by viewModel.allDecks.collectAsState()
    val selectedDeck by viewModel.selectedDeck.collectAsState()
    val currentIndex by viewModel.currentCardIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    val filteredCards = remember(allCards, selectedDeck) {
        if (selectedDeck == "All") allCards else allCards.filter { it.deckName == selectedDeck }
    }

    val currentCard = filteredCards.getOrNull(currentIndex)
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("flashcards_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Deck Filter Chips and Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDeck == "All",
                        onClick = { viewModel.setSelectedDeck("All") },
                        label = { Text("All (${allCards.size})") }
                    )
                }
                items(allDecks) { deck ->
                    FilterChip(
                        selected = selectedDeck == deck,
                        onClick = { viewModel.setSelectedDeck(deck) },
                        label = { Text(deck) }
                    )
                }
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .size(36.dp)
                    .testTag("add_flashcard_fab")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Card", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No flashcards in this deck yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Button(onClick = { showAddDialog = true }) {
                        Text("Create First Card")
                    }
                }
            }
        } else if (currentCard != null) {
            // Progress counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${currentIndex + 1} of ${filteredCards.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentCard.isKnown) SuccessGreenContainer else AmberTertiaryContainer
                ) {
                    Text(
                        text = if (currentCard.isKnown) "Status: Mastered ✅" else "Status: Review Needed ⏳",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (currentCard.isKnown) SuccessGreen else AmberTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Flippable Flashcard Card
            Card(
                onClick = { viewModel.flipCard() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("interactive_flashcard_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFlipped) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isFlipped) "ANSWER / EXPLANATION" else "QUESTION / CONCEPT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "${currentCard.deckName} • ${currentCard.subject}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Content text
                    Text(
                        text = if (isFlipped) currentCard.back else currentCard.front,
                        style = if (isFlipped) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleLarge,
                        fontWeight = if (isFlipped) FontWeight.Normal else FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )

                    Text(
                        text = "👆 Tap card to flip",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Controls: Mark Known, Review Later, Previous, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.markCardKnown(currentCard, false)
                        viewModel.nextCard(filteredCards.size)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_review_later_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberTertiary)
                ) {
                    Icon(imageVector = Icons.Filled.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review Later")
                }

                Button(
                    onClick = {
                        viewModel.markCardKnown(currentCard, true)
                        viewModel.nextCard(filteredCards.size)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_mark_known_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mastered")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.previousCard(filteredCards.size) },
                    modifier = Modifier.testTag("prev_card_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                IconButton(
                    onClick = { viewModel.deleteFlashcard(currentCard.id) }
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete Card", tint = ErrorRed)
                }

                TextButton(
                    onClick = { viewModel.nextCard(filteredCards.size) },
                    modifier = Modifier.testTag("next_card_button")
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }

    if (showAddDialog) {
        var newFront by remember { mutableStateOf("") }
        var newBack by remember { mutableStateOf("") }
        var newDeck by remember { mutableStateOf(selectedDeck.ifBlank { "Operating Systems" }) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Flashcard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDeck,
                        onValueChange = { newDeck = it },
                        label = { Text("Deck / Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFront,
                        onValueChange = { newFront = it },
                        label = { Text("Front (Question / Term)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBack,
                        onValueChange = { newBack = it },
                        label = { Text("Back (Answer / Definition)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFront.isNotBlank() && newBack.isNotBlank()) {
                            viewModel.addNewFlashcard(newDeck, "General", newFront, newBack)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// 6. POMODORO STUDY TIMER (25m study, 5m break, progress ring)
// -----------------------------------------------------------------------------------------
@Composable
fun PomodoroTimerView(viewModel: StudyViewModel) {
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.timerDurationSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isBreak by viewModel.isBreakMode.collectAsState()
    val sessionCount by viewModel.timerSessionCount.collectAsState()
    val totalMinutes by viewModel.totalStudyMinutes.collectAsState()

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("pomodoro_timer_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mode Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isBreak) SuccessGreenContainer else MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isBreak) Icons.Filled.Coffee else Icons.Filled.Timer,
                    contentDescription = null,
                    tint = if (isBreak) SuccessGreen else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isBreak) "Break Mode (5 Mins)" else "Study Focus Mode (25 Mins)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBreak) SuccessGreen else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Circular Timer Display
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("pomodoro_circular_ring"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                color = if (isBreak) SuccessGreen else IndigoPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isRunning) "Deep Focus Active" else "Ready to Focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Preset Duration Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(15, 25, 45, 50).forEach { mins ->
                OutlinedButton(
                    onClick = { viewModel.setTimerPreset(mins) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (totalSeconds == mins * 60) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Timer Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .testTag("timer_reset_button")
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset")
            }

            Button(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier
                    .height(64.dp)
                    .width(140.dp)
                    .testTag("timer_play_pause_button"),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) ErrorRed else IndigoPrimary
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            IconButton(
                onClick = { viewModel.skipTimerPhase() },
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .testTag("timer_skip_button")
            ) {
                Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Skip Phase")
            }
        }

        // Sessions stats footer
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sessions Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("$sessionCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Focus Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${totalMinutes ?: 0} mins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                }
            }
        }
    }
}
