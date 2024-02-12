package com.example.newplacesapidemoapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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