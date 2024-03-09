package com.example.new_places_client

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

// Simple data class to hold the place data needed from the search results
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String?,
    val location: LatLng,
)

fun Place.toPlaceDetails(): PlaceDetails? {
    val name = this.name
    val placeId = this.id
    val address = this.address
    val latLng = this.latLng
    return if (placeId != null && name != null && latLng != null) {
        PlaceDetails(
            placeId = placeId,
            name = name,
            address = address,
            location = latLng
        )
    } else {
        null
    }
}