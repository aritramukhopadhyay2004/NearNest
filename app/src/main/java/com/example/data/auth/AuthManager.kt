package com.example.data.auth

import android.content.Context
import com.example.data.model.FamilyMember
import com.example.data.repository.FamilyTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * Authentication Controller managing device onboarding, OTP generation,
 * SMS/Gmail delivery simulation, and session verification.
 */
class AuthManager(
    private val context: Context,
    private val repository: FamilyTrackingRepository? = null
) {
    private val prefsManager = AuthPreferencesManager(context)
    private val timeFormat12h = SimpleDateFormat("h:mm a", Locale.getDefault())

    private val _authState = MutableStateFlow(
        if (prefsManager.hasValidSession()) AuthState.AUTHENTICATED else AuthState.UNAUTHENTICATED
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentSession = MutableStateFlow<UserSession?>(prefsManager.getSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _activeChallenge = MutableStateFlow<OtpChallenge?>(null)
    val activeChallenge: StateFlow<OtpChallenge?> = _activeChallenge.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _lastDeliveredOtpMessage = MutableStateFlow<String?>(null)
    val lastDeliveredOtpMessage: StateFlow<String?> = _lastDeliveredOtpMessage.asStateFlow()

    /**
     * Request OTP via SMS for Mobile Phone login
     */
    fun requestPhoneOtp(
        rawPhone: String,
        countryCode: String = "+91",
        userName: String = "",
        nestCode: String = "NEST-4821"
    ): Boolean {
        _authError.value = null
        val cleanDigits = rawPhone.filter { it.isDigit() }
        if (cleanDigits.length < 8) {
            _authError.value = "Please enter a valid mobile number (at least 8-10 digits)."
            return false
        }

        val fullPhone = if (rawPhone.startsWith("+")) rawPhone.trim() else "$countryCode $rawPhone".trim()
        val generatedCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val message = "[NearNest SMS Security] Your OTP verification code is $generatedCode. Valid for 2 minutes."

        val challenge = OtpChallenge(
            target = fullPhone,
            method = AuthMethod.PHONE_SMS,
            code = generatedCode,
            timestampEpoch = System.currentTimeMillis(),
            expirySeconds = 120,
            deliveryMessage = message
        )

        _activeChallenge.value = challenge
        _lastDeliveredOtpMessage.value = "SMS to $fullPhone: Code is $generatedCode"
        _authState.value = AuthState.ENTER_OTP
        return true
    }

    /**
     * Request OTP via Gmail for Google Account login
     */
    fun requestGoogleOtp(
        rawEmail: String,
        userName: String = "",
        nestCode: String = "NEST-4821"
    ): Boolean {
        _authError.value = null
        val email = rawEmail.trim()
        if (!email.contains("@") || !email.contains(".")) {
            _authError.value = "Please enter a valid Google Account / Gmail address."
            return false
        }

        val generatedCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val message = "[NearNest Google Auth] Verification code for $email is $generatedCode. Use it to sign into your family nest."

        val challenge = OtpChallenge(
            target = email,
            method = AuthMethod.GOOGLE_GMAIL,
            code = generatedCode,
            timestampEpoch = System.currentTimeMillis(),
            expirySeconds = 120,
            deliveryMessage = message
        )

        _activeChallenge.value = challenge
        _lastDeliveredOtpMessage.value = "Gmail to $email: Code is $generatedCode"
        _authState.value = AuthState.ENTER_OTP
        return true
    }

    /**
     * Resend current active OTP
     */
    fun resendActiveOtp(): Boolean {
        val current = _activeChallenge.value ?: return false
        val newCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val newMsg = if (current.method == AuthMethod.PHONE_SMS) {
            "[NearNest SMS Security] Your new OTP is $newCode."
        } else {
            "[NearNest Google Auth] Your new verification code is $newCode."
        }

        val updated = current.copy(
            code = newCode,
            timestampEpoch = System.currentTimeMillis(),
            deliveryMessage = newMsg
        )
        _activeChallenge.value = updated
        _lastDeliveredOtpMessage.value = "${if (current.method == AuthMethod.PHONE_SMS) "SMS" else "Gmail"} to ${current.target}: Code is $newCode"
        _authError.value = null
        return true
    }

    /**
     * Verify the entered OTP code and complete login
     */
    fun verifyOtp(enteredCode: String, customDisplayName: String = ""): Boolean {
        _authError.value = null
        val challenge = _activeChallenge.value
        if (challenge == null) {
            _authError.value = "No active verification session. Please request a new code."
            return false
        }

        if (challenge.isExpired()) {
            _authError.value = "Verification code has expired. Tap 'Resend OTP' to get a fresh code."
            return false
        }

        val cleanEntered = enteredCode.trim()
        if (cleanEntered != challenge.code) {
            _authError.value = "Invalid verification code. Please check the code and try again."
            return false
        }

        // OTP Verified successfully!
        val resolvedName = when {
            customDisplayName.isNotBlank() -> customDisplayName.trim()
            challenge.method == AuthMethod.PHONE_SMS -> "User (${challenge.target.takeLast(4)})"
            else -> challenge.target.substringBefore("@").replace(".", " ").capitalizeWords()
        }

        val now12h = timeFormat12h.format(Date())
        val session = UserSession(
            userId = "user_" + UUID.randomUUID().toString().take(8),
            displayName = resolvedName,
            authMethod = challenge.method,
            contact = challenge.target,
            avatarColorHex = if (challenge.method == AuthMethod.PHONE_SMS) 0xFF0284C7 else 0xFF10B981,
            familyNestCode = "NEST-4821",
            token = UUID.randomUUID().toString(),
            loginTimestamp12h = now12h,
            isVerified = true
        )

        // Save session locally
        prefsManager.saveSession(session)
        _currentSession.value = session
        _activeChallenge.value = null
        _authState.value = AuthState.AUTHENTICATED
        _lastDeliveredOtpMessage.value = null

        // Sync with repository family members if available
        repository?.let { repo ->
            syncUserWithFamily(session, repo)
        }

        return true
    }

    /**
     * Fast 1-tap demo login for rapid testing on real devices
     */
    fun quickDemoLogin(method: AuthMethod) {
        val now12h = timeFormat12h.format(Date())
        val session = if (method == AuthMethod.PHONE_SMS) {
            UserSession(
                userId = "user_john",
                displayName = "John (You)",
                authMethod = AuthMethod.PHONE_SMS,
                contact = "+91 98765 43210",
                avatarColorHex = 0xFF0284C7,
                familyNestCode = "NEST-4821",
                token = UUID.randomUUID().toString(),
                loginTimestamp12h = now12h,
                isVerified = true
            )
        } else {
            UserSession(
                userId = "user_sarah",
                displayName = "Sarah (Google)",
                authMethod = AuthMethod.GOOGLE_GMAIL,
                contact = "sarah.nearnest@gmail.com",
                avatarColorHex = 0xFFD946EF,
                familyNestCode = "NEST-4821",
                token = UUID.randomUUID().toString(),
                loginTimestamp12h = now12h,
                isVerified = true
            )
        }

        prefsManager.saveSession(session)
        _currentSession.value = session
        _activeChallenge.value = null
        _authState.value = AuthState.AUTHENTICATED
        _authError.value = null
    }

    /**
     * Cancel current OTP verification and change input
     */
    fun cancelOtpVerification() {
        _activeChallenge.value = null
        _authError.value = null
        _lastDeliveredOtpMessage.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }

    /**
     * Sign out and clear local device session
     */
    fun signOut() {
        prefsManager.clearSession()
        _currentSession.value = null
        _activeChallenge.value = null
        _authError.value = null
        _lastDeliveredOtpMessage.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }

    private fun syncUserWithFamily(session: UserSession, repo: FamilyTrackingRepository) {
        val existing = repo.members.value.find { it.id == session.userId }
        if (existing == null) {
            val newMember = FamilyMember(
                id = session.userId,
                name = session.displayName,
                relation = "Self (Device Owner)",
                colorHex = session.avatarColorHex,
                batteryPercent = 95,
                currentPlace = "Home",
                currentLat = 37.7749,
                currentLng = -122.4194,
                lastSeenTime12h = session.loginTimestamp12h,
                phone = if (session.authMethod == AuthMethod.PHONE_SMS) session.contact else null,
                email = if (session.authMethod == AuthMethod.GOOGLE_GMAIL) session.contact else null
            )
            // Add or update member in local repository
            repo.registerUserAsFamilyMember(newMember)
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
