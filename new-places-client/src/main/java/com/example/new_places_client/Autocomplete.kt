package com.example.new_places_client

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach

@Composable
fun AutocompleteScreen(placesClient: PlacesClient, onShowMessage: (String) -> Unit) {
  PlacesAutocomplete(
    placesClient,
    modifier = Modifier.fillMaxWidth(),
    onPlacesError = { errorMessage -> onShowMessage(errorMessage) },
    onPlaceSelected = { place ->
      onShowMessage("Selected place: ${place.getPrimaryText(null)}")
    }
  )
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun PlacesAutocomplete(
  placesClient: PlacesClient,
  modifier: Modifier = Modifier,
  onPlacesError: (String) -> Unit = {},
  onPlaceSelected: (AutocompletePrediction) -> Unit = {},
) {
  val searchTextFlow = remember {
    MutableStateFlow("")
  }

  val searchText by searchTextFlow.collectAsState()

  var isSearching by rememberSaveable {
    mutableStateOf(false)
  }

  val bias: LocationBias = RectangularBounds.newInstance(
    LatLng(39.95106569567399, -105.31827513517003), // SW lat, lng
    LatLng(40.07399086773728, -105.18096421332513) // NE lat, lng
  )

  val autocompleteResults = remember {
    mutableStateListOf<AutocompletePrediction>()
  }

  var expanded by remember { mutableStateOf(false) }

  // Need to stop this when an item is selected!
  LaunchedEffect(true) {
    searchTextFlow
      .debounce(500L)
      .onEach { isSearching = true }
      .collectLatest { text ->
        if (text.isNotBlank()) {
          // TODO: Add highlighting:
          // https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/model/AutocompletePrediction
          val acp = placesClient.awaitFindAutocompletePredictions {
            locationBias = bias
            typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
            this.query = text
            countries = listOf("US")
          }.autocompletePredictions

          autocompleteResults.clear()
          autocompleteResults.addAll(acp)
          expanded = true
        }
        isSearching = false
      }
  }
  Column(
    modifier = modifier
  ) {
    val STYLE_BOLD = StyleSpan(Typeface.BOLD)
    val STYLE_NORMAL = StyleSpan(Typeface.NORMAL)

    var selectedPrediction by remember {
      mutableStateOf<AutocompletePrediction?>(null)
    }

    Box(
      modifier = Modifier
          .fillMaxWidth()
          .wrapContentSize(Alignment.TopStart)
    ) {
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchText,
        onValueChange = { searchTextFlow.value = it },
        label = { Text("Place search") },
      )
      if (isSearching) {
        LinearProgressIndicator(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colorScheme.secondary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
      DropdownMenu(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onDismissRequest = { expanded = false },
      ) {
        autocompleteResults.forEach { prediction ->
          DropdownMenuItem(
            text = {
              Column(
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(prediction.getPrimaryText(STYLE_BOLD).toString())
                Text(prediction.getSecondaryText(STYLE_NORMAL).toString())
              }
            },
            onClick = {
              selectedPrediction = prediction
              expanded = false
            }
          )
        }
      }
    }
    selectedPrediction?.let { prediction ->
      Spacer(modifier = Modifier.height(16.dp))
      Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = prediction.getPrimaryText(STYLE_BOLD).toString()
        )
        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = prediction.getSecondaryText(STYLE_NORMAL).toString()
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}
