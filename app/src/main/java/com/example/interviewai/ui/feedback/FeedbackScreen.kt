package com.example.interviewai.ui.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.google.gson.Gson
import com.example.interviewai.data.model.AnalysisResult
import com.example.interviewai.ui.interview.InterviewViewModel
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*
import java.net.URLDecoder

@Composable
fun FeedbackScreen(
    navController: NavController,
    resultJson: String,
    // Share the SAME InterviewViewModel instance from the back stack
    interviewViewModel: InterviewViewModel = hiltViewModel(
        viewModelStoreOwner = remember(navController) {
            navController.getBackStackEntry(
                // Get the interview screen that's still in backstack
                navController.previousBackStackEntry?.destination?.route
                    ?: Screen.Home.route
            )
        }
    )
) {
    val result = remember {
        Gson().fromJson(URLDecoder.decode(resultJson, "UTF-8"), AnalysisResult::class.java)
    }

    val state by interviewViewModel.uiState.collectAsStateWithLifecycle()

    // Check if there are more questions
    val hasMoreQuestions = remember(state.currentIndex, state.questions.size) {
        state.currentIndex + 1 < state.questions.size
    }
    val nextQuestionNumber = state.currentIndex + 2  // +2 because currentIndex is 0-based
    val totalQuestions = state.questions.size

    Scaffold(containerColor = SurfaceDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ─── Header with question progress ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.your_feedback),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (totalQuestions > 1) {
                        Text(
                            stringResource(
                                R.string.question_of,
                                state.currentIndex + 1,
                                totalQuestions
                            ),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                // Session progress indicator
                if (totalQuestions > 1) {
                    Surface(
                        color = SurfaceMid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(totalQuestions) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                index < state.currentIndex -> AccentGreen
                                                index == state.currentIndex -> PrimaryBlue
                                                else -> SurfaceLight
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Overall score dial
            ScoreDial(score = result.overallScore)

            Spacer(Modifier.height(24.dp))

            // Breakdown cards
            Text(
                stringResource(R.string.score_breakdown),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))

            ScoreBreakdownCard(
                confidence = result.confidenceScore,
                grammar    = result.grammarScore,
                relevance  = result.relevanceScore,
                keywords   = result.keywordScore
            )

            Spacer(Modifier.height(20.dp))

            KeywordsSection(
                matched = result.keywordsMatched,
                missed  = result.keywordsMissed
            )

            Spacer(Modifier.height(20.dp))

            FeedbackCard(stringResource(R.string.ai_feedback), result.feedback, PrimaryBlue)

            Spacer(Modifier.height(12.dp))

            FeedbackCard(stringResource(R.string.improved_answer), result.improvedAnswer, AccentGreen)

            Spacer(Modifier.height(28.dp))

            // ─── Action Buttons ───────────────────────────────────────────────
            if (hasMoreQuestions) {
                // Next question button — primary action
                Button(
                    onClick = {
                        interviewViewModel.nextQuestion()
                        interviewViewModel.startTimer()
                        // Go back to InterviewScreen (still in backstack)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.next_question, nextQuestionNumber, totalQuestions),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Secondary actions row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = { navController.navigate(Screen.Progress.route) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border   = BorderStroke(1.dp, SurfaceLight)
                    ) {
                        Text(stringResource(R.string.progress), fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            // End session early
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border   = BorderStroke(1.dp, SurfaceLight)
                    ) {
                        Text(stringResource(R.string.end_session), fontSize = 13.sp)
                    }
                }

            } else {
                // Last question — show session complete UI
                SessionCompleteCard(totalQuestions = totalQuestions)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Progress.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.view_full_progress), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border   = BorderStroke(1.dp, SurfaceLight)
                    ) {
                        Text(stringResource(R.string.new_session))
                    }
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Resume.route) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                        border   = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f))
                    ) {
                        Text(stringResource(R.string.resume_ai))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SessionCompleteCard(totalQuestions: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = AccentGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier              = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.session_complete),
                color      = AccentGreen,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.you_answered_all_questions, totalQuestions),
                color    = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

// ─── All the private composables stay the same ────────────────────────────────

@Composable
private fun ScoreDial(score: Float) {
    val animScore by animateFloatAsState(
        targetValue   = score,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label         = stringResource(R.string.score)
    )
    val color = when {
        score >= 75 -> ScoreGood
        score >= 50 -> ScoreMid
        else        -> ScoreBad
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .drawBehind {
                    val strokeWidth = 16.dp.toPx()
                    drawArc(
                        color = SurfaceLight,
                        startAngle = 135f, sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth, size.height - strokeWidth
                        )
                    )
                    drawArc(
                        color = color,
                        startAngle = 135f,
                        sweepAngle = 270f * (animScore / 100f),
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth, size.height - strokeWidth
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${animScore.toInt()}",
                    color      = color,
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("/ 100", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ScoreBreakdownCard(
    confidence: Float, grammar: Float,
    relevance: Float,  keywords: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreRow(stringResource(R.string.confidence), confidence, PrimaryBlue)
            ScoreRow(stringResource(R.string.grammar),    grammar,    AccentGreen)
            ScoreRow(stringResource(R.string.relevance),  relevance,  AccentOrange)
            ScoreRow(stringResource(R.string.keywords),   keywords,   AccentRed)
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Float, color: Color) {
    val animScore by animateFloatAsState(
        targetValue   = score / 100f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = label
    )
    Column {
        Row {
            Text(label, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text("${score.toInt()}%", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress    = { animScore },
            modifier    = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color       = color,
            trackColor  = SurfaceLight
        )
    }
}

@Composable
private fun KeywordsSection(matched: List<String>, missed: List<String>) {
    Column {
        Text(stringResource(R.string.keywords), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        if (matched.isNotEmpty()) {
            Text(stringResource(R.string.covered), color = ScoreGood, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            KeywordFlowRow(matched, ScoreGood)
        }
        if (missed.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.missed), color = ScoreBad, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            KeywordFlowRow(missed, ScoreBad)
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordFlowRow(items: List<String>, color: Color) {
    FlowRow(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { keyword ->
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    keyword,
                    color    = color,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(title: String, content: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid),
        border   = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(content, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
        }
    }
}