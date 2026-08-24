package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromSavedItemType(value: SavedItemType): String = value.name

    @TypeConverter
    fun toSavedItemType(value: String): SavedItemType = try {
        SavedItemType.valueOf(value)
    } catch (e: Exception) {
        SavedItemType.NOTE_SUMMARY
    }
}

@Database(
    entities = [
        SavedItemEntity::class,
        FlashcardEntity::class,
        QuizResultEntity::class,
        PomodoroSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedItemDao(): SavedItemDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_studymate_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
