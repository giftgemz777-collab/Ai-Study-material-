package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppDestination {
    HOME,
    STUDY,
    QUIZ,
    SAVED,
    PROFILE
}

enum class StudyTool {
    ASSISTANT,
    SUMMARIZER,
    VIVA,
    EXPLAIN,
    FLASHCARDS,
    POMODORO
}

enum class QuizMode {
    SETUP,
    ACTIVE,
    RESULT
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = StudyRepository(database)

    // App Theme State (System, Light, Dark)
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Daily Streak (Active motivation for college students)
    private val _studyStreakDays = MutableStateFlow(4)
    val studyStreakDays: StateFlow<Int> = _studyStreakDays.asStateFlow()

    // Navigation State
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentStudyTool = MutableStateFlow(StudyTool.ASSISTANT)
    val currentStudyTool: StateFlow<StudyTool> = _currentStudyTool.asStateFlow()

    // Student Profile & Quota
    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile

    // Flashcards & Saved Items & History
    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecks: StateFlow<List<String>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavedItems: StateFlow<List<SavedItemEntity>> = repository.allSavedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizResults: StateFlow<List<QuizResultEntity>> = repository.allQuizResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudyMinutes: StateFlow<Int?> = repository.totalStudyMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val totalCompletedSessions: StateFlow<Int> = repository.totalCompletedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    // AI Chat Assistant
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello Alex! 👋 I am your AI StudyMate. Ask me anything about your syllabus, or choose a quick action below to get started!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // Note Summarizer State
    private val _summarizerInput = MutableStateFlow("")
    val summarizerInput: StateFlow<String> = _summarizerInput.asStateFlow()

    private val _summaryResult = MutableStateFlow<NoteSummaryResult?>(null)
    val summaryResult: StateFlow<NoteSummaryResult?> = _summaryResult.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    // MCQ Practice / Generator State
    private val _quizMode = MutableStateFlow(QuizMode.SETUP)
    val quizMode: StateFlow<QuizMode> = _quizMode.asStateFlow()

    private val _mcqSubject = MutableStateFlow("Computer Science")
    val mcqSubject: StateFlow<String> = _mcqSubject.asStateFlow()

    private val _mcqTopic = MutableStateFlow("Operating Systems Scheduling")
    val mcqTopic: StateFlow<String> = _mcqTopic.asStateFlow()

    private val _mcqCount = MutableStateFlow(5)
    val mcqCount: StateFlow<Int> = _mcqCount.asStateFlow()

    private val _mcqDifficulty = MutableStateFlow("Medium")
    val mcqDifficulty: StateFlow<String> = _mcqDifficulty.asStateFlow()

    private val _activeQuizQuestions = MutableStateFlow<List<McqQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<McqQuestion>> = _activeQuizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    // Viva Prep State
    private val _vivaSubject = MutableStateFlow("Computer Science")
    val vivaSubject: StateFlow<String> = _vivaSubject.asStateFlow()

    private val _vivaTopic = MutableStateFlow("Processes & Deadlocks")
    val vivaTopic: StateFlow<String> = _vivaTopic.asStateFlow()

    private val _vivaDifficulty = MutableStateFlow("Medium")
    val vivaDifficulty: StateFlow<String> = _vivaDifficulty.asStateFlow()

    private val _vivaQuestions = MutableStateFlow<List<VivaQuestion>>(emptyList())
    val vivaQuestions: StateFlow<List<VivaQuestion>> = _vivaQuestions.asStateFlow()

    private val _isGeneratingViva = MutableStateFlow(false)
    val isGeneratingViva: StateFlow<Boolean> = _isGeneratingViva.asStateFlow()

    // Flashcards Viewer State
    private val _selectedDeck = MutableStateFlow("All")
    val selectedDeck: StateFlow<String> = _selectedDeck.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // Pomodoro Timer State
    private val _timerDurationSeconds = MutableStateFlow(25 * 60)
    val timerDurationSeconds: StateFlow<Int> = _timerDurationSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isBreakMode = MutableStateFlow(false)
    val isBreakMode: StateFlow<Boolean> = _isBreakMode.asStateFlow()

    private val _timerSessionCount = MutableStateFlow(0)
    val timerSessionCount: StateFlow<Int> = _timerSessionCount.asStateFlow()

    private var timerJob: Job? = null

    // UI Feedback Banner / Dialogs
    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadInitialVivaQuestions()
    }

    private fun loadInitialVivaQuestions() {
        viewModelScope.launch {
            _vivaQuestions.value = repository.generateVivaQuestions(
                _vivaSubject.value,
                _vivaTopic.value,
                _vivaDifficulty.value
            )
        }
    }

    // Navigation Actions
    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun openStudyTool(tool: StudyTool) {
        _currentStudyTool.value = tool
        _currentDestination.value = AppDestination.STUDY
    }

    fun showPremiumUpgrade(show: Boolean) {
        _showPremiumDialog.value = show
    }

    // Chat Actions
    fun sendChatMessage(prompt: String, modifier: String? = null) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(
            text = if (modifier != null) "[$modifier] $prompt" else prompt,
            isUser = true,
            promptType = modifier
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatGenerating.value = true

        viewModelScope.launch {
            try {
                val response = repository.generateAiChatResponse(prompt, modifier)
                val aiMsg = ChatMessage(
                    text = response,
                    isUser = false,
                    promptType = modifier
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    text = "Sorry, I encountered an error answering that. Please try again.",
                    isUser = false,
                    isError = true
                )
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    fun bookmarkChatMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.saveItem(
                SavedItemEntity(
                    type = SavedItemType.CHAT_QA,
                    title = "AI Chat QA (${message.promptType ?: "Concept"})",
                    content = message.text,
                    subtitle = "AI Study Assistant",
                    subject = "General Study",
                    tags = "Chat, Assistant"
                )
            )
            _snackbarMessage.emit("Saved to bookmarks!")
        }
    }

    // Note Summarizer
    fun setSummarizerInput(text: String) {
        _summarizerInput.value = text
    }

    fun generateNoteSummary(text: String) {
        if (text.isBlank()) return
        _isSummarizing.value = true
        viewModelScope.launch {
            try {
                val result = repository.summarizeNotes(text)
                _summaryResult.value = result
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to summarize notes: ${e.message}")
            } finally {
                _isSummarizing.value = false
            }
        }
    }

    fun saveSummaryToBookmarks(summary: NoteSummaryResult) {
        viewModelScope.launch {
            repository.saveItem(
                SavedItemEntity(
                    type = SavedItemType.NOTE_SUMMARY,
                    title = summary.title,
                    content = "${summary.shortSummary}\n\n• " + summary.importantPoints.joinToString("\n• "),
                    subtitle = "Note Summary & Key Points",
                    subject = "College Notes",
                    tags = "Summary, High-Yield"
                )
            )
            _snackbarMessage.emit("Summary saved to Bookmarks!")
        }
    }

    fun convertSummaryToFlashcards(summary: NoteSummaryResult) {
        viewModelScope.launch {
            val cards = summary.keyTerms.map {
                FlashcardEntity(
                    deckName = summary.title.take(25),
                    subject = "Summarized Notes",
                    front = "What is ${it.term}?",
                    back = it.definition,
                    isKnown = false
                )
            }
            for (card in cards) {
                repository.insertFlashcard(card)
            }
            _snackbarMessage.emit("Created ${cards.size} flashcards from key terms!")
        }
    }

    // MCQ Practice & Generator
    fun setMcqConfig(subject: String, topic: String, count: Int, difficulty: String) {
        _mcqSubject.value = subject
        _mcqTopic.value = topic
        _mcqCount.value = count
        _mcqDifficulty.value = difficulty
    }

    fun startMcqGeneration() {
        _isGeneratingQuiz.value = true
        viewModelScope.launch {
            try {
                val questions = repository.generateMcqs(
                    _mcqSubject.value,
                    _mcqTopic.value,
                    _mcqCount.value,
                    _mcqDifficulty.value
                )
                _activeQuizQuestions.value = questions
                _currentQuizIndex.value = 0
                _quizScore.value = 0
                _quizMode.value = QuizMode.ACTIVE
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to generate MCQs: ${e.message}")
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    fun selectMcqOption(questionIndex: Int, optionIndex: Int) {
        val current = _activeQuizQuestions.value.toMutableList()
        if (questionIndex in current.indices) {
            val q = current[questionIndex]
            current[questionIndex] = q.copy(selectedIndex = optionIndex)
            _activeQuizQuestions.value = current
        }
    }

    fun nextQuizQuestion() {
        if (_currentQuizIndex.value < _activeQuizQuestions.value.size - 1) {
            _currentQuizIndex.value += 1
        } else {
            finishQuiz()
        }
    }

    fun previousQuizQuestion() {
        if (_currentQuizIndex.value > 0) {
            _currentQuizIndex.value -= 1
        }
    }

    fun finishQuiz() {
        val questions = _activeQuizQuestions.value
        val correctCount = questions.count { it.selectedIndex == it.correctIndex }
        _quizScore.value = correctCount
        _quizMode.value = QuizMode.RESULT

        viewModelScope.launch {
            repository.saveQuizResult(
                subject = _mcqSubject.value,
                topic = _mcqTopic.value,
                score = correctCount,
                totalQuestions = questions.size,
                difficulty = _mcqDifficulty.value
            )
        }
    }

    fun resetQuiz() {
        _quizMode.value = QuizMode.SETUP
        _currentQuizIndex.value = 0
        _activeQuizQuestions.value = emptyList()
    }

    fun bookmarkMcqQuestion(question: McqQuestion) {
        viewModelScope.launch {
            repository.saveItem(
                SavedItemEntity(
                    type = SavedItemType.MCQ_QUESTION,
                    title = question.question,
                    content = "Options:\n" + question.options.mapIndexed { idx, opt ->
                        val check = if (idx == question.correctIndex) " (Correct ✅)" else ""
                        "${('A' + idx)}) $opt$check"
                    }.joinToString("\n") + "\n\nExplanation: ${question.explanation}",
                    subtitle = "${_mcqSubject.value} • ${_mcqDifficulty.value}",
                    subject = _mcqSubject.value,
                    topic = _mcqTopic.value,
                    tags = "MCQ, Practice"
                )
            )
            _snackbarMessage.emit("Question bookmarked!")
        }
    }

    // Viva Prep
    fun setVivaConfig(subject: String, topic: String, difficulty: String) {
        _vivaSubject.value = subject
        _vivaTopic.value = topic
        _vivaDifficulty.value = difficulty
    }

    fun generateVivaQuestions() {
        _isGeneratingViva.value = true
        viewModelScope.launch {
            try {
                val list = repository.generateVivaQuestions(
                    _vivaSubject.value,
                    _vivaTopic.value,
                    _vivaDifficulty.value
                )
                _vivaQuestions.value = list
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to generate viva questions: ${e.message}")
            } finally {
                _isGeneratingViva.value = false
            }
        }
    }

    fun toggleVivaExpand(index: Int) {
        val list = _vivaQuestions.value.toMutableList()
        if (index in list.indices) {
            val item = list[index]
            list[index] = item.copy(isExpanded = !item.isExpanded)
            _vivaQuestions.value = list
        }
    }

    fun bookmarkVivaQuestion(viva: VivaQuestion) {
        viewModelScope.launch {
            repository.saveItem(
                SavedItemEntity(
                    type = SavedItemType.VIVA_QUESTION,
                    title = viva.question,
                    content = "Answer: ${viva.answer}\n\nKey Concept: ${viva.keyConcept}",
                    subtitle = "${_vivaSubject.value} • ${viva.difficulty} Viva",
                    subject = _vivaSubject.value,
                    topic = _vivaTopic.value,
                    tags = "Viva Defense"
                )
            )
            _snackbarMessage.emit("Viva question bookmarked!")
        }
    }

    // Flashcards
    fun setSelectedDeck(deck: String) {
        _selectedDeck.value = deck
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard(totalCards: Int) {
        if (totalCards > 0) {
            _currentCardIndex.value = (_currentCardIndex.value + 1) % totalCards
            _isCardFlipped.value = false
        }
    }

    fun previousCard(totalCards: Int) {
        if (totalCards > 0) {
            _currentCardIndex.value = if (_currentCardIndex.value > 0) _currentCardIndex.value - 1 else totalCards - 1
            _isCardFlipped.value = false
        }
    }

    fun markCardKnown(card: FlashcardEntity, isKnown: Boolean) {
        viewModelScope.launch {
            repository.updateFlashcardStatus(card.id, isKnown)
        }
    }

    fun addNewFlashcard(deck: String, subject: String, front: String, back: String) {
        viewModelScope.launch {
            repository.insertFlashcard(
                FlashcardEntity(
                    deckName = deck.ifBlank { "General" },
                    subject = subject.ifBlank { "General" },
                    front = front,
                    back = back,
                    isKnown = false
                )
            )
            _snackbarMessage.emit("Flashcard added!")
        }
    }

    fun deleteFlashcard(id: Long) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
            _snackbarMessage.emit("Flashcard deleted")
        }
    }

    // Pomodoro Timer
    fun setTimerPreset(minutes: Int) {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timerDurationSeconds.value = minutes * 60
        _timerRemainingSeconds.value = minutes * 60
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerRemainingSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value == 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerRemainingSeconds.value = _timerDurationSeconds.value
    }

    fun skipTimerPhase() {
        pauseTimer()
        _isBreakMode.value = !_isBreakMode.value
        val newDuration = if (_isBreakMode.value) 5 * 60 else 25 * 60
        _timerDurationSeconds.value = newDuration
        _timerRemainingSeconds.value = newDuration
    }

    private fun onTimerFinished() {
        _isTimerRunning.value = false
        if (!_isBreakMode.value) {
            _timerSessionCount.value += 1
            viewModelScope.launch {
                repository.logCompletedSession(
                    durationMinutes = _timerDurationSeconds.value / 60,
                    subject = "Study Session"
                )
                _snackbarMessage.emit("🎉 Great focus session completed! Time for a 5-minute break.")
            }
            _isBreakMode.value = true
            _timerDurationSeconds.value = 5 * 60
            _timerRemainingSeconds.value = 5 * 60
        } else {
            viewModelScope.launch {
                _snackbarMessage.emit("Break over! Ready for the next focus session?")
            }
            _isBreakMode.value = false
            _timerDurationSeconds.value = 25 * 60
            _timerRemainingSeconds.value = 25 * 60
        }
    }

    // Saved Items
    fun deleteSavedItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedItem(id)
            _snackbarMessage.emit("Bookmark removed")
        }
    }

    // Theme Mode Management
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
    }

    // Profile & Settings
    fun updateStudentProfile(name: String, major: String, goalMinutes: Int) {
        repository.updateProfile(name, major, goalMinutes)
        viewModelScope.launch {
            _snackbarMessage.emit("Profile updated successfully!")
        }
    }

    fun resetAiDailyQuota() {
        repository.resetDailyAiUsage()
        viewModelScope.launch {
            _snackbarMessage.emit("AI daily quota reset for testing!")
        }
    }

    fun toggleProSubscription(isPro: Boolean) {
        repository.togglePremium(isPro)
        viewModelScope.launch {
            val msg = if (isPro) "👑 Upgraded to AI StudyMate Pro!" else "Switched to Free Plan"
            _snackbarMessage.emit(msg)
        }
    }

    fun resetAllDataToDefault() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarMessage.emit("Database reset with sample study data!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
