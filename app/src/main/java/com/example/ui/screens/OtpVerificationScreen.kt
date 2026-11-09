package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue

/**
 * Strict Design Replication of OTP Verification ("Verify Code") Screen
 * Supporting real EmailJS OTP verification, clipboard paste, and direct auto-sign in.
 */
@Composable
fun OtpVerificationScreen(
    targetEmail: String? = null,
    deliveryMessage: String? = null,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onNavigateBack: () -> Unit,
    errorMessage: String? = null
) {
    var otpCode by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section with illustration and back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .statusBarsPadding()
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.illus_otp),
                    contentDescription = "OTP Verification Illustration",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.9f)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            // Bottom White Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp)
                        .padding(top = 32.dp, bottom = 28.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Verify Code",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (!targetEmail.isNullOrBlank()) {
                                "Please enter the code sent to $targetEmail"
                            } else {
                                "Please enter a code from email"
                            },
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )

                        // Notification pill showing the message delivery format
                        if (!deliveryMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4), shape = RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFFBBF7D0), shape = RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailRead,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = deliveryMessage,
                                            fontSize = 13.sp,
                                            color = Color(0xFF166534),
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Code Label & Paste Code helper button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your code",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandBlack
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        val clipboardText = clipboardManager.getText()?.text?.trim().orEmpty()
                                        val digits = clipboardText.filter { it.isDigit() }.take(6)
                                        if (digits.isNotEmpty()) {
                                            otpCode = digits
                                            localError = null
                                            Toast.makeText(context, "Pasted OTP: $digits", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No code found in clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Paste Code",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Paste Code",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Outlined Pill Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color(0xFFF9FAFB), shape = CircleShape)
                                .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (otpCode.isEmpty()) {
                                Text(
                                    text = "******",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 18.sp,
                                    letterSpacing = 6.sp
                                )
                            }
                            BasicTextField(
                                value = otpCode,
                                onValueChange = {
                                    val digitsOnly = it.filter { ch -> ch.isDigit() }
                                    if (digitsOnly.length <= 6) {
                                        otpCode = digitsOnly
                                        localError = null
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    color = BrandBlack,
                                    fontSize = 18.sp,
                                    letterSpacing = 6.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(BrandBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input")
                            )
                        }

                        val activeError = errorMessage ?: localError
                        if (activeError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = activeError,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (otpCode.length < 4) {
                                    localError = "Please enter the 6-digit verification code."
                                } else {
                                    onVerifyOtp(otpCode)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("otp_verify_continue_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlack,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Verify & Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Didn't receive code? ",
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Resend",
                                color = BrandBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        onResendOtp()
                                        Toast.makeText(context, "Resending OTP code...", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
