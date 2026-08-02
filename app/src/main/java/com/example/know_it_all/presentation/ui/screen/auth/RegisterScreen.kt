package com.example.know_it_all.presentation.ui.screen.auth
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.AcidGreenDark
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.WarmGray
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.ErrorContainerColor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.presentation.ui.navigation.Screen
import com.example.know_it_all.presentation.viewmodel.AuthViewModel

private fun nameError(name: String) = when {
    name.isBlank() -> "Full name is required"
    name.length < 2 -> "At least 2 characters"
    else -> null
}

private fun regEmailError(email: String) = when {
    email.isBlank() -> "Email is required"
    !"""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$""".toRegex().matches(email) -> "Invalid email format"
    else -> null
}

private fun regPasswordError(password: String) = when {
    password.isBlank() -> "Password is required"
    password.length < 6 -> "At least 6 characters"
    password.length > 128 -> "Password too long"
    else -> null
}

private fun confirmError(password: String, confirm: String) = when {
    confirm.isBlank() -> "Confirm your password"
    password != confirm -> "Passwords do not match"
    else -> null
}

/**
 * Fixes applied:
 *  1. authState.token removed (no longer in AuthUiState).
 *  2. clearError() called before navigation.
 *  3. Keyboard IME actions wire all fields in sequence.
 *  4. Design aligned to KnowItAllTheme — cream bg, near-black type,
 *     acid green CTA, consistent with LoginScreen.
 */
@Composable
fun RegisterScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            authViewModel.clearError()
            navController.navigate(Screen.Radar.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val nameErr    = if (showErrors) nameError(name) else null
    val emailErr   = if (showErrors) regEmailError(email) else null
    val passErr    = if (showErrors) regPasswordError(password) else null
    val confirmErr = if (showErrors) confirmError(password, confirmPassword) else null

    val isFormValid = nameError(name) == null && regEmailError(email) == null &&
            regPasswordError(password) == null && confirmError(password, confirmPassword) == null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Logo mark
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(NearBlack, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Black,
                    color = AcidGreen)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Join\nKnowItAll.",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack,
                lineHeight = 46.sp,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start sharing your skills today.",
                fontSize = 15.sp,
                color = CharcoalGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Full name
            FieldLabel("Full Name")
            KnowItAllTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Jane Smith",
                error = nameErr,
                enabled = !authState.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email
            FieldLabel("Email address")
            KnowItAllTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "your@email.com",
                error = emailErr,
                enabled = !authState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            FieldLabel("Password")
            KnowItAllTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••",
                error = passErr,
                enabled = !authState.isLoading,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = WarmGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm password
            FieldLabel("Confirm Password")
            KnowItAllTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "••••••",
                error = confirmErr,
                enabled = !authState.isLoading,
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = WarmGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        showErrors = true
                        if (isFormValid) authViewModel.register(name, email, password)
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // API error
            AnimatedVisibility(
                visible = authState.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                authState.error?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorContainerColor, RoundedCornerShape(10.dp))
                            .border(1.dp, ErrorRed.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(err, fontSize = 13.sp, color = ErrorRed)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA
            Button(
                onClick = {
                    showErrors = true
                    if (isFormValid) authViewModel.register(name, email, password)
                },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidGreen,
                    contentColor = NearBlack,
                    disabledContainerColor = AcidGreen.copy(alpha = 0.4f),
                    disabledContentColor = NearBlack.copy(alpha = 0.4f)
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NearBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?", fontSize = 13.sp,
                    color = CharcoalGray)
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = !authState.isLoading
                ) {
                    Text("Sign in", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = NearBlack)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}