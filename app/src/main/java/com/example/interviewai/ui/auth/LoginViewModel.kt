package com.example.interviewai.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val passwordVisible: Boolean = false
) {
    val isFormValid get() = email.isNotBlank()
            && password.length >= 6
            && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }
    fun togglePasswordVisibility()  = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun login() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(state.email, state.password)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isSuccess = true)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}