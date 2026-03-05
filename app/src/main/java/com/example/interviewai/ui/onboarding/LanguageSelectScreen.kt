package com.example.interviewai.ui.onboarding


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val subtitle: String
)

@Composable
fun LanguageSelectScreen(
    onLanguageSelected: () -> Unit,
    viewModel: LanguageSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? android.app.Activity

    LaunchedEffect(state.languageChanged) {
        if (state.languageChanged) {
            viewModel.onLanguageChangedHandled()
            activity?.recreate()
        }
    }

    val languages = listOf(
        LanguageOption("en", "English", "English", "🇬🇧", "Continue in English"),
        LanguageOption("hi", "Hindi",   "हिंदी",   "🇮🇳", "हिंदी में जारी रखें")
    )

    // Animate in
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PrimaryDark,
                        SurfaceDark,
                        SurfaceDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(600)) + slideInVertically(
                tween(600, easing = EaseOutCubic)
            ) { it / 3 }
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─── App Icon ─────────────────────────────────────────────────
                Image(
                    painter = painterResource(id = R.drawable.cropped_circle_image),
                    contentDescription = "Target",
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.app_name),
                    color      = TextPrimary,
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Choose your language\nभाषा चुनें",
                    color     = TextSecondary,
                    fontSize  = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(48.dp))

                // ─── Language Cards ───────────────────────────────────────────
                languages.forEach { lang ->
                    LanguageCard(
                        language   = lang,
                        isSelected = state.selectedCode == lang.code,
                        isLoading  = state.isLoading && state.selectedCode == lang.code,
                        onClick    = { viewModel.selectLanguage(lang.code) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(
    language: LanguageOption,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PrimaryBlue.copy(alpha = 0.15f)
            else
                SurfaceMid
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryBlue else SurfaceLight
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 0.dp
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag
            Text(language.flag, fontSize = 36.sp)

            Spacer(Modifier.width(16.dp))

            // Names
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    language.nativeName,
                    color      = TextPrimary,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    language.subtitle,
                    color    = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Check or loading
            if (isLoading) {
                CircularProgressIndicator(
                    color       = PrimaryBlue,
                    modifier    = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}