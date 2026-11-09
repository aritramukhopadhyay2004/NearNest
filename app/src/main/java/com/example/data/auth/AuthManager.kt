package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.auth.email.EmailJsClient
import com.example.data.auth.email.EmailJsResult
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
 * Pending user registration before OTP verification
 */
data class PendingSignUp(
    val name: String,
    val email: String,
    val password: String,
    val otpCode: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Authentication Controller managing device onboarding, OTP generation,
 * SMS/Gmail delivery simulation, session verification, and Family Nest pairing.
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

    private val _currentNest = MutableStateFlow<FamilyNest>(prefsManager.getNest())
    val currentNest: StateFlow<FamilyNest> = _currentNest.asStateFlow()

    private val _activeChallenge = MutableStateFlow<OtpChallenge?>(null)
    val activeChallenge: StateFlow<OtpChallenge?> = _activeChallenge.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _lastDeliveredOtpMessage = MutableStateFlow<String?>(null)
    val lastDeliveredOtpMessage: StateFlow<String?> = _lastDeliveredOtpMessage.asStateFlow()

    val clerkClient = com.example.data.auth.clerk.ClerkAuthClient()
    val emailJsClient = EmailJsClient()

    private val _pendingSignUp = MutableStateFlow<PendingSignUp?>(null)
    val pendingSignUp: StateFlow<PendingSignUp?> = _pendingSignUp.asStateFlow()

    private val _currentAuthPage = MutableStateFlow(
        if (!prefsManager.hasSeenOnboarding()) AuthPage.ONBOARDING else AuthPage.LOGIN
    )
    val currentAuthPage: StateFlow<AuthPage> = _currentAuthPage.asStateFlow()

    private val _clerkVerificationPending = MutableStateFlow<com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification?>(null)
    val clerkVerificationPending: StateFlow<com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification?> = _clerkVerificationPending.asStateFlow()

    fun setAuthPage(page: AuthPage) {
        _currentAuthPage.value = page
        _authError.value = null
    }

    fun completeOnboarding() {
        prefsManager.setSeenOnboarding(true)
        _currentAuthPage.value = AuthPage.LOGIN
    }

    /**
     * Authenticates existing user using Clerk signIn() method
     */
    suspend fun signInWithClerk(identifier: String, password: String): com.example.data.auth.clerk.ClerkAuthResult {
        _isProcessing.value = true
        _authError.value = null
        val result = clerkClient.signIn(identifier, password)
        _isProcessing.value = false

        when (result) {
            is com.example.data.auth.clerk.ClerkAuthResult.Success -> {
                completeClerkLogin(result.session)
            }
            is com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification -> {
                _clerkVerificationPending.value = result
            }
            is com.example.data.auth.clerk.ClerkAuthResult.Error -> {
                _authError.value = result.message
            }
        }
        return result
    }

    /**
     * Initiates NearNest Sign Up flow:
     * Generates a secure 6-digit OTP and dispatches it via EmailJS to the user's email address
     * with format: "hey (name) your otp for verification for signing up is (------)"
     * Navigates directly to OTP Verification screen.
     */
    suspend fun initiateSignUpWithEmailOtp(
        name: String,
        email: String,
        password: String
    ): Boolean {
        _isProcessing.value = true
        _authError.value = null

        val cleanName = name.trim().ifBlank { "User" }
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _authError.value = "Please enter a valid email address."
            _isProcessing.value = false
            return false
        }

        if (cleanPassword.isEmpty()) {
            _authError.value = "Please enter a password."
            _isProcessing.value = false
            return false
        }

        val generatedCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val deliveryMessage = "hey $cleanName your otp for verification for signing up is ($generatedCode)"

        val challenge = OtpChallenge(
            target = cleanEmail,
            method = AuthMethod.EMAIL_OTP,
            code = generatedCode,
            timestampEpoch = System.currentTimeMillis(),
            expirySeconds = 300,
            deliveryMessage = deliveryMessage
        )

        _activeChallenge.value = challenge
        _pendingSignUp.value = PendingSignUp(cleanName, cleanEmail, cleanPassword, generatedCode)
        _lastDeliveredOtpMessage.value = "Email to $cleanEmail: Code is $generatedCode"

        // Dispatch email via EmailJS client
        val emailResult = emailJsClient.sendOtpEmail(
            toName = cleanName,
            toEmail = cleanEmail,
            otpCode = generatedCode
        )

        _isProcessing.value = false

        when (emailResult) {
            is EmailJsResult.Success -> {
                Log.d("AuthManager", "EmailJS delivered successfully: ${emailResult.message}")
            }
            is EmailJsResult.Warning -> {
                Log.w("AuthManager", "EmailJS warning: ${emailResult.message}")
            }
            is EmailJsResult.Error -> {
                Log.e("AuthManager", "EmailJS error: ${emailResult.message}")
            }
        }

        _authState.value = AuthState.ENTER_OTP
        _currentAuthPage.value = AuthPage.OTP_VERIFY
        return true
    }

    /**
     * Registers new user account using Clerk signUp() method
     */
    suspend fun signUpWithClerk(
        email: String,
        password: String,
        firstName: String = "",
        lastName: String = "",
        phoneNumber: String = ""
    ): com.example.data.auth.clerk.ClerkAuthResult {
        _isProcessing.value = true
        _authError.value = null
        val result = clerkClient.signUp(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber
        )
        _isProcessing.value = false

        when (result) {
            is com.example.data.auth.clerk.ClerkAuthResult.Success -> {
                completeClerkLogin(result.session)
            }
            is com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification -> {
                _clerkVerificationPending.value = result
            }
            is com.example.data.auth.clerk.ClerkAuthResult.Error -> {
                _authError.value = result.message
            }
        }
        return result
    }

    /**
     * Verifies Clerk OTP / email code
     */
    suspend fun verifyClerkCode(code: String): com.example.data.auth.clerk.ClerkAuthResult {
        val pending = _clerkVerificationPending.value
        if (pending == null) {
            val err = "No pending Clerk verification found."
            _authError.value = err
            return com.example.data.auth.clerk.ClerkAuthResult.Error(err)
        }

        _isProcessing.value = true
        _authError.value = null
        val result = clerkClient.attemptVerification(
            code = code,
            signUpId = pending.signUpId,
            signInId = pending.signInId,
            fallbackEmail = pending.target
        )
        _isProcessing.value = false

        when (result) {
            is com.example.data.auth.clerk.ClerkAuthResult.Success -> {
                _clerkVerificationPending.value = null
                completeClerkLogin(result.session)
            }
            is com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification -> {
                _clerkVerificationPending.value = result
            }
            is com.example.data.auth.clerk.ClerkAuthResult.Error -> {
                _authError.value = result.message
            }
        }
        return result
    }

    fun dismissClerkVerification() {
        _clerkVerificationPending.value = null
    }

    fun setAuthError(error: String?) {
        _authError.value = error
    }

    fun startClerkOAuth(provider: String) {
        val method = if (provider.lowercase().contains("google")) AuthMethod.GOOGLE_GMAIL else AuthMethod.CLERK_AUTH
        quickDemoLogin(method)
    }

    private fun completeClerkLogin(clerkSession: com.example.data.auth.clerk.ClerkSession) {
        val now12h = timeFormat12h.format(Date())
        val nest = _currentNest.value
        val user = clerkSession.user
        val resolvedName = user?.displayName?.ifBlank { null }
            ?: user?.email?.substringBefore("@")
            ?: "NearNest Member"

        val session = UserSession(
            userId = user?.id ?: "user_${clerkSession.sessionId.takeLast(8)}",
            displayName = resolvedName,
            authMethod = AuthMethod.CLERK_AUTH,
            contact = user?.email?.ifBlank { user.phoneNumber } ?: "clerk_auth",
            avatarColorHex = 0xFF00ADB5,
            nestNo = nest.nestNo,
            nestPass = nest.nestPass,
            nestName = nest.nestName,
            token = clerkSession.token,
            loginTimestamp12h = now12h,
            isVerified = true
        )

        prefsManager.saveSession(session)
        _currentSession.value = session
        _activeChallenge.value = null
        _clerkVerificationPending.value = null
        _authState.value = AuthState.AUTHENTICATED
        _authError.value = null

        repository?.let { repo ->
            syncUserWithFamily(session, repo)
        }
    }

    /**
     * Unified Login: Dispatches OTP based on input format (Phone SMS or Google Gmail)
     */
    fun requestOtpForInput(
        rawInput: String,
        countryCode: String = "+91",
        userName: String = ""
    ): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            _authError.value = "Please enter your email address or mobile phone number."
            return false
        }

        return if (trimmed.contains("@")) {
            requestGoogleOtp(rawEmail = trimmed, userName = userName)
        } else {
            requestPhoneOtp(rawPhone = trimmed, countryCode = countryCode, userName = userName)
        }
    }

    /**
     * Request OTP via SMS for Mobile Phone login
     */
    fun requestPhoneOtp(
        rawPhone: String,
        countryCode: String = "+91",
        userName: String = "",
        nestCode: String = "NEST-7492"
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
        nestCode: String = "NEST-7492"
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
     * Resend current active OTP (dispatches fresh email via EmailJS for Sign Up)
     */
    suspend fun resendActiveOtp(): Boolean {
        val current = _activeChallenge.value ?: return false
        val newCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val pending = _pendingSignUp.value

        val newMsg = if (pending != null) {
            "hey ${pending.name} your otp for verification for signing up is ($newCode)"
        } else if (current.method == AuthMethod.PHONE_SMS) {
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
        _lastDeliveredOtpMessage.value = "${if (current.method == AuthMethod.PHONE_SMS) "SMS" else "Email"} to ${current.target}: Code is $newCode"
        _authError.value = null

        if (pending != null) {
            _pendingSignUp.value = pending.copy(otpCode = newCode)
            emailJsClient.sendOtpEmail(
                toName = pending.name,
                toEmail = pending.email,
                otpCode = newCode
            )
        }
        return true
    }

    /**
     * Verify the entered OTP code and complete initial authentication
     */
    fun verifyOtp(enteredCode: String, customDisplayName: String = ""): Boolean {
        _authError.value = null
        val challenge = _activeChallenge.value
        if (challenge == null) {
            _authError.value = "No active verification session. Please request a new code."
            return false
        }

        if (challenge.isExpired()) {
            _authError.value = "Verification code has expired. Tap 'Resend' to get a fresh code."
            return false
        }

        val cleanEntered = enteredCode.trim()
        if (cleanEntered != challenge.code) {
            _authError.value = "Invalid verification code. Please check the code and try again."
            return false
        }

        // OTP Verified successfully!
        val pending = _pendingSignUp.value
        val resolvedName = when {
            pending != null && pending.name.isNotBlank() -> pending.name.trim()
            customDisplayName.isNotBlank() -> customDisplayName.trim()
            challenge.method == AuthMethod.PHONE_SMS -> "User (${challenge.target.takeLast(4)})"
            else -> challenge.target.substringBefore("@").replace(".", " ").capitalizeWords()
        }
        val resolvedContact = pending?.email ?: challenge.target
        val resolvedMethod = if (pending != null) AuthMethod.EMAIL_OTP else challenge.method

        val now12h = timeFormat12h.format(Date())
        val nest = _currentNest.value
        val session = UserSession(
            userId = "user_" + UUID.randomUUID().toString().take(8),
            displayName = resolvedName,
            authMethod = resolvedMethod,
            contact = resolvedContact,
            avatarColorHex = 0xFF0284C7,
            nestNo = nest.nestNo,
            nestPass = nest.nestPass,
            nestName = nest.nestName,
            token = UUID.randomUUID().toString(),
            loginTimestamp12h = now12h,
            isVerified = true
        )

        // Save session locally
        prefsManager.saveSession(session)
        _currentSession.value = session
        _activeChallenge.value = null
        _pendingSignUp.value = null
        _authState.value = AuthState.AUTHENTICATED
        _currentAuthPage.value = AuthPage.LOGIN
        _lastDeliveredOtpMessage.value = null

        // Sync with repository family members if available
        repository?.let { repo ->
            syncUserWithFamily(session, repo)
        }

        return true
    }

    /**
     * Create a brand new Family Nest with unique Nest No and secret Nest Pass
     */
    fun createNewNest(
        nestName: String = "The Family Nest",
        creatorName: String = ""
    ): FamilyNest {
        val uniqueNo = "NEST-${Random.nextInt(1000, 9999)}"
        val uniquePass = "PASS-${Random.nextInt(1000, 9999)}"
        val resolvedCreator = creatorName.ifBlank { _currentSession.value?.displayName ?: "Family Admin" }
        val now12h = timeFormat12h.format(Date())

        val newNest = FamilyNest(
            nestNo = uniqueNo,
            nestPass = uniquePass,
            nestName = nestName.trim().ifBlank { "The Family Nest" },
            creatorName = resolvedCreator,
            createdAt12h = now12h,
            membersCount = 1
        )

        prefsManager.saveNest(newNest)
        _currentNest.value = newNest

        // Update active session with new Nest details if authenticated
        _currentSession.value?.let { session ->
            val updatedSession = session.copy(
                nestNo = uniqueNo,
                nestPass = uniquePass,
                nestName = newNest.nestName
            )
            prefsManager.saveSession(updatedSession)
            _currentSession.value = updatedSession
        }

        _authError.value = null
        return newNest
    }

    /**
     * Join an existing Family Nest using Nest No and Nest Pass
     */
    fun joinNest(
        rawNestNo: String,
        rawNestPass: String,
        customNestName: String? = null
    ): Result<FamilyNest> {
        _authError.value = null
        val cleanNo = rawNestNo.trim().uppercase()
        val cleanPass = rawNestPass.trim().uppercase()

        if (cleanNo.isBlank() || cleanPass.isBlank()) {
            val err = "Both Nest No and Nest Pass are required to connect."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        // Format validation: accept "NEST-XXXX" or just "XXXX"
        val formattedNestNo = if (cleanNo.startsWith("NEST-")) cleanNo else "NEST-$cleanNo"
        val formattedNestPass = if (cleanPass.startsWith("PASS-")) cleanPass else "PASS-$cleanPass"

        if (formattedNestNo.length < 7 || formattedNestPass.length < 7) {
            val err = "Invalid Nest credentials. Please enter a valid 4-digit Nest No & Pass."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        val resolvedNestName = customNestName?.trim()?.ifBlank { null }
            ?: if (formattedNestNo == _currentNest.value.nestNo) _currentNest.value.nestName else "Connected Family Nest"

        val joinedNest = FamilyNest(
            nestNo = formattedNestNo,
            nestPass = formattedNestPass,
            nestName = resolvedNestName,
            creatorName = "Family Admin",
            createdAt12h = timeFormat12h.format(Date()),
            membersCount = 4
        )

        prefsManager.saveNest(joinedNest)
        _currentNest.value = joinedNest

        // Update active session
        _currentSession.value?.let { session ->
            val updatedSession = session.copy(
                nestNo = formattedNestNo,
                nestPass = formattedNestPass,
                nestName = joinedNest.nestName
            )
            prefsManager.saveSession(updatedSession)
            _currentSession.value = updatedSession

            // Register user in repository
            repository?.let { repo ->
                syncUserWithFamily(updatedSession, repo)
            }
        }

        return Result.success(joinedNest)
    }

    /**
     * Complete Nest Setup and proceed into dashboard
     */
    fun completeNestSetup() {
        _authState.value = AuthState.AUTHENTICATED
    }

    /**
     * Fast 1-tap demo login for rapid testing on real devices
     */
    fun quickDemoLogin(method: AuthMethod) {
        val now12h = timeFormat12h.format(Date())
        val defaultNest = _currentNest.value
        val session = if (method == AuthMethod.PHONE_SMS) {
            UserSession(
                userId = "user_john",
                displayName = "John (You)",
                authMethod = AuthMethod.PHONE_SMS,
                contact = "+91 98765 43210",
                avatarColorHex = 0xFF0284C7,
                nestNo = defaultNest.nestNo,
                nestPass = defaultNest.nestPass,
                nestName = defaultNest.nestName,
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
                nestNo = defaultNest.nestNo,
                nestPass = defaultNest.nestPass,
                nestName = defaultNest.nestName,
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

    /**
     * Update current user profile details
     */
    fun updateProfile(displayName: String, email: String, phone: String) {
        val current = _currentSession.value ?: return
        val updated = current.copy(
            displayName = displayName.ifBlank { current.displayName },
            contact = email.ifBlank { current.contact }
        )
        _currentSession.value = updated
        prefsManager.saveSession(updated)

        val contactVal = if (email.isNotBlank()) email else phone
        repository?.updateMemberProfile(current.userId, updated.displayName, contactVal)
    }

    private fun syncUserWithFamily(session: UserSession, repo: FamilyTrackingRepository) {
        val existing = repo.members.value.find { it.id == session.userId }
        if (existing == null) {
            val newMember = FamilyMember(
                id = session.userId,
                name = session.displayName,
                relation = "Self (Device Owner)",
                colorHex = session.avatarColorHex,
                batteryPercent = com.example.data.telemetry.DeviceBatteryManager(context).readCurrentBattery().percentage,
                currentPlace = "Home",
                currentLat = 21.9868,
                currentLng = 87.8542,
                lastSeenTime12h = session.loginTimestamp12h,
                phone = if (session.authMethod == AuthMethod.PHONE_SMS) session.contact else null,
                email = if (session.authMethod != AuthMethod.PHONE_SMS) session.contact else null
            )
            // Add or update member in local repository
            repo.registerUserAsFamilyMember(newMember)
        } else {
            repo.updateMemberProfile(session.userId, session.displayName, session.contact)
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
