package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.UserSession
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue

/**
 * Strict Design Replication of Account Info Screen (main_account_info.png)
 * Blue header with < Back, photo picker card with + badge, Gender radio selector,
 * Name fields, Email, Phone & Emergency Number with country code, and Save button
 */
@Composable
fun NearNestAccountInfoView(
    currentUserSession: UserSession?,
    onSaveProfile: (firstName: String, lastName: String, email: String, phone: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("Sir") }
    var firstName by remember {
        mutableStateOf(currentUserSession?.displayName?.substringBefore(" ") ?: "Amit")
    }
    var lastName by remember {
        mutableStateOf(currentUserSession?.displayName?.substringAfter(" ", "") ?: "Mukhopadhyay")
    }
    var email by remember {
        mutableStateOf(currentUserSession?.contact ?: "amit@nearnest.app")
    }
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var emergencyNumber by remember { mutableStateOf("9123456780") }

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
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp, bottom = 90.dp)
                ) {
                    Text(
                        text = "Account Info",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Photo card with avatar & + badge
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(54.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.avatar_user),
                                    contentDescription = "User Photo",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(BrandBlack)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Your Photo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlack
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Adding a profile picture makes\nyour profile more personalized.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gender Radio Selector
                    Text(
                        text = "Gender",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedGender = "Sir" }
                        ) {
                            RadioButton(
                                selected = selectedGender == "Sir",
                                onClick = { selectedGender = "Sir" },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandBlack)
                            )
                            Text(text = "Sir", fontSize = 14.sp, color = BrandBlack, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedGender = "Mrs." }
                        ) {
                            RadioButton(
                                selected = selectedGender == "Mrs.",
                                onClick = { selectedGender = "Mrs." },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandBlack)
                            )
                            Text(text = "Mrs.", fontSize = 14.sp, color = BrandBlack, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Section
                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AccountPillField(
                        label = "First Name",
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "Your first name"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountPillField(
                        label = "Last Name",
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Your last name"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email, Phone & Emergency Number
                    Text(
                        text = "Email, Phone Number & Emergency Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AccountPillField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "youremail@yahoo.co.id"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountPhoneField(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        countryCode = "+91"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountPhoneField(
                        label = "Emergency Number",
                        value = emergencyNumber,
                        onValueChange = { emergencyNumber = it },
                        countryCode = "+91"
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Save Button
                    Button(
                        onClick = {
                            onSaveProfile(firstName, lastName, email, phoneNumber)
                            onNavigateBack()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlack,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountPillField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BrandBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFFF9FAFB), shape = CircleShape)
                .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = BrandBlack, fontSize = 14.sp),
                cursorBrush = SolidColor(BrandBlue),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AccountPhoneField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    countryCode: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BrandBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFFF9FAFB), shape = CircleShape)
                .border(1.dp, Color(0xFFD1D5DB), shape = CircleShape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ID ($countryCode)",
                        color = BrandBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = BrandBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = BrandBlack, fontSize = 14.sp),
                    cursorBrush = SolidColor(BrandBlue),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

