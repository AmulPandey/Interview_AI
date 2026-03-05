package com.example.interviewai.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val targetRole: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val passwordVisible: Boolean = false
) {
    val passwordMatch get() = password == confirmPassword
    val isFormValid get() = name.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6
            && passwordMatch
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    val roles = listOf(
        "Android Developer", "Backend Developer",
        "ML Engineer", "Full Stack Developer", "DevOps Engineer"
    )

    fun onNameChange(v: String)            = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String)           = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String)        = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }
    fun onTargetRoleChange(v: String)      = _uiState.update { it.copy(targetRole = v) }
    fun togglePasswordVisibility()         = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun register() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = registerUseCase(
                state.name, state.email, state.password, state.targetRole
            )) {
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