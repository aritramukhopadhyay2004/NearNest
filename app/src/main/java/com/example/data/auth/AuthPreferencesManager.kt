package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent user session and Family Nest storage on the Android device
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
        private const val KEY_NEST_NO = "key_nest_no"
        private const val KEY_NEST_PASS = "key_nest_pass"
        private const val KEY_NEST_NAME = "key_nest_name"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_LOGIN_TIME_12H = "key_login_time_12h"

        // Global Nest Prefs
        private const val KEY_STORED_NEST_NO = "key_stored_nest_no"
        private const val KEY_STORED_NEST_PASS = "key_stored_nest_pass"
        private const val KEY_STORED_NEST_NAME = "key_stored_nest_name"
        private const val KEY_STORED_CREATOR_NAME = "key_stored_creator_name"
        private const val KEY_STORED_CREATED_AT = "key_stored_created_at"
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
            .putString(KEY_NEST_NO, session.nestNo)
            .putString(KEY_NEST_PASS, session.nestPass)
            .putString(KEY_NEST_NAME, session.nestName)
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_LOGIN_TIME_12H, session.loginTimestamp12h)
            .apply()

        // Also update stored active nest
        saveNest(
            FamilyNest(
                nestNo = session.nestNo,
                nestPass = session.nestPass,
                nestName = session.nestName,
                creatorName = session.displayName,
                createdAt12h = session.loginTimestamp12h
            )
        )
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
        val nestNo = prefs.getString(KEY_NEST_NO, "NEST-7492") ?: "NEST-7492"
        val nestPass = prefs.getString(KEY_NEST_PASS, "PASS-4821") ?: "PASS-4821"
        val nestName = prefs.getString(KEY_NEST_NAME, "The Family Nest") ?: "The Family Nest"
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        val loginTime12h = prefs.getString(KEY_LOGIN_TIME_12H, "Just now") ?: "Just now"

        return UserSession(
            userId = userId,
            displayName = displayName,
            authMethod = authMethod,
            contact = contact,
            avatarColorHex = avatarColor,
            nestNo = nestNo,
            nestPass = nestPass,
            nestName = nestName,
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
     * Save active Family Nest details
     */
    fun saveNest(nest: FamilyNest) {
        prefs.edit()
            .putString(KEY_STORED_NEST_NO, nest.nestNo)
            .putString(KEY_STORED_NEST_PASS, nest.nestPass)
            .putString(KEY_STORED_NEST_NAME, nest.nestName)
            .putString(KEY_STORED_CREATOR_NAME, nest.creatorName)
            .putString(KEY_STORED_CREATED_AT, nest.createdAt12h)
            .apply()
    }

    /**
     * Retrieve active Family Nest details (defaults to standard family nest)
     */
    fun getNest(): FamilyNest {
        val nestNo = prefs.getString(KEY_STORED_NEST_NO, "NEST-7492") ?: "NEST-7492"
        val nestPass = prefs.getString(KEY_STORED_NEST_PASS, "PASS-4821") ?: "PASS-4821"
        val nestName = prefs.getString(KEY_STORED_NEST_NAME, "The Family Nest") ?: "The Family Nest"
        val creatorName = prefs.getString(KEY_STORED_CREATOR_NAME, "Family Admin") ?: "Family Admin"
        val createdAt = prefs.getString(KEY_STORED_CREATED_AT, "12:00 AM") ?: "12:00 AM"

        return FamilyNest(
            nestNo = nestNo,
            nestPass = nestPass,
            nestName = nestName,
            creatorName = creatorName,
            createdAt12h = createdAt,
            membersCount = 3
        )
    }

    /**
     * Clear session on logout
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_ID)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_AUTH_METHOD)
            .remove(KEY_CONTACT)
            .remove(KEY_AVATAR_COLOR)
            .remove(KEY_TOKEN)
            .remove(KEY_LOGIN_TIME_12H)
            .apply()
    }
}
