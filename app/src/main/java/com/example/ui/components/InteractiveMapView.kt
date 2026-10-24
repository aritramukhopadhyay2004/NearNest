package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.PlaceGeofence
import com.example.data.model.TimelineVisit
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.SurfaceCardDark

@Composable
fun InteractiveMapView(
    members: List<FamilyMember>,
    geofences: List<PlaceGeofence>,
    timelineVisits: List<TimelineVisit>,
    selectedMemberId: String,
    isOfflineMode: Boolean,
    syncLatencyMs: Long,
    onSelectMember: (String) -> Unit,
    onSimulatePing: () -> Unit,
    onCaptureRealGps: () -> Unit = {},
    onToggleOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()

    // Interactive Pan & Zoom offset
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulse animation for live markers
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // --- 1. Custom Vector Map Canvas ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + panOffsetX
            val centerY = canvasH / 2f + panOffsetY

            // Base map coordinate bounding box centered around (37.775, -122.420)
            val baseLat = 37.775
            val baseLng = -122.420
            val latSpan = 0.05 / zoomScale
            val lngSpan = 0.05 / zoomScale

            fun projectToScreen(lat: Double, lng: Double): Offset {
                val x = centerX + ((lng - baseLng) / lngSpan * (canvasW * 0.45f)).toFloat()
                val y = centerY - ((lat - baseLat) / latSpan * (canvasH * 0.45f)).toFloat()
                return Offset(x, y)
            }

            // Dark map terrain background
            drawRect(color = Color(0xFF0F172A))

            // Draw Parks / Greenery
            val parkTopLeft = projectToScreen(37.7690, -122.4500)
            drawRoundRect(
                color = Color(0xFF132E27),
                topLeft = parkTopLeft,
                size = Size(180f * zoomScale, 140f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )

            // Draw River / Waterway
            val riverPath = Path().apply {
                val start = projectToScreen(37.795, -122.390)
                moveTo(start.x, start.y)
                val cp1 = projectToScreen(37.780, -122.405)
                val cp2 = projectToScreen(37.765, -122.410)
                val end = projectToScreen(37.750, -122.415)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF1E3A5F),
                style = Stroke(width = 38f * zoomScale)
            )

            // Draw Grid Street Roads
            val roadColor = Color(0xFF1E293B)
            val roadMajor = Color(0xFF334155)

            for (i in -4..4) {
                val p1 = projectToScreen(baseLat + i * 0.008, baseLng - 0.03)
                val p2 = projectToScreen(baseLat + i * 0.008, baseLng + 0.03)
                drawLine(
                    color = if (i == 0) roadMajor else roadColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (i == 0) 6f * zoomScale else 3f * zoomScale
                )

                val v1 = projectToScreen(baseLat - 0.03, baseLng + i * 0.008)
                val v2 = projectToScreen(baseLat + 0.03, baseLng + i * 0.008)
                drawLine(
                    color = if (i == 0) roadMajor else roadColor,
                    start = v1,
                    end = v2,
                    strokeWidth = if (i == 0) 6f * zoomScale else 3f * zoomScale
                )
            }

            // Draw Geofences (Safe Zones)
            for (geo in geofences) {
                val center = projectToScreen(geo.latitude, geo.longitude)
                val radius = (geo.radiusMeters * 0.4f * zoomScale).toFloat().coerceAtLeast(30f)

                // Fill zone
                drawCircle(
                    color = Color(0x2200E5FF),
                    radius = radius,
                    center = center
                )
                // Zone border
                drawCircle(
                    color = Color(0x6600E5FF),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )

                // Draw geofence label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = (12f * zoomScale).coerceIn(24f, 34f)
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                    drawText(geo.name, center.x, center.y + radius + 22f, paint)
                }
            }

            // Draw Movement Polyline for Selected Member (Breadcrumbs)
            if (selectedMember != null) {
                val memberVisits = timelineVisits.filter { it.userId == selectedMember.id }
                if (memberVisits.size >= 2) {
                    val path = Path()
                    memberVisits.forEachIndexed { idx, v ->
                        val geo = geofences.find { it.name.equals(v.placeName, ignoreCase = true) }
                        val lat = geo?.latitude ?: (selectedMember.currentLat + idx * 0.003)
                        val lng = geo?.longitude ?: (selectedMember.currentLng + idx * 0.003)
                        val point = projectToScreen(lat, lng)
                        if (idx == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                    }
                    drawPath(
                        path = path,
                        color = Color(selectedMember.colorHex).copy(alpha = 0.7f),
                        style = Stroke(
                            width = 4f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                        )
                    )
                }
            }

            // Draw Family Member Pins
            for (m in members) {
                val pos = projectToScreen(m.currentLat, m.currentLng)
                val isSelected = m.id == selectedMemberId
                val memberColor = Color(m.colorHex)

                // Radar pulse animation
                if (isSelected) {
                    drawCircle(
                        color = memberColor.copy(alpha = pulseAlpha),
                        radius = (32f * pulseRadiusFraction * zoomScale).coerceAtLeast(20f),
                        center = pos
                    )
                }

                // Outer circle border
                drawCircle(
                    color = if (isSelected) Color.White else Color(0xFF1E293B),
                    radius = 26f * zoomScale,
                    center = pos
                )
                // Member filled avatar circle
                drawCircle(
                    color = memberColor,
                    radius = 22f * zoomScale,
                    center = pos
                )

                // Initial text
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = (16f * zoomScale).coerceIn(24f, 38f)
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(m.name.take(1), pos.x, pos.y + (7f * zoomScale), paint)
                }

                // Label card text below pin
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
                        textSize = (13f * zoomScale).coerceIn(22f, 32f)
                        isFakeBoldText = isSelected
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                    drawText("${m.name} (${m.currentPlace})", pos.x, pos.y - 32f * zoomScale, paint)
                }
            }
        }

        // --- 2. Top Telemetry & Status Badges ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TC01 Latency badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceCardDark.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151)),
                    modifier = Modifier.testTag("telemetry_latency_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOfflineMode) StatusOffline else StatusOnline)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOfflineMode) "Offline Mode (Cached)" else "TC01 Live Sync: ${syncLatencyMs}ms (< 2s)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOfflineMode) AccentRose else AccentCyan
                            )
                        )
                    }
                }

                // Reset map position button
                Surface(
                    shape = CircleShape,
                    color = SurfaceCardDark.copy(alpha = 0.92f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 1.0f
                        }
                        .testTag("reset_map_center_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Center Map",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Family member horizontal selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEach { member ->
                    val isSelected = member.id == selectedMemberId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(member.colorHex) else SurfaceCardDark.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .clickable { onSelectMember(member.id) }
                            .testTag("member_chip_${member.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${member.currentPlace} • ${member.batteryPercent}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Bottom Controls & Action Bar ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceCardDark.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (selectedMember != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedMember.name} (${selectedMember.relation})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Current Location: ${selectedMember.currentPlace} (at ${selectedMember.lastSeenTime12h})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AccentCyan
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = "Battery",
                                tint = if (selectedMember.batteryPercent > 20) AccentEmerald else AccentRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedMember.batteryPercent}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Real GPS (Play Services), Simulate Ping (TC01), and Offline Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCaptureRealGps,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("real_gps_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Real GPS",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Real GPS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onSimulatePing,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("simulate_ping_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color(0xFF00363A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Simulate Ping",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ping (TC01)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onToggleOffline,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("toggle_offline_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOfflineMode) AccentRose else Color(0xFF334155),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.SignalCellularAlt,
                            contentDescription = "Offline Mode",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOfflineMode) "Online" else "Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
