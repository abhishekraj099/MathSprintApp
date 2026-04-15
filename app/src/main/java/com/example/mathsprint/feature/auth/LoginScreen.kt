package com.example.mathsprint.feature.auth

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.LimeGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val OTP_DEBUG_MODE = false
private const val DEBUG_OTP = "123456"

suspend fun sendOtpApi(email: String): Pair<Boolean, String?> {
    if (OTP_DEBUG_MODE) {
        Log.d("OTP_API", "DEBUG: OTP sent to $email. Use: $DEBUG_OTP")
        return Pair(true, "✅ DEBUG MODE: Use OTP $DEBUG_OTP")
    }
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    val url = "https://new-otp-sand.vercel.app/api/auth/send-otp"
    return withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply { put("email", email) }
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = client.newCall(request).execute()
            response.use { resp ->
                val responseBody = resp.body?.string() ?: "{}"
                if (resp.code == 200) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "OTP sent")
                    Pair(success, message)
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }
}

suspend fun verifyOtpApi(email: String, otp: String): Pair<Boolean, String?> {
    if (OTP_DEBUG_MODE) {
        Log.d("OTP_API", "DEBUG: Verifying OTP for $email")
        return if (otp == DEBUG_OTP) {
            Pair(true, "✅ DEBUG: Login successful!")
        } else {
            Pair(false, "❌ Invalid OTP. Use: $DEBUG_OTP")
        }
    }
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    val url = "https://new-otp-sand.vercel.app/api/auth/verify-otp"
    return withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("otp", otp)
            }
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = client.newCall(request).execute()
            response.use { resp ->
                val responseBody = resp.body?.string() ?: "{}"
                if (resp.code == 200) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", false)
                    if (success) Pair(true, "Login successful!") else Pair(false, "Invalid OTP")
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Theme colors
    val darkBackground = Color(0xFF0A0A0A)
    val accentColor = LimeGreen
    val textWhite = Color.White
    val textGray = Color(0xFF888888)
    val inputBackground = Color(0xFF1E1E1E)
    val borderColor = Color(0xFF333333)
    val highlightColor = Color(0xFF39D353).copy(alpha = 0.15f)

    // Animation states
    val logoScale = remember { Animatable(0.5f) }
    val contentAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(-50f) }

    // Email field focus
    var isEmailFocused by remember { mutableStateOf(false) }
    val emailBgColor by animateColorAsState(
        targetValue = if (isEmailFocused) highlightColor else inputBackground,
        animationSpec = tween(200),
        label = "emailBg"
    )
    val emailBorderColor by animateColorAsState(
        targetValue = if (isEmailFocused) accentColor else borderColor,
        animationSpec = tween(200),
        label = "emailBorder"
    )
    val emailElevation by animateDpAsState(
        targetValue = if (isEmailFocused) 8.dp else 0.dp,
        animationSpec = tween(200),
        label = "emailElevation"
    )

    // OTP field focus
    var isOtpFocused by remember { mutableStateOf(false) }
    val otpBgColor by animateColorAsState(
        targetValue = if (isOtpFocused) highlightColor else inputBackground,
        animationSpec = tween(200),
        label = "otpBg"
    )
    val otpBorderColor by animateColorAsState(
        targetValue = if (isOtpFocused) accentColor else borderColor,
        animationSpec = tween(200),
        label = "otpBorder"
    )

    // Login button animation
    var isLoginButtonPressed by remember { mutableStateOf(false) }
    val loginButtonScale by animateFloatAsState(
        targetValue = if (isLoginButtonPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "loginScale"
    )

    fun isValidEmail(email: String): Boolean {
        return email.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex())
    }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        titleOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        contentAlpha.animateTo(1f, tween(600))
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate(Screen.Home.route) {
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
            Spacer(Modifier.height(48.dp))

            // Title with animation
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textWhite,
                    fontSize = 36.sp
                ),
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Enter your email and verify with OTP",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textGray
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(Modifier.height(32.dp))

            // Email Field with focus animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = emailElevation,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = accentColor.copy(alpha = 0.3f),
                        spotColor = accentColor.copy(alpha = 0.3f)
                    )
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.lowercase().trim()
                        emailError = if (it.isNotEmpty() && !isValidEmail(it)) {
                            "Please enter a valid email address"
                        } else ""
                    },
                    label = { Text("Email", color = if (isEmailFocused) accentColor else textGray) },
                    placeholder = { Text("example@gmail.com", color = textGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isEmailFocused = focusState.isFocused
                        },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !showOtpField,
                    isError = emailError.isNotEmpty(),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = if (isEmailFocused) accentColor else textGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        errorTextColor = Color.White,
                        focusedContainerColor = emailBgColor,
                        unfocusedContainerColor = inputBackground,
                        disabledContainerColor = inputBackground,
                        focusedBorderColor = emailBorderColor,
                        unfocusedBorderColor = borderColor,
                        disabledBorderColor = borderColor,
                        errorBorderColor = Color.Red,
                        cursorColor = accentColor,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = textGray,
                        disabledLabelColor = textGray
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    supportingText = {
                        if (emailError.isNotEmpty()) {
                            Text(emailError, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // OTP Field with focus animation (shown after sending OTP)
            AnimatedVisibility(
                visible = showOtpField,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(
                            elevation = if (isOtpFocused) 8.dp else 0.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = accentColor.copy(alpha = 0.3f),
                            spotColor = accentColor.copy(alpha = 0.3f)
                        )
                ) {
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otpInput = it
                            }
                        },
                        label = { Text("Enter OTP", color = if (isOtpFocused) accentColor else textGray) },
                        placeholder = { Text("6-digit code", color = textGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isOtpFocused = focusState.isFocused
                            },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isVerifying,
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (isOtpFocused) accentColor else textGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedContainerColor = otpBgColor,
                            unfocusedContainerColor = inputBackground,
                            disabledContainerColor = inputBackground,
                            focusedBorderColor = otpBorderColor,
                            unfocusedBorderColor = borderColor,
                            disabledBorderColor = borderColor,
                            cursorColor = accentColor,
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = textGray,
                            disabledLabelColor = textGray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text("Check your email for the 6-digit code", color = textGray)
                        }
                    )
                }
            }

            // Error message
            uiState.error?.let { error ->
                Spacer(Modifier.height(16.dp))
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

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF39D353).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF51CF66),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Login/Verify Button
            Button(
                onClick = {
                    scope.launch {
                        isLoginButtonPressed = true
                        delay(100)
                        isLoginButtonPressed = false
                    }

                    if (!showOtpField) {
                        // SEND OTP
                        if (!isValidEmail(email)) {
                            message = "Please enter a valid email address"
                        } else {
                            isLoading = true
                            scope.launch {
                                // Check if user exists in database
                                val userExists = withContext(Dispatchers.IO) {
                                    try {
                                        viewModel.checkUserExists(email)
                                    } catch (e: Exception) {
                                        false
                                    }
                                }

                                if (!userExists) {
                                    isLoading = false
                                    message = "❌ User not found. Please create an account first!"
                                    return@launch
                                }

                                // User exists, send OTP
                                val result = withContext(Dispatchers.IO) {
                                    sendOtpApi(email)
                                }
                                isLoading = false

                                if (result.first) {
                                    showOtpField = true
                                    message = "✅ OTP sent to your email!"
                                    otpInput = ""
                                } else {
                                    message = result.second ?: "Failed to send OTP"
                                }
                            }
                        }
                    } else {
                        // VERIFY OTP
                        if (otpInput.length != 6) {
                            message = "Please enter a valid 6-digit OTP"
                        } else {
                            isVerifying = true
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        verifyOtpApi(email, otpInput)
                                    }
                                    isVerifying = false

                                    if (result.first) {
                                        Log.d("LoginScreen", "OTP verified successfully for $email")
                                        message = "Login successful!"
                                        // Call the viewModel to complete login
                                        viewModel.loginWithEmail(email)
                                        delay(1500)
                                    } else {
                                        message = result.second ?: "Invalid OTP"
                                    }
                                } catch (e: Exception) {
                                    isVerifying = false
                                    Log.e("LoginScreen", "Error verifying OTP", e)
                                    message = "Error: ${e.message}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(loginButtonScale)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = accentColor.copy(alpha = 0.4f),
                        spotColor = accentColor.copy(alpha = 0.4f)
                    ),
                enabled = if (!showOtpField) {
                    email.isNotEmpty() && isValidEmail(email) && !isLoading
                } else {
                    otpInput.length == 6 && !isVerifying
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading || isVerifying) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (!showOtpField) "SEND OTP" else "VERIFY & LOGIN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
                }
            }

            // Change Email Link (when OTP is shown)
            AnimatedVisibility(
                visible = showOtpField,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TextButton(
                    onClick = {
                        showOtpField = false
                        otpInput = ""
                        message = ""
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "Change Email Address",
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New here? ", color = textGray)
                Text(
                    "Sign Up",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

