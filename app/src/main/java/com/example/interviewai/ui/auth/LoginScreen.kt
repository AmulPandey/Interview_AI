package com.example.interviewai.ui.auth



import androidx.compose.animation.*
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
import com.example.interviewai.R
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.interviewai.ui.navigation.Screen
import com.example.interviewai.ui.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
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
            Spacer(Modifier.height(72.dp))

            // Logo / branding
            Image(
                painter = painterResource(id = R.drawable.cropped_circle_image),
                contentDescription = "Target",
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.app_name),
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                stringResource(R.string.ace_every_interview),
                color = TextSecondary,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(48.dp))

            // Email
            AuthTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.sign_in),
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
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    viewModel.login()
                }
            )

            // Error
            AnimatedVisibility(visible = state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    state.error ?: "",
                    color = AccentRed,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = viewModel::login,
                enabled = state.isFormValid && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = TextPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.sign_in), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Navigate to Register
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.don_t_have_an_account), color = TextSecondary, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(stringResource(R.string.sign_up), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}