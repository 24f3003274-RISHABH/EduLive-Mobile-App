package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.EduRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EduRepository(db.appDao())

    // Firebase Auth State
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    val currentUser = repository.currentUser
    val enrolledCourseIds = repository.enrolledCourseIds
    val liveChatMessages = repository.liveChatMessages
    val currentPoll = repository.currentPoll

    init {
        try {
            _firebaseUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                val fUser = firebaseAuth.currentUser
                _firebaseUser.value = fUser
                if (fUser != null) {
                    val name = fUser.displayName ?: fUser.email?.substringBefore("@") ?: "EduLive Student"
                    repository.updateUserNameAndEmail(name, fUser.email ?: "")
                }
            }
        } catch (e: Exception) {
            // Firebase may fail if google-services.json missing in local build environment, handled gracefully
        }
    }

    fun handleSignInSuccess(email: String, displayName: String) {
        repository.updateUserNameAndEmail(displayName, email)
        showToast("Authenticated as $displayName ($email)")
        _currentTab.value = 0 // Navigate to Home Screen
    }

    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testAttempts: StateFlow<List<AttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation & Tab State
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Batches, 2: Live Stream, 3: EduAI, 4: Tests, 5: Forum, 6: Dashboard
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(repository.getAllCourses().first())
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    val allLiveSessions: StateFlow<List<LiveSession>> = repository.liveSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getLiveSessions())

    private val _selectedLiveSession = MutableStateFlow<LiveSession?>(repository.getLiveSessions().firstOrNull())
    val selectedLiveSession: StateFlow<LiveSession?> = _selectedLiveSession.asStateFlow()

    fun selectLiveSession(session: LiveSession) {
        _selectedLiveSession.value = session
        _currentTab.value = 2 // Switch to Live Stream Tab
    }

    fun joinLiveClassByLink(linkOrCode: String) {
        val session = repository.findOrJoinSessionByLink(linkOrCode)
        _selectedLiveSession.value = session
        _currentTab.value = 2
        showToast("🎓 Joined Live Stream: ${session.title}")
    }

    fun startTeacherLiveBroadcast(currentSession: LiveSession?) {
        val activeSession = (currentSession ?: _selectedLiveSession.value)?.copy(
            status = "LIVE NOW",
            viewerCount = 198,
            startTimeFormatted = "LIVE NOW"
        ) ?: LiveSession(
            id = "live_physics_101",
            title = "Physics Live Seminar - Rotational Mechanics",
            courseTitle = "JEE Advanced Physics",
            instructorName = currentUser.value.name,
            targetExam = currentUser.value.targetExam,
            status = "LIVE NOW",
            viewerCount = 198,
            startTimeFormatted = "LIVE NOW",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            streamKey = "jee_physics_101",
            shareLink = "https://ais-dev-ejhrxhfwsa3xlvrsvmjay2-208743066769.asia-east1.run.app?classId=jee_physics_101",
            maxStudentsCapacity = 200,
            description = "Live mobile camera broadcast with real-time doubt solver."
        )
        repository.updateLiveSession(activeSession)
        _selectedLiveSession.value = activeSession
        showToast("🔴 YOU ARE NOW LIVE! Class Code: ${activeSession.streamKey}")
    }

    fun scheduleLiveClass(
        title: String,
        subjectCourse: String,
        instructor: String,
        exam: TargetExam,
        timeString: String,
        streamUrl: String,
        maxCapacity: Int,
        description: String
    ) {
        val newSession = repository.scheduleNewLiveSession(
            title = title,
            subjectCourse = subjectCourse,
            instructor = instructor,
            exam = exam,
            timeString = timeString,
            streamUrl = streamUrl,
            maxCapacity = maxCapacity,
            description = description
        )
        viewModelScope.launch {
            repository.persistLiveSessionToDb(newSession)
        }
        _selectedLiveSession.value = newSession
        _currentTab.value = 2
        showToast("📢 Class Scheduled! Share link generated: ${newSession.shareLink}")
    }

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Low Bandwidth & Video Controls
    private val _isLowBandwidth = MutableStateFlow(false)
    val isLowBandwidth: StateFlow<Boolean> = _isLowBandwidth.asStateFlow()

    private val _isAudioOnly = MutableStateFlow(false)
    val isAudioOnly: StateFlow<Boolean> = _isAudioOnly.asStateFlow()

    // AI Tutor States
    private val _aiDoubtInput = MutableStateFlow("")
    val aiDoubtInput: StateFlow<String> = _aiDoubtInput.asStateFlow()

    private val _aiDoubtSubject = MutableStateFlow("Physics")
    val aiDoubtSubject: StateFlow<String> = _aiDoubtSubject.asStateFlow()

    private val _aiDoubtResult = MutableStateFlow<String?>(null)
    val aiDoubtResult: StateFlow<String?> = _aiDoubtResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiFlashcards = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiFlashcards: StateFlow<List<Pair<String, String>>> = _aiFlashcards.asStateFlow()

    // Test Taking Engine
    val testSeriesData = repository.getSampleTestSeries()
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // qIndex -> optionIndex
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    private val _isTestSubmitted = MutableStateFlow(false)
    val isTestSubmitted: StateFlow<Boolean> = _isTestSubmitted.asStateFlow()

    private val _lastTestScore = MutableStateFlow<Pair<Int, Int>?>(null)
    val lastTestScore: StateFlow<Pair<Int, Int>?> = _lastTestScore.asStateFlow()

    // Notification / Toast Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setTargetExam(exam: TargetExam) {
        repository.setTargetExam(exam)
    }

    fun setUserRole(role: UserRole) {
        repository.setUserRole(role)
        showToast("Switched active view to ${role.label}")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun enrollCourse(courseId: String) {
        repository.enrollInCourse(courseId)
        showToast("🎉 Successfully enrolled in course!")
    }

    fun sendLiveChatMessage(text: String, isDoubt: Boolean = false) {
        if (text.isNotBlank()) {
            repository.sendLiveChatMessage(text, isDoubt)
        }
    }

    fun votePoll(optionIndex: Int) {
        repository.votePollOption(optionIndex)
        showToast("Poll vote recorded successfully!")
    }

    fun toggleLowBandwidth() {
        _isLowBandwidth.value = !_isLowBandwidth.value
        showToast(if (_isLowBandwidth.value) "⚡ Low Bandwidth Mode Enabled (Data Saver)" else "Standard Bitrate Restored")
    }

    fun toggleAudioOnly() {
        _isAudioOnly.value = !_isAudioOnly.value
        showToast(if (_isAudioOnly.value) "🎧 Audio-Only Mode Active" else "Full Video Stream Active")
    }

    fun setAiDoubtInput(text: String) {
        _aiDoubtInput.value = text
    }

    fun setAiDoubtSubject(subject: String) {
        _aiDoubtSubject.value = subject
    }

    fun askAIDoubt() {
        val query = _aiDoubtInput.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            _aiDoubtResult.value = null
            val answer = repository.askAIDoubt(query, _aiDoubtSubject.value)
            _aiDoubtResult.value = answer
            _isAiLoading.value = false

            // Auto-generate flashcards
            val cards = repository.getAIFlashcards(query)
            _aiFlashcards.value = cards
        }
    }

    fun selectTestOption(questionIndex: Int, optionIndex: Int) {
        val current = _userAnswers.value.toMutableMap()
        current[questionIndex] = optionIndex
        _userAnswers.value = current
    }

    fun setQuestionIndex(index: Int) {
        _currentQuestionIndex.value = index
    }

    fun submitTest() {
        val questions = testSeriesData.questions
        var score = 0
        var correct = 0
        var wrong = 0
        var unattempted = 0

        questions.forEachIndexed { idx, q ->
            val userAns = _userAnswers.value[idx]
            if (userAns == null) {
                unattempted++
            } else if (userAns == q.correctOptionIndex) {
                score += 4
                correct++
            } else {
                score -= 1
                wrong++
            }
        }

        val maxScore = questions.size * 4
        _lastTestScore.value = Pair(score, maxScore)
        _isTestSubmitted.value = true

        viewModelScope.launch {
            repository.saveTestAttempt(testSeriesData.title, score, maxScore, correct, wrong, unattempted)
        }
        showToast("Test Submitted! Score: $score / $maxScore")
    }

    fun resetTest() {
        _userAnswers.value = emptyMap()
        _currentQuestionIndex.value = 0
        _isTestSubmitted.value = false
        _lastTestScore.value = null
    }

    fun downloadLecture(lecture: VideoLecture) {
        val courseTitle = _selectedCourse.value?.title ?: "EduLive Batch"
        viewModelScope.launch {
            repository.saveDownload(lecture, courseTitle)
            showToast("📥 Lecture '${lecture.title}' saved for offline viewing!")
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            repository.removeDownload(videoId)
            showToast("🗑️ Offline download removed")
        }
    }

    fun bookmarkItem(title: String, subtitle: String, type: String) {
        viewModelScope.launch {
            repository.saveBookmark(title, subtitle, type)
            showToast("🔖 Saved to Bookmarks")
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun getFilteredCourses(): List<Course> {
        val exam = currentUser.value.targetExam
        val query = _searchQuery.value.trim().lowercase()
        var list = repository.getCoursesForExam(exam)
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.lowercase().contains(query) ||
                        it.instructorName.lowercase().contains(query) ||
                        it.category.lowercase().contains(query)
            }
        }
        return list
    }

    fun getLecturesForSelectedCourse(): List<VideoLecture> {
        val courseId = _selectedCourse.value?.id ?: "c_jee_01"
        return repository.getLecturesForCourse(courseId)
    }

    fun getCategories(): List<CourseCategory> = repository.getCategories()
    fun getLiveSessions(): List<LiveSession> = repository.getLiveSessions()
    fun getCommunityPosts(): List<CommunityPost> = repository.getCommunityPosts()
    fun getInvoices(): List<ERPInvoice> = repository.getInvoices()
    fun getParentReport(): ParentAnalyticsReport = ParentAnalyticsReport()
}
