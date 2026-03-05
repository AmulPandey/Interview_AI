package com.example.interviewai.data.repository

import com.example.interviewai.data.model.*
import com.example.interviewai.data.remote.ApiService
import com.example.interviewai.data.remote.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(
    private val api: ApiService
) : InterviewRepository {

    override suspend fun getQuestions(
        category: String?,
        difficulty: String?
    ): NetworkResult<CategoryQuestionsResponse> {
        println(">>> InterviewRepository.getQuestions: category=$category, difficulty=$difficulty")
        return safeApiCall {
            println(">>> Calling API: /interview/questions?category=$category&difficulty=$difficulty")
            val response = api.getQuestions(category, difficulty)
            println(">>> API response code: ${response.code()}")
            response
        }
    }

    // Add to InterviewRepositoryImpl

    override suspend fun generateQuestions(
        category: String,
        difficulty: String
    ): NetworkResult<CategoryQuestionsResponse> {
        println(">>> generateQuestions: category=$category, difficulty=$difficulty")
        return safeApiCall {
            api.generateQuestions(category, difficulty)
        }
    }

    override suspend fun analyzeAnswer(
        request: AnswerRequest
    ): NetworkResult<AnalysisResult> = safeApiCall {
        api.analyzeAnswer(request)
    }

    private suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    println(">>> API returned success but body is NULL")
                    NetworkResult.Error("Empty response body")
                } else {
                    NetworkResult.Success(body)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println(">>> API error: code=${response.code()}, body=$errorBody")
                NetworkResult.Error(errorBody ?: response.message(), response.code())
            }
        } catch (e: Exception) {
            println(">>> API exception: ${e.javaClass.simpleName}: ${e.message}")
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}