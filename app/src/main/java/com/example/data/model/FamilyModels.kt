package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Family member profile
 */
data class FamilyMember(
    val id: String,
    val name: String,
    val relation: String,
    val colorHex: Long,
    val batteryPercent: Int,
    val isCharging: Boolean = false,
    val currentPlace: String,
    val currentLat: Double,
    val currentLng: Double,
    val lastSeenTime12h: String,
    val isOnline: Boolean = true,
    val isSharingLocation: Boolean = true,
    val email: String? = null,
    val phone: String? = null
)

/**
 * Family invitation contact method
 */
enum class InviteContactType {
    EMAIL,
    PHONE
}

/**
 * Status of family invitation / join request
 */
enum class InviteStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

/**
 * Family Invitation / Join Request
 */
data class FamilyInvite(
    val id: String,
    val senderId: String,
    val senderName: String,
    val recipientContact: String, // email or phone number
    val contactType: InviteContactType,
    val recipientName: String,
    val relation: String,
    val status: InviteStatus = InviteStatus.PENDING,
    val timestamp12h: String,
    val colorHex: Long = 0xFF8B5CF6,
    val inviteCode: String = "NEST-${(1000..9999).random()}"
)

/**
 * Geofence safe zone / designated place
 */
data class PlaceGeofence(
    val id: String,
    val name: String,
    val category: String, // "Residence", "School", "Workplace", "Grocery Store", "Pharmacy", "Park", "Library"
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double = 150.0,
    val iconType: String = "place"
)

/**
 * Historical and live location telemetry record (Room Entity)
 */
@Entity(tableName = "location_records")
data class LocationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val placeName: String,
    val category: String,
    val timestampEpochMillis: Long,
    val timestamp12h: String, // 12-hour AM/PM format
    val batteryPercent: Int = 85,
    val accuracyMeters: Float = 6.5f,
    val isSynced: Boolean = true // For TC04 offline batch upload
)

/**
 * Structured timeline visit item for daily movement summaries
 */
data class TimelineVisit(
    val id: String,
    val userId: String,
    val placeName: String,
    val category: String,
    val startTime12h: String, // e.g. "08:15 AM"
    val endTime12h: String,   // e.g. "03:00 PM"
    val startTime24h: String, // e.g. "08:15"
    val endTime24h: String,   // e.g. "15:00"
    val durationText: String,
    val notes: String = ""
)

/**
 * Chat message for the AI Movement Assistant
 */
data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp12h: String,
    val isCoolingDownAlert: Boolean = false,
    val isSecurityRefusal: Boolean = false,
    val queryTargetName: String? = null,
    val isGroundingVerified: Boolean = false
)

enum class MessageSender {
    USER,
    ASSISTANT
}
