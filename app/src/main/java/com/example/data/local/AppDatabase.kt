package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttemptEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.LiveChatMessageEntity
import com.example.data.model.LiveSessionEntity
import com.example.data.model.UserSessionEntity

@Database(
    entities = [
        DownloadEntity::class,
        BookmarkEntity::class,
        AttemptEntity::class,
        LiveSessionEntity::class,
        LiveChatMessageEntity::class,
        UserSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edulive_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
