# InterviewAI 🎯
An AI-powered Android application designed to help developers ace technical interviews through 
personalized practice sessions, real-time speech recognition, and intelligent feedback.

## Features

### Practice Modes
- **General Practice**: Category-based questions across Android, Backend, ML, System Design & DSA
- **Resume Interview**: Upload your CV and get AI-generated questions tailored to your experience
- **Difficulty Levels**: Easy, Medium, and Hard question sets

### AI-Powered Analysis
- Real-time answer analysis powered by **Gemini AI**
- Detailed score breakdown: Confidence, Grammar, Relevance & Keywords
- Improved answer suggestions after each response
- Keyword matching to highlight what you covered and missed

### Progress Tracking
- Session history with score trends
- Category-wise performance breakdown
- Streak tracking to maintain daily practice habits
- Interactive score history chart

### User Experience
- **Speech-to-Text**: Answer questions hands-free using your microphone
- **Light & Dark Mode**: Seamless theme switching with persistence
- **Multi-language Support**: English & Hindi (हिंदी)
- **Poppins Typography**: Clean, modern UI throughout

### Profile & Settings
- Profile management with picture upload
- Target role customization
- Notification, sound, and auto-submit preferences
- Secure JWT authentication with silent token refresh

## **Screenshots**

<p align="center">
<img alt="Overview1"  src="https://github.com/AmulPandey/Library_Bee/blob/main/app/src/main/assets/LibraryScreenshot1.jpg">
</p>
<p align="center">
<img alt="Overview2"  src="https://github.com/AmulPandey/Library_Bee/blob/main/app/src/main/assets/LibraryScreenshot2.jpg">
</p>
<p align="center">
<img alt="Overview3"  src="https://github.com/AmulPandey/Library_Bee/blob/main/app/src/main/assets/LibraryScreenshot3.jpg">
</p>
<p align="center">
<img alt="Overview4"  src="https://github.com/AmulPandey/Library_Bee/blob/main/app/src/main/assets/LibraryScreenshot4.jpg">
</p>

## **Preview**

https://github.com/user-attachments/assets/0209c225-a14e-46ca-9222-f8ee3f1152cc

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Async** | Coroutines + StateFlow |
| **Networking** | Retrofit + OkHttp |
| **Local Storage** | DataStore Preferences |
| **Image Loading** | Coil |
| **Charts** | Vico |
| **Backend** | Spring Boot (Kotlin) |
| **Database** | PostgreSQL |
| **AI** | Google Gemini API |
| **NLP Scoring** | HuggingFace Inference API |

## Architecture
```
app/
├── data/
│   ├── local/          # DataStore, TokenDataStore, Preferences
│   ├── model/          # Data classes, DTOs
│   ├── remote/         # ApiService, NetworkResult
│   └── repository/     # Repository implementations
├── di/                 # Hilt modules
├── domain/
│   └── usecase/        # Business logic use cases
├── ui/
│   ├── auth/           # Login, Register screens
│   ├── home/           # Home screen + ViewModel
│   ├── interview/      # Interview screen + ViewModel
│   ├── feedback/       # Feedback & scoring screen
│   ├── progress/       # Progress charts & history
│   ├── resume/         # Resume upload & analysis
│   ├── settings/       # Settings & profile management
│   ├── onboarding/     # Language selection
│   ├── navigation/     # NavGraph, Screen routes
│   └── theme/          # Colors, Typography, Theme
└── util/               # LocaleHelper, extensions
```

## Backend

The backend is built with **Spring Boot** and exposes REST APIs for:
- JWT Authentication (login, register, token refresh)
- Question generation via Gemini AI
- Answer analysis & scoring
- User profile & progress tracking
- Resume parsing & question generation
- Profile picture upload & serving

**Backend repo:** [interviewai-backend](https://github.com/AmulPandey/interviewai-backend)

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Backend server running (see backend repo)

### Setup

1. Clone the repository
```bash
git clone https://github.com/AmulPandey/Interview_AI.git
```

2. Add your backend URL to `local.properties`
```properties
BASE_URL=http://your-server-ip:8080/api/v1/
```

3. Build and run in Android Studio


*Coming soon*

## Roadmap

- [ ] iOS version
- [ ] Mock interview with video recording
- [ ] Peer-to-peer practice sessions
- [ ] Company-specific question banks
- [ ] Offline mode with cached questions
- [ ] Export progress as PDF report

## Contact

- **Author**: Amul Pandey
- **Email**: amulpandey007@gmail.com
- **GitHub**: [@AmulPandey](https://github.com/AmulPandey)
