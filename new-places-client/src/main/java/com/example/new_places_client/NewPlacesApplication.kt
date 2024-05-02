package com.example.new_places_client

import android.app.Application
import com.google.android.libraries.places.api.Places

class NewPlacesApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    // TODO: You can also use the New Places API
    // Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.PLACES_API_KEY)
    Places.initialize(this, BuildConfig.PLACES_API_KEY)
  }
}