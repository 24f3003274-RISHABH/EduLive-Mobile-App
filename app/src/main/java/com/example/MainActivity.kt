package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EduBottomBar
import com.example.ui.components.EduTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.EduLiveTheme
import com.example.ui.viewmodel.EduViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduLiveTheme {
                EduLiveApp()
            }
        }
    }
}

@Composable
fun EduLiveApp(viewModel: EduViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val enrolledCourseIds by viewModel.enrolledCourseIds.collectAsStateWithLifecycle()
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val selectedLiveSession by viewModel.selectedLiveSession.collectAsStateWithLifecycle()
    val liveChatMessages by viewModel.liveChatMessages.collectAsStateWithLifecycle()
    val currentPoll by viewModel.currentPoll.collectAsStateWithLifecycle()
    val isLowBandwidth by viewModel.isLowBandwidth.collectAsStateWithLifecycle()
    val isAudioOnly by viewModel.isAudioOnly.collectAsStateWithLifecycle()

    val aiDoubtInput by viewModel.aiDoubtInput.collectAsStateWithLifecycle()
    val aiDoubtSubject by viewModel.aiDoubtSubject.collectAsStateWithLifecycle()
    val aiDoubtResult by viewModel.aiDoubtResult.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiFlashcards by viewModel.aiFlashcards.collectAsStateWithLifecycle()

    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
    val isTestSubmitted by viewModel.isTestSubmitted.collectAsStateWithLifecycle()
    val lastTestScore by viewModel.lastTestScore.collectAsStateWithLifecycle()

    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val testAttempts by viewModel.testAttempts.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            EduTopAppBar(
                user = currentUser,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onExamSelected = { viewModel.setTargetExam(it) },
                onRoleSelected = { viewModel.setUserRole(it) },
                onOpenAuth = { viewModel.setTab(7) },
                toastMessage = toastMessage,
                onClearToast = { viewModel.clearToast() }
            )
        },
        bottomBar = {
            EduBottomBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.setTab(it) },
                userRole = currentUser.role
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    user = currentUser,
                    categories = viewModel.getCategories(),
                    featuredCourses = viewModel.getFilteredCourses(),
                    liveSessions = viewModel.getLiveSessions(),
                    onCourseClick = { course ->
                        viewModel.selectCourse(course)
                        viewModel.setTab(1)
                    },
                    onLiveSessionClick = { session ->
                        viewModel.selectLiveSession(session)
                    },
                    onQuickActionClick = { tabIdx ->
                        viewModel.setTab(tabIdx)
                    },
                    onEnrollClick = { courseId ->
                        viewModel.enrollCourse(courseId)
                    }
                )

                1 -> CourseExplorerScreen(
                    courses = viewModel.getFilteredCourses(),
                    selectedCourse = selectedCourse,
                    lectures = viewModel.getLecturesForSelectedCourse(),
                    enrolledCourseIds = enrolledCourseIds,
                    onSelectCourse = { viewModel.selectCourse(it) },
                    onEnrollCourse = { viewModel.enrollCourse(it) },
                    onDownloadLecture = { viewModel.downloadLecture(it) }
                )

                2 -> LiveClassScreen(
                    session = selectedLiveSession,
                    chatMessages = liveChatMessages,
                    poll = currentPoll,
                    isLowBandwidth = isLowBandwidth,
                    isAudioOnly = isAudioOnly,
                    onSendMessage = { text, isDoubt -> viewModel.sendLiveChatMessage(text, isDoubt) },
                    onVotePoll = { optionIdx -> viewModel.votePoll(optionIdx) },
                    onToggleLowBandwidth = { viewModel.toggleLowBandwidth() },
                    onToggleAudioOnly = { viewModel.toggleAudioOnly() }
                )

                3 -> AIDoubtSolverScreen(
                    doubtInput = aiDoubtInput,
                    subject = aiDoubtSubject,
                    aiResult = aiDoubtResult,
                    isLoading = isAiLoading,
                    flashcards = aiFlashcards,
                    onDoubtInputChange = { viewModel.setAiDoubtInput(it) },
                    onSubjectChange = { viewModel.setAiDoubtSubject(it) },
                    onAskClick = { viewModel.askAIDoubt() }
                )

                4 -> TestSeriesScreen(
                    testData = viewModel.testSeriesData,
                    currentQuestionIndex = currentQuestionIndex,
                    userAnswers = userAnswers,
                    isTestSubmitted = isTestSubmitted,
                    lastTestScore = lastTestScore,
                    onSelectOption = { qIdx, optIdx -> viewModel.selectTestOption(qIdx, optIdx) },
                    onQuestionIndexChange = { viewModel.setQuestionIndex(it) },
                    onSubmitTest = { viewModel.submitTest() },
                    onResetTest = { viewModel.resetTest() }
                )

                5 -> CommunityNotesScreen(
                    posts = viewModel.getCommunityPosts(),
                    onBookmarkNote = { title, sub -> viewModel.bookmarkItem(title, sub, "NOTE") }
                )

                6 -> DashboardScreen(
                    user = currentUser,
                    role = currentUser.role,
                    downloads = downloads,
                    bookmarks = bookmarks,
                    testAttempts = testAttempts,
                    invoices = viewModel.getInvoices(),
                    parentReport = viewModel.getParentReport(),
                    onDeleteDownload = { viewModel.deleteDownload(it) }
                )

                7 -> SignInScreen(
                    currentFirebaseUser = firebaseUser,
                    onSignInSuccess = { email, displayName ->
                        viewModel.handleSignInSuccess(email, displayName)
                    },
                    onSignOut = {
                        viewModel.setTab(0)
                    },
                    showToast = { msg ->
                        viewModel.handleSignInSuccess(currentUser.email, currentUser.name)
                    }
                )
            }
        }
    }
}
