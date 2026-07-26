package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EduRepository(private val dao: AppDao) {

    // Current Active User
    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Enrolled Course IDs
    private val _enrolledCourseIds = MutableStateFlow(setOf("c_jee_01", "c_neet_01"))
    val enrolledCourseIds: StateFlow<Set<String>> = _enrolledCourseIds.asStateFlow()

    // Live Stream Chat Messages
    private val _liveChatMessages = MutableStateFlow(
        listOf(
            ChatMessage("msg_1", "Priya Verma", "Sir, can you re-explain the right-hand thumb rule?", "10:15 AM"),
            ChatMessage("msg_2", "Aman Sharma", "Got it sir! Option B is correct for Q3.", "10:16 AM"),
            ChatMessage("msg_3", "Dr. Alok Nath (Faculty)", "Remember: Magnetic force is perpendicular to velocity vector!", "10:17 AM", isTeacher = true),
            ChatMessage("msg_4", "Rohan Das", "Is this class recorded for offline viewing?", "10:18 AM")
        )
    )
    val liveChatMessages: StateFlow<List<ChatMessage>> = _liveChatMessages.asStateFlow()

    // Live Stream Poll
    private val _currentPoll = MutableStateFlow(
        LivePoll(
            id = "poll_101",
            question = "Which law governs the electromagnetic induction in a closed loop?",
            options = listOf("Gauss's Law", "Faraday's Law of Induction", "Ampere's Circuital Law", "Coulomb's Law"),
            votesPercent = listOf(12, 68, 14, 6),
            totalVotes = 1420,
            isActive = true
        )
    )
    val currentPoll: StateFlow<LivePoll> = _currentPoll.asStateFlow()

    // Offline Downloads & Bookmarks from Room
    val allDownloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val allAttempts: Flow<List<AttemptEntity>> = dao.getAllAttempts()

    // --- Action Handlers ---

    fun setTargetExam(exam: TargetExam) {
        _currentUser.value = _currentUser.value.copy(targetExam = exam)
    }

    fun updateUserNameAndEmail(name: String, email: String) {
        _currentUser.value = _currentUser.value.copy(
            name = if (name.isNotBlank()) name else _currentUser.value.name,
            email = if (email.isNotBlank()) email else _currentUser.value.email
        )
    }

    fun setUserRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(role = role)
    }

    fun enrollInCourse(courseId: String) {
        _enrolledCourseIds.value = _enrolledCourseIds.value + courseId
    }

    fun sendLiveChatMessage(text: String, isDoubt: Boolean = false) {
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = _currentUser.value.name,
            text = text,
            timestamp = "Just now",
            isTeacher = _currentUser.value.role == UserRole.TEACHER,
            isDoubtQuestion = isDoubt
        )
        _liveChatMessages.value = _liveChatMessages.value + newMsg
    }

    fun votePollOption(optionIndex: Int) {
        val poll = _currentPoll.value
        val updatedVotes = poll.votesPercent.toMutableList()
        updatedVotes[optionIndex] = updatedVotes[optionIndex] + 2
        _currentPoll.value = poll.copy(
            votesPercent = updatedVotes,
            totalVotes = poll.totalVotes + 1,
            activeOptionIndex = optionIndex
        )
    }

    suspend fun saveDownload(lecture: VideoLecture, courseTitle: String) {
        val entity = DownloadEntity(
            videoId = lecture.id,
            courseId = lecture.courseId,
            title = lecture.title,
            durationMinutes = lecture.durationMinutes,
            sizeMB = 180,
            localFilePath = "/storage/emulated/0/EduLive/Downloads/${lecture.id}.mp4"
        )
        dao.insertDownload(entity)
    }

    suspend fun removeDownload(videoId: String) {
        dao.deleteDownload(videoId)
    }

    suspend fun saveBookmark(title: String, subtitle: String, type: String) {
        val entity = BookmarkEntity(
            id = "bm_${System.currentTimeMillis()}",
            type = type,
            title = title,
            subtitle = subtitle
        )
        dao.insertBookmark(entity)
    }

    suspend fun saveTestAttempt(testTitle: String, score: Int, maxScore: Int, correct: Int, wrong: Int, unattempted: Int) {
        val entity = AttemptEntity(
            attemptId = "att_${System.currentTimeMillis()}",
            testId = "test_01",
            testTitle = testTitle,
            scoreObtained = score,
            maxScore = maxScore,
            correctCount = correct,
            wrongCount = wrong,
            unattemptedCount = unattempted
        )
        dao.insertAttempt(entity)
    }

    suspend fun askAIDoubt(question: String, subject: String): String {
        val exam = _currentUser.value.targetExam.displayName
        return GeminiClient.generateDoubtAnswer(question, subject, exam)
    }

    suspend fun getAIFlashcards(topic: String): List<Pair<String, String>> {
        val exam = _currentUser.value.targetExam.displayName
        return GeminiClient.generateFlashcards(topic, exam)
    }

    // --- Static Data Source Generators ---

    fun getCategories(): List<CourseCategory> {
        return listOf(
            CourseCategory("cat_jee", "JEE Main & Adv", "JEE", 42, "Functions"),
            CourseCategory("cat_neet", "NEET Medical", "NEET", 38, "Healing"),
            CourseCategory("cat_upsc", "UPSC IAS", "UPSC", 29, "AccountBalance"),
            CourseCategory("cat_ssc", "SSC & Banking", "SSC", 31, "Calculate"),
            CourseCategory("cat_gate", "GATE Computer", "GATE", 24, "Computer"),
            CourseCategory("cat_school", "Class 10th & 12th", "SCHOOL", 50, "School")
        )
    }

    fun getCoursesForExam(exam: TargetExam): List<Course> {
        return getAllCourses().filter { it.targetExam == exam || it.category == exam.categoryCode }
            .ifEmpty { getAllCourses() }
    }

    fun getAllCourses(): List<Course> {
        return listOf(
            Course(
                id = "c_jee_01",
                title = "Lakshya JEE 2026 Ultimate Batch",
                category = "JEE",
                targetExam = TargetExam.JEE_MAIN,
                rating = 4.9f,
                reviewCount = 18400,
                instructorName = "Prof. Alok Pandey & Team",
                instructorTitle = "Ex-IITian Senior Physics Faculty",
                priceOriginal = 9999,
                priceDiscounted = 3999,
                isLiveBatch = true,
                durationHours = 450,
                totalLectures = 180,
                syllabusTopics = listOf("Electrodynamics", "Calculus & Algebra", "Organic Mechanisms", "Thermodynamics"),
                bannerGradientStart = 0xFF1E1B4B,
                bannerGradientEnd = 0xFF4338CA
            ),
            Course(
                id = "c_neet_01",
                title = "Yakeen NEET 2026 Droppers Express",
                category = "NEET",
                targetExam = TargetExam.NEET_UG,
                rating = 4.95f,
                reviewCount = 24100,
                instructorName = "Dr. Shweta Roy",
                instructorTitle = "AIIMS New Delhi Gold Medalist",
                priceOriginal = 8999,
                priceDiscounted = 3499,
                isLiveBatch = true,
                durationHours = 520,
                totalLectures = 220,
                syllabusTopics = listOf("Human Physiology", "Plant Genetics", "Chemical Bonding", "Ray Optics"),
                bannerGradientStart = 0xFF064E3B,
                bannerGradientEnd = 0xFF059669
            ),
            Course(
                id = "c_upsc_01",
                title = "Sankalp UPSC CSE 2026 Prelims + Mains",
                category = "UPSC",
                targetExam = TargetExam.UPSC_CSE,
                rating = 4.88f,
                reviewCount = 12900,
                instructorName = "Vikramaditya Singh",
                instructorTitle = "Former Civil Servant & Polity Expert",
                priceOriginal = 24999,
                priceDiscounted = 11999,
                isLiveBatch = true,
                durationHours = 800,
                totalLectures = 350,
                syllabusTopics = listOf("Indian Polity & Governance", "Modern Indian History", "Macroeconomics", "Ethics & Essay Writing"),
                bannerGradientStart = 0xFF78350F,
                bannerGradientEnd = 0xFFD97706
            ),
            Course(
                id = "c_ssc_01",
                title = "SSC CGL 2026 Rankers Foundation",
                category = "SSC",
                targetExam = TargetExam.SSC_CGL,
                rating = 4.8f,
                reviewCount = 8900,
                instructorName = "Rajesh Verma",
                instructorTitle = "10+ Yrs Quant & Reasoning Guru",
                priceOriginal = 4999,
                priceDiscounted = 1999,
                isLiveBatch = false,
                durationHours = 300,
                totalLectures = 140,
                syllabusTopics = listOf("Quantitative Aptitude", "Logical Reasoning", "English Comprehension", "General Awareness"),
                bannerGradientStart = 0xFF311B92,
                bannerGradientEnd = 0xFF673AB7
            )
        )
    }

    private val _liveSessions = MutableStateFlow(
        listOf(
            LiveSession(
                id = "live_jee_01",
                title = "Rotational Mechanics: Moment of Inertia & Rolling without Slip",
                courseTitle = "Lakshya JEE 2026 Batch",
                instructorName = "Prof. Alok Pandey",
                targetExam = TargetExam.JEE_MAIN,
                status = "LIVE NOW",
                viewerCount = 184,
                startTimeFormatted = "Started at 10:00 AM",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                streamKey = "jee_physics_101",
                shareLink = "https://edulive.app/class/jee_physics_101",
                maxStudentsCapacity = 200,
                description = "Master rotational motion formulas with live problem solving and instant faculty doubt clearance."
            ),
            LiveSession(
                id = "live_neet_02",
                title = "Human Heart Architecture & Cardiac Cycle In-Depth",
                courseTitle = "Yakeen NEET 2026 Batch",
                instructorName = "Dr. Shweta Roy",
                targetExam = TargetExam.NEET_UG,
                status = "LIVE NOW",
                viewerCount = 196,
                startTimeFormatted = "Started at 10:15 AM",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                streamKey = "neet_bio_202",
                shareLink = "https://edulive.app/class/neet_bio_202",
                maxStudentsCapacity = 250,
                description = "Complete 3D anatomical walkthrough of cardiac muscle contraction and ECG signal analysis."
            ),
            LiveSession(
                id = "live_upsc_03",
                title = "Constitutional Amendments & Basic Structure Doctrine Seminar",
                courseTitle = "Sankalp UPSC CSE 2026",
                instructorName = "Vikramaditya Singh",
                targetExam = TargetExam.UPSC_CSE,
                status = "UPCOMING",
                viewerCount = 0,
                startTimeFormatted = "Today at 02:30 PM",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                streamKey = "upsc_polity_303",
                shareLink = "https://edulive.app/class/upsc_polity_303",
                maxStudentsCapacity = 300,
                description = "Special Live Seminar covering Kesavananda Bharati case landmark rulings and Indian polity Mains Q&A."
            )
        )
    )
    val liveSessionsFlow: StateFlow<List<LiveSession>> = _liveSessions.asStateFlow()

    fun getLiveSessions(): List<LiveSession> = _liveSessions.value

    suspend fun persistLiveSessionToDb(session: LiveSession) {
        val entity = LiveSessionEntity(
            id = session.id,
            title = session.title,
            courseTitle = session.courseTitle,
            instructorName = session.instructorName,
            targetExamName = session.targetExam.name,
            status = session.status,
            viewerCount = session.viewerCount,
            startTimeFormatted = session.startTimeFormatted,
            streamUrl = session.streamUrl,
            streamKey = session.streamKey,
            shareLink = session.shareLink,
            maxStudentsCapacity = session.maxStudentsCapacity,
            description = session.description
        )
        dao.insertLiveSession(entity)
    }

    fun scheduleNewLiveSession(
        title: String,
        subjectCourse: String,
        instructor: String,
        exam: TargetExam,
        timeString: String,
        streamUrl: String,
        maxCapacity: Int,
        description: String
    ): LiveSession {
        val cleanKey = "class_" + System.currentTimeMillis().toString().takeLast(6)
        val shareUrl = "https://edulive.app/class/$cleanKey"
        val newSession = LiveSession(
            id = "live_$cleanKey",
            title = title,
            courseTitle = subjectCourse,
            instructorName = instructor,
            targetExam = exam,
            status = if (timeString.contains("NOW", ignoreCase = true) || timeString.isBlank()) "LIVE NOW" else "UPCOMING",
            viewerCount = if (timeString.contains("NOW", ignoreCase = true)) 1 else 0,
            startTimeFormatted = if (timeString.isNotBlank()) timeString else "Started Just Now",
            streamUrl = if (streamUrl.isNotBlank()) streamUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            streamKey = cleanKey,
            shareLink = shareUrl,
            maxStudentsCapacity = maxCapacity,
            description = description.ifBlank { "Live online class seminar scheduled on EduLive+ platform." }
        )
        _liveSessions.value = listOf(newSession) + _liveSessions.value
        return newSession
    }

    fun findOrJoinSessionByLink(linkOrCode: String): LiveSession? {
        val cleanQuery = linkOrCode.trim()
            .removePrefix("https://edulive.app/class/")
            .removePrefix("edulive://live/")
            .lowercase()

        // 1. Search existing sessions
        val matched = _liveSessions.value.find {
            it.id.lowercase() == cleanQuery ||
            it.streamKey.lowercase() == cleanQuery ||
            it.shareLink.lowercase().contains(cleanQuery)
        }

        if (matched != null) return matched

        // 2. If it's a URL or new link, construct a live stream room dynamically
        val customSession = LiveSession(
            id = "live_custom_${System.currentTimeMillis().toString().takeLast(5)}",
            title = "Shared Seminar / Online Class ($cleanQuery)",
            courseTitle = "Joined via Link / Code",
            instructorName = "EduLive Faculty Host",
            targetExam = _currentUser.value.targetExam,
            status = "LIVE NOW",
            viewerCount = 198,
            startTimeFormatted = "Joined via Direct Link",
            streamUrl = if (cleanQuery.startsWith("http")) cleanQuery else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            streamKey = cleanQuery,
            shareLink = if (cleanQuery.startsWith("http")) cleanQuery else "https://edulive.app/class/$cleanQuery",
            maxStudentsCapacity = 200,
            description = "Joined live seminar room via direct URL or class access code."
        )

        _liveSessions.value = listOf(customSession) + _liveSessions.value
        return customSession
    }

    fun getLecturesForCourse(courseId: String): List<VideoLecture> {
        return listOf(
            VideoLecture("v_101", courseId, "Lecture 01: Vectors & Resolution in 3D Space", 58, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", isFreePreview = true),
            VideoLecture("v_102", courseId, "Lecture 02: Kinematics 2D - Projectile Motion", 64, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
            VideoLecture("v_103", courseId, "Lecture 03: Newton's Laws of Motion & Friction", 72, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
            VideoLecture("v_104", courseId, "Lecture 04: Work, Energy & Power Theorem", 60, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
            VideoLecture("v_105", courseId, "Lecture 05: Center of Mass & Impulse Conservation", 68, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4")
        )
    }

    fun getSampleTestSeries(): TestSeries {
        return TestSeries(
            id = "ts_jee_full_01",
            title = "JEE Main 2026 All India Full Mock Test #04",
            targetExam = TargetExam.JEE_MAIN,
            durationMinutes = 180,
            totalQuestions = 5,
            totalMarks = 20,
            questions = listOf(
                MockQuestion(
                    id = "q1",
                    questionText = "A particle moves in a circle of radius R with constant speed v. What is the magnitude of average acceleration during half a revolution?",
                    options = listOf("2v² / (πR)", "v² / R", "πv² / (2R)", "Zero"),
                    correctOptionIndex = 0,
                    explanation = "Average acceleration = Δv / Δt. Δv = v - (-v) = 2v. Time Δt = πR/v. Hence avg acceleration = 2v / (πR/v) = 2v² / (πR).",
                    topic = "Circular Motion"
                ),
                MockQuestion(
                    id = "q2",
                    questionText = "In an AC circuit containing an inductor L and capacitor C in series, resonance occurs at frequency f₀ equal to:",
                    options = listOf("1 / (2π√(LC))", "2π / √(LC)", "√(LC) / 2π", "1 / (LC)"),
                    correctOptionIndex = 0,
                    explanation = "At resonance, inductive reactance XL = XC => ωL = 1/(ωC) => ω² = 1/(LC) => f₀ = 1 / (2π√(LC)).",
                    topic = "Alternating Current"
                ),
                MockQuestion(
                    id = "q3",
                    questionText = "The value of integral ∫ (x² * e^x) dx is equal to:",
                    options = listOf("e^x * (x² - 2x + 2) + C", "e^x * (x² + 2x - 2) + C", "x² * e^x - 2x + C", "e^x / (x + 1) + C"),
                    correctOptionIndex = 0,
                    explanation = "Using Integration by Parts twice: ∫ x² e^x dx = x² e^x - 2 ∫ x e^x dx = x² e^x - 2(x e^x - e^x) + C = e^x (x² - 2x + 2) + C.",
                    topic = "Integral Calculus"
                ),
                MockQuestion(
                    id = "q4",
                    questionText = "Which of the following molecules has zero dipole moment?",
                    options = listOf("BF₃ (Boron Trifluoride)", "NH₃ (Ammonia)", "H₂O (Water)", "SO₂ (Sulfur Dioxide)"),
                    correctOptionIndex = 0,
                    explanation = "BF₃ has trigonal planar geometry with 120° bond angles. The vector sum of three B-F bond dipoles cancels out to zero.",
                    topic = "Chemical Bonding"
                ),
                MockQuestion(
                    id = "q5",
                    questionText = "Under Article 32 of the Indian Constitution, who has the power to issue Writs for enforcement of Fundamental Rights?",
                    options = listOf("Supreme Court of India", "High Courts only", "District Courts", "Prime Minister's Office"),
                    correctOptionIndex = 0,
                    explanation = "Article 32 guarantees the right to move the Supreme Court for enforcement of Fundamental Rights via Habeas Corpus, Mandamus, Prohibition, Quo-Warranto, and Certiorari.",
                    topic = "Indian Polity"
                )
            )
        )
    }

    fun getCommunityPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "post_1",
                authorName = "Ananya Roy",
                authorRole = "JEE Aspirant",
                targetExam = TargetExam.JEE_MAIN,
                title = "Short Tricks for Integration of Rational Functions?",
                content = "Does anyone have concise notes or partial fraction shortcuts for quadratic denominators?",
                upvotes = 142,
                commentCount = 18,
                verifiedAnswer = "Faculty Tip: Use the derivative derivative test or standard formula ∫ dx/(x²+a²) = (1/a) arctan(x/a) + C.",
                timestamp = "2 hours ago"
            ),
            CommunityPost(
                id = "post_2",
                authorName = "Karan Sharma",
                authorRole = "NEET Ranker",
                targetExam = TargetExam.NEET_UG,
                title = "Important NCERT Diagrams to memorize for NEET Biology 2026",
                content = "Compiled a list of top 25 high-probability diagrams from Genetics and Human Physiology.",
                upvotes = 320,
                commentCount = 45,
                verifiedAnswer = "Verified by Dr. Shweta: Make sure to review the Krebs cycle mitochondrial matrix diagram!",
                timestamp = "5 hours ago"
            )
        )
    }

    fun getInvoices(): List<ERPInvoice> {
        return listOf(
            ERPInvoice("INV-2026-9012", "Rishabh Kumar", "Lakshya JEE 2026 Ultimate Batch", 3389, 610, 3999, "24 Jul 2026"),
            ERPInvoice("INV-2026-8814", "Rishabh Kumar", "Yakeen NEET 2026 Express", 2965, 534, 3499, "10 Jun 2026")
        )
    }
}
