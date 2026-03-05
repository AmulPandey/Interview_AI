package com.example.interviewai.data.model

data class UserStats(
     val totalSessions: Int = 0,
     val avgScore: Float = 0f,
     val currentStreak: Int = 0,
     val totalQuestions: Int = 0,
     val bestScore: Float = 0f,
     val thisWeekSessions: Int = 0
)