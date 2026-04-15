package com.example.mathsprint.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val DEBUG_MODE = false
private const val DEBUG_OTP = "123456"
private const val API_BASE_URL = "https://new-otp-sand.vercel.app/api/auth"

suspend fun sendOtpApi(email: String): Pair<Boolean, String?> {
    if (DEBUG_MODE) {
        Log.d("OTP_API", "DEBUG: OTP sent to $email. Use: $DEBUG_OTP")
        return Pair(true, "✅ DEBUG MODE: Use OTP 123456")
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val url = "$API_BASE_URL/send-otp"

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
                    val message = json.optString("message", "OTP sent successfully")
                    Pair(success, message)
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("OTP_API", "Error sending OTP", e)
            Pair(false, "Network error: ${e.message}")
        }
    }
}

suspend fun verifyOtpApi(email: String, otp: String): Pair<Boolean, String?> {
    if (DEBUG_MODE) {
        Log.d("OTP_API", "DEBUG: Verifying OTP $otp for $email")
        return if (otp == DEBUG_OTP) {
            Pair(true, "✅ DEBUG: Login successful!")
        } else {
            Pair(false, "❌ Invalid OTP. Use: 123456")
        }
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val url = "$API_BASE_URL/verify-otp"

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
                    if (success) {
                        Pair(true, "Login successful!")
                    } else {
                        Pair(false, "Invalid OTP")
                    }
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("OTP_API", "Error verifying OTP", e)
            Pair(false, "Network error: ${e.message}")
        }
    }
}

suspend fun loginApi(email: String, password: String): Pair<Boolean, String?> {
    if (DEBUG_MODE) {
        Log.d("LOGIN_API", "DEBUG: Login attempt for $email")
        return Pair(true, "✅ DEBUG: Login successful!")
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val url = "$API_BASE_URL/login"

    return withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
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
                    val message = json.optString("message", "Login successful")
                    Pair(success, message)
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("LOGIN_API", "Error logging in", e)
            Pair(false, "Network error: ${e.message}")
        }
    }
}

suspend fun registerApi(name: String, email: String, password: String): Pair<Boolean, String?> {
    if (DEBUG_MODE) {
        Log.d("REGISTER_API", "DEBUG: Register attempt for $email")
        return Pair(true, "✅ DEBUG: Registration successful!")
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val url = "$API_BASE_URL/register"

    return withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("name", name)
                put("email", email)
                put("password", password)
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
                    val message = json.optString("message", "Registration successful")
                    Pair(success, message)
                } else {
                    Pair(false, "Server error: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("REGISTER_API", "Error registering", e)
            Pair(false, "Network error: ${e.message}")
        }
    }
}

