package com.example.interviewai.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.local.OnboardingPreferences
import com.example.interviewai.ui.theme.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageSelectUiState(
    val selectedCode:      String  = "",
    val isLoading:         Boolean = false,
    val selectionComplete: Boolean = false
)

@HiltViewModel
class LanguageSelectViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    data class UiState(
        val selectedCode: String = "en",
        val isLoading: Boolean   = false,
        val languageChanged: Boolean = false,
        val selectionComplete: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun selectLanguage(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedCode = code, isLoading = true) }
            languagePreferences.setLanguage(code)               // saves to DataStore + SharedPrefs
            onboardingPreferences.setLanguageSelected(true)     // mark onboarding done
            _uiState.update { it.copy(isLoading = false, languageChanged = true) }
        }
    }

    fun onLanguageChangedHandled() {
        _uiState.update { it.copy(languageChanged = false) }
    }
}