package com.hometunes.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val youtubeId: String?,
    val title: String,
    val artist: String?,
    val duration: Int,
    val filePath: String,
    val thumbnailPath: String?,
    val fileSize: Long,
    val isLocal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
