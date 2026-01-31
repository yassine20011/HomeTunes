package com.hometunes.app.data.api

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.hometunes.app.data.database.TrackEntity
import com.hometunes.app.data.repository.SettingsRepository
import com.hometunes.app.data.repository.TrackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class DownloadResult {
    data class Success(val track: TrackEntity) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
    data class AlreadyExists(val message: String) : DownloadResult()
}

@Singleton
class DownloadRepository
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
        private val trackRepository: TrackRepository,
        private val gson: Gson
) {
    // Default to public Music/HomeTunes directory
    private fun getDefaultMusicDir(): File {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return File(musicDir, "HomeTunes").also { it.mkdirs() }
    }

    // Thumbnails always go to internal storage (not user-visible)
    private val thumbnailDir: File by lazy {
        File(context.filesDir, "thumbnails").also { it.mkdirs() }
    }

    private fun saveThumbnail(metadata: TrackMetadata): String? {
        if (metadata.thumbnailBase64.isNullOrBlank()) return null
        return try {
            val thumbnailFile = File(thumbnailDir, "${metadata.youtubeId}.jpg")
            // Strip potential "data:image/jpeg;base64," prefix
            val cleanBase64 = metadata.thumbnailBase64.substringAfter("base64,")
            thumbnailFile.writeBytes(Base64.decode(cleanBase64, Base64.DEFAULT))
            thumbnailFile.absolutePath
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Failed to decode thumbnail base64", e)
            null
        }
    }

    private fun createApiClient(baseUrl: String): HomeTunesApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val client =
                OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .build()

        return Retrofit.Builder()
                .baseUrl(baseUrl.trimEnd('/') + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HomeTunesApi::class.java)
    }

    suspend fun checkServerHealth(): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val serverUrl = settingsRepository.serverUrl.first()
                    if (serverUrl.isBlank()) return@withContext false

                    val api = createApiClient(serverUrl)
                    val response = api.checkHealth()
                    response.isSuccessful && response.body()?.status == "ok"
                } catch (e: Exception) {
                    false
                }
            }

    suspend fun downloadTrack(
            youtubeUrl: String,
            onProgress: (Float) -> Unit = {}
    ): DownloadResult =
            withContext(Dispatchers.IO) {
                try {
                    val serverUrl = settingsRepository.serverUrl.first()
                    if (serverUrl.isBlank()) {
                        return@withContext DownloadResult.Error("Server URL not configured")
                    }

                    val quality = settingsRepository.audioQuality.first()
                    val api = createApiClient(serverUrl)

                    onProgress(0.1f)

                    val response = api.downloadTrack(DownloadRequest(youtubeUrl, quality))
                    if (!response.isSuccessful) {
                        return@withContext DownloadResult.Error("Server error: ${response.code()}")
                    }

                    onProgress(0.4f)

                    val body =
                            response.body()
                                    ?: return@withContext DownloadResult.Error("Empty response")
                    val source = body.source()

                    // Read first line as JSON metadata
                    val metadataLine =
                            source.readUtf8Line()
                                    ?: return@withContext DownloadResult.Error(
                                            "Invalid response format"
                                    )
                    val metadata = gson.fromJson(metadataLine, TrackMetadata::class.java)

                    // Check for duplicate
                    if (trackRepository.isDuplicate(metadata.youtubeId)) {
                        return@withContext DownloadResult.AlreadyExists("Track already in library")
                    }

                    onProgress(0.6f)

                    // Get music directory - use user setting or default Music/HomeTunes
                    val musicDirPath = settingsRepository.musicDirectory.first()
                    val musicDir =
                            if (musicDirPath.isNotBlank()) {
                                File(musicDirPath).also { it.mkdirs() }
                            } else {
                                getDefaultMusicDir()
                            }

                    // Save audio file to music directory (M4A format for best quality)
                    val audioFile = File(musicDir, "${metadata.youtubeId}.m4a")
                    audioFile.outputStream().use { output -> source.inputStream().copyTo(output) }

                    // Notify MediaStore so other music apps can find the file
                    MediaScannerConnection.scanFile(
                            context,
                            arrayOf(audioFile.absolutePath),
                            arrayOf("audio/mp4"), // M4A MIME type
                            null
                    )

                    onProgress(0.9f)

                    // Save thumbnail if present
                    val thumbnailPath = saveThumbnail(metadata)

                    // Create track entity
                    val track =
                            TrackEntity(
                                    id = UUID.randomUUID().toString(),
                                    youtubeId = metadata.youtubeId,
                                    title = metadata.title,
                                    artist = metadata.artist,
                                    duration = metadata.duration,
                                    filePath = audioFile.absolutePath,
                                    thumbnailPath = thumbnailPath,
                                    fileSize = metadata.fileSize
                            )

                    // Save to database
                    trackRepository.insertTrack(track)

                    onProgress(1f)

                    DownloadResult.Success(track)
                } catch (e: Exception) {
                    Log.e("DownloadRepository", "Download failed", e)
                    DownloadResult.Error(e.message ?: "Download failed")
                }
            }
}
