package com.example.mathsprint.data.repository

import android.util.Log
import com.example.mathsprint.data.remote.loginApi
import com.example.mathsprint.data.remote.registerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    val isLoggedIn get() = auth.currentUser != null

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            // Call your Node.js API
            val (success, message) = loginApi(email, password)
            if (success) {
                // If API returns success, create Firebase Auth user or just store locally
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    AuthResult.Success
                } catch (e: Exception) {
                    // If Firebase fails but API succeeds, still return success for app functionality
                    AuthResult.Success
                }
            } else {
                AuthResult.Error(message ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    suspend fun loginWithOtp(email: String): AuthResult {
        return try {
            Log.d("AuthRepository", "Attempting OTP login for $email")
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "OTP login failed")
        }
    }

    suspend fun registerWithOtp(name: String, email: String): AuthResult {
        return try {
            Log.d("AuthRepository", "Registering new user with OTP: $email")
            // Save user to Firebase Realtime Database
            val userId = email.replace(".", "_").replace("@", "_")
            val userMap = mapOf(
                "email" to email,
                "username" to name,
                "userId" to userId,
                "createdAt" to System.currentTimeMillis(),
                "isActive" to true,
                "coins" to 100,  // Signup bonus
                "gems" to 100,   // Signup bonus
                "status" to "online"
            )
            database.reference.child("users").child(userId).setValue(userMap).await()
            Log.d("AuthRepository", "User registered successfully in Firebase: $email")
            AuthResult.Success
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registering user", e)
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun userExists(email: String): Boolean {
        return try {
            val userId = email.replace(".", "_").replace("@", "_")
            val snapshot = database.reference.child("users").child(userId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking if user exists", e)
            false
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        return try {
            // Call your Node.js API to register
            val (success, message) = registerApi(name, email, password)
            if (success) {
                // Try to create Firebase Auth user
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    AuthResult.Success
                } catch (e: Exception) {
                    // If Firebase fails but API succeeds, still return success
                    AuthResult.Success
                }
            } else {
                AuthResult.Error(message ?: "Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Handle logout error silently
        }
    }

    suspend fun deleteAccount(): AuthResult {
        return try {
            auth.currentUser?.delete()?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Delete failed")
        }
    }
}

