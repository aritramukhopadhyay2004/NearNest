package com.example.data.auth

import java.util.UUID

/**
 * Authentication methods supported by NearNest
 */
enum class AuthMethod(val displayName: String, val channelName: String) {
    PHONE_SMS("Mobile Phone", "SMS"),
    GOOGLE_GMAIL("Google Account", "Gmail")
}

/**
 * Authentication lifecycle states
 */
enum class AuthState {
    UNAUTHENTICATED,
    ENTER_OTP,
    AUTHENTICATED
}

/**
 * Persisted user session data
 */
data class UserSession(
    val userId: String,
    val displayName: String,
    val authMethod: AuthMethod,
    val contact: String, // E.g. "+91 98765 43210" or "alex.smith@gmail.com"
    val avatarColorHex: Long = 0xFF0284C7,
    val familyNestCode: String = "NEST-4821",
    val token: String = UUID.randomUUID().toString(),
    val loginTimestamp12h: String = "Just now",
    val isVerified: Boolean = true
)

/**
 * Active OTP verification challenge
 */
data class OtpChallenge(
    val target: String,
    val method: AuthMethod,
    val code: String, // 6-digit code e.g. "582914"
    val timestampEpoch: Long = System.currentTimeMillis(),
    val expirySeconds: Int = 120,
    val deliveryMessage: String = ""
) {
    fun isExpired(): Boolean {
        val elapsed = (System.currentTimeMillis() - timestampEpoch) / 1000
        return elapsed > expirySeconds
    }

    fun remainingSeconds(): Int {
        val elapsed = ((System.currentTimeMillis() - timestampEpoch) / 1000).toInt()
        return (expirySeconds - elapsed).coerceAtLeast(0)
    }
}
