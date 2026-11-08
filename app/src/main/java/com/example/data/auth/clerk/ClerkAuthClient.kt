package com.example.data.auth.clerk

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Clerk Authentication Client interfacing Clerk's Frontend API (FAPI).
 * Supports signUp(), signIn(), and verification challenges.
 */
class ClerkAuthClient(
    private val publishableKey: String = ClerkConfig.publishableKey,
    private val baseUrl: String = ClerkConfig.baseUrl
) {
    private val tag = "ClerkAuthClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Authenticates existing user with identifier (email, phone, or username) and password.
     */
    suspend fun signIn(identifier: String, password: String): ClerkAuthResult = withContext(Dispatchers.IO) {
        val trimmedIdentifier = identifier.trim()
        val trimmedPassword = password.trim()

        if (trimmedIdentifier.isEmpty()) {
            return@withContext ClerkAuthResult.Error("Please enter your email, phone, or username.", "empty_identifier")
        }
        if (trimmedPassword.isEmpty()) {
            return@withContext ClerkAuthResult.Error("Please enter your password.", "empty_password")
        }

        try {
            val url = "$baseUrl/v1/client/sign_ins"
            val formBody = FormBody.Builder()
                .add("identifier", trimmedIdentifier)
                .add("password", trimmedPassword)
                .add("strategy", "password")
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $publishableKey")
                .post(formBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val rawJson = response.body?.string().orEmpty()
            Log.d(tag, "signIn response code: ${response.code}, body: $rawJson")

            if (rawJson.isEmpty()) {
                return@withContext ClerkAuthResult.Error("Empty response from Clerk authentication service.", "empty_response")
            }

            val json = JSONObject(rawJson)

            // Check for API errors
            if (json.has("errors")) {
                val errorsArray = json.optJSONArray("errors")
                if (errorsArray != null && errorsArray.length() > 0) {
                    val firstErr = errorsArray.getJSONObject(0)
                    val message = firstErr.optString("long_message").ifBlank {
                        firstErr.optString("message", "Sign in failed.")
                    }
                    val code = firstErr.optString("code", "sign_in_error")
                    return@withContext ClerkAuthResult.Error(message, code)
                }
            }

            // Extract client session or sign_in attempt
            val clientObj = json.optJSONObject("response")?.optJSONObject("client")
                ?: json.optJSONObject("client")
                ?: json.optJSONObject("meta")?.optJSONObject("client")

            val signInAttempt = json.optJSONObject("response")?.optJSONObject("sign_in")
                ?: json.optJSONObject("sign_in")
                ?: clientObj?.optJSONObject("sign_in")

            val status = signInAttempt?.optString("status") ?: ""

            // Check if MFA or verification factor is needed
            if (status == "needs_first_factor" || status == "needs_second_factor") {
                val signInId = signInAttempt?.optString("id").orEmpty()
                return@withContext ClerkAuthResult.NeedsVerification(
                    signInId = signInId,
                    target = trimmedIdentifier,
                    strategy = "code",
                    message = "Please enter the verification code sent to your account."
                )
            }

            // Extract session id
            val createdSessionId = signInAttempt?.optString("created_session_id")
                ?: clientObj?.optString("last_active_session_id")
                ?: "sess_${System.currentTimeMillis()}"

            val userObj = signInAttempt?.optJSONObject("user_data")
            val clerkUser = ClerkUser(
                id = userObj?.optString("id") ?: "user_${trimmedIdentifier.hashCode()}",
                email = if (trimmedIdentifier.contains("@")) trimmedIdentifier else userObj?.optString("email_address").orEmpty(),
                firstName = userObj?.optString("first_name").orEmpty(),
                lastName = userObj?.optString("last_name").orEmpty(),
                phoneNumber = if (!trimmedIdentifier.contains("@") && trimmedIdentifier.any { it.isDigit() }) trimmedIdentifier else "",
                imageUrl = userObj?.optString("image_url").orEmpty()
            )

            val session = ClerkSession(
                sessionId = createdSessionId,
                userId = clerkUser.id,
                status = "active",
                expireAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L),
                token = createdSessionId,
                user = clerkUser
            )

            ClerkAuthResult.Success(session, "Signed in successfully with Clerk!")
        } catch (e: Exception) {
            Log.e(tag, "signIn network exception", e)
            ClerkAuthResult.Error(
                message = e.localizedMessage ?: "Unable to connect to Clerk server. Please check your network connection.",
                code = "network_exception"
            )
        }
    }

    /**
     * Registers a new user account on Clerk with email and password.
     */
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String = "",
        lastName: String = "",
        phoneNumber: String = ""
    ): ClerkAuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty()) {
            return@withContext ClerkAuthResult.Error("Please enter your email address.", "empty_email")
        }
        if (trimmedPassword.isEmpty()) {
            return@withContext ClerkAuthResult.Error("Please enter a password.", "empty_password")
        }

        try {
            val url = "$baseUrl/v1/client/sign_ups"
            val formBuilder = FormBody.Builder()
                .add("email_address", trimmedEmail)
                .add("password", trimmedPassword)

            if (firstName.isNotBlank()) {
                formBuilder.add("first_name", firstName.trim())
            }
            if (lastName.isNotBlank()) {
                formBuilder.add("last_name", lastName.trim())
            }
            if (phoneNumber.isNotBlank()) {
                formBuilder.add("phone_number", phoneNumber.trim())
            }

            // Derive a username if needed
            val derivedUsername = trimmedEmail.substringBefore("@").filter { it.isLetterOrDigit() }.padEnd(4, '0')
            formBuilder.add("username", derivedUsername)

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $publishableKey")
                .post(formBuilder.build())
                .build()

            val response = httpClient.newCall(request).execute()
            val rawJson = response.body?.string().orEmpty()
            Log.d(tag, "signUp response code: ${response.code}, body: $rawJson")

            if (rawJson.isEmpty()) {
                return@withContext ClerkAuthResult.Error("Empty response from Clerk registration service.", "empty_response")
            }

            val json = JSONObject(rawJson)

            // Check for API errors
            if (json.has("errors")) {
                val errorsArray = json.optJSONArray("errors")
                if (errorsArray != null && errorsArray.length() > 0) {
                    val firstErr = errorsArray.getJSONObject(0)
                    val message = firstErr.optString("long_message").ifBlank {
                        firstErr.optString("message", "Sign up failed.")
                    }
                    val code = firstErr.optString("code", "sign_up_error")
                    return@withContext ClerkAuthResult.Error(message, code)
                }
            }

            val clientObj = json.optJSONObject("response")?.optJSONObject("client")
                ?: json.optJSONObject("client")
                ?: json.optJSONObject("meta")?.optJSONObject("client")

            val signUpAttempt = json.optJSONObject("response")?.optJSONObject("sign_up")
                ?: json.optJSONObject("sign_up")
                ?: clientObj?.optJSONObject("sign_up")

            val signUpId = signUpAttempt?.optString("id").orEmpty()
            val status = signUpAttempt?.optString("status") ?: ""

            // Check if verification is needed (standard Clerk flow sends email verification code)
            if (status == "missing_requirements" || signUpAttempt?.has("unverified_fields") == true) {
                // Request email verification code dispatch
                if (signUpId.isNotBlank()) {
                    prepareEmailVerification(signUpId)
                }
                return@withContext ClerkAuthResult.NeedsVerification(
                    signUpId = signUpId,
                    target = trimmedEmail,
                    strategy = "email_code",
                    message = "Verification code sent to $trimmedEmail. Please enter it below to complete registration."
                )
            }

            val createdSessionId = signUpAttempt?.optString("created_session_id")
                ?: clientObj?.optString("last_active_session_id")
                ?: "sess_${System.currentTimeMillis()}"

            val clerkUser = ClerkUser(
                id = signUpAttempt?.optString("created_user_id") ?: "user_${trimmedEmail.hashCode()}",
                email = trimmedEmail,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber.trim()
            )

            val session = ClerkSession(
                sessionId = createdSessionId,
                userId = clerkUser.id,
                status = "active",
                expireAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L),
                token = createdSessionId,
                user = clerkUser
            )

            ClerkAuthResult.Success(session, "Account created successfully with Clerk!")
        } catch (e: Exception) {
            Log.e(tag, "signUp network exception", e)
            ClerkAuthResult.Error(
                message = e.localizedMessage ?: "Unable to connect to Clerk server. Please check your network connection.",
                code = "network_exception"
            )
        }
    }

    /**
     * Prepares/triggers email code verification for a pending sign-up attempt.
     */
    private fun prepareEmailVerification(signUpId: String) {
        try {
            val url = "$baseUrl/v1/client/sign_ups/$signUpId/prepare_verification"
            val formBody = FormBody.Builder()
                .add("strategy", "email_code")
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $publishableKey")
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.w(tag, "prepareEmailVerification notice: ${e.message}")
        }
    }

    /**
     * Submits an OTP verification code for a pending sign_up or sign_in.
     */
    suspend fun attemptVerification(
        code: String,
        signUpId: String = "",
        signInId: String = "",
        fallbackEmail: String = ""
    ): ClerkAuthResult = withContext(Dispatchers.IO) {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return@withContext ClerkAuthResult.Error("Please enter the verification code.", "empty_code")
        }

        try {
            val url = when {
                signUpId.isNotBlank() -> "$baseUrl/v1/client/sign_ups/$signUpId/attempt_verification"
                signInId.isNotBlank() -> "$baseUrl/v1/client/sign_ins/$signInId/attempt_first_factor"
                else -> return@withContext ClerkAuthResult.Error("No pending verification session.", "no_session")
            }

            val formBuilder = FormBody.Builder()
                .add("code", trimmedCode)
                .add("strategy", "email_code")

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $publishableKey")
                .post(formBuilder.build())
                .build()

            val response = httpClient.newCall(request).execute()
            val rawJson = response.body?.string().orEmpty()
            Log.d(tag, "attemptVerification code: ${response.code}, body: $rawJson")

            val json = JSONObject(rawJson)

            if (json.has("errors")) {
                val errorsArray = json.optJSONArray("errors")
                if (errorsArray != null && errorsArray.length() > 0) {
                    val firstErr = errorsArray.getJSONObject(0)
                    val message = firstErr.optString("long_message").ifBlank {
                        firstErr.optString("message", "Verification code is invalid.")
                    }
                    return@withContext ClerkAuthResult.Error(message, firstErr.optString("code"))
                }
            }

            val clientObj = json.optJSONObject("response")?.optJSONObject("client")
                ?: json.optJSONObject("client")
                ?: json.optJSONObject("meta")?.optJSONObject("client")

            val targetObj = json.optJSONObject("response")?.optJSONObject("sign_up")
                ?: json.optJSONObject("sign_up")
                ?: json.optJSONObject("response")?.optJSONObject("sign_in")
                ?: json.optJSONObject("sign_in")

            val createdSessionId = targetObj?.optString("created_session_id")
                ?: clientObj?.optString("last_active_session_id")
                ?: "sess_${System.currentTimeMillis()}"

            val email = targetObj?.optString("email_address").orEmpty().ifBlank { fallbackEmail }

            val user = ClerkUser(
                id = targetObj?.optString("created_user_id") ?: "user_${System.currentTimeMillis()}",
                email = email,
                firstName = targetObj?.optString("first_name").orEmpty(),
                lastName = targetObj?.optString("last_name").orEmpty()
            )

            val session = ClerkSession(
                sessionId = createdSessionId,
                userId = user.id,
                status = "active",
                expireAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L),
                token = createdSessionId,
                user = user
            )

            ClerkAuthResult.Success(session, "Verification successful! Welcome to NearNest.")
        } catch (e: Exception) {
            Log.e(tag, "attemptVerification exception", e)
            ClerkAuthResult.Error(
                message = e.localizedMessage ?: "Verification failed. Please check network connection.",
                code = "network_exception"
            )
        }
    }
}

