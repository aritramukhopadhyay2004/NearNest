package com.example.data.repository

import com.example.data.model.FamilyMember
import com.example.data.model.LocationRecord
import com.example.data.model.PlaceGeofence
import com.example.data.model.TimelineVisit
import org.json.JSONArray
import org.json.JSONObject

object FamilyDataSeed {

    val safeGeofences = listOf(
        PlaceGeofence("geo_home", "Home", "Residence", 37.7749, -122.4194, 100.0, "home"),
        PlaceGeofence("geo_techpark", "Tech Park Office", "Workplace", 37.7891, -122.4014, 180.0, "work"),
        PlaceGeofence("geo_school", "Oakridge High School", "School", 37.7612, -122.4350, 200.0, "school"),
        PlaceGeofence("geo_supermarket", "Central Supermarket", "Grocery Store", 37.7800, -122.4210, 120.0, "cart"),
        PlaceGeofence("geo_pharmacy", "Downtown Pharmacy", "Pharmacy", 37.7715, -122.4110, 80.0, "local_pharmacy"),
        PlaceGeofence("geo_park", "Westside Community Park", "Park", 37.7650, -122.4450, 250.0, "park"),
        PlaceGeofence("geo_library", "City Library", "Library", 37.7780, -122.4170, 100.0, "menu_book"),
        PlaceGeofence("geo_senior", "Sunrise Senior Center", "Community Center", 37.7820, -122.4300, 120.0, "diversity_3")
    )

    val familyMembers = listOf(
        FamilyMember(
            id = "user_john",
            name = "John",
            relation = "Father",
            colorHex = 0xFF0284C7, // Vibrant Sky Blue
            batteryPercent = 78,
            currentPlace = "Home",
            currentLat = 37.7749,
            currentLng = -122.4194,
            lastSeenTime12h = "6:25 PM"
        ),
        FamilyMember(
            id = "user_sarah",
            name = "Sarah",
            relation = "Mother",
            colorHex = 0xFFD946EF, // Fuchsia Pink
            batteryPercent = 88,
            currentPlace = "Home",
            currentLat = 37.7749,
            currentLng = -122.4194,
            lastSeenTime12h = "12:45 PM"
        ),
        FamilyMember(
            id = "user_emma",
            name = "Emma",
            relation = "Daughter",
            colorHex = 0xFF059669, // Emerald Green
            batteryPercent = 64,
            currentPlace = "Home",
            currentLat = 37.7749,
            currentLng = -122.4194,
            lastSeenTime12h = "7:20 PM"
        ),
        FamilyMember(
            id = "user_michael",
            name = "Michael",
            relation = "Grandfather",
            colorHex = 0xFFD97706, // Amber Gold
            batteryPercent = 92,
            currentPlace = "Home",
            currentLat = 37.7749,
            currentLng = -122.4194,
            lastSeenTime12h = "1:30 PM"
        )
    )

    val allTimelineVisits = listOf(
        // John's visits today
        TimelineVisit(
            id = "v_j1", userId = "user_john", placeName = "Home", category = "Residence",
            startTime12h = "12:00 AM", endTime12h = "8:15 AM",
            startTime24h = "00:00", endTime24h = "08:15",
            durationText = "8h 15m", notes = "Departed for office commute"
        ),
        TimelineVisit(
            id = "v_j2", userId = "user_john", placeName = "Metro Station", category = "Transit",
            startTime12h = "8:30 AM", endTime12h = "8:50 AM",
            startTime24h = "08:30", endTime24h = "08:50",
            durationText = "20m", notes = "Subway transfer"
        ),
        TimelineVisit(
            id = "v_j3", userId = "user_john", placeName = "Tech Park Office", category = "Workplace",
            startTime12h = "9:05 AM", endTime12h = "12:15 PM",
            startTime24h = "09:05", endTime24h = "12:15",
            durationText = "3h 10m", notes = "Morning work"
        ),
        TimelineVisit(
            id = "v_j4", userId = "user_john", placeName = "Greenwood Cafe", category = "Restaurant",
            startTime12h = "12:20 PM", endTime12h = "1:00 PM",
            startTime24h = "12:20", endTime24h = "13:00",
            durationText = "40m", notes = "Lunch"
        ),
        TimelineVisit(
            id = "v_j5", userId = "user_john", placeName = "Tech Park Office", category = "Workplace",
            startTime12h = "1:05 PM", endTime12h = "5:10 PM",
            startTime24h = "13:05", endTime24h = "17:10",
            durationText = "4h 05m", notes = "Afternoon work"
        ),
        TimelineVisit(
            id = "v_j6", userId = "user_john", placeName = "Central Supermarket", category = "Grocery Store",
            startTime12h = "5:35 PM", endTime12h = "6:05 PM",
            startTime24h = "17:35", endTime24h = "18:05",
            durationText = "30m", notes = "Central Supermarket"
        ),
        TimelineVisit(
            id = "v_j7", userId = "user_john", placeName = "Home", category = "Residence",
            startTime12h = "6:25 PM", endTime12h = "11:59 PM",
            startTime24h = "18:25", endTime24h = "23:59",
            durationText = "Evening", notes = "Arrived home"
        ),

        // Sarah's visits today
        TimelineVisit(
            id = "v_s1", userId = "user_sarah", placeName = "Home", category = "Residence",
            startTime12h = "12:00 AM", endTime12h = "9:30 AM",
            startTime24h = "00:00", endTime24h = "09:30",
            durationText = "9h 30m"
        ),
        TimelineVisit(
            id = "v_s2", userId = "user_sarah", placeName = "Downtown Pharmacy", category = "Pharmacy",
            startTime12h = "10:00 AM", endTime12h = "10:30 AM",
            startTime24h = "10:00", endTime24h = "10:30",
            durationText = "30m", notes = "Downtown Pharmacy visit"
        ),
        TimelineVisit(
            id = "v_s3", userId = "user_sarah", placeName = "Community Health Center", category = "Medical",
            startTime12h = "10:45 AM", endTime12h = "12:15 PM",
            startTime24h = "10:45", endTime24h = "12:15",
            durationText = "1h 30m"
        ),
        TimelineVisit(
            id = "v_s4", userId = "user_sarah", placeName = "Home", category = "Residence",
            startTime12h = "12:45 PM", endTime12h = "11:59 PM",
            startTime24h = "12:45", endTime24h = "23:59",
            durationText = "Afternoon & Evening"
        ),

        // Emma's visits today
        TimelineVisit(
            id = "v_e1", userId = "user_emma", placeName = "Home", category = "Residence",
            startTime12h = "12:00 AM", endTime12h = "7:45 AM",
            startTime24h = "00:00", endTime24h = "07:45",
            durationText = "7h 45m"
        ),
        TimelineVisit(
            id = "v_e2", userId = "user_emma", placeName = "Oakridge High School", category = "School",
            startTime12h = "8:15 AM", endTime12h = "3:00 PM",
            startTime24h = "08:15", endTime24h = "15:00",
            durationText = "6h 45m", notes = "School classes"
        ),
        TimelineVisit(
            id = "v_e3", userId = "user_emma", placeName = "Westside Community Park", category = "Park",
            startTime12h = "3:20 PM", endTime12h = "5:30 PM",
            startTime24h = "15:20", endTime24h = "17:30",
            durationText = "2h 10m", notes = "Track practice"
        ),
        TimelineVisit(
            id = "v_e4", userId = "user_emma", placeName = "City Library", category = "Library",
            startTime12h = "5:45 PM", endTime12h = "7:00 PM",
            startTime24h = "17:45", endTime24h = "19:00",
            durationText = "1h 15m", notes = "Study hall"
        ),
        TimelineVisit(
            id = "v_e5", userId = "user_emma", placeName = "Home", category = "Residence",
            startTime12h = "7:20 PM", endTime12h = "11:59 PM",
            startTime24h = "19:20", endTime24h = "23:59",
            durationText = "Evening", notes = "Home for dinner"
        ),

        // Michael's visits today
        TimelineVisit(
            id = "v_m1", userId = "user_michael", placeName = "Home", category = "Residence",
            startTime12h = "12:00 AM", endTime12h = "7:00 AM",
            startTime24h = "00:00", endTime24h = "07:00",
            durationText = "7h 00m"
        ),
        TimelineVisit(
            id = "v_m2", userId = "user_michael", placeName = "Sunrise Senior Center", category = "Community Center",
            startTime12h = "7:30 AM", endTime12h = "11:00 AM",
            startTime24h = "07:30", endTime24h = "11:00",
            durationText = "3h 30m"
        ),
        TimelineVisit(
            id = "v_m3", userId = "user_michael", placeName = "Westside Community Park", category = "Park",
            startTime12h = "11:20 AM", endTime12h = "1:00 PM",
            startTime24h = "11:20", endTime24h = "13:00",
            durationText = "1h 40m", notes = "Walk in park"
        ),
        TimelineVisit(
            id = "v_m4", userId = "user_michael", placeName = "Home", category = "Residence",
            startTime12h = "1:30 PM", endTime12h = "11:59 PM",
            startTime24h = "13:30", endTime24h = "23:59",
            durationText = "Afternoon & Evening"
        )
    )

    /**
     * Builds the JSON location history string strictly according to the format required by the AI Assistant prompt
     */
    fun buildLocationHistoryJson(): String {
        val root = JSONObject()
        val membersArray = JSONArray()

        for (member in familyMembers) {
            val memberObj = JSONObject()
            memberObj.put("id", member.id)
            memberObj.put("name", member.name)
            memberObj.put("relation", member.relation)
            memberObj.put("current_place", member.currentPlace)
            memberObj.put("battery_percent", member.batteryPercent)
            memberObj.put("last_seen", member.lastSeenTime12h)

            val timelineArray = JSONArray()
            val visits = allTimelineVisits.filter { it.userId == member.id }
            for (v in visits) {
                val visitObj = JSONObject()
                visitObj.put("place", v.placeName)
                visitObj.put("category", v.category)
                visitObj.put("start_time_24h", v.startTime24h)
                visitObj.put("end_time_24h", v.endTime24h)
                visitObj.put("start_time_12h", v.startTime12h)
                visitObj.put("end_time_12h", v.endTime12h)
                visitObj.put("duration", v.durationText)
                if (v.notes.isNotEmpty()) {
                    visitObj.put("notes", v.notes)
                }
                timelineArray.put(visitObj)
            }
            memberObj.put("timeline", timelineArray)
            membersArray.put(memberObj)
        }

        root.put("family_members", membersArray)
        root.put("time_zone", "Local (User Time Zone)")
        root.put("retention_policy", "30-Day Rolling TTL")
        return root.toString(2)
    }

    /**
     * Generate sample initial LocationRecords for Room database seeding
     */
    fun createSeedRecords(baseTimeMillis: Long = System.currentTimeMillis()): List<LocationRecord> {
        val records = mutableListOf<LocationRecord>()
        val oneHourMillis = 3_600_000L

        // Generate location points for today
        for (visit in allTimelineVisits) {
            val member = familyMembers.first { it.id == visit.userId }
            val geofence = safeGeofences.find { it.name.equals(visit.placeName, ignoreCase = true) }
            val lat = geofence?.latitude ?: (37.7749 + (records.size % 5) * 0.005)
            val lng = geofence?.longitude ?: (-122.4194 + (records.size % 5) * 0.005)

            records.add(
                LocationRecord(
                    userId = visit.userId,
                    userName = member.name,
                    latitude = lat,
                    longitude = lng,
                    placeName = visit.placeName,
                    category = visit.category,
                    timestampEpochMillis = baseTimeMillis - (records.size * oneHourMillis),
                    timestamp12h = visit.startTime12h,
                    batteryPercent = member.batteryPercent,
                    accuracyMeters = 5.0f,
                    isSynced = true
                )
            )
        }
        return records
    }
}
