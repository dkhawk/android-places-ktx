package com.example.newplacesapidemoapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import com.example.newplacesapidemoapp.ui.theme.AndroidPlacesKtxTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import com.example.newplacesapidemoapp.presentation.PlacesScreen
import com.example.newplacesapidemoapp.presentation.PlacesViewModel
import com.example.newplacesapidemoapp.presentation.PlacesViewModelEvent
import com.example.newplacesapidemoapp.presentation.components.RequestPermissions

@AndroidEntryPoint
class NewPlacesApiDemoActivity : ComponentActivity() {
  @OptIn(ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AndroidPlacesKtxTheme {
        val permissionState = rememberPermissionState(
          Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        if (permissionState.status.isGranted) {
          val viewModel: PlacesViewModel by viewModels()
          PlacesScreen(
            errors = viewModel.errorMessages,
            places = viewModel.places,
            onClearErrorsClicked = { viewModel.onEvent(PlacesViewModelEvent.OnErrorsCloseClick) },
            onFabClicked = { viewModel.onEvent(PlacesViewModelEvent.OnAddNewPlaceClick) },
            onPlaceClick = { placeId -> viewModel.onEvent(PlacesViewModelEvent.OnPlaceClick(placeId)) },
            onPlaceCloseClick = { placeId -> viewModel.onEvent(PlacesViewModelEvent.OnPlaceCloseClick(placeId)) },
          )
        } else {
          if (permissionState.status.shouldShowRationale) {
            RequestPermissions(permissionState)
          } else {
            SideEffect {
              permissionState.launchPermissionRequest()
            }
          }
        }
      }
    }
  }
}
