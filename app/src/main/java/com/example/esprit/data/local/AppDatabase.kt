package com.example.esprit.data.local


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.esprit.data.local.dao.MessageDao
import com.example.esprit.data.local.dao.AnnouncementDao
import com.example.esprit.data.local.entities.MessageEntity
import com.example.esprit.data.local.entities.AnnouncementEntity

@Database(
    entities = [
        MessageEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    abstract fun announcementDao(): AnnouncementDao
}
