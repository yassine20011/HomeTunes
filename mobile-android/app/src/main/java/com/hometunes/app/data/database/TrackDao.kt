package com.hometunes.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY createdAt DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksSync(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE youtubeId = :youtubeId")
    suspend fun getTrackByYoutubeId(youtubeId: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
}
