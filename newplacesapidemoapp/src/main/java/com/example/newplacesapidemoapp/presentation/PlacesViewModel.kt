package com.example.newplacesapidemoapp.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newplacesapidemoapp.data.model.PlaceData
import com.example.newplacesapidemoapp.domain.usecases.FindCurrentPlaceUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel
@Inject
constructor(
  val findCurrentPlaceUseCase: FindCurrentPlaceUseCase,
) : ViewModel() {
  private val errors = mutableStateListOf<TimestampedError>()
  private val _places = mutableStateListOf<PlaceData>()
  val places: List<PlaceData> = _places

  private val placeSequence = generateSequence(0) { it + 1 }.iterator()

  fun onEvent(event: PlacesViewModelEvent) {
    when (event) {
      PlacesViewModelEvent.OnErrorsCloseClick -> errors.clear()
      PlacesViewModelEvent.OnAddNewPlaceClick -> addPlace()
      is PlacesViewModelEvent.OnPlaceClick -> addError("Not implemented, yet!")
      is PlacesViewModelEvent.OnPlaceCloseClick -> placeClose(event.placeId)
      PlacesViewModelEvent.OnUpdateCurrentPlace -> updateCurrentPlace()
      PlacesViewModelEvent.OnTextSearchClick -> showTextSearchDialog()
    }
  }

  private fun showTextSearchDialog() {
    TODO("Not yet implemented")
  }

  @SuppressLint("MissingPermission")
  private fun updateCurrentPlace() {
    viewModelScope.launch {
      findCurrentPlaceUseCase()
        .onSuccess { places ->
          // Add a place for each result?

        }
        .onFailure { status ->
          addError(status.toString())
        }
    }
  }

  private fun placeClose(placeId: String) {
    _places.removeById(placeId)
  }

  private fun addPlace() {
    val placeId = placeSequence.next()
    _places.upsert(
        PlaceData(
            placeId = "$placeId",
            label = "Place $placeId"
        )
    )
  }

  data class TimestampedError(
    val message: String,
    val timestamp: Instant
  )

  val errorMessages by derivedStateOf {
      errors.map { it.message }
  }

  private fun addError(message: String) {
    TimestampedError(message, Instant.now()).also { msg ->
      errors.add(msg)
      viewModelScope.launch {
        delay(5000)
        errors.remove(msg)
      }
    }
  }
}

private fun MutableList<PlaceData>.upsert(place: PlaceData) {
  val index = this.indexOfFirst { it.placeId == place.placeId }
  if (index >= 0) {
    this[index] = place
  } else {
    this.add(place)
  }
}

private fun MutableList<PlaceData>.removeById(placeId: String) {
  removeIf { it.placeId == placeId }
}
