package com.example.interviewai.data.repository


import com.example.interviewai.data.model.ResumeQuestions
import com.example.interviewai.data.model.ResumeUploadResponse
import com.example.interviewai.data.remote.ApiService
import com.example.interviewai.data.remote.NetworkResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ResumeRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ResumeRepository {

    override suspend fun uploadResume(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): NetworkResult<ResumeUploadResponse> {
        return try {
            val requestBody = fileBytes.toRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = api.uploadResume(part)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error(response.message(), response.code())
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Upload failed")
        }
    }

    override suspend fun getResumeQuestions(
        resumeId: String,
        count: Int
    ): NetworkResult<ResumeQuestions> {
        return try {
            val response = api.getResumeQuestions(resumeId, count)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error(response.message(), response.code())
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to get questions")
        }
    }
}