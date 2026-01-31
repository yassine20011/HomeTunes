package com.hometunes.app.data.api

import com.google.gson.annotations.SerializedName

data class DownloadRequest(
    val url: String,
    val quality: String = "192"
)

data class TrackMetadata(
    val title: String,
    val artist: String?,
    val duration: Int,
    @SerializedName("youtube_id")
    val youtubeId: String,
    @SerializedName("file_size")
    val fileSize: Long,
    @SerializedName("thumbnail_base64")
    val thumbnailBase64: String?
)

data class HealthResponse(
    val status: String
)
