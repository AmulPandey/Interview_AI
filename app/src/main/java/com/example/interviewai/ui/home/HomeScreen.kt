package com.example.interviewai.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.example.interviewai.data.model.UserStats
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == androidx.lifecycle.Lifecycle.State.RESUMED) {
                viewModel.loadStats()
            }
        }
    }

    Scaffold(
        containerColor = SurfaceDark,
        bottomBar = {
            BottomNavBar(navController, currentRoute = Screen.Home.route)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                userName         = state.userName,
                bestScore        = state.stats.bestScore,
                thisWeekSessions = state.stats.thisWeekSessions
            )

            Spacer(Modifier.height(24.dp))

            StatsRow(
                stats     = state.stats,
                isLoading = state.isLoadingStats
            )

            Spacer(Modifier.height(28.dp))

            // ── Interview Mode Selector ──────────────────────────────────────
            SectionTitle(stringResource(R.string.interview_mode))
            Spacer(Modifier.height(12.dp))
            InterviewModeCards(
                onResumeClick = { navController.navigate(Screen.Resume.route) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Category Picker ──────────────────────────────────────────────
            SectionTitle(stringResource(R.string.choose_category))
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.categoryKeys) { (apiKey, stringId) ->
                    CategoryChip(
                        label    = stringResource(stringId),  // ← fresh on language change
                        selected = apiKey == state.selectedCategory,
                        onClick  = { viewModel.selectCategory(apiKey) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Difficulty Picker ────────────────────────────────────────────
            SectionTitle(stringResource(R.string.difficulty))
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                viewModel.difficultyKeys.forEach { (apiKey, stringId) ->
                    DifficultyChip(
                        label    = stringResource(stringId),
                        selected = apiKey == state.selectedDifficulty,
                        onClick  = { viewModel.selectDifficulty(apiKey) }
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // In StartInterviewButton onClick — add logs
            StartInterviewButton(
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick  = {
                    println(">>> Navigating to interview: category=${state.selectedCategory}, difficulty=${state.selectedDifficulty}")
                    navController.navigate(
                        Screen.Interview.createRoute(
                            state.selectedCategory,
                            state.selectedDifficulty
                        )
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Interview Mode Cards ─────────────────────────────────────────────────────

@Composable
private fun InterviewModeCards(
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // General Practice card (left — decorative, current mode)
        Card(
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(18.dp),
            colors   = CardDefaults.cardColors(containerColor = SurfaceMid),
            border   = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint     = PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    stringResource(R.string.general_practice),
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    lineHeight = 20.sp
                )
                Text(
                    stringResource(R.string.category_based_questions),
                    color    = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        stringResource(R.string.active),
                        color    = PrimaryBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Resume Interview card (right — clickable CTA)
        Card(
            onClick   = onResumeClick,
            modifier  = Modifier.weight(1f),
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.5.dp, AccentGreen.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AccentGreen.copy(alpha = 0.15f),
                                PrimaryBlue.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint     = AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.resume_interview),
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        stringResource(R.string.ai_questions_from_your_cv),
                        color    = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = AccentGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                stringResource(R.string.try_now),
                                color    = AccentGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

// Change HeaderSection signature:
@Composable
private fun HeaderSection(
    userName: String,
    bestScore: Float,
    thisWeekSessions: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Column {
            Text(
                stringResource(R.string.hey, userName.ifEmpty { "there" }),
                color      = TextPrimary,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("🎯 "+
                stringResource(
                    R.string.best_score_sessions_this_week,
                    if (bestScore > 0) "${bestScore.toInt()}%" else "—",
                    thisWeekSessions
                ),
                color    = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

// ─── Stats ────────────────────────────────────────────────────────────────────

@Composable
fun StatsRow(stats: UserStats, isLoading: Boolean) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label    = stringResource(R.string.sessions),
            value    = if (isLoading) "—" else "${stats.totalSessions}",
            icon     = "🎯",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label    = stringResource(R.string.avg_score),
            value    = if (isLoading) "—" else "${stats.avgScore.toInt()}%",
            icon     = "📊",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label    = stringResource(R.string.streak),
            value    = if (isLoading) "—" else "${stats.currentStreak}🔥",
            icon     = "⚡",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()        // ← add this
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center   // ← optional: center vertically too
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color      = TextPrimary,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                label,
                color    = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ─── Section Title ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier   = Modifier.padding(horizontal = 20.dp),
        color      = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp
    )
}

// ─── Category Chip ────────────────────────────────────────────────────────────

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor   = if (selected) PrimaryBlue else SurfaceMid
    val textColor = if (selected) Color.White else TextSecondary

    Surface(
        onClick   = onClick,
        shape     = RoundedCornerShape(50),
        color     = bgColor,
        modifier  = Modifier.height(40.dp)
    ) {
        Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Text(label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// ─── Difficulty Chip ──────────────────────────────────────────────────────────

@Composable
private fun DifficultyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (label) {
        "EASY"   -> ScoreGood
        "MEDIUM" -> ScoreMid
        else     -> ScoreBad
    }
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier,
        border   = BorderStroke(1.5.dp, if (selected) color else SurfaceLight),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
            contentColor   = if (selected) color else TextSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

// ─── Start Button ─────────────────────────────────────────────────────────────

@Composable
private fun StartInterviewButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.start_interview), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// ─── Bottom Nav ───────────────────────────────────────────────────────────────

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = SurfaceMid) {
        val items = listOf(
            Triple(Screen.Home.route,     Icons.Default.Home, stringResource(R.string.home)),
            Triple(Screen.Progress.route, Icons.Default.DateRange,   stringResource(R.string.progress)),
            Triple(Screen.Resume.route,   Icons.Default.Description, stringResource(R.string.resume)),
            Triple(Screen.Settings.route, Icons.Default.Settings, stringResource(R.string.settings))
        )

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon     = { Icon(icon, contentDescription = label) },
                label    = { Text(label) },
                selected = currentRoute == route,
                onClick  = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState    = true
                                inclusive    = false
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = PrimaryBlue,
                    selectedTextColor   = PrimaryBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor      = PrimaryBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}
