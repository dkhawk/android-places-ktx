package com.example.newplacesapidemoapp.di

import android.app.Application
import com.example.newplacesapidemoapp.BuildConfig
import com.example.newplacesapidemoapp.data.impl.PlacesImpl
import com.example.newplacesapidemoapp.data.providers.PlacesProvider
import com.google.android.libraries.places.api.Places
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePlaces(app: Application): PlacesProvider {
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            error("Missing PLACES_API_KEY")
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(app.applicationContext, apiKey)
        }

        return PlacesImpl(Places.createClient(app.applicationContext))
    }
}
