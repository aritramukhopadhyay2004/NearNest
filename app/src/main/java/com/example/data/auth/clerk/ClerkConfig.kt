package com.example.data.auth.clerk

import android.util.Base64
import com.example.BuildConfig

/**
 * Configuration and Endpoint Resolvers for Clerk Authentication
 */
object ClerkConfig {
    /**
     * Clerk Publishable Key provided in .env (NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY)
     */
    val publishableKey: String = try {
        BuildConfig.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY.ifBlank {
            "pk_test_ZW1pbmVudC1kYXNzaWUtMzI3MS5jbGVyay5hY2NvdW50cy5kZXYk"
        }
    } catch (_: Throwable) {
        "pk_test_ZW1pbmVudC1kYXNzaWUtMzI3MS5jbGVyay5hY2NvdW50cy5kZXYk"
    }

    /**
     * Resolves the Clerk Frontend API domain from the base64-encoded publishable key.
     * Default: "eminent-dassie-3271.clerk.accounts.dev"
     */
    val frontendApiHost: String by lazy {
        try {
            val key = publishableKey.trim()
            val base64Payload = when {
                key.startsWith("pk_test_") -> key.removePrefix("pk_test_")
                key.startsWith("pk_live_") -> key.removePrefix("pk_live_")
                else -> key
            }
            val decodedBytes = try {
                java.util.Base64.getDecoder().decode(base64Payload)
            } catch (_: Throwable) {
                Base64.decode(base64Payload, Base64.DEFAULT)
            }
            val decodedString = String(decodedBytes).trim().removeSuffix("$")
            if (decodedString.isNotEmpty() && decodedString.contains(".")) {
                decodedString
            } else {
                "eminent-dassie-3271.clerk.accounts.dev"
            }
        } catch (_: Throwable) {
            "eminent-dassie-3271.clerk.accounts.dev"
        }
    }

    val baseUrl: String get() = "https://$frontendApiHost"
}
