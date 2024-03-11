package com.example.new_places_client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.new_places_client.widget.ExperimentalPlacesApi
import com.example.new_places_client.widget.PlacesAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFetchPlace
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val predictionsHighlightStyle = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPlacesApi::class)
@Composable
fun AutocompleteScreen(placesClient: PlacesClient, onShowMessage: (String) -> Unit) {
    // The list of place details fields to retrieve from the server for the selected place.
    // See the full list at https://developers.google.com/maps/documentation/places/android-sdk/place-data-fields
    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    var selectedPlace by remember {
        mutableStateOf<AutocompletePrediction?>(null)
    }

    var placeDetails by remember {
        mutableStateOf<PlaceDetails?>(null)
    }

    LaunchedEffect(selectedPlace) {
        placeDetails = selectedPlace?.placeId?.let { placeId ->
            val result = placesClient.awaitFetchPlace(placeId, fields)
            result.place.toPlaceDetails()
        }
    }

    val resources = LocalContext.current.resources

    Column(Modifier.fillMaxSize()) {
        PlacesAutocomplete(
            placesClient,
            actions = {
                locationBias = RectangularBounds.newInstance(
                    LatLng(39.95106, -105.31828), // SW lat, lng
                    LatLng(40.07399, -105.18096) // NE lat, lng
                )
                typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
                countries = listOf("US")
            },
            onPlaceSelected = { place ->
                place?.getPrimaryText(null)?.toString()?.let { placeText ->
                    onShowMessage(resources.getString(R.string.selected_place, placeText))
                }
                selectedPlace = place
            },
            modifier = Modifier.fillMaxWidth(),
            predictionsHighlightStyle = predictionsHighlightStyle
        )

        selectedPlace?.let { prediction ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = prediction.getFullText(null).toString()
                )
            }
        }

        placeDetails?.let { place ->
            Spacer(modifier = Modifier.height(8.dp))
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(place.location, 15f)
            }

            LaunchedEffect(place.location) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(place.location, 15f)
                )
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(place.location),
                        title = place.name,
                        snippet = place.address
                    )
                }
            }
        }
    }
}

