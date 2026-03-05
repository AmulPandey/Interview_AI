package com.example.interviewai.data.model


data class AnalysisResult(
    val overallScore: Float,
    val confidenceScore: Float,
    val grammarScore: Float,
    val relevanceScore: Float,
    val keywordScore: Float,
    val keywordsMatched: List<String>,
    val keywordsMissed: List<String>,
    val grammarIssues: List<String>,
    val feedback: String,
    val improvedAnswer: String
)