package com.example

import com.example.data.auth.AuthMethod
import com.example.data.auth.AuthPage
import com.example.data.auth.OtpPurpose
import com.example.data.auth.clerk.ClerkConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying Clerk configuration, routing enums, and authentication models.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testClerkPublishableKeyMatchesConfiguredValue() {
    val key = ClerkConfig.publishableKey
    assertTrue("Key should start with pk_test_", key.startsWith("pk_test_"))
    assertEquals("pk_test_ZW1pbmVudC1kYXNzaWUtMzI3MS5jbGVyay5hY2NvdW50cy5kZXYk", key)
  }

  @Test
  fun testClerkFrontendApiHostDecodedCorrectly() {
    val host = ClerkConfig.frontendApiHost
    assertEquals("eminent-dassie-3271.clerk.accounts.dev", host)
    assertEquals("https://eminent-dassie-3271.clerk.accounts.dev", ClerkConfig.baseUrl)
  }

  @Test
  fun testAuthEnumsIncludeClerkAndPages() {
    assertEquals("CLERK_AUTH", AuthMethod.CLERK_AUTH.name)
    assertEquals("LOGIN", AuthPage.LOGIN.name)
    assertEquals("SIGNUP", AuthPage.SIGNUP.name)
    assertEquals("FORGOT_PASSWORD", AuthPage.FORGOT_PASSWORD.name)
    assertEquals("OTP_VERIFY", AuthPage.OTP_VERIFY.name)
    assertEquals("RESET_PASSWORD", AuthPage.RESET_PASSWORD.name)
    assertEquals(3, OtpPurpose.values().size)
  }
}

