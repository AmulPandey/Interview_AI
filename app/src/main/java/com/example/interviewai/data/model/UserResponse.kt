package com.example.interviewai.data.model


data class UserResponse(
          val id: String = "",
          val name: String = "",
          val email: String = "",
          val targetRole: String = "",
          val role: String = "",
          val profilePicUrl: String? = null,
          val createdAt: String = ""
)
