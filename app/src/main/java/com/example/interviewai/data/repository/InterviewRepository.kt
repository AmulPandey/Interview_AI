package com.example.interviewai.data.repository


import com.example.interviewai.data.model.UserProgress
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.data.model.*
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    suspend fun getQuestions(category: String?, difficulty: String?): NetworkResult<CategoryQuestionsResponse>
    suspend fun analyzeAnswer(request: AnswerRequest): NetworkResult<AnalysisResult>

    // Add to InterviewRepository interface
    suspend fun generateQuestions(
        category: String,
        difficulty: String
    ): NetworkResult<CategoryQuestionsResponse>
}