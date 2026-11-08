package com.example.data.auth.clerk

/**
 * Clerk Authenticated User
 */
data class ClerkUser(
    val id: String,
    val email: String,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val imageUrl: String = ""
) {
    val displayName: String
        get() {
            val combined = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            return when {
                combined.isNotBlank() -> combined
                username.isNotBlank() -> username
                email.isNotBlank() -> email.substringBefore("@")
                phoneNumber.isNotBlank() -> phoneNumber
                else -> "NearNest Member"
            }
        }
}

/**
 * Active Clerk Session
 */
data class ClerkSession(
    val sessionId: String,
    val userId: String,
    val status: String,
    val expireAt: Long = 0L,
    val token: String = "",
    val user: ClerkUser? = null
)

/**
 * Result state returned by Clerk signUp() and signIn() flows
 */
sealed class ClerkAuthResult {
    data class Success(
        val session: ClerkSession,
        val message: String = "Authentication successful"
    ) : ClerkAuthResult()

    data class NeedsVerification(
        val signUpId: String = "",
        val signInId: String = "",
        val target: String = "",
        val strategy: String = "email_code",
        val message: String = "Verification code sent. Please enter the code."
    ) : ClerkAuthResult()

    data class Error(
        val message: String,
        val code: String = "clerk_error"
    ) : ClerkAuthResult()
}
