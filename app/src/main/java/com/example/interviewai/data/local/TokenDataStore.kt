package com.example.interviewai.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN   = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN  = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID        = stringPreferencesKey("user_id")
        private val KEY_USER_NAME      = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL     = stringPreferencesKey("user_email")
        private val KEY_TARGET_ROLE    = stringPreferencesKey("target_role")
        private val KEY_PROFILE_PIC    = stringPreferencesKey("profile_pic")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit {
            it[KEY_ACCESS_TOKEN]  = accessToken
            it[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUser(
        id: String, name: String,
        email: String, targetRole: String,
        profilePicUrl: String? = null
    ) {
        context.dataStore.edit {
            it[KEY_USER_ID]     = id
            it[KEY_USER_NAME]   = name
            it[KEY_USER_EMAIL]  = email
            it[KEY_TARGET_ROLE] = targetRole
            if (profilePicUrl != null) it[KEY_PROFILE_PIC] = profilePicUrl
        }
    }

    suspend fun updateProfile(name: String, targetRole: String) {
        context.dataStore.edit {
            it[KEY_USER_NAME]   = name
            it[KEY_TARGET_ROLE] = targetRole
        }
    }

    suspend fun updateProfilePic(url: String) {
        context.dataStore.edit { it[KEY_PROFILE_PIC] = url }
    }

    suspend fun clear() { context.dataStore.edit { it.clear() } }

    val accessToken:  Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId:       Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userName:     Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }
    val userEmail:    Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] }
    val targetRole:   Flow<String?> = context.dataStore.data.map { it[KEY_TARGET_ROLE] }
    val profilePic:   Flow<String?> = context.dataStore.data.map { it[KEY_PROFILE_PIC] }
    val isLoggedIn:   Flow<Boolean> = context.dataStore.data.map { !it[KEY_ACCESS_TOKEN].isNullOrBlank() }
}