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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.ui.FamilyTrackerViewModel
import com.example.ui.components.AuthScreen
import com.example.ui.components.FamilySafeZonesView
import com.example.ui.components.LiveVoiceConversationDialog
import com.example.ui.components.NearNestAccountInfoView
import com.example.ui.components.NearNestAiChatView
import com.example.ui.components.NearNestBottomNav
import com.example.ui.components.NearNestDailyTraversalView
import com.example.ui.components.NearNestMapView
import com.example.ui.components.NearNestNotificationBanner
import com.example.ui.components.NearNestNotificationsView
import com.example.ui.components.NearNestSettingsView
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.MyApplicationTheme

enum class MainSubScreen {
    NONE,
    ACCOUNT_INFO,
    NOTIFICATIONS,
    FAMILY_NEST_MANAGEMENT
}

class MainActivity : ComponentActivity() {

    private val viewModel: FamilyTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val authState by viewModel.authState.collectAsState()
                if (authState != AuthState.AUTHENTICATED) {
                    AuthScreen(viewModel = viewModel)
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: FamilyTrackerViewModel) {
    val currentUserSession by viewModel.currentUserSession.collectAsState()
    val currentNest by viewModel.currentNest.collectAsState()
    var currentSubScreen by remember { mutableStateOf(MainSubScreen.NONE) }

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val members by viewModel.familyMembers.collectAsState()
    val geofences by viewModel.geofences.collectAsState()
    val visits by viewModel.timelineVisits.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val isBackgroundTrackingActive by viewModel.isBackgroundTrackingActive.collectAsState()
    val outgoingInvites by viewModel.outgoingInvites.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    val isLiveVoiceActive by viewModel.isLiveVoiceActive.collectAsState()
    val liveVoiceLatestReply by viewModel.liveVoiceLatestReply.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.captureRealGpsLocation()
            viewModel.startContinuousLiveTracking()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.locationServiceManager.hasLocationPermission()) {
            viewModel.startContinuousLiveTracking()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentSubScreen) {
            MainSubScreen.ACCOUNT_INFO -> {
                NearNestAccountInfoView(
                    currentUserSession = currentUserSession,
                    onSaveProfile = { firstName, lastName, email, phone ->
                        viewModel.updateCurrentUserProfile("$firstName $lastName", email, phone)
                        currentSubScreen = MainSubScreen.NONE
                    },
                    onNavigateBack = { currentSubScreen = MainSubScreen.NONE }
                )
            }

            MainSubScreen.NOTIFICATIONS -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    NearNestNotificationsView(
                        onNavigateBack = { currentSubScreen = MainSubScreen.NONE }
                    )
                    NearNestBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            currentSubScreen = MainSubScreen.NONE
                            viewModel.setSelectedTab(tab)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            MainSubScreen.FAMILY_NEST_MANAGEMENT -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar for Returning to Settings
                    Surface(
                        color = BrandBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentSubScreen = MainSubScreen.NONE }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Nest & Safe Zones",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    FamilySafeZonesView(
                        members = members,
                        geofences = geofences,
                        isOfflineMode = isOfflineMode,
                        pendingSyncCount = pendingSyncCount,
                        isBackgroundTrackingActive = isBackgroundTrackingActive,
                        outgoingInvites = outgoingInvites,
                        incomingRequests = incomingRequests,
                        currentUserSession = currentUserSession,
                        currentNest = currentNest,
                        onCreateNest = { viewModel.createNewNest(it) },
                        onJoinNest = { no, pass -> viewModel.joinNest(no, pass) },
                        onSignOut = { viewModel.signOut() },
                        onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                        onSyncOfflineCache = { viewModel.syncOfflineLocations() },
                        onToggleBackgroundTracking = { viewModel.toggleBackgroundTracking() },
                        onPurgeTtlRecords = { viewModel.purgeTtlOldRecords() },
                        onSendInvite = { contact, type, name, relation, colorHex ->
                            viewModel.sendFamilyInvite(contact, type, name, relation, colorHex)
                        },
                        onAcceptIncomingRequest = { viewModel.acceptIncomingRequest(it) },
                        onDeclineIncomingRequest = { viewModel.declineIncomingRequest(it) },
                        onSimulateTargetDeviceAccept = { viewModel.simulateTargetDeviceAcceptance(it) },
                        onCancelOutgoingInvite = { viewModel.cancelOutgoingInvite(it) },
                        onRemoveMember = { viewModel.removeFamilyMember(it) },
                        onToggleSharing = { viewModel.toggleMemberLocationSharing(it) }
                    )
                }
            }

            MainSubScreen.NONE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> NearNestMapView(
                            currentUserSession = currentUserSession,
                            members = members,
                            geofences = geofences,
                            timelineVisits = visits,
                            onOpenProfile = { currentSubScreen = MainSubScreen.ACCOUNT_INFO },
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
                            onSimulatePing = { viewModel.triggerLiveLocationPing() }
                        )

                        1 -> NearNestAiChatView(
                            currentUserSession = currentUserSession,
                            messages = chatMessages,
                            isAiThinking = isAiThinking,
                            onSendMessage = { viewModel.sendUserMessage(it) },
                            onStartVoiceRecording = {
                                if (viewModel.audioRecordingManager.hasRecordPermission()) {
                                    viewModel.startVoiceRecording()
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onNavigateBack = { viewModel.setSelectedTab(0) },
                            onEditProfile = { currentSubScreen = MainSubScreen.ACCOUNT_INFO }
                        )

                        2 -> NearNestDailyTraversalView(
                            currentUserSession = currentUserSession,
                            members = members,
                            visits = visits,
                            selectedMemberId = selectedMemberId,
                            onSelectMember = { viewModel.setSelectedMember(it) },
                            onNavigateBack = { viewModel.setSelectedTab(0) },
                            onOpenProfile = { currentSubScreen = MainSubScreen.ACCOUNT_INFO }
                        )

                        3 -> NearNestSettingsView(
                            currentUserSession = currentUserSession,
                            currentNest = currentNest,
                            onOpenAccountInfo = { currentSubScreen = MainSubScreen.ACCOUNT_INFO },
                            onOpenNotifications = { currentSubScreen = MainSubScreen.NOTIFICATIONS },
                            onOpenNestManagement = { currentSubScreen = MainSubScreen.FAMILY_NEST_MANAGEMENT },
                            onToggleLocation = {
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
                            onSignOut = { viewModel.signOut() },
                            onNavigateBack = { viewModel.setSelectedTab(0) }
                        )
                    }

                    // Floating Bottom Navigation Bar (replicated from design)
                    NearNestBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            viewModel.setSelectedTab(tab)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Top Status / Telemetry Banner (Strict NearNest Design System)
        NearNestNotificationBanner(
            message = statusBanner,
            onDismiss = { viewModel.dismissBanner() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

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
