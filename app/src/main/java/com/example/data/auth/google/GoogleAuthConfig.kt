package com.example.data.auth.google

import android.content.Context
import com.example.BuildConfig

/**
 * Configuration resolver for Google Authentication
 */
object GoogleAuthConfig {
    /**
     * Resolves the OAuth 2.0 Web Client ID from BuildConfig or string resources (e.g. from google-services.json).
     * If null or blank, Google Play Services still presents the native account picker
     * and signs the user in with their full profile (email, display name, photo).
     */
    fun getWebClientId(context: Context): String? {
        // 1. Check BuildConfig if added in secrets / env
        try {
            val field = BuildConfig::class.java.getField("GOOGLE_WEB_CLIENT_ID")
            val id = field.get(null) as? String
            if (!id.isNullOrBlank()) return id.trim()
        } catch (_: Throwable) {}

        // 2. Check standard Firebase / Google Services default_web_client_id in strings.xml
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val str = context.getString(resId)
                if (str.isNotBlank()) return str.trim()
            }
        } catch (_: Throwable) {}

        return null
    }
}
