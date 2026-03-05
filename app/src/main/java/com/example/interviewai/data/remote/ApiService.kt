package com.example.interviewai.data.remote

import com.example.interviewai.data.model.AnalysisResult
import com.example.interviewai.data.model.AnswerRequest
import com.example.interviewai.data.model.AuthResponse
import com.example.interviewai.data.model.CategoryQuestionsResponse
import com.example.interviewai.data.model.GeneratedQuestion
import com.example.interviewai.data.model.LoginRequest
import com.example.interviewai.data.model.ProfilePicResponse
import com.example.interviewai.data.model.RegisterRequest
import com.example.interviewai.data.model.ResumeQuestions
import com.example.interviewai.data.model.ResumeUploadResponse
import com.example.interviewai.data.model.UpdateProfileRequest
import com.example.interviewai.data.model.UserProgress
import com.example.interviewai.data.model.UserResponse
import com.example.interviewai.data.model.UserStats
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("interview/questions")
    suspend fun getQuestions(
        @Query("category") category: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<CategoryQuestionsResponse>


    @POST("answer/analyze")
    suspend fun analyzeAnswer(
        @Body request: AnswerRequest
    ): Response<AnalysisResult>


    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Query("token") token: String): Response<AuthResponse>

    @Multipart
    @POST("resume/upload")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part
    ): Response<ResumeUploadResponse>

    @GET("interview/generate")
    suspend fun generateQuestions(
        @Query("category")   category: String,
        @Query("difficulty") difficulty: String,
        @Query("count")      count: Int = 10
    ): Response<CategoryQuestionsResponse>

    @GET("resume/questions/{resumeId}")
    suspend fun getResumeQuestions(
        @Path("resumeId") resumeId: String,
        @Query("count") count: Int = 10
    ): Response<ResumeQuestions>

    @GET("user/me")
    suspend fun getMe(): Response<UserResponse>

    @PUT("user/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @DELETE("user/me")
    suspend fun deleteAccount(): Response<Unit>

    @Multipart
    @POST("user/me/picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): Response<ProfilePicResponse>


    @GET("user/me/stats")
    suspend fun getMyStats(): Response<UserStats>

    @GET("user/me/progress")
    suspend fun getMyProgress(): Response<UserProgress>


}