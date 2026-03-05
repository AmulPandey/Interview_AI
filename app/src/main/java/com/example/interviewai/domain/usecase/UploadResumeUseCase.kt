package com.example.interviewai.domain.usecase



import com.example.interviewai.data.repository.ResumeRepository
import javax.inject.Inject

class UploadResumeUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(bytes: ByteArray, name: String, mime: String) =
        repository.uploadResume(bytes, name, mime)
}