package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttemptEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.LiveChatMessageEntity
import com.example.data.model.LiveSessionEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Downloads
    @Query("SELECT * FROM offline_downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("DELETE FROM offline_downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    // Bookmarks
    @Query("SELECT * FROM user_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM user_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    // Test Attempts
    @Query("SELECT * FROM test_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<AttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity)

    // Live Sessions
    @Query("SELECT * FROM live_sessions ORDER BY createdAt DESC")
    fun getAllLiveSessions(): Flow<List<LiveSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveSession(session: LiveSessionEntity)

    @Query("DELETE FROM live_sessions WHERE id = :id")
    suspend fun deleteLiveSession(id: String)

    // Live Chat Messages
    @Query("SELECT * FROM live_chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getChatMessagesForSession(sessionId: String): Flow<List<LiveChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: LiveChatMessageEntity)

    // User Sessions
    @Query("SELECT * FROM user_sessions ORDER BY lastActive DESC LIMIT 1")
    fun getActiveUserSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(user: UserSessionEntity)
}
