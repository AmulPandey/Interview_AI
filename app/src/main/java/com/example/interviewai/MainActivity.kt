package com.example.interviewai


import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewai.ui.navigation.AppNavigation
import com.example.interviewai.ui.theme.InterviewAITheme
import com.example.interviewai.ui.theme.LanguagePreferences
import com.example.interviewai.ui.theme.ThemePreferences
import com.example.interviewai.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// MainActivity.kt — read from SharedPreferences in attachBaseContext
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themePreferences: ThemePreferences
    @Inject lateinit var languagePreferences: LanguagePreferences

    override fun attachBaseContext(newBase: Context) {
        // Read from SharedPreferences — safe before Hilt injection
        val langCode = newBase
            .getSharedPreferences(LanguagePreferences.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LanguagePreferences.KEY_LANG_SIMPLE, "en") ?: "en"
        println(">>> attachBaseContext locale: $langCode")
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by themePreferences.isDarkMode
                .collectAsStateWithLifecycle(initialValue = true)
            InterviewAITheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}