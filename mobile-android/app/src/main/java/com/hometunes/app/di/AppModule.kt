package com.hometunes.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.hometunes.app.data.database.AppDatabase
import com.hometunes.app.data.database.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hometunes.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }
}
