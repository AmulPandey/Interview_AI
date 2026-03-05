package com.example.interviewai.domain.usecase


import com.example.interviewai.data.repository.ResumeRepository
import javax.inject.Inject

class GetResumeQuestionsUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(resumeId: String, count: Int = 10) =
        repository.getResumeQuestions(resumeId, count)
}