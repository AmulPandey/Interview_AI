package com.example.interviewai.ui.progress

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.example.interviewai.data.model.UserProgress
import com.example.interviewai.ui.home.BottomNavBar
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == androidx.lifecycle.Lifecycle.State.RESUMED) {
                viewModel.loadProgress()
            }
        }
    }

    Scaffold(
        containerColor = SurfaceDark,
        bottomBar      = { BottomNavBar(navController, Screen.Progress.route) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            state.progress != null && state.progress!!.totalAttempts > 0 -> {
                ProgressContent(
                    progress = state.progress!!,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                EmptyProgressState(
                    modifier   = Modifier.padding(padding),
                    onPractice = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyProgressState(modifier: Modifier = Modifier, onPractice: () -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("📊", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.no_sessions_yet),
                color      = TextPrimary,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.complete_your_first_interview_to_see_your_progress_tracked_here),
                color    = TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick  = onPractice,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.start_practicing), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Progress Content ─────────────────────────────────────────────────────────

@Composable
private fun ProgressContent(progress: UserProgress, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.your_progress), color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text( "🔥 "+
            stringResource(
                R.string.day_streak_total_sessions,
                progress.streak,
                progress.totalAttempts
            ),
            color    = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        // ─── Summary cards ────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(stringResource(R.string.avg_score),  "${progress.averageScore.toInt()}%", PrimaryBlue,  Modifier.weight(1f))
            SummaryCard(stringResource(R.string.best_score), "${progress.stats.bestScore.toInt()}%", AccentGreen, Modifier.weight(1f))
            SummaryCard(stringResource(R.string.this_week),  "${progress.stats.thisWeekSessions}", AccentOrange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // ─── Score chart ──────────────────────────────────────────────────────
        if (progress.scoreHistory.isNotEmpty()) {
            ScoreLineChart(progress)
            Spacer(Modifier.height(24.dp))
        }

        // ─── Category breakdown ───────────────────────────────────────────────
        if (progress.categoryBreakdown.isNotEmpty()) {
            Text(
                stringResource(R.string.category_performance),
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp
            )
            Spacer(Modifier.height(12.dp))
            progress.categoryBreakdown.forEach { (category, score) ->
                CategoryProgressRow(category, score)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border   = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier            = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color,       fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─── Score Line Chart ─────────────────────────────────────────────────────────

@Composable
private fun ScoreLineChart(progress: UserProgress) {
    val entries = progress.scoreHistory.mapIndexed { index, entry ->
        FloatEntry(index.toFloat(), entry.score)
    }
    val model = entryModelOf(entries)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.score_history),
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
            Text(
                stringResource(
                    R.string.last_sessions_avg,
                    progress.scoreHistory.size,
                    progress.averageScore.toInt()
                ),
                color    = PrimaryBlue,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            Chart(
                chart = lineChart(
                    lines = listOf(
                        LineSpec(
                            lineColor = PrimaryBlue.toArgb(),
                            lineBackgroundShader = DynamicShaders.fromBrush(
                                Brush.verticalGradient(
                                    listOf(PrimaryBlue.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                        )
                    )
                ),
                model       = model,
                startAxis   = rememberStartAxis(),
                bottomAxis  = rememberBottomAxis(),
                modifier    = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

// ─── Category Row ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryProgressRow(category: String, score: Float) {
    val color = when {
        score >= 75 -> ScoreGood
        score >= 50 -> ScoreMid
        else        -> ScoreBad
    }
    Column {
        Row {
            Text(category, color = TextPrimary,  modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text("${score.toInt()}%", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress   = { score / 100f },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color      = color,
            trackColor = SurfaceLight
        )
    }
}