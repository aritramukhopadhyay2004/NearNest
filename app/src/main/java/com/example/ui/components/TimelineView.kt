package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.TimelineVisit
import com.example.data.repository.FamilyDataSeed
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TimelineView(
    members: List<FamilyMember>,
    visits: List<TimelineVisit>,
    selectedMemberId: String,
    onSelectMember: (String) -> Unit,
    onCaptureLiveGps: () -> Unit = {},
    onSimulatePing: () -> Unit = {},
    onViewOnMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
    val memberVisits = visits.filter { it.userId == selectedMemberId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Top Traversal Map Card (Above Movement Record Graph) ---
        if (selectedMember != null) {
            item(key = "traversal_map_${selectedMember.id}") {
                Spacer(modifier = Modifier.height(6.dp))
                TimelineTraversalMapCard(
                    selectedMember = selectedMember,
                    visits = memberVisits,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- 2. Movement Trajectory & Record Graph (Today & Monthly) ---
            item(key = "graph_${selectedMember.id}") {
                val monthlyHistory = remember(selectedMember.id) {
                    FamilyDataSeed.getMonthlyMovementHistory(selectedMember.id)
                }
                TimelineGraphCard(
                    selectedMember = selectedMember,
                    visits = memberVisits,
                    monthlyRecords = monthlyHistory,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 3. Member Selector Row ---
        item(key = "member_selector") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEach { m ->
                    val isSelected = m.id == selectedMemberId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(m.colorHex) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable { onSelectMember(m.id) }
                            .testTag("timeline_filter_${m.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = m.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Live Movement Telemetry & Capture Card ---
        if (selectedMember != null) {
            item(key = "live_telemetry_${selectedMember.id}") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE TELEMETRY ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                            }

                            Text(
                                text = "${selectedMember.lastSeenTime12h} • ${selectedMember.batteryPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Current Position: ${selectedMember.currentPlace}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentCyan.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCaptureLiveGps() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "GPS",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Record Real GPS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSimulatePing() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = "Simulate",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Simulate Move",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.clickable { onViewOnMap() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Map Traversal",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Timeline Visits Feed ---
        itemsIndexed(memberVisits, key = { _, v -> v.id }) { index, visit ->
            TimelineVisitCard(
                visit = visit,
                index = index,
                isLast = index == memberVisits.size - 1,
                memberColor = selectedMember?.let { Color(it.colorHex) } ?: AccentCyan
            )
        }

        item(key = "timeline_bottom_space") {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun TimelineVisitCard(
    visit: TimelineVisit,
    index: Int = 0,
    isLast: Boolean,
    memberColor: Color
) {
    val categoryIcon = getCategoryIcon(visit.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_card_${visit.id}")
    ) {
        // Left timeline node & vertical track line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(memberColor.copy(alpha = 0.2f))
                    .border(2.dp, memberColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = visit.category,
                    tint = memberColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(68.dp)
                        .background(Color(0xFF334155))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Main visit detail card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = memberColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = memberColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = visit.placeName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (visit.isLiveTracking || visit.endTime12h.equals("Present", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentEmerald.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                            ) {
                                Text(
                                    text = "● LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = visit.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentCyan,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Time in 12-Hour AM/PM format (Rule 4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${visit.startTime12h} - ${visit.endTime12h}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${visit.durationText}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (visit.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = visit.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Synced",
                            tint = AccentEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Ground Truth Record",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentEmerald,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (visit.latitude != null && visit.longitude != null) {
                            Text(
                                text = "📍 ${"%.4f".format(visit.latitude)}, ${"%.4f".format(visit.longitude)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Text(
                            text = "24h: ${visit.startTime24h}-${visit.endTime24h}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "residence" -> Icons.Default.Home
        "school" -> Icons.Default.School
        "workplace" -> Icons.Default.Work
        "grocery store" -> Icons.Default.ShoppingCart
        "pharmacy" -> Icons.Default.LocalPharmacy
        "park" -> Icons.Default.Park
        "library" -> Icons.Default.MenuBook
        else -> Icons.Default.DirectionsWalk
    }
}
