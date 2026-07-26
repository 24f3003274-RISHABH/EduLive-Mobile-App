package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TargetExam(val displayName: String, val categoryCode: String) {
    JEE_MAIN("JEE Main & Advanced", "JEE"),
    NEET_UG("NEET UG (Medical)", "NEET"),
    UPSC_CSE("UPSC Civil Services", "UPSC"),
    SSC_CGL("SSC & Bank Exams", "SSC"),
    GATE_CS("GATE Computer Science", "GATE"),
    CLASS_10_12("CBSE Class 10th & 12th", "SCHOOL")
}

enum class UserRole(val label: String) {
    STUDENT("Student"),
    TEACHER("Educator / Faculty"),
    PARENT("Parent Guardian"),
    ADMIN("System Admin")
}

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Rishabh Kumar",
    val email: String = "rishabh.student@edulive.in",
    val phone: String = "+91 98765 43210",
    val role: UserRole = UserRole.STUDENT,
    val targetExam: TargetExam = TargetExam.JEE_MAIN,
    val streakDays: Int = 14,
    val totalCoins: Int = 1250,
    val isSubscribed: Boolean = true,
    val subscriptionName: String = "EduLive+ All Access Pass"
)

data class CourseCategory(
    val id: String,
    val name: String,
    val code: String,
    val courseCount: Int,
    val iconName: String
)

data class Course(
    val id: String,
    val title: String,
    val category: String,
    val targetExam: TargetExam,
    val rating: Float,
    val reviewCount: Int,
    val instructorName: String,
    val instructorTitle: String,
    val priceOriginal: Int,
    val priceDiscounted: Int,
    val isLiveBatch: Boolean,
    val isEnrolled: Boolean = false,
    val durationHours: Int,
    val totalLectures: Int,
    val syllabusTopics: List<String>,
    val bannerGradientStart: Long,
    val bannerGradientEnd: Long
)

data class VideoLecture(
    val id: String,
    val courseId: String,
    val title: String,
    val durationMinutes: Int,
    val videoUrl: String,
    val hasNotesPdf: Boolean = true,
    val isFreePreview: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f
)

data class LiveSession(
    val id: String,
    val title: String,
    val courseTitle: String,
    val instructorName: String,
    val targetExam: TargetExam,
    val status: String, // "LIVE NOW", "UPCOMING", "ENDED"
    val viewerCount: Int,
    val startTimeFormatted: String,
    val isLowBandwidthAvailable: Boolean = true,
    val streamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val streamKey: String = id,
    val shareLink: String = "https://edulive.app/class/$id",
    val maxStudentsCapacity: Int = 200,
    val description: String = "Interactive Live Class & Seminar with real-time doubts and high-concurrency chat."
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isTeacher: Boolean = false,
    val isDoubtQuestion: Boolean = false
)

data class LivePoll(
    val id: String,
    val question: String,
    val options: List<String>,
    val votesPercent: List<Int>,
    val totalVotes: Int,
    val activeOptionIndex: Int? = null,
    val isActive: Boolean = true
)

data class MockQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val topic: String,
    val difficulty: String = "Medium"
)

data class TestSeries(
    val id: String,
    val title: String,
    val targetExam: TargetExam,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val totalMarks: Int,
    val questions: List<MockQuestion>
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val targetExam: TargetExam,
    val title: String,
    val content: String,
    val upvotes: Int,
    val commentCount: Int,
    val verifiedAnswer: String? = null,
    val timestamp: String
)

data class ERPInvoice(
    val invoiceId: String,
    val studentName: String,
    val courseTitle: String,
    val baseAmount: Int,
    val gstAmount: Int,
    val totalPaid: Int,
    val dateFormatted: String,
    val paymentMethod: String = "UPI (Razorpay)",
    val status: String = "SUCCESS"
)

data class ParentAnalyticsReport(
    val studentName: String = "Rishabh Kumar",
    val targetExam: String = "JEE Main 2026",
    val attendancePercentage: Int = 94,
    val testsCompleted: Int = 18,
    val avgScorePercent: Int = 82,
    val streakDays: Int = 14,
    val studyHoursThisWeek: Float = 28.5f,
    val weakTopics: List<String> = listOf("Rotational Motion", "Integration by Parts", "Organic Isomerism"),
    val strongTopics: List<String> = listOf("Electrostatics", "Coordinate Geometry", "Chemical Bonding"),
    val teacherFeedback: String = "Excellent consistency in live class polls. Focused practice needed in Calculus."
)

// --- Room Local Database Entities ---

@Entity(tableName = "offline_downloads")
data class DownloadEntity(
    @PrimaryKey val videoId: String,
    val courseId: String,
    val title: String,
    val durationMinutes: Int,
    val sizeMB: Int,
    val localFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val type: String, // "LECTURE", "QUESTION", "NOTE"
    val title: String,
    val subtitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_attempts")
data class AttemptEntity(
    @PrimaryKey val attemptId: String,
    val testId: String,
    val testTitle: String,
    val scoreObtained: Int,
    val maxScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_sessions")
data class LiveSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val courseTitle: String,
    val instructorName: String,
    val targetExamName: String,
    val status: String,
    val viewerCount: Int,
    val startTimeFormatted: String,
    val streamUrl: String,
    val streamKey: String,
    val shareLink: String,
    val maxStudentsCapacity: Int,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_chat_messages")
data class LiveChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isTeacher: Boolean,
    val isDoubtQuestion: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val targetExam: String,
    val isSubscribed: Boolean,
    val lastActive: Long = System.currentTimeMillis()
)
