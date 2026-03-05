package com.example.interviewai.data.repository



import com.example.interviewai.data.model.ResumeQuestions
import com.example.interviewai.data.model.ResumeUploadResponse
import com.example.interviewai.data.remote.NetworkResult
import kotlinx.coroutines.flow.Flow

interface ResumeRepository {
    suspend fun uploadResume(fileBytes: ByteArray, fileName: String, mimeType: String): NetworkResult<ResumeUploadResponse>
    suspend fun getResumeQuestions(resumeId: String, count: Int): NetworkResult<ResumeQuestions>
}