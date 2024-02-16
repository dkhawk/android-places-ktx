package com.example.newplacesapidemoapp.presentation

import com.example.newplacesapidemoapp.data.model.PlaceData

sealed class PlacesViewState {
}

data class PlacesList(
  val places: List<PlaceData>,
) : PlacesViewState()


data class PlacesSearch(
  val searchText: String,
  // TODO: Included type https://developers.google.com/maps/documentation/places/android-sdk/place-types#table-a
  // https://developers.google.com/maps/documentation/places/android-sdk/text-search#location-bias
)
