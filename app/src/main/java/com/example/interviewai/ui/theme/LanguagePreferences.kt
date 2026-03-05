package com.example.interviewai.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.langDataStore by preferencesDataStore(name = "lang_prefs")

@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LANGUAGE      = stringPreferencesKey("language")
        const val PREFS_NAME          = "lang_prefs_simple"      // ← SharedPrefs name
        const val KEY_LANG_SIMPLE     = "language"               // ← SharedPrefs key

        val SUPPORTED_LANGUAGES = mapOf(
            "en" to "English",
            "hi" to "हिंदी (Hindi)",
        )
    }

    val languageCode: Flow<String> = context.langDataStore.data
        .map { it[KEY_LANGUAGE] ?: "en" }

    suspend fun setLanguage(code: String) {
        // 1. Save to DataStore for Flow observation in UI
        context.langDataStore.edit { it[KEY_LANGUAGE] = code }

        // 2. Save to SharedPreferences for attachBaseContext (runs before Hilt)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG_SIMPLE, code)
            .apply()

        println(">>> Language saved: $code")
    }
}