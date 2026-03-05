package com.example.interviewai.data.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserSummary
)

data class UserSummary(
    val id: String,
    val name: String,
    val email: String,
    val targetRole: String,
    val profilePicUrl: String? = null
)