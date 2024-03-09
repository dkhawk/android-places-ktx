package com.example.new_places_client

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFetchPlace
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

private val styleSpan = StyleSpan(Typeface.BOLD)
private val predictionsHighlightStyle = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AutocompleteScreen(placesClient: PlacesClient, onShowMessage: (String) -> Unit) {
    // The list of fields to retrieve from the server
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

    Column(Modifier.fillMaxSize()) {

        PlacesAutocomplete(
            placesClient,
            modifier = Modifier.fillMaxWidth(),
            onPlaceSelected = { place ->
                onShowMessage(
                    "Selected place: ${
                        place?.getPrimaryText(styleSpan)?.toAnnotatedString(
                            predictionsHighlightStyle
                        )
                    }"
                )
                selectedPlace = place
            },
            actions = {
                locationBias = RectangularBounds.newInstance(
                    LatLng(39.95106569567399, -105.31827513517003), // SW lat, lng
                    LatLng(40.07399086773728, -105.18096421332513) // NE lat, lng
                )
                typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
                countries = listOf("US")
            },
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlacesAutocomplete(
    placesClient: PlacesClient,
    modifier: Modifier = Modifier,
    onPlaceSelected: (AutocompletePrediction?) -> Unit = {},
    actions: FindAutocompletePredictionsRequest.Builder.() -> Unit,
    searchLabelContent: @Composable () -> Unit = { PlacesAutocompleteDefaultLabel() },
    predictionsHighlightStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
) {
    val searchTextFlow = remember {
        MutableStateFlow("")
    }

    val autocompleteResults = remember {
        mutableStateListOf<AutocompletePrediction>()
    }

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("", TextRange(0, 0)))
    }

    val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
    val expanded = allowExpanded && autocompleteResults.isNotEmpty()

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(true) {
        searchTextFlow
            .debounce(500L)
            .collectLatest { text ->
                if (text.isNotBlank()) {
                    // https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/model/AutocompletePrediction
                    val acp = placesClient.awaitFindAutocompletePredictions {
                        apply(actions)
                        this.query = text
                    }.autocompletePredictions
                    autocompleteResults.clear()
                    autocompleteResults.addAll(acp)
                } else {
                    autocompleteResults.clear()
                    onPlaceSelected(null)
                }
            }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = setExpanded,
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = text,
            onValueChange = {
                text = it
                searchTextFlow.value = it.text
            },
            singleLine = true,
            label = searchLabelContent,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            autocompleteResults.forEach { prediction ->
                val primary = prediction.getPrimaryText(styleSpan).toAnnotatedString(
                    predictionsHighlightStyle
                )
                val secondary = prediction.getSecondaryText(styleSpan).toAnnotatedString(
                    predictionsHighlightStyle
                )
                DropdownMenuItem(
                    text = {
                        Column(Modifier.fillMaxWidth()) {
                            Text(primary, style = MaterialTheme.typography.bodyLarge)
                            Text(secondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        text = TextFieldValue(primary, TextRange(primary.length))
                        onPlaceSelected(prediction)
                        setExpanded(false)
                        keyboardController?.hide()
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private fun SpannableString.toAnnotatedString(spanStyle: SpanStyle): AnnotatedString {
    val string = this.toString()
    return buildAnnotatedString {
        var last = 0
        for (span in getSpans(0, length, Any::class.java)) {
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            append(string.substring(last, start))
            pushStyle(spanStyle)
            append(string.substring(start, end))
            pop()
            last = end
        }
        append(string.substring(last))
    }
}

@Composable
fun PlacesAutocompleteDefaultLabel() {
    Text(stringResource(id = R.string.auto_complete_hint))
}
