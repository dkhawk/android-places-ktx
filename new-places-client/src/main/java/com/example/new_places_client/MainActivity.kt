package com.example.new_places_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.new_places_client.ui.theme.AndroidplacesktxTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

      val snackbarHostState = remember { SnackbarHostState() }

      val scope = rememberCoroutineScope()

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
            snackbarHost = {
              SnackbarHost(hostState = snackbarHostState)
            },
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
            NavHost(
              navController = navController,
              startDestination = Screen.TextSearch.route,
              modifier = Modifier.padding(innerPadding)
            ) {
              composable(Screen.TextSearch.route) {
                TextSearchScreen(placesClient) { message: String ->
                  if (message.isNotEmpty()) {
                    scope.launch {
                      snackbarHostState.showSnackbar(message = message)
                    }
                  }
                }
              }
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

@Composable
fun BigSpinner() {
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
}

@Composable
fun  AutocompleteScreen(placesClient: PlacesClient) {
  Text("Autocomplete")
}

