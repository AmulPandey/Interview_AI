package com.example.interviewai.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LANGUAGE_SELECTED = booleanPreferencesKey("language_selected")
    }

    val isLanguageSelected: Flow<Boolean> = context.onboardingDataStore.data
        .map { it[KEY_LANGUAGE_SELECTED] ?: false }

    // OnboardingPreferences.kt
    suspend fun setLanguageSelected(selected: Boolean = true) {   // ← default = true
        context.onboardingDataStore.edit { it[KEY_LANGUAGE_SELECTED] = selected }
    }
}