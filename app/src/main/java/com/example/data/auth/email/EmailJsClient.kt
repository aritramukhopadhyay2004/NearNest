package com.example.data.auth.email

import com.example.data.auth.OtpPurpose
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class EmailJsResult {
    data class Success(val message: String) : EmailJsResult()
    data class Warning(val message: String, val otpCode: String) : EmailJsResult()
    data class Error(val code: Int, val message: String, val otpCode: String) : EmailJsResult()
}

/**
 * EmailJS Client for NearNest OTP verification
 * Sends real OTP verification emails via EmailJS REST API.
 */
class EmailJsClient(
    private val serviceId: String = EmailJsConfig.serviceId,
    private val templateId: String = EmailJsConfig.templateId,
    private val publicKey: String = EmailJsConfig.publicKey
) {
    private val tag = "EmailJsClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Dispatches an OTP verification email to the user with the requested format:
     * "hey (name) your otp for verification for signing up is (------)"
     */
    suspend fun sendOtpEmail(
        toName: String,
        toEmail: String,
        otpCode: String,
        purpose: OtpPurpose = OtpPurpose.SIGN_UP
    ): EmailJsResult = withContext(Dispatchers.IO) {
        val trimmedEmail = toEmail.trim()
        val trimmedName = toName.trim().ifBlank { "User" }

        val actionPhrase = when (purpose) {
            OtpPurpose.SIGN_UP -> "for signing up"
            OtpPurpose.SIGN_IN -> "for sign in"
            OtpPurpose.FORGOT_PASSWORD -> "for password reset"
        }

        // Plain text message adhering to user request
        val plainMessage = "hey $trimmedName your otp for verification $actionPhrase is ($otpCode)\n\n" +
                "=========================\n" +
                "OTP CODE: $otpCode\n" +
                "=========================\n\n" +
                "Please copy and paste this code into NearNest to complete your verification."

        // HTML styled snippet for templates that support HTML
        val htmlSnippet = """
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; border: 1px solid #E2E8F0; border-radius: 16px; background-color: #FFFFFF;">
                <div style="margin-bottom: 20px;">
                    <span style="font-size: 18px; font-weight: 700; color: #0284C7;">NearNest Security</span>
                </div>
                <p style="font-size: 16px; color: #1E293B; line-height: 1.6; margin: 0 0 16px 0;">
                    hey <strong>$trimmedName</strong> your otp for verification $actionPhrase is <strong>($otpCode)</strong>
                </p>
                <div style="margin: 24px 0; text-align: center;">
                    <div style="display: inline-block; background-color: #0F172A; color: #FFFFFF; font-size: 26px; font-weight: 700; letter-spacing: 8px; padding: 14px 28px; border-radius: 12px; font-family: monospace;">
                        $otpCode
                    </div>
                </div>
                <p style="font-size: 13px; color: #64748B; line-height: 1.5; margin: 0 0 12px 0;">
                    Copy this 6-digit code and paste it into the Verify Code screen in NearNest to continue.
                </p>
            </div>
        """.trimIndent()

        val jsonPayload = JSONObject().apply {
            put("service_id", serviceId)
            put("template_id", templateId)
            put("user_id", publicKey)
            put("template_params", JSONObject().apply {
                put("name", trimmedName)
                put("to_name", trimmedName)
                put("email", trimmedEmail)
                put("to_email", trimmedEmail)
                put("otp_code", otpCode)
                put("code", otpCode)
                put("passcode", otpCode)
                put("time", "10 minutes")
                put("message", plainMessage)
                put("html_message", htmlSnippet)
            })
        }

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(EmailJsConfig.apiEndpoint)
                .addHeader("Content-Type", "application/json")
                .addHeader("Origin", EmailJsConfig.defaultOrigin)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            val statusCode = response.code

            Log.d(tag, "EmailJS send response: code=$statusCode, body=$responseBody")

            when {
                statusCode in 200..299 -> {
                    EmailJsResult.Success("Verification email dispatched to $trimmedEmail.")
                }
                statusCode == 412 || responseBody.contains("Invalid grant", ignoreCase = true) -> {
                    Log.w(tag, "EmailJS Gmail OAuth requires reconnect in dashboard. Code generated: $otpCode")
                    EmailJsResult.Warning(
                        "EmailJS Gmail account needs reconnect in dashboard. Verification code: $otpCode",
                        otpCode
                    )
                }
                else -> {
                    Log.e(tag, "EmailJS error code $statusCode: $responseBody")
                    EmailJsResult.Error(statusCode, responseBody.ifBlank { "HTTP error $statusCode" }, otpCode)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "EmailJS network exception: ${e.message}", e)
            EmailJsResult.Error(-1, e.localizedMessage ?: "Network connection error", otpCode)
        }
    }
}

