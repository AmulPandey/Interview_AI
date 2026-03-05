package com.example.interviewai.ui.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.interviewai.data.local.OnboardingPreferences
import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.ui.AppViewModel
import com.example.interviewai.ui.auth.LoginScreen
import com.example.interviewai.ui.auth.RegisterScreen
import com.example.interviewai.ui.feedback.FeedbackScreen
import com.example.interviewai.ui.home.HomeScreen
import com.example.interviewai.ui.interview.InterviewDirectScreen
import com.example.interviewai.ui.interview.InterviewScreen
import com.example.interviewai.ui.onboarding.LanguageSelectScreen
import com.example.interviewai.ui.progress.ProgressScreen
import com.example.interviewai.ui.resume.ResumeScreen
import com.example.interviewai.ui.settings.SettingsScreen
import com.example.interviewai.ui.theme.PrimaryBlue
import com.example.interviewai.ui.theme.SurfaceDark
import java.net.URLEncoder


sealed class Screen(val route: String) {

    object LanguageSelect : Screen("language_select")
    object Home      : Screen("home")
    object Interview : Screen("interview/{category}/{difficulty}") {
        fun createRoute(category: String, difficulty: String) =
            "interview/$category/$difficulty"
    }

    object InterviewDirect : Screen("interview_direct/{questionJson}") {
        fun createRoute(questionJson: String): String {
            val encoded = URLEncoder.encode(questionJson, "UTF-8")
            return "interview_direct/$encoded"
        }
    }

    object Settings : Screen("settings")

    object Resume : Screen("resume")
    object Feedback  : Screen("feedback/{resultJson}") {
        fun createRoute(encoded: String) = "feedback/$encoded"
    }
    object Progress  : Screen("progress")

    object Login    : Screen("login")
    object Register : Screen("register")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val tokenDataStore: TokenDataStore = hiltViewModel<AppViewModel>().tokenDataStore
    val onboardingPrefs: OnboardingPreferences = hiltViewModel<AppViewModel>().onboardingPreferences

    val isLoggedIn         by tokenDataStore.isLoggedIn.collectAsStateWithLifecycle(false)
    val isLanguageSelected by onboardingPrefs.isLanguageSelected.collectAsStateWithLifecycle(false)

    // ← Use null as initial to detect "still loading"
    val isLoggedInState         = tokenDataStore.isLoggedIn.collectAsStateWithLifecycle(null)
    val isLanguageSelectedState = onboardingPrefs.isLanguageSelected.collectAsStateWithLifecycle(null)

    // Wait until both DataStores have loaded
    if (isLoggedInState.value == null || isLanguageSelectedState.value == null) {
        // Show splash/loading while DataStore loads
        Box(
            modifier         = Modifier.fillMaxSize().background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val loggedIn         = isLoggedInState.value!!
    val languageSelected = isLanguageSelectedState.value!!

    // ← Now correctly used in NavHost
    val startDestination = when {
        !languageSelected -> Screen.LanguageSelect.route
        loggedIn          -> Screen.Home.route
        else              -> Screen.Login.route
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination   // ← use calculated value
    ) {
        composable(Screen.LanguageSelect.route) {
            LanguageSelectScreen(
                onLanguageSelected = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.LanguageSelect.route) { inclusive = true }
                    }
                },

            )
        }

        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Progress.route) { ProgressScreen(navController) }
        composable(Screen.Resume.route) { ResumeScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }

        composable(
            Screen.Interview.route,
            arguments = listOf(
                navArgument("category")   { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStack ->
            val category   = backStack.arguments?.getString("category")   ?: "Android"
            val difficulty = backStack.arguments?.getString("difficulty") ?: "MEDIUM"
            InterviewScreen(navController, category, difficulty)
        }

        composable(
            Screen.InterviewDirect.route,
            arguments = listOf(
                navArgument("questionJson") { type = NavType.StringType }
            )
        ) { backStack ->
            val json = backStack.arguments?.getString("questionJson") ?: ""
            InterviewDirectScreen(navController, json)
        }

        composable(
            Screen.Feedback.route,
            arguments = listOf(
                navArgument("resultJson") { type = NavType.StringType }
            )
        ) { backStack ->
            FeedbackScreen(
                navController = navController,
                resultJson    = backStack.arguments?.getString("resultJson") ?: ""
            )
        }
    }
}