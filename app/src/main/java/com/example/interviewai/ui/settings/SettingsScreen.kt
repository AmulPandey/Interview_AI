package com.example.interviewai.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.interviewai.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFullScreenPic by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? android.app.Activity

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadProfilePicture(it, context) } }

    // Navigate on logout/delete
    LaunchedEffect(state.logoutSuccess) {
        if (state.logoutSuccess) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }


    LaunchedEffect(state.languageChanged) {
        if (state.languageChanged) {
            println(">>> Language changed — recreating activity")
            viewModel.dismissLanguageDialog()
            activity?.recreate()    // ← restarts activity with new locale
        }
    }

    // Dialogs
    if (state.showLogoutDialog) {
        LogoutConfirmDialog(
            isLoading = state.isLoggingOut,
            onConfirm = viewModel::logout,
            onDismiss = viewModel::dismissLogoutDialog
        )
    }

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = viewModel::deleteAccount,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }

    if (state.showEditProfileDialog) {
        EditProfileDialog(
            state      = state,
            roles      = viewModel.roles,
            onNameChange = viewModel::onEditNameChange,
            onRoleChange = viewModel::onEditTargetRoleChange,
            onSave     = viewModel::saveProfile,
            onDismiss  = viewModel::dismissEditProfile
        )
    }

    // Add language dialog state
    if (state.showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = state.selectedLanguage,
            languages       = viewModel.supportedLanguages,
            onSelect        = { code ->
                viewModel.setLanguage(code)
                viewModel.dismissLanguageDialog()
            },
            onDismiss = viewModel::dismissLanguageDialog
        )
    }

    Scaffold(containerColor = SurfaceDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(PrimaryDark.copy(alpha = 0.6f), SurfaceDark))
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text( stringResource(R.string.settings), color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }

            // ─── Profile Card ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceMid)
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture with upload overlay
                    Box(
                        modifier            = Modifier.size(88.dp),
                        contentAlignment    = Alignment.BottomEnd
                    ) {
                        // Add state for full screen viewer
                        var showFullScreenPic by remember { mutableStateOf(false) }

                        // Full screen image viewer dialog
                        if (showFullScreenPic && !state.profilePicUrl.isNullOrBlank()) {
                            Dialog(
                                onDismissRequest = { showFullScreenPic = false },
                                properties = DialogProperties(usePlatformDefaultWidth = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black)
                                        .clickable { showFullScreenPic = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(state.profilePicUrl)
                                            .crossfade(true)
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = "Profile picture full screen",
                                        contentScale       = ContentScale.Fit,
                                        modifier           = Modifier.fillMaxWidth()
                                    )

                                    // Close button top right
                                    IconButton(
                                        onClick  = { showFullScreenPic = false },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            Icons.Default.Close, null,
                                            tint     = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Change photo button at bottom
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 48.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                showFullScreenPic = false
                                                imagePicker.launch("image/*")
                                            },
                                            shape  = RoundedCornerShape(50),
                                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color.Black.copy(alpha = 0.5f),
                                                contentColor   = Color.White
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.CameraAlt, null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.change_photo), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Avatar box — tap opens full screen, camera badge opens picker
                        Box(
                            modifier         = Modifier.size(88.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            // Main avatar — tap = full screen
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                PrimaryBlue,
                                                AccentGreen
                                            )
                                        )
                                    )
                                    .clickable {
                                        if (!state.profilePicUrl.isNullOrBlank()) {
                                            showFullScreenPic = true   // ← open full screen
                                        } else {
                                            imagePicker.launch("image/*")  // ← no pic yet, open picker
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    state.isUploadingPic -> {
                                        CircularProgressIndicator(
                                            color       = Color.White,
                                            modifier    = Modifier.size(28.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    !state.profilePicUrl.isNullOrBlank() -> {
                                        println(">>> Coil loading URL: '${state.profilePicUrl}'")
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(state.profilePicUrl)
                                                .crossfade(true)
                                                .allowHardware(false)
                                                .memoryCacheKey(state.profilePicUrl)
                                                .diskCacheKey(state.profilePicUrl)
                                                .build(),
                                            contentDescription = "Profile picture",
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            onError   = { println(">>> Coil error: ${it.result.throwable}") },
                                            onSuccess = { println(">>> Coil success") }
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text       = state.userName.take(2).uppercase().ifEmpty { "?" },
                                            color      = Color.White,
                                            fontSize   = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Camera badge — always opens image picker directly
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue)
                                    .clickable { imagePicker.launch("image/*") },  // ← always picker
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    tint     = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        state.userName.ifEmpty { stringResource(R.string.loading) },
                        color      = TextPrimary,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        state.userEmail,
                        color    = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    // Target role badge
                    if (state.targetRole.isNotBlank()) {
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                state.targetRole,
                                color    = PrimaryBlue,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick  = viewModel::showEditProfile,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Interview Preferences ────────────────────────────────────────
            SettingsSection(stringResource(R.string.interview_preferences)) {
                ToggleSettingRow(
                    icon     = Icons.Outlined.GraphicEq,
                    title    = stringResource(R.string.sound_effects),
                    subtitle = stringResource(R.string.play_sounds_during_interview_sessions),
                    checked  = state.soundEnabled,
                    onToggle = viewModel::toggleSound
                )
                SettingsDivider()
                ToggleSettingRow(
                    icon     = Icons.Outlined.Timer,
                    title    = stringResource(R.string.auto_submit),
                    subtitle = stringResource(R.string.auto_submit_after_silence_is_detected),
                    checked  = state.autoSubmitEnabled,
                    onToggle = viewModel::toggleAutoSubmit
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── App Settings ─────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.app_settings)) {
                ToggleSettingRow(
                    icon     = Icons.Outlined.Notifications,
                    title    = stringResource(R.string.notifications),
                    subtitle = stringResource(R.string.daily_practice_reminders),
                    checked  = state.notificationsEnabled,
                    onToggle = viewModel::toggleNotifications
                )
                SettingsDivider()
                ToggleSettingRow(
                    icon     = Icons.Outlined.DarkMode,
                    title    = stringResource(R.string.dark_mode),
                    subtitle = stringResource(R.string.use_dark_theme_throughout_the_app),
                    checked  = state.darkModeEnabled,
                    onToggle = viewModel::toggleDarkMode
                )

                SettingsDivider()
                NavigationSettingRow(
                    icon     = Icons.Outlined.Language,
                    title    = stringResource(R.string.language),
                    subtitle = LanguagePreferences.SUPPORTED_LANGUAGES[state.selectedLanguage] ?: "English",
                    onClick  = viewModel::showLanguageDialog
                )
            }

            Spacer(Modifier.height(16.dp))



            // ─── Account ──────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.account)) {
                NavigationSettingRow(
                    icon     = Icons.Outlined.Person,
                    title    =  stringResource(R.string.edit_profile),
                    subtitle = stringResource(R.string.update_your_name_and_target_role),
                    onClick  = viewModel::showEditProfile
                )
                SettingsDivider()
                NavigationSettingRow(
                    icon     = Icons.Outlined.Share,
                    title    = stringResource(R.string.export_progress),
                    subtitle = stringResource(R.string.share_your_interview_history),
                    onClick  = { navController.navigate(Screen.Progress.route) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── About ────────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.about)) {
                NavigationSettingRow(
                    icon     = Icons.Outlined.Star,
                    title    = stringResource(R.string.rate_the_app),
                    subtitle = stringResource(R.string.enjoying_the_app_leave_a_review),
                    onClick  = { /* open Play Store */ }
                )
                SettingsDivider()
                NavigationSettingRow(
                    icon     = Icons.Outlined.BugReport,
                    title    = stringResource(R.string.report_a_bug),
                    subtitle = stringResource(R.string.help_us_improve_the_experience),
                    onClick  = { /* open email */ }
                )
                SettingsDivider()
                InfoSettingRow(
                    icon  = Icons.Outlined.Info,
                    title = stringResource(R.string.app_version),
                    value = "1.0.0"
                )
            }

            Spacer(Modifier.height(24.dp))

            // Error snackbar
            AnimatedVisibility(state.error != null) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    color  = AccentRed.copy(alpha = 0.1f),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        state.error ?: "",
                        color    = AccentRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ─── Logout ───────────────────────────────────────────────────────
            OutlinedButton(
                onClick  = viewModel::showLogoutDialog,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, AccentRed.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AccentRed.copy(alpha = 0.08f),
                    contentColor   = AccentRed
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.log_out), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick  = viewModel::showDeleteDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.delete_account),
                    color      = AccentRed.copy(alpha = 0.5f),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Edit Profile Dialog ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    state: SettingsUiState,
    roles: List<String>,
    onNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!state.isSavingProfile) onDismiss() },
        containerColor   = SurfaceMid,
        title = {
            Text( stringResource(R.string.edit_profile), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Name field
                OutlinedTextField(
                    value         = state.editName,
                    onValueChange = onNameChange,
                    label         = { Text( stringResource(R.string.full_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryBlue,
                        unfocusedBorderColor = SurfaceLight,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedLabelColor    = PrimaryBlue,
                        unfocusedLabelColor  = TextSecondary
                    )
                )

                // Role dropdown
                ExposedDropdownMenuBox(
                    expanded          = roleMenuExpanded,
                    onExpandedChange  = { roleMenuExpanded = it },
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = state.editTargetRole.ifEmpty { "Select Role" },
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text( stringResource(R.string.target_role)) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(roleMenuExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PrimaryBlue,
                            unfocusedBorderColor = SurfaceLight,
                            focusedTextColor     = TextPrimary,
                            unfocusedTextColor   = TextPrimary,
                            focusedLabelColor    = PrimaryBlue,
                            unfocusedLabelColor  = TextSecondary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                        containerColor   = SurfaceMid
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text    = { Text(role, color = TextPrimary) },
                                onClick = {
                                    onRoleChange(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = onSave,
                enabled  = state.editName.isNotBlank() && !state.isSavingProfile,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (state.isSavingProfile) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSavingProfile) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}

// ─── Delete Account Dialog ────────────────────────────────────────────────────

@Composable
private fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceMid,
        icon = {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DeleteForever, null, tint = AccentRed, modifier = Modifier.size(28.dp))
            }
        },
        title = { Text( stringResource(R.string.delete_account), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text  = {
            Text(
                stringResource(R.string.this_will_permanently_delete_your_account_and_all_progress_this_cannot_be_undone),
                color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) { Text(stringResource(R.string.delete_forever), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text( stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}

// ─── Shared composables ───────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            title.uppercase(),
            color         = TextSecondary,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = SurfaceMid)
        ) { Column(content = content) }
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    languages: Map<String, String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceMid,
        title = {
            Text(
                stringResource(R.string.language),
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(code) }
                            .background(
                                if (code == currentLanguage)
                                    PrimaryBlue.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = TextPrimary, fontSize = 15.sp)
                        if (code == currentLanguage) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint     = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ToggleSettingRow(
    icon: ImageVector, title: String,
    subtitle: String, checked: Boolean, onToggle: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title,    color = TextPrimary,   fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = PrimaryBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceLight
            )
        )
    }
}

@Composable
private fun NavigationSettingRow(
    icon: ImageVector, title: String,
    subtitle: String, onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title,    color = TextPrimary,   fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun InfoSettingRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(14.dp))
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier         = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = SurfaceLight, thickness = 0.5.dp)
}

@Composable
private fun LogoutConfirmDialog(isLoading: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor   = SurfaceMid,
        icon = {
            Box(Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AccentRed.copy(0.15f)), Alignment.Center) {
                Icon(Icons.Default.Logout, null, tint = AccentRed, modifier = Modifier.size(28.dp))
            }
        },
        title = { Text( stringResource(R.string.log_out), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text  = { Text(
            text = stringResource(R.string.you_ll_need_to_sign_in_again_to_access_your_sessions),
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        ) },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                if (isLoading)CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
                else Text(stringResource(R.string.yes_log_out), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}