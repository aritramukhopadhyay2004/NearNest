package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.FamilyNest
import com.example.data.auth.UserSession
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue

/**
 * Strict Design Replication of Settings Screen (main_settings.png)
 * Blue header with < Back, rounded white card with Settings menu list
 */
@Composable
fun NearNestSettingsView(
    currentUserSession: UserSession?,
    currentNest: FamilyNest,
    onOpenAccountInfo: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenNestManagement: () -> Unit,
    onToggleLocation: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showPasswordModal by remember { mutableStateOf(false) }
    var showSecurityModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar: "< Back" on Blue Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Back",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }

            // 2. White Card Sheet with Top Rounded Corners
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp, bottom = 84.dp)
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Menu Item 1: Signed in as Mr. Amit
                    SettingsMenuCard(
                        iconContent = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BrandBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Signed in as ${currentUserSession?.displayName ?: "Mr. Amit"}",
                        onClick = onOpenAccountInfo
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Menu Item 2: Password
                    SettingsMenuCard(
                        iconContent = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_vuesax_lock_circle),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Password",
                        onClick = { showPasswordModal = true }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Menu Item 3: Security
                    SettingsMenuCard(
                        iconContent = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_vuesax_security_safe),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Security",
                        onClick = { showSecurityModal = true }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Menu Item 4: Create Nest, Add to Nest, Invite
                    SettingsMenuCard(
                        iconContent = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_vuesax_dollar_circle),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Create Nest, Add to Nest, Invite",
                        onClick = onOpenNestManagement
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Menu Item 5: Notifications
                    SettingsMenuCard(
                        iconContent = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = BrandBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Notifications",
                        onClick = onOpenNotifications
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Bottom Group: Location
                    SettingsMenuCard(
                        iconContent = {
                            Image(
                                painterResource(id = R.drawable.ic_vuesax_location),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Location",
                        onClick = onToggleLocation
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Group: Sign Out
                    SettingsMenuCard(
                        iconContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Sign Out",
                        onClick = onSignOut
                    )
                }
            }
        }
    }

    if (showPasswordModal) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPasswordModal = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Password & Security", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Your password is encrypted and managed through Clerk Identity Protection.", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(20.dp))
                    androidx.compose.material3.Button(
                        onClick = { showPasswordModal = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }

    if (showSecurityModal) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSecurityModal = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Privacy & Security", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("NearNest enforces zero coordinate leakage, end-to-end encrypted GPS telemetry, and 12-hour AM/PM timestamps.", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(20.dp))
                    androidx.compose.material3.Button(
                        onClick = { showSecurityModal = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    iconContent: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF9FAFB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                iconContent()
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandBlack,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BrandBlack,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

