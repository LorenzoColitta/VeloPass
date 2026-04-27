package com.velopass.app.di

import android.content.Context
import androidx.room.Room
import com.velopass.app.data.VeloPassDatabase
import com.velopass.app.data.BikeDao
import com.velopass.app.data.BikeRepository
import com.velopass.app.data.BikeIndexRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.serialization.JsonFeature
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesAppName(): String {
        return "VeloPass"
    }

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context
    ): VeloPassDatabase {
        return Room.databaseBuilder(
            context,
            VeloPassDatabase::class.java,
            "velopass_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesBikeDao(database: VeloPassDatabase): BikeDao {
        return database.bikeDao()
    }

    @Provides
    @Singleton
    fun providesHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(Logging)
        }
    }

    @Provides
    @Singleton
    fun providesBikeRepository(
        bikeDao: BikeDao,
        httpClient: HttpClient
    ): BikeRepository {
        return BikeRepository(bikeDao, httpClient)
    }

    @Provides
    @Singleton
    fun providesBikeIndexRepository(
        httpClient: HttpClient
    ): BikeIndexRepository {
        return BikeIndexRepository(httpClient)
    }
}
