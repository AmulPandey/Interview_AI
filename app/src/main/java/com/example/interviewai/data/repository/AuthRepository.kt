package com.example.interviewai.data.repository



import com.example.interviewai.data.model.AuthResponse
import com.example.interviewai.data.model.LoginRequest
import com.example.interviewai.data.model.RegisterRequest
import com.example.interviewai.data.remote.NetworkResult

interface AuthRepository {
    suspend fun login(request: LoginRequest): NetworkResult<AuthResponse>
    suspend fun register(request: RegisterRequest): NetworkResult<AuthResponse>
    suspend fun logout()
    suspend fun refreshToken(): NetworkResult<AuthResponse>
}