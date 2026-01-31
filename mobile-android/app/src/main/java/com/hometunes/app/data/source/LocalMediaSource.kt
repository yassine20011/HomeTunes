package com.hometunes.app.data.source

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LocalTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Int, // in milliseconds
    val path: String,
    val size: Long
)

class LocalMediaSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getLocalAudioFiles(): List<LocalTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<LocalTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val duration = cursor.getInt(durationColumn)
                val path = cursor.getString(dataColumn)
                val size = cursor.getLong(sizeColumn)

                tracks.add(
                    LocalTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        path = path,
                        size = size
                    )
                )
            }
        }
        return@withContext tracks
    }
}
