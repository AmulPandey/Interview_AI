package com.example.interviewai.data.model


data class CategoryQuestionsResponse(
    val category: String,
    val difficulty: String,
    val questions: List<GeneratedQuestion>
)