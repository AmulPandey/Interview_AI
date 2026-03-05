package com.example.interviewai.data.model

data class AnswerRequest(
    val questionId: String = java.util.UUID.randomUUID().toString(), // ← default, never null
    val answerText: String = "",
    val userId: String = "",
    val durationSeconds: Int = 0,
    val questionText: String = "",
    val sampleAnswer: String = "",
    val expectedKeywords: List<String> = emptyList(),
    val category: String = "General"
)