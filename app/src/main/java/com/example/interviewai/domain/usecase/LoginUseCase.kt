package com.example.interviewai.domain.usecase


import com.example.interviewai.data.model.AuthResponse
import com.example.interviewai.data.model.LoginRequest
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<AuthResponse> =
        authRepository.login(LoginRequest(email, password))
}