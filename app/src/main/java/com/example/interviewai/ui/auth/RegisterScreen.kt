package com.example.interviewai.ui.auth


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.R
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var roleMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Register.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryDark, SurfaceDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Text("🎯", fontSize = 44.sp)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.create_account), color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.start_your_interview_journey), color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(36.dp))

            // Name
            AuthTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(R.string.full_name),
                leadingIcon = Icons.Default.Person,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            Spacer(Modifier.height(14.dp))

            // Email
            AuthTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.email),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            Spacer(Modifier.height(14.dp))

            // Password
            AuthTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = state.passwordVisible,
                onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            Spacer(Modifier.height(14.dp))

            // Confirm Password
            AuthTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.confirm_password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = state.passwordVisible,
                onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                isError = state.confirmPassword.isNotEmpty() && !state.passwordMatch,
                errorMessage = stringResource(R.string.passwords_do_not_match),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )
            Spacer(Modifier.height(14.dp))

            // Target Role Dropdown
            ExposedDropdownMenuBox(
                expanded = roleMenuExpanded,
                onExpandedChange = { roleMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.targetRole.ifEmpty { "Select Target Role" },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text(stringResource(R.string.target_role)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleMenuExpanded) },
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false },
                ) {
                    viewModel.roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role, color = TextPrimary) },
                            onClick = {
                                viewModel.onTargetRoleChange(role)
                                roleMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Error
            AnimatedVisibility(visible = state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(state.error ?: "", color = AccentRed, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(28.dp))

            // Register button
            Button(
                onClick = viewModel::register,
                enabled = state.isFormValid && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.already_have_an_account), color = TextSecondary, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text(stringResource(R.string.sign_in), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Shared Composables ───────────────────────────────────────────────────────

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, null, tint = TextSecondary) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onTogglePasswordVisibility?.invoke() }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = authTextFieldColors()
        )
        AnimatedVisibility(visible = isError && errorMessage.isNotEmpty()) {
            Text(errorMessage, color = AccentRed, fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = PrimaryBlue,
    unfocusedBorderColor = SurfaceLight,
    focusedLabelColor    = PrimaryBlue,
    unfocusedLabelColor  = TextSecondary,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    cursorColor          = PrimaryBlue,
    focusedLeadingIconColor   = PrimaryBlue,
    unfocusedLeadingIconColor = TextSecondary
)