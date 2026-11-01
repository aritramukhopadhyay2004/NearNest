package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.AuthMethod
import com.example.data.auth.AuthState
import com.example.data.auth.OtpChallenge
import com.example.data.auth.UserSession
import com.example.ui.FamilyTrackerViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(viewModel: FamilyTrackerViewModel) {
    val authState by viewModel.authState.collectAsState()
    val activeChallenge by viewModel.activeOtpChallenge.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val lastDeliveredOtpMessage by viewModel.lastDeliveredOtpMessage.collectAsState()

    var selectedMethod by remember { mutableStateOf(AuthMethod.PHONE_SMS) }
    var phoneInput by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var nestCodeInput by remember { mutableStateOf("NEST-4821") }

    val countryCodes = listOf("+91 (India)", "+1 (USA)", "+44 (UK)", "+61 (Australia)", "+971 (UAE)", "+81 (Japan)")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D16),
                        Color(0xFF0F172A),
                        Color(0xFF0B132B)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo & Header
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "NearNest Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NearNest",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "FAMILY SAFETY & MOVEMENT TRACKING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (authState == AuthState.ENTER_OTP && activeChallenge != null) {
                // ==========================================
                // OTP Verification View
                // ==========================================
                OtpVerificationCard(
                    challenge = activeChallenge!!,
                    error = authError,
                    deliveredPreview = lastDeliveredOtpMessage,
                    onVerify = { enteredOtp ->
                        viewModel.verifyOtp(enteredOtp, nameInput)
                    },
                    onResend = {
                        viewModel.resendActiveOtp()
                    },
                    onChangeContact = {
                        viewModel.cancelOtpVerification()
                    }
                )
            } else {
                // ==========================================
                // Login Method Selection & Credentials Input
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Sign In / Register Device",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "New or existing devices can authenticate instantly with Phone SMS or Google Account Gmail OTP.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                        )

                        // Method Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Phone SMS tab
                            val isPhoneSelected = selectedMethod == AuthMethod.PHONE_SMS
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPhoneSelected) AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (isPhoneSelected) 1.dp else 0.dp,
                                        color = if (isPhoneSelected) AccentCyan else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedMethod = AuthMethod.PHONE_SMS }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        tint = if (isPhoneSelected) AccentCyan else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Phone (SMS)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isPhoneSelected) Color.White else TextTertiary,
                                            fontWeight = if (isPhoneSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            // Google Gmail tab
                            val isGoogleSelected = selectedMethod == AuthMethod.GOOGLE_GMAIL
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isGoogleSelected) AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (isGoogleSelected) 1.dp else 0.dp,
                                        color = if (isGoogleSelected) AccentCyan else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedMethod = AuthMethod.GOOGLE_GMAIL }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Google",
                                        tint = if (isGoogleSelected) AccentCyan else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google (Gmail)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isGoogleSelected) Color.White else TextTertiary,
                                            fontWeight = if (isGoogleSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full Name Input
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Name (Optional)", color = TextTertiary) },
                            placeholder = { Text(if (selectedMethod == AuthMethod.PHONE_SMS) "e.g. John" else "e.g. Sarah", color = TextTertiary.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AccentCyan)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = SurfaceBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedMethod == AuthMethod.PHONE_SMS) {
                            // Phone Number Field with Country Code Picker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, SurfaceBorderDark),
                                        modifier = Modifier
                                            .height(56.dp)
                                            .clickable { isCountryDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedCountryCode,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isCountryDropdownExpanded,
                                        onDismissRequest = { isCountryDropdownExpanded = false },
                                        modifier = Modifier.background(SurfaceCardDark)
                                    ) {
                                        countryCodes.forEach { entry ->
                                            val code = entry.substringBefore(" ")
                                            DropdownMenuItem(
                                                text = { Text(entry, color = Color.White) },
                                                onClick = {
                                                    selectedCountryCode = code
                                                    isCountryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Mobile Phone Number", color = TextTertiary) },
                                    placeholder = { Text("98765 43210", color = TextTertiary.copy(alpha = 0.6f)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = AccentCyan)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = SurfaceBorderDark,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        } else {
                            // Google Account Gmail Input
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Google Account / Gmail Address", color = TextTertiary) },
                                placeholder = { Text("your.name@gmail.com", color = TextTertiary.copy(alpha = 0.6f)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = SurfaceBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Family Nest Code Input
                        OutlinedTextField(
                            value = nestCodeInput,
                            onValueChange = { nestCodeInput = it },
                            label = { Text("Family Nest Code", color = TextTertiary) },
                            placeholder = { Text("NEST-4821", color = TextTertiary.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = AccentCyan)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = SurfaceBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Error Banner
                        AnimatedVisibility(visible = authError != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentRose.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = authError ?: "",
                                        color = AccentRose,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Send OTP Action Button
                        Button(
                            onClick = {
                                if (selectedMethod == AuthMethod.PHONE_SMS) {
                                    viewModel.requestPhoneOtp(
                                        rawPhone = phoneInput,
                                        countryCode = selectedCountryCode,
                                        userName = nameInput,
                                        nestCode = nestCodeInput
                                    )
                                } else {
                                    viewModel.requestGoogleOtp(
                                        rawEmail = emailInput,
                                        userName = nameInput,
                                        nestCode = nestCodeInput
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentCyan,
                                contentColor = Color(0xFF00363A)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (selectedMethod == AuthMethod.PHONE_SMS) "Send SMS Verification Code" else "Send Gmail Verification Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fast 1-Tap Testing Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ Instant Testing Shortcuts for New Devices",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.quickDemoLogin(AuthMethod.PHONE_SMS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                            ) {
                                Text("John (Phone)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.quickDemoLogin(AuthMethod.GOOGLE_GMAIL) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD946EF))
                            ) {
                                Text("Sarah (Google)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * OTP Input and Verification Card
 */
@Composable
fun OtpVerificationCard(
    challenge: OtpChallenge,
    error: String?,
    deliveredPreview: String?,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onChangeContact: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var secondsRemaining by remember { mutableIntStateOf(challenge.remainingSeconds()) }

    // Countdown Timer Effect
    LaunchedEffect(challenge.timestampEpoch) {
        secondsRemaining = challenge.remainingSeconds()
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining = challenge.remainingSeconds()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        border = BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Channel Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.15f))
                    .border(1.5.dp, AccentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (challenge.method == AuthMethod.PHONE_SMS) Icons.Default.Sms else Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Enter 6-Digit Code",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            )

            Text(
                text = "Verification code dispatched via ${challenge.method.channelName} to:",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = challenge.target,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Delivered Notification Banner with 1-Tap Quick-Fill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F2537),
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (challenge.method == AuthMethod.PHONE_SMS) "INCOMING SMS SIMULATION" else "GMAIL INBOX SIMULATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Delivered Code: ${challenge.code}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentCyan),
                        modifier = Modifier.clickable {
                            otpCode = challenge.code
                        }
                    ) {
                        Text(
                            text = "Tap to Auto-Fill: ${challenge.code}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // OTP Code Input Field
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("6-Digit Verification Code", color = TextTertiary) },
                placeholder = { Text("• • • • • •", color = TextTertiary.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = SurfaceBorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Error Message
            AnimatedVisibility(visible = error != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentRose.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = error ?: "",
                            color = AccentRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verify Action Button
            Button(
                onClick = { onVerify(otpCode) },
                enabled = otpCode.length >= 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color(0xFF00363A),
                    disabledContainerColor = SurfaceBorderDark,
                    disabledContentColor = TextTertiary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Access NearNest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resend & Change Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onChangeContact() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change Target",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (secondsRemaining > 0) {
                    Text(
                        text = "Resend in ${secondsRemaining}s",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Row(
                        modifier = Modifier.clickable { onResend() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resend Code",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog displaying active user session details with option to switch account or sign out
 */
@Composable
fun UserSessionDialog(
    session: UserSession,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            border = BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Avatar
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(session.avatarColorHex))
                        .border(2.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = session.contact,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Verification details card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, SurfaceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Auth Channel", color = TextTertiary, fontSize = 12.sp)
                            Text(
                                text = if (session.authMethod == AuthMethod.PHONE_SMS) "Mobile SMS OTP" else "Google Gmail OTP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Verification Status", color = TextTertiary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Verified Device", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Family Nest ID", color = TextTertiary, fontSize = 12.sp)
                            Text(
                                text = session.familyNestCode,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Session Active Since", color = TextTertiary, fontSize = 12.sp)
                            Text(
                                text = session.loginTimestamp12h,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Switch Account / Sign Out
                Button(
                    onClick = {
                        onDismiss()
                        onSignOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose.copy(alpha = 0.2f),
                        contentColor = AccentRose
                    ),
                    border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.6f))
                ) {
                    Text(text = "Switch Account / Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Text(text = "Close", fontSize = 12.sp)
                }
            }
        }
    }
}
