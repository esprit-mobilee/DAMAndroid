package com.example.esprit.data.local.dao

import androidx.room.*
import com.example.esprit.data.local.entities.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements")
    suspend fun clearAnnouncements()
}
