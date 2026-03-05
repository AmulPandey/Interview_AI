package com.example.interviewai.data.model

data class ResumeQuestions(
    val resumeId: String,
    val userId: String,
    val questions: List<GeneratedQuestion>
)

data class GeneratedQuestion(
    val id: String? = null,                    // ← nullable from JSON
    val text: String = "",
    val category: String = "",
    val difficulty: String = "MEDIUM",
    val source: String = "",
    val expectedKeywords: List<String> = emptyList(),
    val sampleAnswer: String = ""
) {
    // Safe id — never null, never blank
    val safeId: String get() = id
        ?.takeIf { it.isNotBlank() }
        ?: text.hashCode().toString()   // ← use question text hash as fallback
}

