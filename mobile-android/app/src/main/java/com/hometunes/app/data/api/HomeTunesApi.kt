package com.hometunes.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

interface HomeTunesApi {
    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("download")
    @Streaming
    suspend fun downloadTrack(@Body request: DownloadRequest): Response<okhttp3.ResponseBody>
}
