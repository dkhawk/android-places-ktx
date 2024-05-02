package com.example.new_places_client.widget

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import com.example.new_places_client.R
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * StyleSpan applied by the to the [AutocompletePrediction]s to highlight the
 */
private val predictionStyleSpan = StyleSpan(Typeface.BOLD)

/**
 * Implements a places autocomplete composable that consists of a [TextField] and an
 * [ExposedDropdownMenuBox] wrapped by an [ExposedDropdownMenuBox].  This composable debounces
 * changes to the TextField to reduce excessive calls to the [PlacesClient].
 *
 * @param placesClient - an initialized [PlacesClient] used to fetch [AutocompletePrediction]s
 * @param actions - a block applied to the [AutocompletePrediction.Builder] to create the
 * AutocompletePrediction request
 * @param onPlaceSelected - the lambda called when an [AutocompletePrediction] is selected
 * @param predictionsHighlightStyle - the [SpanStyle] applied to the predictions to highlight the
 * matching search text string
 * @param searchLabelContent - composable for the text search label
 * @param predictionMenuItemContent - composable for rendering an [AutocompletePrediction] in the
 * menu
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@ExperimentalPlacesApi
@Composable
fun PlacesAutocomplete(
  placesClient: PlacesClient,
  actions: FindAutocompletePredictionsRequest.Builder.() -> Unit,
  onPlaceSelected: (AutocompletePrediction?) -> Unit,
  modifier: Modifier = Modifier,
  predictionsHighlightStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
  searchLabelContent: @Composable () -> Unit = { },
  predictionMenuItemContent: @Composable (AutocompletePrediction, SpanStyle?) -> Unit = { prediction, style ->
      DefaultAutocompletePredictionText(prediction, style)
    }
) {
    val autocompleteResults = remember {
      mutableStateListOf<AutocompletePrediction>()
    }

    var searchTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue("", TextRange(0, 0)))
    }

    val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
    val expanded = allowExpanded && autocompleteResults.isNotEmpty()

    val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(true) {
    snapshotFlow { searchTextFieldValue }.debounce(500L).collect { textFieldValue ->
      val searchText = textFieldValue.text
      autocompleteResults.clear()
      if (searchText.isNotBlank()) {
        // https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/api/model/AutocompletePrediction
        val predictions = placesClient.awaitFindAutocompletePredictions {
          apply(actions)
          query = searchText
        }.autocompletePredictions
        autocompleteResults.addAll(predictions)
      } else {
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
      value = searchTextFieldValue,
      onValueChange = {
        searchTextFieldValue = it
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

      autocompleteResults.take(5).forEach { prediction ->
        val text = prediction.getPrimaryText(null).toString()
        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          text = { predictionMenuItemContent(prediction, predictionsHighlightStyle) },
          onClick = {
            searchTextFieldValue = TextFieldValue(text, TextRange(text.length))
            onPlaceSelected(prediction)
            setExpanded(false)
            keyboardController?.hide()
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }

      ProvideTextStyle(MaterialTheme.typography.labelLarge) {
        PoweredByGoogle()
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PoweredByGoogle() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(ExposedDropdownMenuDefaults.ItemContentPadding),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val powered = stringResource(id = R.string.powered_by_google)

    Text(
      text = powered.substringBefore("[Google]"),
      lineHeight = 3.em,
    )
    Image(painter = painterResource(id = R.drawable.google), contentDescription = "Google")
    Text(
      text = powered.substringAfter("[Google]"),
      lineHeight = 3.em,
    )
  }
}

/**
 * Default composable for rendering an [AutocompletePrediction] in the completing drop down menu
 */
@Composable
private fun DefaultAutocompletePredictionText(
  prediction: AutocompletePrediction,
  style: SpanStyle?
) {
  val primaryText = prediction.getPrimaryText(predictionStyleSpan).toAnnotatedString(style)
  val secondaryText = prediction.getSecondaryText(predictionStyleSpan).toAnnotatedString(style)

  Column(Modifier.fillMaxWidth()) {
    Text(
      primaryText, style = MaterialTheme.typography.bodyLarge,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      secondaryText, style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Attempts to convert a [SpannableString] to an [AnnotatedString].  This is not intended to be a
 * general purpose solution.  Instead, all of the spans are styled using the given [spanStyle] in
 * the resulting AnnotatedString.
 */
private fun SpannableString.toAnnotatedString(spanStyle: SpanStyle?): AnnotatedString {
    return buildAnnotatedString {
      if (spanStyle == null) {
        append(this@toAnnotatedString.toString())
      } else {
        var last = 0
        for (span in getSpans(0, length, Any::class.java)) {
          val start = getSpanStart(span)
          val end = getSpanEnd(span)
          append(this@toAnnotatedString.substring(last, start))
          pushStyle(spanStyle)
          append(this@toAnnotatedString.substring(start, end))
          pop()
          last = end
        }
        append(this@toAnnotatedString.substring(last))
      }
    }
}
