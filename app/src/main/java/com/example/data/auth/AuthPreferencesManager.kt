package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent user session storage on the Android device
 */
class AuthPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "nearnest_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_AUTH_METHOD = "key_auth_method"
        private const val KEY_CONTACT = "key_contact"
        private const val KEY_AVATAR_COLOR = "key_avatar_color"
        private const val KEY_NEST_CODE = "key_nest_code"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_LOGIN_TIME_12H = "key_login_time_12h"
    }

    /**
     * Save authenticated user session
     */
    fun saveSession(session: UserSession) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_AUTH_METHOD, session.authMethod.name)
            .putString(KEY_CONTACT, session.contact)
            .putLong(KEY_AVATAR_COLOR, session.avatarColorHex)
            .putString(KEY_NEST_CODE, session.familyNestCode)
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_LOGIN_TIME_12H, session.loginTimestamp12h)
            .apply()
    }

    /**
     * Retrieve active session if logged in
     */
    fun getSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "User") ?: "User"
        val authMethodStr = prefs.getString(KEY_AUTH_METHOD, AuthMethod.PHONE_SMS.name)
        val authMethod = try {
            AuthMethod.valueOf(authMethodStr ?: AuthMethod.PHONE_SMS.name)
        } catch (_: Exception) {
            AuthMethod.PHONE_SMS
        }
        val contact = prefs.getString(KEY_CONTACT, "") ?: ""
        val avatarColor = prefs.getLong(KEY_AVATAR_COLOR, 0xFF0284C7)
        val nestCode = prefs.getString(KEY_NEST_CODE, "NEST-4821") ?: "NEST-4821"
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        val loginTime12h = prefs.getString(KEY_LOGIN_TIME_12H, "Just now") ?: "Just now"

        return UserSession(
            userId = userId,
            displayName = displayName,
            authMethod = authMethod,
            contact = contact,
            avatarColorHex = avatarColor,
            familyNestCode = nestCode,
            token = token,
            loginTimestamp12h = loginTime12h,
            isVerified = true
        )
    }

    /**
     * Check if a session is currently stored
     */
    fun hasValidSession(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Clear session on logout
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
