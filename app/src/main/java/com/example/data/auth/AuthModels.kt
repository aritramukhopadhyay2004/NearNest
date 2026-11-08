package com.example.data.auth

import java.util.UUID

/**
 * Authentication methods supported by NearNest
 */
enum class AuthMethod(val displayName: String, val channelName: String) {
    PHONE_SMS("Mobile Phone", "SMS"),
    GOOGLE_GMAIL("Google Account", "Gmail"),
    CLERK_AUTH("Clerk Auth", "Clerk")
}

/**
 * Authentication Screen Pages for Design Reference & Clerk integration
 */
enum class AuthPage {
    ONBOARDING,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    OTP_VERIFY
}

/**
 * Authentication lifecycle states
 */
enum class AuthState {
    UNAUTHENTICATED,
    ENTER_OTP,
    NEST_SETUP,
    AUTHENTICATED
}

/**
 * Unique Family Nest Model
 * Every family has a unique Nest No and secret Nest Pass for instant device pairing.
 */
data class FamilyNest(
    val nestNo: String = "NEST-7492",
    val nestPass: String = "PASS-4821",
    val nestName: String = "The Family Nest",
    val creatorName: String = "Family Admin",
    val createdAt12h: String = "12:00 AM",
    val membersCount: Int = 3
) {
    fun getShareablePassText(): String {
        return "Join our family on NearNest!\nNest No: $nestNo\nNest Pass: $nestPass\nOpen NearNest and enter our Nest Pass to connect instantly."
    }
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
    val nestNo: String = "NEST-7492",
    val nestPass: String = "PASS-4821",
    val nestName: String = "The Family Nest",
    val token: String = UUID.randomUUID().toString(),
    val loginTimestamp12h: String = "Just now",
    val isVerified: Boolean = true
) {
    // Backwards-compatible alias for existing callers
    val familyNestCode: String get() = nestNo
}

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
