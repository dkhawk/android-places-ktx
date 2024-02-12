package com.example.newplacesapidemoapp.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newplacesapidemoapp.presentation.components.AnimatedCard

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