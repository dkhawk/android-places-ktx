package com.example.new_places_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.new_places_client.ui.theme.AndroidplacesktxTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitSearchByText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

data class NavigationItem(
  val route: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val placesClient = Places.createClient(this)

    setContent {
      val navController = rememberNavController()

      AndroidplacesktxTheme {
        val screenItems = listOf(
          NavigationItem(
            route = "text_search",
            title = "Text Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
          ),
          NavigationItem(
            route = "auto_complete",
            title = "Auto complete",
            selectedIcon = Icons.Filled.Add,
            unselectedIcon = Icons.Outlined.Add,
          ),
        )
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(
            bottomBar = {
              NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screenItems.forEach { item ->
                  val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                  NavigationBarItem(
                    selected = selected,
                    onClick = {
                      navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                          saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                      }
                    },
                    label = {
                      Text(text = item.title)
                    },
                    alwaysShowLabel = true,
                    icon = {
                      Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                      )
                    }
                  )
                }
              }
            }
          ) { innerPadding ->
            NavHost(navController, startDestination = Screen.TextSearch.route, Modifier.padding(innerPadding)) {
              composable(Screen.TextSearch.route) { TextSearchScreen(placesClient) }
              composable(Screen.Autocomplete.route) { AutocompleteScreen(placesClient) }
            }
          }
        }
      }
    }
  }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
  data object Autocomplete : Screen("auto_complete", R.string.auto_complete_label)
  data object TextSearch : Screen("text_search", R.string.text_search_label)
}

data class PlaceDetails(
  val placeId: String,
  val name: String,
  val address: String?,
  val location: LatLng?,
)

val basicFields = listOf(
  Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.BUSINESS_STATUS,
  Place.Field.ICON_BACKGROUND_COLOR, Place.Field.ICON_URL, Place.Field.ID,
  Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.PLUS_CODE,
  Place.Field.TYPES, Place.Field.VIEWPORT, Place.Field.UTC_OFFSET,
  Place.Field.WHEELCHAIR_ACCESSIBLE_ENTRANCE
)

val contactFields = listOf(
  Place.Field.CURRENT_OPENING_HOURS,
  Place.Field.OPENING_HOURS,
  Place.Field.PHONE_NUMBER,
  Place.Field.SECONDARY_OPENING_HOURS,
  Place.Field.WEBSITE_URI
)

val atmosphereFields = listOf(
  Place.Field.CURBSIDE_PICKUP, Place.Field.DELIVERY, Place.Field.EDITORIAL_SUMMARY,
  Place.Field.DINE_IN, Place.Field.PRICE_LEVEL, Place.Field.RATING, Place.Field.RESERVABLE,
  Place.Field.SERVES_BEER, Place.Field.SERVES_BREAKFAST, Place.Field.SERVES_BRUNCH,
  Place.Field.SERVES_DINNER, Place.Field.SERVES_LUNCH, Place.Field.SERVES_VEGETARIAN_FOOD,
  Place.Field.SERVES_WINE,Place.Field.TAKEOUT, Place.Field.USER_RATINGS_TOTAL
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TextSearchScreen(placesClient: PlacesClient) {
  var text by rememberSaveable {
    mutableStateOf("grocery stores near the grand canyon")
  }

  var searchText by rememberSaveable {
    mutableStateOf("")
  }

  var searchResults by rememberSaveable {
    mutableStateOf<List<PlaceDetails>>(emptyList())
  }

  var showSpinner by rememberSaveable {
    mutableStateOf(false)
  }

  var selectedPlace by rememberSaveable {
    mutableStateOf<PlaceDetails?>(null)
  }

  val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

  LaunchedEffect(key1 = searchText) {
    if (searchText.isEmpty()) {
      searchResults = emptyList()
    } else {
      // Start search
      showSpinner = true
      val response = placesClient.awaitSearchByText(searchText, fields) {
        maxResultCount = 10
      }

      searchResults = response.places.mapNotNull { place ->
        val name = place.name
        val placeId = place.id
        val address = place.address
        val latLng = place.latLng
        if (placeId != null && name != null) {
          PlaceDetails(
            placeId = placeId,
            name = name,
            address = address,
            location = latLng
          )
        } else {
          null
        }
      }

      showSpinner = false
    }
  }

  Column(modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = text,
        onValueChange = { newText -> text = newText },
        singleLine = true,
        label = { Text("Places text search") },
      )
      Spacer(modifier = Modifier.weight(1f))
      IconButton(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape),
        onClick = {
          searchText = text
        }
      ) {
        Icon(
          tint = MaterialTheme.colorScheme.onPrimary,
          imageVector = Icons.Default.Search,
          contentDescription = "Search"
        )
      }
    }
    if (showSpinner) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        ) {
        CircularProgressIndicator(
          modifier = Modifier.width(64.dp),
          color = MaterialTheme.colorScheme.secondary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    } else {
      LazyColumn {
        items(searchResults) { place ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp)
          ) {
            Column {
              Text(place.name)
              Text("(${place.placeId})",
                   style = LocalTextStyle.current.copy(color = LocalTextStyle.current.color.copy(alpha = 0.5f)))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { selectedPlace = place }) {
              Icon(imageVector = Icons.Default.Info, contentDescription = "More info")
            }
          }
        }
      }
      val place = selectedPlace
      if (place != null) {
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = place.name)
          Text(text = place.address ?: "No address")
          Text(text = place.location?.prettyPrint() ?: "No location")

          val placeMarker = LatLng(1.35, 103.87)
          val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(placeMarker, 10f)
          }
          GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
          ) {
            Marker(
              state = MarkerState(position = placeMarker),
              title = "Singapore",
              snippet = "Marker in Singapore"
            )
          }        }
      }
    }
  }
}

private fun LatLng.prettyPrint() = "${this.latitude}, ${this.longitude}"

@Composable
fun  AutocompleteScreen(placesClient: PlacesClient) {
  Text("Autocomplete")
}
