package com.example.mathsprint.feature.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.LimeGreen
import com.example.mathsprint.core.theme.GoldYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Theme colors
    val darkBackground = Color(0xFF0A0A0A)
    val cardBackground = Color(0xFF1A1A1A)
    val accentColor = LimeGreen
    val textWhite = Color.White
    val textGray = Color(0xFF888888)
    val inputBackground = Color(0xFF1E1E1E)
    val borderColor = Color(0xFF333333)
    val highlightColor = Color(0xFF39D353).copy(alpha = 0.15f)

    // Animation states
    val logoScale = remember { Animatable(0.5f) }
    val contentAlpha = remember { Animatable(0f) }

    // Sign up button animation
    var isSignupButtonPressed by remember { mutableStateOf(false) }
    val signupButtonScale by animateFloatAsState(
        targetValue = if (isSignupButtonPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "signupScale"
    )

    fun isValidEmail(email: String): Boolean {
        return email.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex())
    }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        contentAlpha.animateTo(1f, tween(600))
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Title
            Text(
                "Create Account",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textWhite,
                    fontSize = 32.sp
                ),
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Start your math journey today!",
                style = MaterialTheme.typography.bodyMedium.copy(color = textGray),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))


            // Form Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name", color = textGray) },
                placeholder = { Text("Enter your name", color = textGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                leadingIcon = { Icon(Icons.Default.Person, null, tint = textGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = highlightColor,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textGray
                ),
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.lowercase().trim()
                    emailError = if (it.isNotEmpty() && !isValidEmail(it)) {
                        "Please enter a valid email"
                    } else ""
                },
                label = { Text("Email", color = textGray) },
                placeholder = { Text("example@gmail.com", color = textGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                isError = emailError.isNotEmpty(),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = textGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = highlightColor,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = borderColor,
                    errorBorderColor = Color.Red,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textGray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                supportingText = { if (emailError.isNotEmpty()) Text(emailError, color = Color.Red) },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = textGray) },
                placeholder = { Text("••••••••", color = textGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = textGray) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = textGray)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = highlightColor,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textGray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = textGray) },
                placeholder = { Text("••••••••", color = textGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = textGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = highlightColor,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textGray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                singleLine = true
            )

            // Error message
            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = {
                    scope.launch {
                        isSignupButtonPressed = true
                        delay(100)
                        isSignupButtonPressed = false
                    }
                    viewModel.register(name, email, password, confirmPassword)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(signupButtonScale)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = accentColor.copy(alpha = 0.4f),
                        spotColor = accentColor.copy(alpha = 0.4f)
                    ),
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && !uiState.isLoading && emailError.isEmpty(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("CREATE ACCOUNT", color = Color.Black, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? ", color = textGray)
                Text(
                    "Log In",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

