package com.example.newplacesapidemoapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.newplacesapidemoapp.ui.theme.AndroidPlacesKtxTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
          PlacesContainer(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesContainer(
  errors: List<String>,
  places: List<PlaceData>,
  onClearErrorsClicked: () -> Unit,
  onFabClicked: () -> Unit,
  onPlaceClick: (String) -> Unit,
  onPlaceCloseClick: (String) -> Unit,
) {
    Scaffold(
      topBar = {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
          ),
          title = {
            Text("Places KTX demo")
          },
        )
      },
      floatingActionButton = {
        FloatingActionButton(onClick = onFabClicked) {
          Icon(Icons.Filled.Add, "Add a place card")
        }
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .padding(innerPadding)
          .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AnimatedErrorList(errors, onClearErrorsClicked)
        PlacesList(
          places,
          onClick = { placeId -> onPlaceClick(placeId) },
          onCloseClick = { placeId -> onPlaceCloseClick(placeId) }
        )
      }
    }
}

data class PlaceData(
  val placeId: String,
  val label: String
)

@Composable
fun PlacesList(
  places: List<PlaceData>,
  onClick: (String) -> Unit,
  onCloseClick: (String) -> Unit,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    items(
      items = places,
      key = { place -> place.placeId }
    ) {
      PlaceCard(it, { onClick(it.placeId) }, { onCloseClick(it.placeId) })
    }
  }
}

@Composable
fun PlaceCard(placeData: PlaceData, onContentClick: () -> Unit, onCloseClick: () -> Unit) {
  CloseableCard(
    title = { Text(text = placeData.label) },
    modifier = Modifier.fillMaxWidth(),
    onContentClicked = onContentClick,
    onCloseClicked = onCloseClick,
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(text = placeData.label)
    }
  }
}

@HiltViewModel
class PlacesViewModel
@Inject
constructor() : ViewModel() {
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

sealed class PlacesViewModelEvent {
  data object OnErrorsCloseClick : PlacesViewModelEvent()
  data object OnAddNewPlaceClick: PlacesViewModelEvent()
  data class OnPlaceClick(val placeId: String): PlacesViewModelEvent()
  data class OnPlaceCloseClick(val placeId: String): PlacesViewModelEvent()
}

@Composable
fun AnimatedCard(
  title: @Composable () -> Unit,
  isVisible: Boolean,
  modifier: Modifier = Modifier,
  colors: CardColors = CardDefaults.cardColors(),
  onContentClicked: () -> Unit,
  onCloseClicked: () -> Unit,
  content: @Composable (ColumnScope.() -> Unit),
) {
  var visible by remember { mutableStateOf(false) }
  visible = isVisible

  // See https://developer.android.com/jetpack/compose/animation/composables-modifiers#animatedvisibility
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(
      // Start the slide from 40 (pixels) above where the content is supposed to go, to
      // produce a parallax effect
      initialOffsetY = { -40 }
    ) + expandVertically(
      expandFrom = Alignment.Top
    ) + scaleIn(
      // Animate scale from 0f to 1f using the top center as the pivot point.
      transformOrigin = TransformOrigin(0.5f, 0f)
    ) + fadeIn(initialAlpha = 0.3f),
    exit = slideOutVertically() + shrinkVertically() + fadeOut() + scaleOut(targetScale = 1.2f)
  ) {
    CloseableCard(
      modifier = modifier,
      colors = colors,
      title = title,
      onContentClicked = onContentClicked,
      onCloseClicked = onCloseClicked,
      content = content
    )
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CloseableCard(
  title: @Composable () -> Unit,
  onContentClicked: () -> Unit,
  onCloseClicked: () -> Unit,
  modifier: Modifier = Modifier,
  colors: CardColors = CardDefaults.cardColors(),
  content: @Composable (ColumnScope.() -> Unit),
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onContentClicked,
    colors = colors,
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
          title()
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
          onClick = onCloseClicked
        ) {
          Icon(
            Icons.Default.Close,
            contentDescription = "Close",
          )
        }
      }
      content()
    }
  }
}

@Preview
@Composable
fun CloseableCardPreview() {
  CloseableCard(
    modifier = Modifier,
    onContentClicked = { /*TODO*/ },
    title = { Text("Card title") },
    onCloseClicked = { /*TODO*/ },
  ) {
    Text(text = "Body text line 1")
    Text(text = "Body text line 2")
    Text(text = "Body text line 3")
  }
}

@Composable
fun AnimatedErrorList(
  errorMessages: List<String>,
  onCloseClicked: () -> Unit
) {
  AnimatedCard(
    title = { Text("Errors") },
    isVisible = errorMessages.isNotEmpty(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer,
    ),
    onContentClicked = { /*TODO*/ },
    onCloseClicked = onCloseClicked,
  ) {
    errorMessages.forEach {
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = it)
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(permissionState: PermissionState) {
  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
    ) {
      Text("This app needs the following permissions to run successfully")

      Spacer(Modifier.height(8.dp))

      val permissionStateString = buildAnnotatedString {
          append(" • ")
          append(permissionState.permission.substringAfterLast("."))
          append(" ‒ ")
          if (permissionState.status.isGranted) {
            withStyle(style = SpanStyle(color = Color.Green)) {
              append("Granted")
            }
          } else {
            withStyle(style = SpanStyle(color = Color.Red)) {
              append("Requested")
            }
          }
          append("\n")
        }

      Text(text = permissionStateString)

      Button(onClick = { permissionState.launchPermissionRequest() }) {
        Text("Request permissions")
      }
    }
  }
}
