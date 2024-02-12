package com.example.newplacesapidemoapp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newplacesapidemoapp.presentation.components.CloseableCard
import com.example.newplacesapidemoapp.data.model.PlaceData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
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
//            SwipeableLanguagesList()
            PlacesList(
                places,
                onClick = { placeId -> onPlaceClick(placeId) },
                onCloseClick = { placeId -> onPlaceCloseClick(placeId) }
            )
        }
    }
}

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
