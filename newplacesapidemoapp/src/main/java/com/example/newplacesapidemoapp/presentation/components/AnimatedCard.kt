package com.example.newplacesapidemoapp.presentation.components

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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin

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