package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.UserSession
import com.example.data.model.FamilyMember
import com.example.data.model.TimelineVisit
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

data class TraversalStopItem(
    val index: Int,
    val name: String,
    val timeRange: String,
    val status: String // "Present" or "visited"
)

/**
 * Strict Design Replication of Daily Traversal Screen (main_daily_traversal.png)
 * Features map route preview card, traversal stat row (Steps, Dist, Duration, Stops),
 * Current location pill, date chips, member selector chips, and chronological stops list
 */
@Composable
fun NearNestDailyTraversalView(
    currentUserSession: UserSession?,
    members: List<FamilyMember>,
    visits: List<TimelineVisit>,
    selectedMemberId: String?,
    onSelectMember: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenProfile: () -> Unit
) {
    var selectedDateRange by remember { mutableStateOf("15 Dec - 20 Dec 2023") }
    var selectedMemberName by remember { mutableStateOf(currentUserSession?.displayName ?: "Amit") }

    val defaultLocation = LatLng(21.9868, 87.8542)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    val routePoints = remember {
        listOf(
            LatLng(21.9960, 87.8480),
            LatLng(21.9920, 87.8500),
            LatLng(21.9880, 87.8490),
            LatLng(21.9840, 87.8540),
            LatLng(21.9810, 87.8580),
            LatLng(21.9770, 87.8630)
        )
    }

    val stopsList = remember {
        listOf(
            TraversalStopItem(1, "Home", "12:04 PM – 05:40 PM", "Present"),
            TraversalStopItem(2, "Office", "8:30 AM – 12:04 PM", "visited"),
            TraversalStopItem(3, "National Park", "7:00 AM – 08:30 AM", "visited"),
            TraversalStopItem(4, "Supermarket", "6:00 AM – 07:00 AM", "visited")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // 1. Top Section: Interactive Map Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                // Traversal blue polyline
                Polyline(
                    points = routePoints,
                    color = Color(0xFF0284C7),
                    width = 8f
                )

                Marker(
                    state = MarkerState(position = routePoints.first()),
                    title = "Origin: Home"
                )
                Marker(
                    state = MarkerState(position = routePoints.last()),
                    title = "Current: Muradpur, Chandipur"
                )
            }

            // Top Buttons: Back Arrow & Profile Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.avatar_user),
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onOpenProfile() },
                    contentScale = ContentScale.Crop
                )
            }

            // Bottom Floating Card on Map: "Daily Traversal / See monthly"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Traversal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "See monthly",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Thumbnail with 5+ overlay
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.traversal_thumb),
                            contentDescription = "Monthly Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "5+",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Traversal Stat Row (Steps, Est. Dist., Duration, Stops)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(label = "Steps", value = "4500")
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFFE5E7EB)))
                StatColumn(label = "Est. Dist.", value = "8.5 km")
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFFE5E7EB)))
                StatColumn(label = "Duration", value = "5h")
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFFE5E7EB)))
                StatColumn(label = "Stops", value = "10")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Current Location Outlined Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                Text(
                    text = "Current Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3VFQ+WHP, Muradpur, Chandipur, West Bengal",
                    fontSize = 11.sp,
                    color = Color(0xFF4B5563)
                )
                Text(
                    text = "7:30 AM | 65%",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Choose Date Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose date",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Date Info",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "15 Dec – 20 Dec 2023",
                isSelected = selectedDateRange == "15 Dec – 20 Dec 2023",
                onClick = { selectedDateRange = "15 Dec – 20 Dec 2023" }
            )
            FilterChipItem(
                label = "25 Dec – 30 Dec 2023",
                isSelected = selectedDateRange == "25 Dec – 30 Dec 2023",
                onClick = { selectedDateRange = "25 Dec – 30 Dec 2023" }
            )
            FilterChipItem(
                label = "Choose Date",
                isSelected = false,
                icon = Icons.Default.CalendarMonth,
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Choose Member Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose Member",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Member Info",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val membersDisplay = remember {
            listOf("Amit", "Ronit", "Sarah", "Mou", "Dipak", "Ajay")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            membersDisplay.forEach { name ->
                FilterChipItem(
                    label = name,
                    isSelected = selectedMemberName.equals(name, ignoreCase = true),
                    onClick = { selectedMemberName = name }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Chronological Stop Cards (matching main_daily_traversal.png)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stopsList.forEach { stop ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${stop.index}. ${stop.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stop.timeRange,
                                fontSize = 12.sp,
                                color = Color(0xFF4B5563)
                            )
                        }

                        Text(
                            text = stop.status,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlack
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) BrandBlack else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) BrandBlack else Color(0xFFD1D5DB)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else BrandBlack,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else BrandBlack
            )
        }
    }
}

