package com.example.interviewai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.R
import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.data.model.UserStats
import com.example.interviewai.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val selectedCategory: String  = "Android",
    val selectedDifficulty: String = "MEDIUM",
    val userName: String          = "",
    val stats: UserStats          = UserStats(),
    val isLoadingStats: Boolean   = true,
    val statsError: String?       = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore,

) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val categoryKeys = listOf(
        "Android"       to R.string.category_android,
        "Backend"       to R.string.category_backend,
        "ML"            to R.string.category_ml,
        "System Design" to R.string.category_system_design,
        "DSA"           to R.string.category_dsa
    )

    val difficultyKeys = listOf(
        "EASY"   to R.string.difficulty_easy,
        "MEDIUM" to R.string.difficulty_medium,
        "HARD"   to R.string.difficulty_hard
    )

    init {
        loadUserName()
        loadStats()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            tokenDataStore.userName.collect { name ->
                _uiState.update { it.copy(userName = name ?: "") }
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, statsError = null) }
            try {
                val response = apiService.getMyStats()
                if (response.isSuccessful) {
                    val stats = response.body()!!
                    println(">>> Stats: sessions=${stats.totalSessions}, avg=${stats.avgScore}, streak=${stats.currentStreak}")
                    _uiState.update { it.copy(stats = stats, isLoadingStats = false) }
                } else {
                    println(">>> Stats error: ${response.code()}")
                    _uiState.update { it.copy(isLoadingStats = false, statsError = "Failed to load stats") }
                }
            } catch (e: Exception) {
                println(">>> Stats exception: ${e.message}")
                _uiState.update { it.copy(isLoadingStats = false, statsError = e.message) }
            }
        }
    }

    fun selectCategory(cat: String)   = _uiState.update { it.copy(selectedCategory = cat) }
    fun selectDifficulty(diff: String) = _uiState.update { it.copy(selectedDifficulty = diff) }
}