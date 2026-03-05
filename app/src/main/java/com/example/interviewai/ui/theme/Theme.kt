package com.example.interviewai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ─── Composable-accessible color tokens ──────────────────────────────────────
// These are what all screens use — they change based on theme
var SurfaceDark   by mutableStateOf(DarkSurfaceDark)
    private set
var SurfaceMid    by mutableStateOf(DarkSurfaceMid)
    private set
var SurfaceLight  by mutableStateOf(DarkSurfaceLight)
    private set
var PrimaryDark   by mutableStateOf(DarkPrimaryDark)
    private set
var TextPrimary   by mutableStateOf(DarkTextPrimary)
    private set
var TextSecondary by mutableStateOf(DarkTextSecondary)
    private set

private val DarkColorScheme = darkColorScheme(
    primary       = PrimaryBlue,
    background    = DarkSurfaceDark,
    surface       = DarkSurfaceMid,
    onBackground  = DarkTextPrimary,
    onSurface     = DarkTextPrimary,
    onPrimary     = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary       = PrimaryBlue,
    background    = LightSurfaceDark,
    surface       = LightSurfaceMid,
    onBackground  = LightTextPrimary,
    onSurface     = LightTextPrimary,
    onPrimary     = Color.White
)

@Composable
fun InterviewAITheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Update global color tokens when theme changes
    SurfaceDark   = if (darkTheme) DarkSurfaceDark   else LightSurfaceDark
    SurfaceMid    = if (darkTheme) DarkSurfaceMid    else LightSurfaceMid
    SurfaceLight  = if (darkTheme) DarkSurfaceLight  else LightSurfaceLight
    PrimaryDark   = if (darkTheme) DarkPrimaryDark   else LightPrimaryDark
    TextPrimary   = if (darkTheme) DarkTextPrimary   else LightTextPrimary
    TextSecondary = if (darkTheme) DarkTextSecondary else LightTextSecondary

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = AppTypography,
        content     = content
    )
}