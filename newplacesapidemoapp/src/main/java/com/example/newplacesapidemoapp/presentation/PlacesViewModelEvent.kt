package com.example.newplacesapidemoapp.presentation

sealed class PlacesViewModelEvent {
  data object OnErrorsCloseClick : PlacesViewModelEvent()
  data object OnAddNewPlaceClick: PlacesViewModelEvent()
  data class OnPlaceClick(val placeId: String): PlacesViewModelEvent()
  data class OnPlaceCloseClick(val placeId: String): PlacesViewModelEvent()
  data object OnUpdateCurrentPlace : PlacesViewModelEvent()
  data object OnTextSearchClick: PlacesViewModelEvent()
}