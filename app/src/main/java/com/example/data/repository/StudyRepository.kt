package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.GeminiStudyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRepository(
    private val database: AppDatabase,
    private val geminiService: GeminiStudyService = GeminiStudyService()
) {
    private val savedItemDao = database.savedItemDao()
    private val flashcardDao = database.flashcardDao()
    private val quizResultDao = database.quizResultDao()
    private val pomodoroSessionDao = database.pomodoroSessionDao()

    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            name = "Alex Chen",
            major = "Computer Science & Engineering",
            university = "State Tech University",
            semester = "6th Semester",
            dailyStudyGoalMinutes = 60,
            isPremium = false,
            dailyAiLimit = 10,
            dailyAiUsed = 4
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    val allSavedItems: Flow<List<SavedItemEntity>> = savedItemDao.getAllSavedItems()
    val allFlashcards: Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards()
    val allDecks: Flow<List<String>> = flashcardDao.getAllDecks()
    val allQuizResults: Flow<List<QuizResultEntity>> = quizResultDao.getAllQuizResults()
    val totalStudyMinutes: Flow<Int?> = pomodoroSessionDao.getTotalStudyMinutes()
    val totalCompletedSessions: Flow<Int> = pomodoroSessionDao.getTotalCompletedSessions()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedSampleDataIfEmpty()
        }
    }

    private suspend fun seedSampleDataIfEmpty() {
        val existingCards = flashcardDao.getAllFlashcards().first()
        if (existingCards.isEmpty()) {
            val sampleCards = listOf(
                FlashcardEntity(
                    deckName = "Operating Systems",
                    subject = "Computer Science",
                    front = "What is the primary difference between a Process and a Thread?",
                    back = "A Process has its own independent address space and resources. A Thread is a lightweight execution unit that shares address space and memory with other threads in the same process.",
                    isKnown = true,
                    reviewCount = 2
                ),
                FlashcardEntity(
                    deckName = "Operating Systems",
                    subject = "Computer Science",
                    front = "What are the 4 Coffman conditions required for Deadlock?",
                    back = "1. Mutual Exclusion\n2. Hold and Wait\n3. No Preemption\n4. Circular Wait",
                    isKnown = false,
                    reviewCount = 1
                ),
                FlashcardEntity(
                    deckName = "Data Structures",
                    subject = "Computer Science",
                    front = "What is the average and worst-case time complexity of QuickSort?",
                    back = "Average: O(n log n)\nWorst-case: O(n²) (when pivot selection consistently results in maximally unbalanced partitions).",
                    isKnown = true,
                    reviewCount = 3
                ),
                FlashcardEntity(
                    deckName = "Data Structures",
                    subject = "Computer Science",
                    front = "Explain the AVL Tree balance factor invariant.",
                    back = "For every node, the height difference between left and right subtrees must be at most 1 (-1, 0, or +1). Rotations (LL, RR, LR, RL) restore balance.",
                    isKnown = false,
                    reviewCount = 1
                ),
                FlashcardEntity(
                    deckName = "Macroeconomics",
                    subject = "Economics",
                    front = "Define Fiscal Policy vs. Monetary Policy.",
                    back = "Fiscal Policy: Government taxation and expenditure decisions (Congress/Treasury).\nMonetary Policy: Central Bank control of interest rates and money supply (Federal Reserve/RBI).",
                    isKnown = true,
                    reviewCount = 2
                ),
                FlashcardEntity(
                    deckName = "Cellular Biology",
                    subject = "Biology",
                    front = "What is the net ATP yield from one glucose molecule in aerobic respiration?",
                    back = "Approximately 30 to 32 ATP molecules (Glycolysis: 2, Krebs Cycle: 2, Oxidative Phosphorylation: 26-28).",
                    isKnown = false,
                    reviewCount = 0
                )
            )
            flashcardDao.insertAll(sampleCards)
        }

        val existingQuizzes = quizResultDao.getAllQuizResults().first()
        if (existingQuizzes.isEmpty()) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val sampleQuizzes = listOf(
                QuizResultEntity(
                    subject = "Operating Systems",
                    topic = "CPU Scheduling & Semaphores",
                    score = 5,
                    totalQuestions = 5,
                    difficulty = "Medium",
                    percentage = 100,
                    dateFormatted = dateFormat.format(Date(System.currentTimeMillis() - 86400000L))
                ),
                QuizResultEntity(
                    subject = "Data Structures",
                    topic = "Graph Algorithms & BST",
                    score = 4,
                    totalQuestions = 5,
                    difficulty = "Hard",
                    percentage = 80,
                    dateFormatted = dateFormat.format(Date(System.currentTimeMillis() - 172800000L))
                ),
                QuizResultEntity(
                    subject = "Macroeconomics",
                    topic = "Inflation & Monetary Policy",
                    score = 4,
                    totalQuestions = 5,
                    difficulty = "Easy",
                    percentage = 80,
                    dateFormatted = dateFormat.format(Date(System.currentTimeMillis() - 259200000L))
                )
            )
            for (q in sampleQuizzes) {
                quizResultDao.insertQuizResult(q)
            }
        }

        val existingSaved = savedItemDao.getAllSavedItems().first()
        if (existingSaved.isEmpty()) {
            savedItemDao.insertSavedItem(
                SavedItemEntity(
                    type = SavedItemType.NOTE_SUMMARY,
                    title = "OS Scheduling Summary & Key Formulas",
                    content = "Round Robin gives optimal responsiveness (interactive tasks). SJF is provably optimal for minimizing average waiting time, but requires future CPU burst prediction (exponential smoothing).",
                    subtitle = "Computer Science • Operating Systems",
                    subject = "Computer Science",
                    topic = "CPU Scheduling",
                    tags = "High-Yield, Exam Prep"
                )
            )
            savedItemDao.insertSavedItem(
                SavedItemEntity(
                    type = SavedItemType.VIVA_QUESTION,
                    title = "Why does virtual memory use paging over segmentation?",
                    content = "Answer: Paging eliminates external fragmentation by using fixed-size blocks (pages and frames). It simplifies memory allocation and hardware support via TLB.",
                    subtitle = "Viva Question • Operating Systems",
                    subject = "Computer Science",
                    topic = "Virtual Memory",
                    tags = "Viva Defense"
                )
            )
        }

        val existingSessions = pomodoroSessionDao.getAllSessions().first()
        if (existingSessions.isEmpty()) {
            pomodoroSessionDao.insertSession(
                PomodoroSessionEntity(
                    durationMinutes = 25,
                    sessionType = "STUDY",
                    subject = "Operating Systems",
                    completedAt = System.currentTimeMillis() - 3600000L
                )
            )
            pomodoroSessionDao.insertSession(
                PomodoroSessionEntity(
                    durationMinutes = 25,
                    sessionType = "STUDY",
                    subject = "Data Structures",
                    completedAt = System.currentTimeMillis() - 7200000L
                )
            )
        }
    }

    // AI Generation
    suspend fun generateAiChatResponse(prompt: String, promptModifier: String? = null): String {
        incrementAiUsage()
        val fullPrompt = if (promptModifier != null) "$promptModifier for: $prompt" else prompt
        return geminiService.generateAiResponse(fullPrompt)
    }

    suspend fun summarizeNotes(rawText: String): NoteSummaryResult {
        incrementAiUsage()
        return geminiService.summarizeNotes(rawText)
    }

    suspend fun generateMcqs(subject: String, topic: String, count: Int, difficulty: String): List<McqQuestion> {
        incrementAiUsage()
        return geminiService.generateMcqs(subject, topic, count, difficulty)
    }

    suspend fun generateVivaQuestions(subject: String, topic: String, difficulty: String): List<VivaQuestion> {
        incrementAiUsage()
        return geminiService.generateVivaQuestions(subject, topic, difficulty)
    }

    private fun incrementAiUsage() {
        val current = _studentProfile.value
        if (!current.isPremium) {
            _studentProfile.value = current.copy(dailyAiUsed = (current.dailyAiUsed + 1).coerceAtMost(current.dailyAiLimit))
        }
    }

    fun resetDailyAiUsage() {
        _studentProfile.value = _studentProfile.value.copy(dailyAiUsed = 0)
    }

    fun togglePremium(isPremium: Boolean) {
        _studentProfile.value = _studentProfile.value.copy(isPremium = isPremium)
    }

    fun updateProfile(name: String, major: String, goalMinutes: Int) {
        _studentProfile.value = _studentProfile.value.copy(
            name = name,
            major = major,
            dailyStudyGoalMinutes = goalMinutes
        )
    }

    // Saved Items
    suspend fun saveItem(item: SavedItemEntity): Long = withContext(Dispatchers.IO) {
        savedItemDao.insertSavedItem(item)
    }

    suspend fun deleteSavedItem(id: Long) = withContext(Dispatchers.IO) {
        savedItemDao.deleteById(id)
    }

    // Flashcards
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = withContext(Dispatchers.IO) {
        flashcardDao.insertFlashcard(flashcard)
    }

    suspend fun updateFlashcardStatus(id: Long, isKnown: Boolean) = withContext(Dispatchers.IO) {
        flashcardDao.updateStatus(id, isKnown)
    }

    suspend fun deleteFlashcard(id: Long) = withContext(Dispatchers.IO) {
        flashcardDao.deleteById(id)
    }

    // Quiz Results
    suspend fun saveQuizResult(
        subject: String,
        topic: String,
        score: Int,
        totalQuestions: Int,
        difficulty: String
    ): Long = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val entity = QuizResultEntity(
            subject = subject,
            topic = topic,
            score = score,
            totalQuestions = totalQuestions,
            difficulty = difficulty,
            percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0,
            dateFormatted = dateFormat.format(Date())
        )
        quizResultDao.insertQuizResult(entity)
    }

    // Pomodoro Sessions
    suspend fun logCompletedSession(durationMinutes: Int, subject: String, sessionType: String = "STUDY") = withContext(Dispatchers.IO) {
        val entity = PomodoroSessionEntity(
            durationMinutes = durationMinutes,
            sessionType = sessionType,
            subject = subject,
            completedAt = System.currentTimeMillis()
        )
        pomodoroSessionDao.insertSession(entity)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        savedItemDao.deleteAll()
        flashcardDao.deleteAll()
        quizResultDao.deleteAll()
        pomodoroSessionDao.deleteAll()
        seedSampleDataIfEmpty()
    }
}
