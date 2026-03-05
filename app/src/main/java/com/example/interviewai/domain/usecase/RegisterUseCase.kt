package com.example.interviewai.domain.usecase


import com.example.interviewai.data.model.AuthResponse
import com.example.interviewai.data.model.RegisterRequest
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.data.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        targetRole: String
    ): NetworkResult<AuthResponse> =
        authRepository.register(RegisterRequest(name, email, password, targetRole))
}