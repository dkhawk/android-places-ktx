package com.example.newplacesapidemoapp.domain.usecases

import androidx.annotation.RequiresPermission
import com.example.newplacesapidemoapp.data.model.Status
import com.example.newplacesapidemoapp.data.providers.PlacesProvider
import com.github.michaelbull.result.Result
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import javax.inject.Inject

class FindCurrentPlaceUseCase
@Inject
constructor(
    private val placesClient: PlacesProvider
) {
    private val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.ADDRESS)

    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
        ]
    )
    suspend operator fun invoke(): Result<List<PlaceLikelihood>, Status> {
        val currentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        return placesClient.findCurrentPlace(currentPlaceRequest)
    }
}