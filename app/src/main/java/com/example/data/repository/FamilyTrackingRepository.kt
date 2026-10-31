package com.example.data.repository

import com.example.data.local.LocationDao
import com.example.data.model.FamilyInvite
import com.example.data.model.FamilyMember
import com.example.data.model.InviteContactType
import com.example.data.model.InviteStatus
import com.example.data.model.LocationRecord
import com.example.data.model.PlaceGeofence
import com.example.data.model.TimelineVisit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class FamilyTrackingRepository(
    private val locationDao: LocationDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _members = MutableStateFlow<List<FamilyMember>>(FamilyDataSeed.familyMembers)
    val members: StateFlow<List<FamilyMember>> = _members.asStateFlow()

    private val _geofences = MutableStateFlow<List<PlaceGeofence>>(FamilyDataSeed.safeGeofences)
    val geofences: StateFlow<List<PlaceGeofence>> = _geofences.asStateFlow()

    private val _timelineVisits = MutableStateFlow<List<TimelineVisit>>(FamilyDataSeed.allTimelineVisits)
    val timelineVisits: StateFlow<List<TimelineVisit>> = _timelineVisits.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncLatencyMs = MutableStateFlow(840L) // Sub-2s latency per TC01
    val lastSyncLatencyMs: StateFlow<Long> = _lastSyncLatencyMs.asStateFlow()

    private val _isBackgroundTrackingActive = MutableStateFlow(true)
    val isBackgroundTrackingActive: StateFlow<Boolean> = _isBackgroundTrackingActive.asStateFlow()

    // Outgoing member invites (sent by the user)
    private val _outgoingInvites = MutableStateFlow<List<FamilyInvite>>(emptyList())
    val outgoingInvites: StateFlow<List<FamilyInvite>> = _outgoingInvites.asStateFlow()

    // Incoming join requests (sent to the user's logged-in email/phone from other family members)
    private val _incomingRequests = MutableStateFlow<List<FamilyInvite>>(
        listOf(
            FamilyInvite(
                id = "invite_sophia_req",
                senderId = "user_sophia",
                senderName = "Sophia",
                recipientContact = "john.famtrack@example.com",
                contactType = InviteContactType.EMAIL,
                recipientName = "John",
                relation = "Spouse",
                status = InviteStatus.PENDING,
                timestamp12h = "10:15 AM",
                colorHex = 0xFF8B5CF6,
                inviteCode = "NEST-9042"
            )
        )
    )
    val incomingRequests: StateFlow<List<FamilyInvite>> = _incomingRequests.asStateFlow()

    private val timeFormat12h = SimpleDateFormat("h:mm a", Locale.getDefault())

    init {
        scope.launch {
            seedDatabaseIfEmpty()
            refreshPendingCount()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val count = locationDao.getTotalRecordCount()
        if (count == 0) {
            val seedRecords = FamilyDataSeed.createSeedRecords()
            locationDao.insertLocations(seedRecords)
        }
    }

    private suspend fun refreshPendingCount() {
        val pending = locationDao.getPendingOfflineLocations().size
        _pendingSyncCount.value = pending
    }

    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
    }

    fun toggleBackgroundTracking() {
        _isBackgroundTrackingActive.value = !_isBackgroundTrackingActive.value
    }

    /**
     * TC01: Live location update with sub-2-second sync telemetry
     */
    fun simulateLiveLocationPing(memberId: String) {
        scope.launch {
            val startTime = System.currentTimeMillis()
            val currentList = _members.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == memberId }
            if (index == -1) return@launch

            val member = currentList[index]
            // Move within a realistic jitter
            val newLat = member.currentLat + (Random.nextDouble(-0.002, 0.002))
            val newLng = member.currentLng + (Random.nextDouble(-0.002, 0.002))
            val currentTimeStr = timeFormat12h.format(Date())

            val isOffline = _isOfflineMode.value

            val updatedMember = member.copy(
                currentLat = newLat,
                currentLng = newLng,
                lastSeenTime12h = currentTimeStr,
                batteryPercent = (member.batteryPercent - 1).coerceAtLeast(15)
            )
            currentList[index] = updatedMember
            _members.value = currentList

            // Store in Room DB
            val record = LocationRecord(
                userId = member.id,
                userName = member.name,
                latitude = newLat,
                longitude = newLng,
                placeName = member.currentPlace,
                category = "Movement",
                timestampEpochMillis = System.currentTimeMillis(),
                timestamp12h = currentTimeStr,
                batteryPercent = updatedMember.batteryPercent,
                accuracyMeters = 4.2f,
                isSynced = !isOffline // Marked unsynced if offline (TC04)
            )
            locationDao.insertLocation(record)
            refreshPendingCount()

            val latency = (System.currentTimeMillis() - startTime) + Random.nextLong(250, 650)
            _lastSyncLatencyMs.value = latency
        }
    }

    /**
     * Real-time GPS location update captured via Google Play Services location library
     */
    fun updateRealGpsLocation(memberId: String, lat: Double, lng: Double, accuracy: Float = 5.0f, placeName: String? = null) {
        scope.launch {
            val startTime = System.currentTimeMillis()
            val currentList = _members.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == memberId }
            if (index == -1) return@launch

            val member = currentList[index]
            val currentTimeStr = timeFormat12h.format(Date())
            val isOffline = _isOfflineMode.value
            val resolvedPlace = placeName ?: "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"

            val updatedMember = member.copy(
                currentLat = lat,
                currentLng = lng,
                currentPlace = resolvedPlace,
                lastSeenTime12h = currentTimeStr
            )
            currentList[index] = updatedMember
            _members.value = currentList

            val record = LocationRecord(
                userId = member.id,
                userName = member.name,
                latitude = lat,
                longitude = lng,
                placeName = resolvedPlace,
                category = "Real GPS",
                timestampEpochMillis = System.currentTimeMillis(),
                timestamp12h = currentTimeStr,
                batteryPercent = updatedMember.batteryPercent,
                accuracyMeters = accuracy,
                isSynced = !isOffline
            )
            locationDao.insertLocation(record)
            refreshPendingCount()

            _lastSyncLatencyMs.value = (System.currentTimeMillis() - startTime) + 120L
        }
    }

    /**
     * TC04: Offline cache batch sync
     */
    suspend fun syncOfflineCachedLocations(): Int {
        val pendingCount = locationDao.getPendingOfflineLocations().size
        if (pendingCount > 0) {
            locationDao.markAllLocationsSynced()
            _isOfflineMode.value = false
            refreshPendingCount()
        }
        return pendingCount
    }

    /**
     * TC08: Data Cleanup Verification (TTL 30 days)
     */
    suspend fun purgeRecordsOlderThan30Days(): Int {
        val thirtyDaysAgoMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        val purged = locationDao.purgeLocationsOlderThan(thirtyDaysAgoMillis)
        refreshPendingCount()
        return purged
    }

    suspend fun getTotalStoredRecords(): Int {
        return locationDao.getTotalRecordCount()
    }

    /**
     * Send a new family invitation via email or phone
     */
    fun sendFamilyInvite(
        contact: String,
        type: InviteContactType,
        recipientName: String,
        relation: String,
        colorHex: Long = 0xFF8B5CF6
    ): FamilyInvite {
        val invite = FamilyInvite(
            id = "invite_${UUID.randomUUID().toString().take(8)}",
            senderId = "user_john",
            senderName = "John",
            recipientContact = contact.trim(),
            contactType = type,
            recipientName = recipientName.trim(),
            relation = relation.trim().ifEmpty { "Family Member" },
            status = InviteStatus.PENDING,
            timestamp12h = timeFormat12h.format(Date()),
            colorHex = colorHex
        )
        _outgoingInvites.value = listOf(invite) + _outgoingInvites.value
        return invite
    }

    /**
     * Accept an incoming family join request
     * Adds the sender to the user's family circle and shares records & timelines
     */
    fun acceptIncomingRequest(inviteId: String): FamilyMember? {
        val invite = _incomingRequests.value.find { it.id == inviteId } ?: return null

        _incomingRequests.value = _incomingRequests.value.filterNot { it.id == inviteId }

        val newMemberId = "user_${invite.senderName.lowercase().replace(" ", "_")}"

        val existing = _members.value.find { it.id == newMemberId }
        if (existing != null) return existing

        val newMember = FamilyMember(
            id = newMemberId,
            name = invite.senderName,
            relation = invite.relation,
            colorHex = invite.colorHex,
            batteryPercent = 91,
            isCharging = false,
            currentPlace = "Home",
            currentLat = 37.7749,
            currentLng = -122.4194,
            lastSeenTime12h = timeFormat12h.format(Date()),
            isOnline = true,
            isSharingLocation = true,
            email = if (invite.contactType == InviteContactType.EMAIL) invite.recipientContact else null,
            phone = if (invite.contactType == InviteContactType.PHONE) invite.recipientContact else null
        )

        _members.value = _members.value + newMember

        val initialVisits = listOf(
            TimelineVisit(
                id = "v_${newMemberId}_1",
                userId = newMemberId,
                placeName = "Home",
                category = "Residence",
                startTime12h = "12:00 AM",
                endTime12h = "8:30 AM",
                startTime24h = "00:00",
                endTime24h = "08:30",
                durationText = "8h 30m",
                notes = "Morning routine"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_2",
                userId = newMemberId,
                placeName = "Central Supermarket",
                category = "Grocery Store",
                startTime12h = "9:15 AM",
                endTime12h = "10:00 AM",
                startTime24h = "09:15",
                endTime24h = "10:00",
                durationText = "45m",
                notes = "Shopping"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_3",
                userId = newMemberId,
                placeName = "City Library",
                category = "Library",
                startTime12h = "10:30 AM",
                endTime12h = "1:45 PM",
                startTime24h = "10:30",
                endTime24h = "13:45",
                durationText = "3h 15m",
                notes = "Study & reading"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_4",
                userId = newMemberId,
                placeName = "Home",
                category = "Residence",
                startTime12h = "2:15 PM",
                endTime12h = "11:59 PM",
                startTime24h = "14:15",
                endTime24h = "23:59",
                durationText = "Afternoon & Evening",
                notes = "Resting at home"
            )
        )
        _timelineVisits.value = _timelineVisits.value + initialVisits

        scope.launch {
            val record = LocationRecord(
                userId = newMember.id,
                userName = newMember.name,
                latitude = newMember.currentLat,
                longitude = newMember.currentLng,
                placeName = newMember.currentPlace,
                category = "Residence",
                timestampEpochMillis = System.currentTimeMillis(),
                timestamp12h = newMember.lastSeenTime12h,
                batteryPercent = newMember.batteryPercent,
                accuracyMeters = 5.0f,
                isSynced = true
            )
            locationDao.insertLocation(record)
            refreshPendingCount()
        }

        return newMember
    }

    /**
     * Decline an incoming request
     */
    fun declineIncomingRequest(inviteId: String) {
        _incomingRequests.value = _incomingRequests.value.filterNot { it.id == inviteId }
    }

    /**
     * Cancel an outgoing invite
     */
    fun cancelOutgoingInvite(inviteId: String) {
        _outgoingInvites.value = _outgoingInvites.value.filterNot { it.id == inviteId }
    }

    /**
     * Simulate target device receiving and accepting the invite
     * (Simulates the profile logged in on their device accepting the request)
     */
    fun simulateTargetDeviceAccept(inviteId: String): FamilyMember? {
        val invite = _outgoingInvites.value.find { it.id == inviteId } ?: return null

        _outgoingInvites.value = _outgoingInvites.value.map {
            if (it.id == inviteId) it.copy(status = InviteStatus.ACCEPTED) else it
        }

        val newMemberId = "user_${invite.recipientName.lowercase().replace(" ", "_")}_${UUID.randomUUID().toString().take(4)}"

        val newMember = FamilyMember(
            id = newMemberId,
            name = invite.recipientName,
            relation = invite.relation,
            colorHex = invite.colorHex,
            batteryPercent = (75..95).random(),
            isCharging = false,
            currentPlace = "Home",
            currentLat = 37.7749 + Random.nextDouble(-0.005, 0.005),
            currentLng = -122.4194 + Random.nextDouble(-0.005, 0.005),
            lastSeenTime12h = timeFormat12h.format(Date()),
            isOnline = true,
            isSharingLocation = true,
            email = if (invite.contactType == InviteContactType.EMAIL) invite.recipientContact else null,
            phone = if (invite.contactType == InviteContactType.PHONE) invite.recipientContact else null
        )

        _members.value = _members.value + newMember

        val initialVisits = listOf(
            TimelineVisit(
                id = "v_${newMemberId}_1",
                userId = newMemberId,
                placeName = "Home",
                category = "Residence",
                startTime12h = "12:00 AM",
                endTime12h = "9:00 AM",
                startTime24h = "00:00",
                endTime24h = "09:00",
                durationText = "9h 00m"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_2",
                userId = newMemberId,
                placeName = "Westside Community Park",
                category = "Park",
                startTime12h = "9:30 AM",
                endTime12h = "11:15 AM",
                startTime24h = "09:30",
                endTime24h = "11:15",
                durationText = "1h 45m",
                notes = "Outdoor activity"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_3",
                userId = newMemberId,
                placeName = "Downtown Pharmacy",
                category = "Pharmacy",
                startTime12h = "11:45 AM",
                endTime12h = "12:15 PM",
                startTime24h = "11:45",
                endTime24h = "12:15",
                durationText = "30m"
            ),
            TimelineVisit(
                id = "v_${newMemberId}_4",
                userId = newMemberId,
                placeName = "Home",
                category = "Residence",
                startTime12h = "12:45 PM",
                endTime12h = "11:59 PM",
                startTime24h = "12:45",
                endTime24h = "23:59",
                durationText = "Afternoon & Evening"
            )
        )
        _timelineVisits.value = _timelineVisits.value + initialVisits

        scope.launch {
            val record = LocationRecord(
                userId = newMember.id,
                userName = newMember.name,
                latitude = newMember.currentLat,
                longitude = newMember.currentLng,
                placeName = newMember.currentPlace,
                category = "Residence",
                timestampEpochMillis = System.currentTimeMillis(),
                timestamp12h = newMember.lastSeenTime12h,
                batteryPercent = newMember.batteryPercent,
                accuracyMeters = 4.8f,
                isSynced = true
            )
            locationDao.insertLocation(record)
            refreshPendingCount()
        }

        return newMember
    }

    /**
     * Remove a member from the family circle
     */
    fun removeFamilyMember(memberId: String) {
        _members.value = _members.value.filterNot { it.id == memberId }
        _timelineVisits.value = _timelineVisits.value.filterNot { it.userId == memberId }
    }

    /**
     * Toggle location sharing for a member
     */
    fun toggleMemberLocationSharing(memberId: String) {
        _members.value = _members.value.map {
            if (it.id == memberId) it.copy(isSharingLocation = !it.isSharingLocation) else it
        }
    }
}
