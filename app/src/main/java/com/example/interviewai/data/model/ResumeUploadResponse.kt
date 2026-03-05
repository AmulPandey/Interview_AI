package com.example.interviewai.data.model


data class ResumeUploadResponse(
    val resumeId: String,
    val fileName: String,
    val skillsExtracted: List<String>,
    val experienceYears: Int,
    val jobTitle: String,
    val projectsFound: Int,
    val message: String
)