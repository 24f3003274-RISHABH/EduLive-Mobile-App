# 🎓 EduLive+ | EdTech & Live Online Learning Android App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Geometric%20Balance-0061A4.svg?style=flat)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**EduLive+** is a modern, high-performance Android application built for competitive exam preparation (JEE, NEET, UPSC, SSC, CBSE). Powered by **Jetpack Compose** and structured with **Clean MVVM Architecture**, EduLive+ delivers live interactive classes, AI-powered instant doubt resolution, community study notes, adaptive practice test series, and comprehensive multi-role management (Student, Teacher, Parent).

---

## 🎨 Visual Identity & "Geometric Balance" Design System

EduLive+ strictly adheres to **Material Design 3** and features the **Geometric Balance** design theme:
- **Primary Indigo & Deep Ocean (`#0061A4` / `#D1E4FF`)**: Balanced headers, rounded geometric cards (24dp–28dp border radii), and continuous progress meters.
- **Secondary Royal Lavender (`#6750A4` / `#EADDFF`)**: Accent hubs for practice tests, doubt solvers, and interactive tools.
- **High-Contrast Dark & Light Surfaces (`#F8F9FF` background)**: Crisp typography hierarchy paired with dynamic status badges (`LIVE` red, success green).
- **Responsive Layouts**: Full Edge-to-Edge compliance with flexible `WindowInsets` and Material 3 Navigation Rails/Bars.

---

## ✨ Key Features

- 📹 **Live Classes & Interactive Chat**: Low-latency live video player simulation, live chat stream, live polling, raise hand, and PDF lecture notes downloads.
- 🤖 **AI-Powered Instant Doubt Solver**: Voice and image prompt support to instantly resolve complex Physics, Chemistry, and Math queries using AI.
- 📚 **Course Catalog & Enrollment**: Target exam filters (JEE Advanced, NEET UG, UPSC CSE, CBSE 12th, SSC CGL), batch enrollment, structured syllabus roadmaps, and pricing/discount badges.
- 📝 **Adaptive Test Series**: Real-time timer-based mock tests, dynamic question navigator grid, review flags, and detailed score breakdown with subject-wise analytics.
- 📑 **Community Study Notes & Resources**: Student & faculty note sharing, chapter-wise PDF downloads, upvoting system, and bookmarks.
- 👥 **Multi-Role View Switcher**:
  - **Student View**: Dashboard, progress tracking, coin rewards, and ongoing courses.
  - **Faculty View**: Live session host launcher, student engagement stats, revenue analytics, and batch management.
  - **Parent View**: Real-time attendance monitoring, test performance reports, and direct faculty feedback.

---

## 🏗️ Architecture & Project Structure

The project follows standard Android **MVVM (Model-View-ViewModel)** with Unidirectional Data Flow (UDF) powered by Kotlin `StateFlow`.

```
com.example
├── MainActivity.kt                  # Single Activity host with Navigation Compose
├── data
│   ├── local                        # Room Database Entities & DAOs
│   ├── model                        # Domain Data Models (User, Course, Test, Note)
│   ├── remote                       # Retrofit API Services & Gemini AI Client
│   └── repository                   # Educational Repository (Data Source Abstraction)
└── ui
    ├── components                   # Reusable M3 Composable UI Components
    │   ├── EduBottomBar.kt          # Adaptive M3 NavigationBar with live badges
    │   └── EduTopAppBar.kt          # App bar with Exam Selector & Role Switcher
    ├── screens                      # Feature Screen Composables
    │   ├── HomeScreen.kt            # Dashboard, Continue Learning Hero & Quick Actions
    │   ├── LiveClassScreen.kt       # Live streaming, chat & polling interface
    │   ├── CourseExplorerScreen.kt  # Course discovery, category filter & enrollment
    │   ├── AIDoubtSolverScreen.kt   # AI tutor chat with voice/image upload
    │   ├── TestSeriesScreen.kt      # Exam engine, timer & result analytics
    │   ├── CommunityNotesScreen.kt  # Study material feed & notes uploader
    │   └── DashboardScreen.kt       # Faculty & Parent analytical dashboards
    ├── theme                        # M3 Geometric Balance Palette, Typography & Shapes
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── viewmodel                    # EduViewModel managing UI State Flow
```

---

## 🛠️ Tech Stack & Dependencies

- **Language**: [Kotlin](https://kotlinlang.org/) (1.9+)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM + Unidirectional Data Flow (UDF)
- **State Management**: `StateFlow`, `MutableStateFlow`, `collectAsStateWithLifecycle()`
- **Navigation**: Navigation Compose with Type-Safe Routing
- **Async Operations**: Kotlin Coroutines & Flow
- **Data Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP
- **Networking & Serialization**: Retrofit, Ktor Client & `kotlinx.serialization`
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/)
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) & Version Catalog (`libs.versions.toml`)

---

## 🚀 Setup & Local Installation

### Prerequisites
- **Android Studio**: Jellyfish (2023.3.1) or newer
- **JDK**: Version 17
- **Android SDK**: API Level 34 (Android 14.0)
- **Minimum Android Device SDK**: API Level 24 (Android 7.0)

### Steps to Run Locally

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/EduLiveApp.git
   cd EduLiveApp
   ```

2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **Open an Existing Project** and navigate to the cloned `EduLiveApp` root directory.

3. **Sync Gradle**:
   - Wait for Android Studio to automatically index files and sync Gradle dependencies.
   - If prompted, click **Sync Project with Gradle Files**.

4. **Build & Run**:
   - Select an Android Emulator (API 24+) or connect a physical device with USB Debugging enabled.
   - Click the green **Run** button (`Shift + F10`) or execute:
     ```bash
     ./gradlew assembleDebug
     ```

5. **Run Unit Tests**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
