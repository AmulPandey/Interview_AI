package com.example.interviewai.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.model.UserProgress
import com.example.interviewai.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val progress: UserProgress? = null,
    val isLoading: Boolean      = true,
    val error: String?          = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState = _uiState.asStateFlow()

    init { loadProgress() }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getMyProgress()
                if (response.isSuccessful) {
                    val progress = response.body()!!
                    println(">>> Progress loaded: attempts=${progress.totalAttempts}, avg=${progress.averageScore}, streak=${progress.streak}")
                    _uiState.update { it.copy(progress = progress, isLoading = false) }
                } else {
                    println(">>> Progress error: ${response.code()} — ${response.errorBody()?.string()}")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load progress") }
                }
            } catch (e: Exception) {
                println(">>> Progress exception: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}