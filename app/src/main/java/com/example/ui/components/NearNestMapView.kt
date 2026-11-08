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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.UserSession
import com.example.data.model.FamilyMember
import com.example.data.model.PlaceGeofence
import com.example.data.model.TimelineVisit
import com.example.ui.theme.BrandBlack
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Strict Design Replication of Map Screen (main_map.png)
 * Light map style with route, pins, top NearNest brand bar & user profile trigger
 */
@Composable
fun NearNestMapView(
    currentUserSession: UserSession?,
    members: List<FamilyMember>,
    geofences: List<PlaceGeofence>,
    timelineVisits: List<TimelineVisit>,
    onOpenProfile: () -> Unit,
    onCaptureRealGps: () -> Unit,
    onSimulatePing: () -> Unit
) {
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    // Coordinates default: Chandipur, West Bengal
    val defaultLocation = LatLng(21.9868, 87.8542)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14.5f)
    }

    // Traversal route polyline points (replicates red line in main_map.png)
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Google Map Light Surface
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = mapType
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Traversal Route Line (Coral/Red as in main_map.png)
            Polyline(
                points = routePoints,
                color = Color(0xFFE0534C),
                width = 8f
            )

            // Start & End Route Markers
            Marker(
                state = MarkerState(position = routePoints.first()),
                title = "Route Origin",
                snippet = "Chandipur, 116b"
            )

            Marker(
                state = MarkerState(position = routePoints.last()),
                title = "Destination",
                snippet = "Arrival Point"
            )

            // Real Live Family Member Markers
            members.forEach { member ->
                Marker(
                    state = MarkerState(position = LatLng(member.currentLat, member.currentLng)),
                    title = member.name,
                    snippet = "${member.relation} • Battery ${member.batteryPercent}%"
                )
            }
        }

        // 2. Top Header Bar (Strict replication of main_map.png)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: NearNest Teal Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "NearNest Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NearNest",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )
                }

                // Right: Location & Profile Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Chandipur, 116b",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = currentUserSession?.displayName ?: "Mr. Amit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Image(
                        painter = painterResource(id = R.drawable.avatar_user),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFE2E8F0), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 3. Floating Action Controls on Map
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Map Layers Toggle
            FloatingActionButton(
                onClick = {
                    mapType = if (mapType == MapType.NORMAL) MapType.TERRAIN else MapType.NORMAL
                },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = BrandBlack,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Toggle Map Layer",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Re-center Location Button (crosshairs as in main_map.png)
            FloatingActionButton(
                onClick = {
                    onCaptureRealGps()
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
                },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = BrandBlack,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Re-center GPS",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

