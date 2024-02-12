package com.example.newplacesapidemoapp.data.providers

import androidx.annotation.RequiresPermission
import com.example.newplacesapidemoapp.data.model.Status
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.github.michaelbull.result.Result

interface PlacesProvider {
    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
        ]
    )
    suspend fun findCurrentPlace(
        placeRequest: FindCurrentPlaceRequest
    ): Result<List<PlaceLikelihood>, Status>
}
