package com.hometunes.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val MUSIC_DIRECTORY = stringPreferencesKey("music_directory")
    }

    val serverUrl: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.SERVER_URL] ?: ""
            }

    val audioQuality: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUDIO_QUALITY] ?: "320" // Default to highest quality
            }

    val musicDirectory: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.MUSIC_DIRECTORY] ?: ""
            }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.SERVER_URL] = url }
    }

    suspend fun setAudioQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality
        }
    }

    suspend fun setMusicDirectory(uri: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.MUSIC_DIRECTORY] = uri }
    }
}
