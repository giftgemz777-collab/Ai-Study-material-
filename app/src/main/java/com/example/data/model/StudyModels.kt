package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SavedItemType {
    NOTE_SUMMARY,
    MCQ_QUESTION,
    VIVA_QUESTION,
    CHAT_QA,
    FLASHCARD
}

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: SavedItemType,
    val title: String,
    val content: String,
    val subtitle: String = "",
    val subject: String = "General",
    val topic: String = "",
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckName: String = "Default Deck",
    val subject: String = "General",
    val front: String,
    val back: String,
    val isKnown: Boolean = false,
    val reviewCount: Int = 0,
    val lastReviewed: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val difficulty: String,
    val percentage: Int = if (totalQuestions > 0) (score * 100) / totalQuestions else 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = ""
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int = 25,
    val sessionType: String = "STUDY", // STUDY or BREAK
    val subject: String = "General",
    val completedAt: Long = System.currentTimeMillis()
)

// In-Memory UI Data Models
data class McqQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    var selectedIndex: Int = -1,
    var isBookmarked: Boolean = false
)

data class VivaQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val keyConcept: String = "",
    val difficulty: String = "Medium",
    var isBookmarked: Boolean = false,
    var isExpanded: Boolean = false
)

data class KeyTerm(
    val term: String,
    val definition: String
)

data class NoteSummaryResult(
    val title: String,
    val shortSummary: String,
    val importantPoints: List<String>,
    val keyTerms: List<KeyTerm>,
    val mcqs: List<McqQuestion>,
    val vivaQuestions: List<VivaQuestion>
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val promptType: String? = null,
    val isBookmarked: Boolean = false,
    val isGenerating: Boolean = false,
    val isError: Boolean = false
)

data class StudentProfile(
    val name: String = "Alex Chen",
    val major: String = "Computer Science & Engineering",
    val university: String = "College of Engineering",
    val semester: String = "6th Semester",
    val dailyStudyGoalMinutes: Int = 60,
    val isPremium: Boolean = false,
    val dailyAiLimit: Int = 10,
    val dailyAiUsed: Int = 3
)
