package com.hometunes.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
