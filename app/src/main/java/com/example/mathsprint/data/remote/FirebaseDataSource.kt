package com.example.mathsprint.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val usersRef get() = database.reference.child("users")

    suspend fun saveUserData(uid: String, data: Map<String, Any>) {
        try {
            Log.d("FirebaseDataSource", "Saving user data for $uid")
            usersRef.child(uid).setValue(data).await()
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error saving user data", e)
            throw e
        }
    }

    suspend fun deleteUserData(uid: String) {
        try {
            Log.d("FirebaseDataSource", "Deleting user data for $uid")
            usersRef.child(uid).removeValue().await()
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error deleting user data", e)
            throw e
        }
    }

    suspend fun updateLastActive(uid: String) {
        try {
            usersRef.child(uid).child("lastActiveAt")
                .setValue(System.currentTimeMillis()).await()
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error updating last active", e)
            // Don't throw - this is not critical
        }
    }

    suspend fun updateUserEmail(uid: String, newEmail: String) {
        try {
            Log.d("FirebaseDataSource", "Updating email for $uid to $newEmail")
            usersRef.child(uid).child("email").setValue(newEmail).await()
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error updating user email", e)
            throw e
        }
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return try {
            Log.d("FirebaseDataSource", "Getting user data for $uid")
            val snapshot = usersRef.child(uid).get().await()
            @Suppress("UNCHECKED_CAST")
            snapshot.value as? Map<String, Any>
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error getting user data", e)
            null
        }
    }

    fun getCurrentUserId() = auth.currentUser?.uid

    suspend fun deleteAccountAndData(uid: String) {
        try {
            Log.d("FirebaseDataSource", "Deleting account and data for $uid")
            deleteUserData(uid)
            auth.currentUser?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error deleting account", e)
            throw e
        }
    }
}

