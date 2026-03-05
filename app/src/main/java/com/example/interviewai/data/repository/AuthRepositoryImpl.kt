package com.example.interviewai.data.repository


import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.data.model.AuthResponse
import com.example.interviewai.data.model.LoginRequest
import com.example.interviewai.data.model.RegisterRequest
import com.example.interviewai.data.remote.ApiService
import com.example.interviewai.data.remote.NetworkResult
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(request: LoginRequest): NetworkResult<AuthResponse> =
        safeAuthCall { api.login(request) }

    override suspend fun register(request: RegisterRequest): NetworkResult<AuthResponse> =
        safeAuthCall { api.register(request) }

    override suspend fun logout() {
        tokenDataStore.clear()
    }

    override suspend fun refreshToken(): NetworkResult<AuthResponse> {
        val token = tokenDataStore.refreshToken.firstOrNull()
            ?: return NetworkResult.Error("No refresh token")
        return safeAuthCall { api.refreshToken(token) }
    }

    private suspend fun safeAuthCall(
        call: suspend () -> retrofit2.Response<AuthResponse>
    ): NetworkResult<AuthResponse> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()!!
                // Persist tokens immediately
                tokenDataStore.saveTokens(body.accessToken, body.refreshToken)
                tokenDataStore.saveUser(
                    id            = body.user.id,
                    name          = body.user.name,
                    email         = body.user.email,
                    targetRole    = body.user.targetRole,
                    profilePicUrl = body.user.profilePicUrl
                )
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}