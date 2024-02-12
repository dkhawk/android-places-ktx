package com.example.newplacesapidemoapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

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