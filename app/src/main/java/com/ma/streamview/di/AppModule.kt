package com.ma.streamview.di

import android.content.Context
import android.util.Log
import com.ma.streamview.data.preferences.PlatformPreferences
import com.ma.streamview.data.repo.MediaRepository
import com.ma.streamview.data.repo.MediaRepositoryImpl
import com.ma.streamview.data.repo.source.MediaLocalDataSource
import com.ma.streamview.data.repo.source.MediaRemoteDataSource
import com.ma.streamview.services.GQLService
import com.ma.streamview.services.GQLServiceImpl
import com.ma.streamview.services.HelixService
import com.ma.streamview.services.HelixServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
            return HttpClient(Android) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            coerceInputValues = true
                        })
                }

                install(Logging) {
                    level = LogLevel.ALL
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            Log.v("HttpClient", message)
                        }
                    }
                }
                install(HttpTimeout) {
                    connectTimeoutMillis = 60_000
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                }
            }
        }

    @Singleton
    @Provides
    fun provideHelixService(httpClient: HttpClient): HelixService {
        return HelixServiceImpl(httpClient.config {
            defaultRequest {
//                    url(TWITCH_HELIX_BASE_URL)
//                    headers {
//                        header(HttpHeaders.ContentType, ContentType.Context.Json)
//                    }
            }
        })
    }
    @Singleton
    @Provides
    fun provideGQLService(httpClient: HttpClient): GQLService {
        return GQLServiceImpl(httpClient)
    }


    @Singleton
    @Provides
    fun provideMediaRemoteDataSource(helixService: HelixService,gqlService: GQLService): MediaRemoteDataSource {
        return MediaRemoteDataSource(helixService = helixService, gqlService = gqlService)
    }

    @Singleton
    @Provides
    fun providePlatformContext(
        @ApplicationContext context: Context,
    ): PlatformPreferences {
        return PlatformPreferences(context)
    }

    @Singleton
    @Provides
    fun provideMediaLocalDataSource(
        @ApplicationContext context: Context,
    ): MediaLocalDataSource {
        return MediaLocalDataSource(
            preferences = PlatformPreferences(context),
            context = context
        )
    }

    @Singleton
    @Provides
    fun provideAppRepository(
        mediaRemoteDataSource: MediaRemoteDataSource,
        mediaLocalDataSource: MediaLocalDataSource
    ): MediaRepository {
        return MediaRepositoryImpl(
            remoteDataSource = mediaRemoteDataSource,
            localDataSource = mediaLocalDataSource
        )
    }
//

//    @Provides
//    @Singleton
//    fun provideDatabase(@ApplicationContext context: Context): StreamDatabase {
//        return  StreamDatabase(driver = AndroidSqliteDriver(StreamDatabase.Schema, context, "stream.db"))
//    }
}

//   @Provides
//    @Singleton
//    fun provideDatabase(@ApplicationContext context: Context): StreamDatabase {
//  return  StreamDatabase(driver = AndroidSqliteDriver(StreamDatabase.Schema, context, "stream.db"))
//    }