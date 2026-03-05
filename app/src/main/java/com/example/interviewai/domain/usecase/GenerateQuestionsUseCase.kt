package com.example.interviewai.domain.usecase



import com.example.interviewai.data.repository.InterviewRepository
import javax.inject.Inject

class GenerateQuestionsUseCase @Inject constructor(
    private val repository: InterviewRepository
) {
    suspend operator fun invoke(
        category: String,
        difficulty: String
    ) = repository.generateQuestions(category, difficulty)
}