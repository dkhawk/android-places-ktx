package com.example.newplacesapidemoapp.data.impl

import androidx.annotation.RequiresPermission
import com.example.newplacesapidemoapp.data.model.Status
import com.example.newplacesapidemoapp.data.providers.PlacesProvider
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlacesImpl(private val placesClient: PlacesClient) : PlacesProvider {
    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
        ]
    )
    override suspend fun findCurrentPlace(
        placeRequest: FindCurrentPlaceRequest
    ): Result<List<PlaceLikelihood>, Status> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { cont ->
                placesClient.findCurrentPlace(placeRequest).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Ok(task.result?.placeLikelihoods ?: emptyList()))
                    } else {
                        val exception = task.exception
                        if (exception is ApiException) {
                            cont.resume(Err(Status.Error("Place not found: ${exception.statusCode}")))
                        } else {
                            cont.resume(Err(Status.Error("Some other error")))
                        }
                    }
                }
            }
        }
    }
}
