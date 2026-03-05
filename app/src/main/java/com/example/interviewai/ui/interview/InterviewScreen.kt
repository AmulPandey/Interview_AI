package com.example.interviewai.ui.interview


import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.gson.Gson
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*
import java.net.URLEncoder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.interviewai.R
import kotlin.collections.getOrNull

@Composable
fun InterviewScreen(
    navController: NavController,
    category: String,
    difficulty: String,
    viewModel: InterviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ← Only load questions if not already loaded
    // This prevents re-fetching when popping back from FeedbackScreen
    LaunchedEffect(Unit) {
        if (state.questions.isEmpty()) {
            println(">>> Loading questions for first time")
            viewModel.loadQuestions(category, difficulty)
        } else {
            println(">>> Questions already loaded (${state.questions.size}), skipping API call")
        }
    }

    InterviewContent(navController, viewModel)
}

// In InterviewScreen.kt — extract the content into this:

@Composable
fun InterviewContent(
    navController: NavController,
    viewModel: InterviewViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startListening(context) }

    LaunchedEffect(state.result) {
        state.result?.let { result ->
            val json = java.net.URLEncoder.encode(Gson().toJson(result), "UTF-8")
            navController.navigate(Screen.Feedback.createRoute(json)) {
                // Don't pop InterviewScreen — keep it in backstack for "Next"
                launchSingleTop = true
            }
        }
    }

    Scaffold(containerColor = SurfaceDark) { padding ->
        when {
            state.isLoading   -> FullScreenLoader(stringResource(R.string.loading_question))
            state.isAnalyzing -> FullScreenLoader(stringResource(R.string.analyzing_your_answer))
            state.questions.isEmpty() -> FullScreenError(state.error ?: stringResource(R.string.no_questions_found))
            else -> {
                val question = state.questions.getOrNull(state.currentIndex)
                if (question != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))
                        ProgressHeader(
                            current = state.currentIndex + 1,
                            total   = state.questions.size,
                            elapsed = state.elapsedSeconds
                        )
                        Spacer(Modifier.height(24.dp))
                        QuestionCard(question.text, question.category, question.difficulty)
                        Spacer(Modifier.height(20.dp))
                        AnswerInput(
                            text          = state.answerText,
                            partialSpeech = state.partialSpeech,
                            isListening   = state.isListening,
                            onTextChange  = viewModel::updateAnswerText,
                            onMicClick    = {
                                if (state.isListening) viewModel.stopListening()
                                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick  = viewModel::submitAnswer,
                            enabled  = state.answerText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(bottom = 8.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(stringResource(R.string.submit_answer), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ProgressHeader(current: Int, total: Int, elapsed: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Question counter
        Text(
            stringResource(R.string.q, current, total),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        // Timer
        Surface(
            color = SurfaceMid,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                val min = elapsed / 60
                val sec = elapsed % 60
                Text(
                    stringResource(R.string._02d_02d).format(min, sec),
                    color = AccentOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    LinearProgressIndicator(
        progress = { current.toFloat() / total },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = PrimaryBlue,
        trackColor = SurfaceLight
    )
}

@Composable
private fun QuestionCard(text: String, category: String, difficulty: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row {
                Badge(containerColor = PrimaryBlue.copy(alpha = 0.2f)) {
                    Text(category, color = PrimaryBlue, fontSize = 11.sp)
                }
                Spacer(Modifier.width(8.dp))
                val diffColor = when (difficulty) {
                    "EASY"  -> ScoreGood
                    "HARD"  -> ScoreBad
                    else    -> ScoreMid
                }
                Badge(containerColor = diffColor.copy(alpha = 0.2f)) {
                    Text(difficulty, color = diffColor, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text, color = TextPrimary, fontSize = 17.sp, lineHeight = 26.sp)
        }
    }
}

@Composable
private fun AnswerInput(
    text: String,
    partialSpeech: String,
    isListening: Boolean,
    onTextChange: (String) -> Unit,
    onMicClick: () -> Unit
) {
    val pulseAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(600), RepeatMode.Reverse
        ),
        label = stringResource(R.string.scale)
    )

    Column {
        OutlinedTextField(
            value = text + if (isListening && partialSpeech.isNotEmpty()) " $partialSpeech..." else "",
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.type_your_answer_or_use_the_mic),
                    color = TextSecondary
                )},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryBlue,
                unfocusedBorderColor = SurfaceLight,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = PrimaryBlue
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Mic button
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(56.dp)
                    .scale(if (isListening) pulseAnim else 1f)
                    .background(
                        if (isListening) AccentRed else PrimaryBlue,
                        CircleShape
                    )
            ) {
                Icon(
                    if (isListening) Icons.Default.Close else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            AnimatedVisibility(isListening) {
                Text(stringResource(R.string.listening), color = AccentRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}



@Composable
private fun FullScreenLoader(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryBlue)
            Spacer(Modifier.height(16.dp))
            Text(message, color = TextSecondary)
        }
    }
}

@Composable
private fun FullScreenError(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = AccentRed)
    }
}

// needed for clip
fun Modifier.clip(shape: androidx.compose.ui.graphics.Shape) =
    this.then(Modifier.clip(shape))