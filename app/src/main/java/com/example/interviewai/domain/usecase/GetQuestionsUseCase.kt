package com.example.interviewai.domain.usecase

import com.example.interviewai.data.model.CategoryQuestionsResponse
import com.example.interviewai.data.model.GeneratedQuestion
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.data.repository.InterviewRepository
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: InterviewRepository
) {
    suspend operator fun invoke(
        category: String? = null,
        difficulty: String? = null,
        limit: Int = 10
    ): NetworkResult<CategoryQuestionsResponse> {
        println(">>> GetQuestionsUseCase: category=$category, difficulty=$difficulty")
        return repository.getQuestions(category, difficulty)
    }
}
