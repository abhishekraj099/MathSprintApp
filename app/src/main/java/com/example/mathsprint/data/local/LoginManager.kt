package com.example.mathsprint.data.local

import android.content.Context
import com.example.mathsprint.data.local.entity.UserEntity
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class LoginManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("mathsprint_prefs", Context.MODE_PRIVATE)
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val gson = Gson()

    fun saveLoginState(email: String) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("email", email)
            apply()
        }

        // Update last login in Firebase
        val userId = email.replace(".", "_")
        usersRef.child(userId).child("lastActive").setValue(System.currentTimeMillis())
        usersRef.child(userId).child("isActive").setValue(true)
    }

    fun saveUserProfile(userProfile: UserEntity) {
        // Save to SharedPreferences for offline access
        prefs.edit().apply {
            putString("user_profile", gson.toJson(userProfile))
            apply()
        }

        // Save to Firebase Database
        val userId = userProfile.uid.replace(".", "_")
        val userMap = hashMapOf(
            "uid" to userProfile.uid,
            "name" to userProfile.name,
            "email" to userProfile.email,
            "coins" to userProfile.coins,
            "gems" to userProfile.gems,
            "xp" to userProfile.xp,
            "streak" to userProfile.streak,
            "lives" to userProfile.lives,
            "rankLevel" to userProfile.rankLevel,
            "winRate" to userProfile.winRate,
            "skillLevel" to userProfile.skillLevel,
            "lastActiveAt" to System.currentTimeMillis(),
            "isActive" to true
        )

        usersRef.child(userId).setValue(userMap)
    }

    fun getUserProfile(): UserEntity? {
        val json = prefs.getString("user_profile", null) ?: return null
        return try {
            gson.fromJson(json, UserEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateCoins(coins: Int) {
        getUserProfile()?.let { profile ->
            val updated = profile.copy(coins = coins)
            saveUserProfile(updated)

            // Update Firebase
            val userId = profile.uid.replace(".", "_")
            usersRef.child(userId).child("coins").setValue(coins)
        }
    }

    fun updateGems(gems: Int) {
        getUserProfile()?.let { profile ->
            val updated = profile.copy(gems = gems)
            saveUserProfile(updated)

            // Update Firebase
            val userId = profile.uid.replace(".", "_")
            usersRef.child(userId).child("gems").setValue(gems)
        }
    }

    fun updateXP(xp: Int) {
        getUserProfile()?.let { profile ->
            val updated = profile.copy(xp = xp)
            saveUserProfile(updated)

            val userId = profile.uid.replace(".", "_")
            usersRef.child(userId).child("xp").setValue(xp)
        }
    }

    fun addRewards(coinsAmount: Int, gemsAmount: Int) {
        getUserProfile()?.let { profile ->
            val newCoins = profile.coins + coinsAmount
            val newGems = profile.gems + gemsAmount
            val updated = profile.copy(coins = newCoins, gems = newGems)
            saveUserProfile(updated)

            val userId = profile.uid.replace(".", "_")
            usersRef.child(userId).updateChildren(
                hashMapOf(
                    "coins" to newCoins,
                    "gems" to newGems
                ) as Map<String, Any>
            )
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getEmail(): String? = prefs.getString("email", null)

    fun clearLoginState() {
        val email = getEmail()
        prefs.edit().clear().apply()

        email?.let {
            usersRef.child(it.replace(".", "_"))
                .child("isActive")
                .setValue(false)
        }
    }
}

