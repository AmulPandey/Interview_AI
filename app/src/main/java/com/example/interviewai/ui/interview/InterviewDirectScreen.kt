package com.example.interviewai.ui.interview


import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.gson.Gson
import com.example.interviewai.data.model.GeneratedQuestion
import com.example.interviewai.ui.navigation.Screen
import java.net.URLDecoder

// Thin wrapper — converts GeneratedQuestion to Question and reuses InterviewScreen logic
@Composable
fun InterviewDirectScreen(
    navController: NavController,
    questionJson: String,
    viewModel: InterviewViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val decoded = remember {
        URLDecoder.decode(questionJson, "UTF-8")
    }

    val generatedQuestion = remember {
        Gson().fromJson(decoded, GeneratedQuestion::class.java)
    }

    // Inject as single-question session
    LaunchedEffect(Unit) {
        viewModel.loadSingleQuestion(generatedQuestion)
        viewModel.startTimer()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.result) {
        state.result?.let { result ->
            val json = java.net.URLEncoder.encode(Gson().toJson(result), "UTF-8")
            navController.navigate(Screen.Feedback.createRoute(json))
        }
    }

    // Reuse same InterviewScreen UI
    InterviewContent(
        navController = navController,
        viewModel     = viewModel
    )
}