package com.example.interviewai.ui.resume

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.google.gson.Gson
import com.example.interviewai.data.model.GeneratedQuestion
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*
import java.net.URLEncoder

@Composable
fun ResumeScreen(
    navController: NavController,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it, context) }
    }

    Scaffold(containerColor = SurfaceDark) { padding ->
        AnimatedContent(
            targetState  = state.step,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = stringResource(R.string.resume_step)
        ) { step ->
            when (step) {
                is ResumeStep.Upload -> UploadStep(
                    state      = state,
                    onPickFile = { filePicker.launch("*/*") },
                    onUpload   = viewModel::uploadResume,
                    modifier   = Modifier.padding(padding)
                )
                is ResumeStep.Parsing -> ParsingStep(
                    modifier = Modifier.padding(padding)
                )
                is ResumeStep.Result -> ResultStep(
                    response       = step.response,
                    onStartSession = { viewModel.loadQuestions(step.response.resumeId) },
                    onReset        = viewModel::resetToUpload,
                    modifier       = Modifier.padding(padding)
                )
                is ResumeStep.Questions -> QuestionsStep(
                    questions     = step.questions,
                    isLoadingMore = step.isLoadingMore,
                    page          = step.page,
                    navController = navController,
                    onLoadMore    = viewModel::loadMoreQuestions,
                    onReset       = viewModel::resetToUpload,
                    modifier      = Modifier.padding(padding)
                )
            }
        }
    }
}

// ─── Step 1: Upload ───────────────────────────────────────────────────────────

@Composable
private fun UploadStep(
    state: ResumeUiState,
    onPickFile: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Title
        Text(
            text = stringResource(R.string.resume_interview),
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.upload_your_resume_and_get_personalized_interview_questions_based_on_your_experience),
            color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(40.dp))

        // Upload zone
        UploadDropZone(
            fileName = state.selectedFileName,
            fileSize = state.selectedFileSize,
            onClick  = onPickFile
        )

        Spacer(Modifier.height(16.dp))

        // Supported formats
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("PDF", "DOCX", "TXT").forEach { fmt ->
                Surface(
                    color = SurfaceMid,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        fmt, color = TextSecondary, fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(stringResource(R.string.supported), color = TextSecondary, fontSize = 12.sp)
        }

        // Error
        AnimatedVisibility(state.error != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = AccentRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(state.error ?: "", color = AccentRed, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // What you'll get
        WhatYouGetCard()

        Spacer(Modifier.height(32.dp))

        // Upload button
        Button(
            onClick  = onUpload,
            enabled  = state.selectedFileName != null && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Upload, null)
            Spacer(Modifier.width(8.dp))
            Text("Analyze Resume", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun UploadDropZone(
    fileName: String?,
    fileSize: String?,
    onClick: () -> Unit
) {
    val borderAnim by rememberInfiniteTransition(label = "border").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = stringResource(R.string.alpha)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (fileName != null) 100.dp else 180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceMid)
            .border(
                BorderStroke(
                    1.5.dp,
                    if (fileName != null) AccentGreen
                    else PrimaryBlue.copy(alpha = 0.4f + 0.3f * borderAnim)
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (fileName != null) {
            // File selected state
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, null, tint = AccentGreen, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(fileName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(fileSize ?: "", color = TextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
            }
        } else {
            // Empty state
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.UploadFile, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.tap_to_select_your_resume), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("PDF, DOCX or TXT", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun WhatYouGetCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.what_you_ll_get), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(14.dp))
            listOf(
                Triple(Icons.Default.Psychology,    PrimaryBlue,
                    stringResource(R.string.skill_based_questions_tailored_to_your_tech_stack)
                ),
                Triple(Icons.Default.WorkHistory,   AccentGreen,
                    stringResource(R.string.experience_questions_matching_your_career_level)
                ),
                Triple(Icons.Default.Folder,        AccentOrange,
                    stringResource(R.string.project_deep_dive_questions_from_your_work)
                ),
                Triple(Icons.Default.RecordVoiceOver, AccentRed,
                    stringResource(R.string.behavioral_questions_for_your_seniority_level)
                )
            ).forEach { (icon, color, text) ->
                Row(
                    Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Step 2: Parsing ──────────────────────────────────────────────────────────

@Composable
private fun ParsingStep(modifier: Modifier = Modifier) {
    val steps = listOf(stringResource(R.string.extracting_text),
        stringResource(R.string.detecting_skills),
        stringResource(R.string.analyzing_experience),
        stringResource(R.string.generating_questions)
    )
    var currentStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        repeat(steps.size) { i ->
            kotlinx.coroutines.delay(800)
            currentStep = i + 1
        }
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.analyzing_your_resume), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(24.dp))
            steps.forEachIndexed { index, step ->
                AnimatedVisibility(visible = index <= currentStep) {
                    Row(
                        Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (index < currentStep) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            null,
                            tint = if (index < currentStep) AccentGreen else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(step, color = if (index < currentStep) TextPrimary else TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─── Step 3: Result ───────────────────────────────────────────────────────────

@Composable
private fun ResultStep(
    response: com.example.interviewai.data.model.ResumeUploadResponse,
    onStartSession: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        // Success header
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(PrimaryDark, SurfaceMid)))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.resume_analyzed), color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(response.fileName, color = TextSecondary, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Stats row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill("${response.skillsExtracted.size}",
                stringResource(R.string.skills), PrimaryBlue, Modifier.weight(1f))
            StatPill(
                stringResource(R.string.yr, response.experienceYears),
                stringResource(R.string.experience), AccentGreen, Modifier.weight(1f))
            StatPill("${response.projectsFound}",
                stringResource(R.string.projects), AccentOrange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // Job title
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Work, null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.detected_job_title), color = TextSecondary, fontSize = 12.sp)
                    Text(response.jobTitle, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Skills chips
        if (response.skillsExtracted.isNotEmpty()) {
            Text("Detected Skills", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            FlowSkillChips(response.skillsExtracted)
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick  = onStartSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.start_interview_session), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.upload_different_resume), color = TextSecondary)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FlowSkillChips(skills: List<String>) {
    val rows = skills.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { skill ->
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Text(
                            skill.replaceFirstChar { it.uppercase() },
                            color    = PrimaryBlue,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Step 4: Questions ────────────────────────────────────────────────────────

@Composable
private fun QuestionsStep(
    questions: List<GeneratedQuestion>,
    isLoadingMore: Boolean,
    page: Int,
    navController: NavController,
    onLoadMore: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {

        // ─── Header ───────────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(PrimaryDark.copy(alpha = 0.6f), SurfaceDark))
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.your_questions),
                    color      = TextPrimary,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.questions_generated, questions.size),
                        color    = TextSecondary,
                        fontSize = 14.sp
                    )
                    if (page > 1) {
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                stringResource(R.string.batch, page),
                                color    = PrimaryBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── Questions List ───────────────────────────────────────────────────
        LazyColumn(
            state               = listState,
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(questions) { index, question ->
                ResumeQuestionCard(
                    index    = index + 1,
                    question = question,
                    onClick  = {
                        val json = Gson().toJson(question)
                        navController.navigate(Screen.InterviewDirect.createRoute(json))
                    }
                )
            }

            // ─── Load More / Loading indicator ────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))

                if (isLoadingMore) {
                    // Loading state
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color    = PrimaryBlue,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    stringResource(R.string.generating_next_batch),
                                    color      = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp
                                )
                                Text(
                                    stringResource(R.string.gemini_ai_is_crafting_new_questions),
                                    color    = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    // Load More button
                    LoadMoreButton(
                        currentCount = questions.size,
                        onClick      = onLoadMore
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Reset button
                TextButton(
                    onClick  = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.UploadFile,
                        contentDescription = null,
                        tint     = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.upload_different_resume), color = TextSecondary, fontSize = 13.sp)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Load More Button ─────────────────────────────────────────────────────────

@Composable
private fun LoadMoreButton(currentCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "spark")
            val sparkAlpha by infiniteTransition.animateFloat(
                initialValue  = 0.5f,
                targetValue   = 1f,
                animationSpec = infiniteRepeatable(
                    tween(900), RepeatMode.Reverse
                ),
                label = (stringResource(R.string.alpha))
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint     = PrimaryBlue.copy(alpha = sparkAlpha),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    stringResource(R.string.load_10_more_questions),
                    color      = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
                Text(
                    stringResource(
                        R.string.ai_will_generate_a_fresh_unique_batch_loaded_so_far,
                        currentCount
                    ),
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint     = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ResumeQuestionCard(
    index: Int,
    question: GeneratedQuestion,
    onClick: () -> Unit
) {
    val sourceColor = when (question.source) {
        stringResource(R.string.skill) -> PrimaryBlue
        stringResource(R.string.project) -> AccentGreen
        (stringResource(R.string.experience)) -> AccentOrange
        else         -> TextSecondary
    }
    val sourceIcon = when (question.source) {
        stringResource(R.string.skill)     -> Icons.Outlined.Code
        stringResource(R.string.project)    -> Icons.Outlined.Folder
        stringResource(R.string.experience) -> Icons.Outlined.WorkHistory
        else         -> Icons.Outlined.RecordVoiceOver
    }
    val diffColor = when (question.difficulty) {
        stringResource(R.string.hard) -> ScoreBad
        stringResource(R.string.easy) -> ScoreGood
        else    -> ScoreMid
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceMid),
        border   = BorderStroke(1.dp, sourceColor.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Index badge
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(sourceColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$index", color = sourceColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(10.dp))

                // Source badge
                Surface(color = sourceColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(sourceIcon, null, tint = sourceColor, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(question.source, color = sourceColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.width(6.dp))

                // Difficulty badge
                Surface(color = diffColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(question.difficulty, color = diffColor, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }

                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text(question.text, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)

            if (question.expectedKeywords.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(
                        R.string.key_topics,
                        question.expectedKeywords.take(3).joinToString(", ")
                    ),
                    color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}