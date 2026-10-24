package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LocationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(record: LocationRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(records: List<LocationRecord>)

    @Query("SELECT * FROM location_records ORDER BY timestampEpochMillis DESC")
    fun getAllLocationsFlow(): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE userId = :userId ORDER BY timestampEpochMillis DESC")
    fun getLocationsForUserFlow(userId: String): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE userId = :userId ORDER BY timestampEpochMillis ASC")
    suspend fun getLocationsForUser(userId: String): List<LocationRecord>

    @Query("SELECT * FROM location_records WHERE isSynced = 0")
    suspend fun getPendingOfflineLocations(): List<LocationRecord>

    @Query("UPDATE location_records SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllLocationsSynced(): Int

    @Query("DELETE FROM location_records WHERE timestampEpochMillis < :cutoffEpochMillis")
    suspend fun purgeLocationsOlderThan(cutoffEpochMillis: Long): Int

    @Query("SELECT COUNT(*) FROM location_records")
    suspend fun getTotalRecordCount(): Int

    @Query("SELECT COUNT(*) FROM location_records WHERE isSynced = 0")
    fun getPendingCountFlow(): Flow<Int>
}
