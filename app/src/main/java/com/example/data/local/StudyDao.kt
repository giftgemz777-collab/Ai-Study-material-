package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM saved_items ORDER BY timestamp DESC")
    fun getAllSavedItems(): Flow<List<SavedItemEntity>>

    @Query("SELECT * FROM saved_items WHERE type = :type ORDER BY timestamp DESC")
    fun getSavedItemsByType(type: SavedItemType): Flow<List<SavedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedItem(item: SavedItemEntity): Long

    @Delete
    suspend fun deleteSavedItem(item: SavedItemEntity)

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_items")
    suspend fun deleteAll()
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY lastReviewed DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY id ASC")
    fun getFlashcardsByDeck(deckName: String): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT deckName FROM flashcards")
    fun getAllDecks(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Query("UPDATE flashcards SET isKnown = :isKnown, reviewCount = reviewCount + 1, lastReviewed = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, isKnown: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM flashcards")
    suspend fun deleteAll()
}

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_results")
    fun getTotalQuizzesCount(): Flow<Int>

    @Query("SELECT AVG(percentage) FROM quiz_results")
    fun getAverageScore(): Flow<Double?>

    @Query("SELECT MAX(percentage) FROM quiz_results")
    fun getMaxScore(): Flow<Int?>

    @Query("DELETE FROM quiz_results")
    suspend fun deleteAll()
}

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE completedAt >= :startOfDayTimestamp ORDER BY completedAt DESC")
    fun getTodaySessions(startOfDayTimestamp: Long): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE sessionType = 'STUDY'")
    fun getTotalStudyMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'STUDY'")
    fun getTotalCompletedSessions(): Flow<Int>

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAll()
}
