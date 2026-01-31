package com.hometunes.app.data.repository

import com.hometunes.app.data.database.TrackDao
import com.hometunes.app.data.database.TrackEntity
import com.hometunes.app.data.source.LocalMediaSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val localMediaSource: LocalMediaSource
) {
    val allTracks: Flow<List<TrackEntity>> = trackDao.getAllTracks()

    suspend fun getTrackById(id: String): TrackEntity? = trackDao.getTrackById(id)

    suspend fun getTrackByYoutubeId(youtubeId: String): TrackEntity? = 
        trackDao.getTrackByYoutubeId(youtubeId)

    suspend fun insertTrack(track: TrackEntity) = trackDao.insertTrack(track)

    suspend fun deleteTrack(track: TrackEntity) = trackDao.deleteTrack(track)

    suspend fun deleteTrackById(id: String) = trackDao.deleteTrackById(id)

    suspend fun isDuplicate(youtubeId: String): Boolean = 
        trackDao.getTrackByYoutubeId(youtubeId) != null
        
    suspend fun importLocalTracks(): Int {
        val localFiles = localMediaSource.getLocalAudioFiles()
        var importedCount = 0
        
        // This is a simple duplication check. 
        // In reality we might want to check against existing paths in DB.
        val existingTracks = trackDao.getAllTracksSync() // We need a sync version or collect
        val existingPaths = existingTracks.map { it.filePath }.toSet()

        localFiles.forEach { localTrack ->
            if (!existingPaths.contains(localTrack.path)) {
                val entity = TrackEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    youtubeId = null,
                    title = localTrack.title,
                    artist = localTrack.artist,
                    duration = localTrack.duration,
                    filePath = localTrack.path,
                    thumbnailPath = null, // Could extract album art later
                    fileSize = localTrack.size,
                    isLocal = true
                )
                trackDao.insertTrack(entity)
                importedCount++
            }
        }
        return importedCount
    }
}
