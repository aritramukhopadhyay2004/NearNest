package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.auth.AuthMethod
import com.example.data.auth.AuthPage
import com.example.data.auth.AuthState
import com.example.data.auth.FamilyNest
import com.example.data.auth.OtpChallenge
import com.example.data.auth.UserSession
import com.example.ui.FamilyTrackerViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay

/**
 * Pinterest-Style NearNest Authentication & Onboarding Screen
 * Features:
 * - Top mosaic preview collage of NearNest capabilities (Radar, Traversal, AI Safety, Telemetry)
 * - Centered circular NearNest emblem
 * - Bold headline ("Keep your loved ones safe & close")
 * - Single unified input field for Email or Mobile Phone
 * - "Continue" primary CTA
 * - "Or" divider
 * - "Continue with Google" button with Google logo
 * - Nest Creation & Join System with Unique Nest No & Nest Pass
 */
@Composable
fun AuthScreen(viewModel: FamilyTrackerViewModel) {
    val authState by viewModel.authState.collectAsState()
    val activeChallenge by viewModel.activeOtpChallenge.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val lastDeliveredOtpMessage by viewModel.lastDeliveredOtpMessage.collectAsState()
    val currentNest by viewModel.currentNest.collectAsState()
    val currentAuthPage by viewModel.currentAuthPage.collectAsState()
    val context = LocalContext.current

    var nameInput by remember { mutableStateOf("") }
    var showNestJoinDialog by remember { mutableStateOf(false) }

    when {
        (authState == AuthState.ENTER_OTP && activeChallenge != null) || currentAuthPage == AuthPage.OTP_VERIFY -> {
            com.example.ui.screens.OtpVerificationScreen(
                onVerifyOtp = { code ->
                    viewModel.verifyOtp(code, nameInput)
                },
                onResendOtp = { viewModel.resendActiveOtp() },
                onNavigateBack = {
                    viewModel.cancelOtpVerification()
                    viewModel.setAuthPage(AuthPage.LOGIN)
                },
                errorMessage = authError
            )
        }
    authState == AuthState.NEST_SETUP -> {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04070D),
                            Color(0xFF0B132B),
                            Color(0xFF070B14)
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
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================================
                // Nest Setup View (Create or Join with Nest Pass)
                // ==========================================
                Spacer(modifier = Modifier.height(28.dp))

                NestSetupCard(
                    currentNest = currentNest,
                    onCreateNest = { nestName ->
                        viewModel.createNewNest(nestName)
                        viewModel.completeNestSetup()
                    },
                    onJoinNest = { nestNo, nestPass ->
                        val success = viewModel.joinNest(nestNo, nestPass)
                        if (success) {
                            viewModel.completeNestSetup()
                        }
                    },
                    onSkip = {
                        viewModel.completeNestSetup()
                    }
                )
            }
        }
    }
    currentAuthPage == AuthPage.ONBOARDING -> {
            com.example.ui.screens.OnboardingScreen(
                onFinish = { viewModel.completeOnboarding() }
            )
        }
        currentAuthPage == AuthPage.SIGNUP -> {
            ClerkSignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = { viewModel.setAuthPage(AuthPage.LOGIN) }
            )
        }
        currentAuthPage == AuthPage.FORGOT_PASSWORD -> {
            com.example.ui.screens.ForgotPasswordScreen(
                onSendOtp = { email ->
                    viewModel.requestGoogleOtp(rawEmail = email)
                    viewModel.setAuthPage(AuthPage.OTP_VERIFY)
                },
                onNavigateBack = { viewModel.setAuthPage(AuthPage.LOGIN) }
            )
        }
        else -> {
            ClerkLoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { viewModel.setAuthPage(AuthPage.SIGNUP) },
                onNavigateToForgotPassword = { viewModel.setAuthPage(AuthPage.FORGOT_PASSWORD) }
            )
        }
    }

    // Modal Dialog to Join Nest with Nest No & Nest Pass directly
    if (showNestJoinDialog) {
        JoinNestDialog(
            currentNest = currentNest,
            onDismiss = { showNestJoinDialog = false },
            onConnect = { nestNo, nestPass ->
                val success = viewModel.joinNest(nestNo, nestPass)
                if (success) {
                    showNestJoinDialog = false
                    Toast.makeText(context, "Connected to Family Nest $nestNo!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * Pinterest-Style Mosaic Hero Collage
 * Beautiful preview tiles showcasing NearNest features:
 * - Live GPS Radar with pulse beacon
 * - Today's Traversal polyline route preview
 * - AI Safety Assistant query card
 * - Family battery & telemetry status
 */
@Composable
fun PinterestMosaicHeroCollage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Column: Live Radar Card + SOS Badge
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Radar Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0D182E),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE RADAR", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("Home Zone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("In Radius • 200m", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // AI Safety Status Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141A29),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Emma at School", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Arrived 8:15 AM", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }

            // Center Column: Today's Traversal Route Card (Prominent Pin)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F1E36),
                border = BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TODAY'S ROUTE", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                    }

                    // Mini Polyline Preview Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF081224))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(size.width * 0.15f, size.height * 0.75f)
                                lineTo(size.width * 0.45f, size.height * 0.35f)
                                lineTo(size.width * 0.65f, size.height * 0.6f)
                                lineTo(size.width * 0.85f, size.height * 0.2f)
                            }
                            drawPath(
                                path = path,
                                color = AccentCyan,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            // Draw stops
                            drawCircle(color = AccentCyan, radius = 4.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.75f))
                            drawCircle(color = AccentEmerald, radius = 4.dp.toPx(), center = Offset(size.width * 0.45f, size.height * 0.35f))
                            drawCircle(color = AccentCyan, radius = 4.dp.toPx(), center = Offset(size.width * 0.65f, size.height * 0.6f))
                            drawCircle(color = Color(0xFFD946EF), radius = 5.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.2f))
                        }
                    }

                    Column {
                        Text("4 Stops • 1 Commute", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("12-Hour AM/PM Logged", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            // Right Column: Family Telemetry & Battery Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Family Telemetry
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF131A2D),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFFD946EF), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TELEMETRY", color = Color(0xFFD946EF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("John", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("78%", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sarah", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("88%", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Family Nest Pass Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F1E2A),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Nest Pass Protected", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("Unique Nest No", color = AccentCyan, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Nest Setup Card: Create a new Nest or Join with Nest Pass
 */
@Composable
fun NestSetupCard(
    currentNest: FamilyNest,
    onCreateNest: (String) -> Unit,
    onJoinNest: (String, String) -> Unit,
    onSkip: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Create, 1 = Join
    var nestNameInput by remember { mutableStateOf("The Family Nest") }
    var nestNoInput by remember { mutableStateOf("") }
    var nestPassInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.15f))
                    .border(1.5.dp, AccentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Connect Your Family Nest",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            )

            Text(
                text = "Create a new family group with a unique Nest No & Pass, or enter your family's Nest Pass to link instantly.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Switch Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .border(if (selectedTab == 0) 1.dp else 0.dp, if (selectedTab == 0) AccentCyan else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create New Nest",
                        color = if (selectedTab == 0) Color.White else TextTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) AccentCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .border(if (selectedTab == 1) 1.dp else 0.dp, if (selectedTab == 1) AccentCyan else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Join with Nest Pass",
                        color = if (selectedTab == 1) Color.White else TextTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                // Tab 0: Create New Nest
                OutlinedTextField(
                    value = nestNameInput,
                    onValueChange = { nestNameInput = it },
                    label = { Text("Family Nest Name", color = TextTertiary) },
                    placeholder = { Text("e.g. The Mukherjee Family", color = TextTertiary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = AccentCyan) },
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onCreateNest(nestNameInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color(0xFF00363A))
                ) {
                    Text("Generate Nest No & Pass", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                // Tab 1: Join Existing Nest
                OutlinedTextField(
                    value = nestNoInput,
                    onValueChange = { nestNoInput = it },
                    label = { Text("Family Nest No", color = TextTertiary) },
                    placeholder = { Text("e.g. NEST-7492", color = TextTertiary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AccentCyan) },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nestPassInput,
                    onValueChange = { nestPassInput = it },
                    label = { Text("Secret Nest Pass", color = TextTertiary) },
                    placeholder = { Text("e.g. PASS-4821", color = TextTertiary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onJoinNest(nestNoInput, nestPassInput) },
                    enabled = nestNoInput.isNotBlank() && nestPassInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color(0xFF00363A))
                ) {
                    Text("Connect to Family Nest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Skip / Default
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceBorderDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Continue to Default Nest (${currentNest.nestNo})", fontSize = 12.sp)
            }
        }
    }
}

/**
 * Modal Dialog to Join Nest with Nest No & Nest Pass directly from anywhere in the app
 */
@Composable
fun JoinNestDialog(
    currentNest: FamilyNest,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit
) {
    var nestNo by remember { mutableStateOf("") }
    var nestPass by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(36.dp))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Connect to Family Nest",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = "Enter your family's unique Nest No and Nest Pass to sync live movement and safe zones.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nestNo,
                    onValueChange = { nestNo = it },
                    label = { Text("Nest No", color = TextTertiary) },
                    placeholder = { Text("e.g. NEST-7492", color = TextTertiary.copy(alpha = 0.5f)) },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nestPass,
                    onValueChange = { nestPass = it },
                    label = { Text("Nest Pass", color = TextTertiary) },
                    placeholder = { Text("e.g. PASS-4821", color = TextTertiary.copy(alpha = 0.5f)) },
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

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onConnect(nestNo, nestPass) },
                    enabled = nestNo.isNotBlank() && nestPass.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color(0xFF00363A))
                ) {
                    Text("Connect Device", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SurfaceBorderDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancel", fontSize = 12.sp)
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

    LaunchedEffect(challenge.timestampEpoch) {
        secondsRemaining = challenge.remainingSeconds()
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining = challenge.remainingSeconds()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
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
 * Dialog displaying active user session details and Family Nest credentials
 * Allows copying Nest No & Nest Pass, toggling visibility, or signing out.
 */
@Composable
fun UserSessionDialog(
    session: UserSession,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var isPassVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            border = BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

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
                            Text(text = "Status", color = TextTertiary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Verified Device", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Family Nest Details
                        Text(
                            text = "FAMILY NEST CREDENTIALS",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Nest No
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Nest No", color = TextTertiary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.nestNo,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Nest No",
                                    tint = AccentCyan,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Nest No", session.nestNo))
                                            Toast.makeText(context, "Copied Nest No: ${session.nestNo}", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nest Pass
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Nest Pass", color = TextTertiary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPassVisible) session.nestPass else "••••••••",
                                    color = AccentCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Pass",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { isPassVisible = !isPassVisible }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Nest Pass",
                                    tint = AccentCyan,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Nest Pass", session.nestPass))
                                            Toast.makeText(context, "Copied Nest Pass to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Share Nest Pass Button
                Button(
                    onClick = {
                        val shareText = "Join our family on NearNest!\nNest No: ${session.nestNo}\nNest Pass: ${session.nestPass}\nOpen NearNest and enter our Nest Pass to connect instantly."
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share NearNest Pass")
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color(0xFF00363A))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share Nest Pass with Family", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: Switch Account / Sign Out
                Button(
                    onClick = {
                        onDismiss()
                        onSignOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose.copy(alpha = 0.2f),
                        contentColor = AccentRose
                    ),
                    border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.6f))
                ) {
                    Text(text = "Switch Account / Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
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
