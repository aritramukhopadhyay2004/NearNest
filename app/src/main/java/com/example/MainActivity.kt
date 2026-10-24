package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FamilyTrackerViewModel
import com.example.ui.components.AssistantChatView
import com.example.ui.components.FamilySafeZonesView
import com.example.ui.components.InteractiveMapView
import com.example.ui.components.LiveVoiceConversationDialog
import com.example.ui.components.TimelineView
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark

class MainActivity : ComponentActivity() {

    private val viewModel: FamilyTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: FamilyTrackerViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val members by viewModel.familyMembers.collectAsState()
    val geofences by viewModel.geofences.collectAsState()
    val visits by viewModel.timelineVisits.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val lastSyncLatencyMs by viewModel.lastSyncLatencyMs.collectAsState()
    val isBackgroundTrackingActive by viewModel.isBackgroundTrackingActive.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isRawJsonVisible by viewModel.isRawJsonVisible.collectAsState()
    val rawJsonContext by viewModel.rawJsonContext.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    val selectedModelType by viewModel.selectedModelType.collectAsState()
    val isMapsGroundingEnabled by viewModel.isMapsGroundingEnabled.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val isLiveVoiceActive by viewModel.isLiveVoiceActive.collectAsState()
    val liveVoiceLatestReply by viewModel.liveVoiceLatestReply.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.captureRealGpsLocation()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        topBar = {
            Surface(
                color = SurfaceCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Logo",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FamTrack AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "AI Movement Assistant",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Mode indicator badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Text(
                            text = if (isOfflineMode) "Offline Cached" else "Live Radar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isOfflineMode) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCardDark,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                val navItems = listOf(
                    Triple(0, "Live Map", Icons.Default.Map),
                    Triple(1, "AI Assistant", Icons.Default.AutoAwesome),
                    Triple(2, "Timeline", Icons.Default.Timeline),
                    Triple(3, "Family & Zones", Icons.Default.People)
                )

                navItems.forEach { (tabIndex, title, icon) ->
                    val isSelected = selectedTab == tabIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(tabIndex) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363A),
                            selectedTextColor = AccentCyan,
                            indicatorColor = AccentCyan,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_$tabIndex")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> InteractiveMapView(
                    members = members,
                    geofences = geofences,
                    timelineVisits = visits,
                    selectedMemberId = selectedMemberId,
                    isOfflineMode = isOfflineMode,
                    syncLatencyMs = lastSyncLatencyMs,
                    onSelectMember = { viewModel.setSelectedMember(it) },
                    onSimulatePing = { viewModel.triggerLiveLocationPing() },
                    onCaptureRealGps = {
                        if (viewModel.locationServiceManager.hasLocationPermission()) {
                            viewModel.captureRealGpsLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    onToggleOffline = { viewModel.toggleOfflineMode() }
                )

                1 -> AssistantChatView(
                    messages = chatMessages,
                    isAiThinking = isAiThinking,
                    isRawJsonVisible = isRawJsonVisible,
                    rawJsonContext = rawJsonContext,
                    selectedModelType = selectedModelType,
                    isMapsGroundingEnabled = isMapsGroundingEnabled,
                    isRecordingVoice = isRecordingVoice,
                    onSelectModel = { viewModel.setModelType(it) },
                    onToggleMapsGrounding = { viewModel.toggleMapsGrounding() },
                    onStartVoiceRecording = {
                        if (viewModel.audioRecordingManager.hasRecordPermission()) {
                            viewModel.startVoiceRecording()
                        } else {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopVoiceRecording = { viewModel.stopVoiceRecordingAndTranscribe() },
                    onLaunchLiveVoice = { viewModel.toggleLiveVoiceMode() },
                    onClearHistory = { viewModel.clearChatHistory() },
                    onSendMessage = { viewModel.sendUserMessage(it) },
                    onTriggerPromptInjectionTest = { viewModel.triggerPromptInjectionTest() },
                    onTriggerRateLimitTest = { viewModel.triggerRateLimitTest() },
                    onToggleRawJson = { viewModel.toggleRawJsonVisibility() }
                )

                2 -> TimelineView(
                    members = members,
                    visits = visits,
                    selectedMemberId = selectedMemberId,
                    onSelectMember = { viewModel.setSelectedMember(it) }
                )

                3 -> FamilySafeZonesView(
                    members = members,
                    geofences = geofences,
                    isOfflineMode = isOfflineMode,
                    pendingSyncCount = pendingSyncCount,
                    isBackgroundTrackingActive = isBackgroundTrackingActive,
                    onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                    onSyncOfflineCache = { viewModel.syncOfflineLocations() },
                    onToggleBackgroundTracking = { viewModel.toggleBackgroundTracking() },
                    onPurgeTtlRecords = { viewModel.purgeTtlOldRecords() }
                )
            }

            // Top dismissible telemetry notification banner
            AnimatedVisibility(
                visible = statusBanner != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                statusBanner?.let { message ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alert",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Live Voice Conversation Dialog (gemini-3.8-live)
            if (isLiveVoiceActive) {
                LiveVoiceConversationDialog(
                    latestReply = liveVoiceLatestReply,
                    onDismiss = { viewModel.toggleLiveVoiceMode() },
                    onSpeakPrompt = { viewModel.sendLiveVoiceTurn(it) }
                )
            }
        }
    }
}
