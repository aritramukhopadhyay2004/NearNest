package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.FamilyTrackerViewModel
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue

/**
 * Strict Design Replication of Sign In Screen (auth_signin.png)
 * Integrated with Clerk Authentication and Reactive Compose State (useState equivalent)
 */
@Composable
fun ClerkLoginScreen(
    viewModel: FamilyTrackerViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    // Form Inputs via reactive state (useState equivalent)
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isAuthProcessing by viewModel.isAuthProcessing.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val clerkPendingVerification by viewModel.clerkVerificationPending.collectAsState()

    // Verification modal dialog if Clerk requires MFA / Email OTP
    clerkPendingVerification?.let { pending ->
        ClerkVerificationDialog(
            needsVerification = pending,
            onDismiss = { viewModel.dismissClerkVerification() },
            onVerify = { code ->
                viewModel.verifyClerkCode(code = code)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section: Camper Van Graphic on Blue Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .statusBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.illus_signin),
                    contentDescription = "Sign In Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.FillWidth
                )
            }

            // Bottom White Card with 40.dp top rounded corners
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.45f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp)
                        .padding(top = 32.dp, bottom = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title & Subtitle
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please enter a valid account",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Input 1: Email
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color(0xFFF9FAFB), shape = CircleShape)
                                .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (identifier.isEmpty()) {
                                Text(
                                    text = "youremails@yahoo.com",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = identifier,
                                onValueChange = { identifier = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                textStyle = TextStyle(
                                    color = BrandBlack,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(BrandBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clerk_login_identifier_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Input 2: Password
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color(0xFFF9FAFB), shape = CircleShape)
                                .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (password.isEmpty()) {
                                Text(
                                    text = "********",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (identifier.isNotBlank() && password.isNotBlank()) {
                                                viewModel.signIn(identifier.trim(), password)
                                            }
                                        }
                                    ),
                                    textStyle = TextStyle(
                                        color = BrandBlack,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(BrandBlue),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("clerk_login_password_input")
                                )
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Forgot Password Right-aligned Link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandBlack,
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }

                    // Error banner
                    AnimatedVisibility(
                        visible = authError != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        authError?.let { err ->
                            Text(
                                text = err,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Primary Button: "Sign In"
                    Button(
                        onClick = {
                            if (identifier.isNotBlank() && password.isNotBlank()) {
                                viewModel.signIn(identifier.trim(), password)
                            } else {
                                viewModel.setAuthError("Please fill in both email and password.")
                            }
                        },
                        enabled = !isAuthProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("clerk_login_submit_btn"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlack,
                            contentColor = Color.White
                        )
                    ) {
                        if (isAuthProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // OR Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE5E7EB)
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = BrandBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE5E7EB)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Social login buttons: Google and X
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            modifier = Modifier
                                .size(width = 64.dp, height = 48.dp)
                                .clickable {
                                    viewModel.startClerkOAuth("google")
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Sign in with Google",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // X (Twitter) button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            modifier = Modifier
                                .size(width = 64.dp, height = 48.dp)
                                .clickable {
                                    viewModel.startClerkOAuth("twitter")
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_x_logo),
                                    contentDescription = "Sign in with X",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Bottom Navigation Toggle: Don't have account? Sign Up
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Don’t have account? ",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sign Up",
                            color = BrandBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClerkVerificationDialog(
    needsVerification: com.example.data.auth.clerk.ClerkAuthResult.NeedsVerification,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please enter the verification code sent to your ${needsVerification.strategy}.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFF9FAFB), shape = CircleShape)
                        .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (code.isEmpty()) {
                        Text(text = "Enter 6-digit code", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = BrandBlack, fontSize = 16.sp, letterSpacing = 2.sp),
                        cursorBrush = SolidColor(BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onVerify(code.trim()) },
                    enabled = code.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Verify & Continue", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

