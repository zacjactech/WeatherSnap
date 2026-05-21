package com.weather.core.network.di

import com.weather.core.network.GeocodingApi
import com.weather.core.network.OpenMeteoApi
import com.weather.core.network.WeatherSnapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Shared OkHttpClient with logging gated behind BuildConfig.DEBUG to avoid
     * leaking full request/response bodies in release builds.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (com.weather.core.network.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provides the Retrofit instance pointing to the Open-Meteo API.
     * Open-Meteo is free, no API key required, base URL: https://api.open-meteo.com/
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenMeteoApi(retrofit: Retrofit): OpenMeteoApi =
        retrofit.create(OpenMeteoApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(
        okHttpClient: OkHttpClient
    ): GeocodingApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GeocodingApi::class.java)
    }

    /**
     * Provides the WeatherSnap backend API client.
     *
     * NOTE: Replace "https://api.weathersnap.example.com/" with the real backend URL.
     * If no backend exists, this client is still wired but sync will gracefully fail
     * and be retried by WorkManager.
     */
    @Provides
    @Singleton
    fun provideWeatherSnapApi(
        okHttpClient: OkHttpClient
    ): WeatherSnapApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(com.weather.core.network.BuildConfig.WEATHERSNAP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(WeatherSnapApi::class.java)
    }
}
