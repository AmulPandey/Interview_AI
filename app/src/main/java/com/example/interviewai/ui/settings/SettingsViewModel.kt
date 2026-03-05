package com.example.interviewai.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.data.model.UpdateProfileRequest
import com.example.interviewai.data.remote.ApiService
import com.example.interviewai.data.repository.AuthRepository
import com.example.interviewai.ui.theme.LanguagePreferences
import com.example.interviewai.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val targetRole: String = "",
    val profilePicUrl: String? = null,
    // Toggles
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoSubmitEnabled: Boolean = false,
    // Dialogs
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    // Loading states
    val isLoggingOut: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isUploadingPic: Boolean = false,
    // Results
    val logoutSuccess: Boolean = false,
    val profileSaveSuccess: Boolean = false,
    val error: String? = null,
    // Edit fields
    val editName: String = "",
    val editTargetRole: String = "",

    val selectedLanguage: String = "en",
    val languageChanged: Boolean = false,
    val showLanguageDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authRepository: AuthRepository,
    private val apiService: ApiService,
    private val themePreferences: ThemePreferences,
    private val languagePreferences: LanguagePreferences
) : ViewModel() {

    val supportedLanguages = LanguagePreferences.SUPPORTED_LANGUAGES

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    val roles = listOf(
        "Android Developer", "Backend Developer",
        "ML Engineer", "Full Stack Developer",
        "DevOps Engineer", "iOS Developer"
    )

    init {
        loadFromDataStore()
        fetchLatestProfile()
        observeTheme()
        observeLanguage()
    }

    // ─── Load cached data from DataStore instantly ────────────────────────────
    private fun loadFromDataStore() {
        viewModelScope.launch {
            combine(
                tokenDataStore.userName,
                tokenDataStore.userEmail,
                tokenDataStore.targetRole,
                tokenDataStore.profilePic
            ) { name, email, role, pic -> listOf(name, email, role, pic) }
                .collect { (name, email, role, pic) ->
                    // ← Force full URL regardless of what's stored
                    val fullPicUrl = when {
                        pic.isNullOrBlank()       -> null
                        pic.startsWith("http")    -> pic
                        pic.startsWith("/uploads") -> "http://192.168.31.28:8080$pic"
                        else                      -> null
                    }
                    println(">>> DataStore pic: '$pic' → fullUrl: '$fullPicUrl'")
                    _uiState.update {
                        it.copy(
                            userName       = name ?: it.userName,
                            userEmail      = email ?: it.userEmail,
                            targetRole     = role ?: it.targetRole,
                            profilePicUrl  = fullPicUrl,
                            editName       = name ?: it.editName,
                            editTargetRole = role ?: it.editTargetRole
                        )
                    }
                }
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            languagePreferences.languageCode.collect { code ->
                _uiState.update { it.copy(selectedLanguage = code) }
            }
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            languagePreferences.setLanguage(code)
            // App needs restart to apply — flag it
            _uiState.update { it.copy(languageChanged = true) }
        }
    }

    // ─── Fetch fresh profile from API ─────────────────────────────────────────
    // Update fetchLatestProfile to build full URL before storing
    private fun fetchLatestProfile() {
        viewModelScope.launch {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful) {
                    val user = response.body()!!

                    // ← Always build full URL here
                    val fullPicUrl = user.profilePicUrl?.let {
                        if (it.startsWith("http")) it
                        else "http://192.168.31.28:8080/api/v1$it"   // ← add /api/v1 prefix
                    }
                    println(">>> fetchLatestProfile pic: '${user.profilePicUrl}' → '$fullPicUrl'")

                    tokenDataStore.saveUser(
                        id            = user.id,
                        name          = user.name,
                        email         = user.email,
                        targetRole    = user.targetRole,
                        profilePicUrl = fullPicUrl   // ← save full URL to DataStore
                    )

                    _uiState.update {
                        it.copy(
                            userName       = user.name,
                            userEmail      = user.email,
                            targetRole     = user.targetRole,
                            profilePicUrl  = fullPicUrl,  // ← set full URL in state
                            editName       = user.name,
                            editTargetRole = user.targetRole
                        )
                    }
                }
            } catch (e: Exception) {
                println(">>> /user/me exception: ${e.message}")
            }
        }
    }


    // ─── Save Profile ─────────────────────────────────────────────────────────
    fun saveProfile() {
        val state = _uiState.value
        if (state.editName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, error = null) }
            try {
                println(">>> Saving profile: name=${state.editName}, role=${state.editTargetRole}")
                val response = apiService.updateProfile(
                    UpdateProfileRequest(
                        name       = state.editName,
                        targetRole = state.editTargetRole
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenDataStore.updateProfile(body.name, body.targetRole)
                    _uiState.update {
                        it.copy(
                            isSavingProfile       = false,
                            showEditProfileDialog = false,
                            userName              = body.name,
                            targetRole            = body.targetRole,
                            error                 = null
                        )
                    }
                } else {
                    val err = response.errorBody()?.string()
                    println(">>> Save profile failed: ${response.code()} — $err")
                    _uiState.update {
                        it.copy(isSavingProfile = false, error = "Failed: $err")
                    }
                }
            } catch (e: Exception) {
                println(">>> Save profile exception: ${e.message}")
                _uiState.update { it.copy(isSavingProfile = false, error = e.message) }
            }
        }
    }

    // ─── Profile Picture ──────────────────────────────────────────────────────
    fun uploadProfilePicture(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPic = true, error = null) }
            try {
                val bytes    = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = getFileName(uri, context)

                val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                val part        = MultipartBody.Part.createFormData("file", fileName, requestBody)

                println(">>> Uploading profile pic: $fileName, size=${bytes.size}")
                val response = apiService.uploadProfilePicture(part)

                println(">>> Upload response code: ${response.code()}")
                println(">>> Upload response body: ${response.body()}")
                println(">>> Upload error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val url = response.body()!!.profilePicUrl
                    println(">>> Upload success, picUrl=$url")

                    // Save to DataStore
                    tokenDataStore.updateProfilePic(url)

                    // Build full URL for immediate display
                    val fullUrl = if (url.startsWith("http")) url else "http://192.168.31.28:8080$url"
                    _uiState.update { it.copy(isUploadingPic = false, profilePicUrl = fullUrl) }

                } else {
                    _uiState.update {
                        it.copy(isUploadingPic = false, error = "Upload failed: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                println(">>> Upload exception: ${e.javaClass.simpleName}: ${e.message}")
                _uiState.update { it.copy(isUploadingPic = false, error = e.message) }
            }
        }
    }

    private fun getFileName(uri: android.net.Uri, context: android.content.Context): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(idx)
        } ?: "profile.jpg"
    }

    // ─── Observe persisted theme ──────────────────────────────────────────────
    private fun observeTheme() {
        viewModelScope.launch {
            themePreferences.isDarkMode.collect { isDark ->
                _uiState.update { it.copy(darkModeEnabled = isDark) }
            }
        }
    }

    // Add these to SettingsViewModel.kt

    fun showLanguageDialog()    = _uiState.update { it.copy(showLanguageDialog = true) }
    fun dismissLanguageDialog() = _uiState.update { it.copy(showLanguageDialog = false, languageChanged = false) }

    // ─── Toggles ──────────────────────────────────────────────────────────────
    fun toggleNotifications() = _uiState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.darkModeEnabled
            themePreferences.setDarkMode(newValue)
            // state updates automatically via observeTheme()
        }
    }
    fun toggleSound()         = _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    fun toggleAutoSubmit()    = _uiState.update { it.copy(autoSubmitEnabled = !it.autoSubmitEnabled) }

    // ─── Dialogs ──────────────────────────────────────────────────────────────
    fun showEditProfile()    = _uiState.update { it.copy(showEditProfileDialog = true, editName = uiState.value.userName, editTargetRole = uiState.value.targetRole) }
    fun dismissEditProfile() = _uiState.update { it.copy(showEditProfileDialog = false, error = null) }
    fun onEditNameChange(v: String)       = _uiState.update { it.copy(editName = v) }
    fun onEditTargetRoleChange(v: String) = _uiState.update { it.copy(editTargetRole = v) }

    fun showLogoutDialog()    = _uiState.update { it.copy(showLogoutDialog = true) }
    fun dismissLogoutDialog() = _uiState.update { it.copy(showLogoutDialog = false) }
    fun showDeleteDialog()    = _uiState.update { it.copy(showDeleteDialog = true) }
    fun dismissDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false, logoutSuccess = true) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                apiService.deleteAccount()
                authRepository.logout()
                _uiState.update { it.copy(logoutSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
