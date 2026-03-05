package com.example.interviewai.data.model




data class UserProgress(
         val stats: UserStats = UserStats(),
         val categoryBreakdown: Map<String, Float> = emptyMap(),
         val recentScores: List<Float> = emptyList(),
         val totalAttempts: Int = 0,
         val averageScore: Float = 0f,
         val streak: Int = 0,
         val scoreHistory: List<ScoreHistoryEntry> = emptyList()
)

data class ScoreHistoryEntry(
     val date: String = "",
     val score: Float = 0f,
     val category: String = ""
)