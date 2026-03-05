package com.example.interviewai.domain.usecase


import com.example.interviewai.data.model.AnalysisResult
import com.example.interviewai.data.model.AnswerRequest
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.data.repository.InterviewRepository
import javax.inject.Inject

class AnalyzeAnswerUseCase @Inject constructor(
    private val repository: InterviewRepository
) {
    suspend operator fun invoke(request: AnswerRequest): NetworkResult<AnalysisResult> =
        repository.analyzeAnswer(request)
}