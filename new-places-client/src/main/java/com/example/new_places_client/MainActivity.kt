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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class NavigationItem(
  val route: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val placesClient = Places.createClient(this)

    setContent {
      val navController = rememberNavController()

      val snackbarHostState = remember { SnackbarHostState() }

      val scope = rememberCoroutineScope()

      AndroidplacesktxTheme {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

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

        var selectedScreen by remember {
          mutableStateOf(screenItems.first())
        }

        LaunchedEffect(currentDestination) {
          selectedScreen = screenItems.firstOrNull { item ->
            currentDestination?.hierarchy?.any { it.route == item.route } == true
          } ?: selectedScreen
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(
            topBar = {
              TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(selectedScreen.title) }
              )
            },
            snackbarHost = {
              SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
              NavigationBar {
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
              startDestination = screenItems.first().route,
              modifier = Modifier.padding(innerPadding)
            ) {
              composable(screenItems[0].route) {
                TextSearchScreen(placesClient) { message: String ->
                  showErrorSnackBar(message, scope, snackbarHostState)
                }
              }
              composable(Screen.Autocomplete.route) {
                AutocompleteScreen(placesClient) { message: String ->
                  showErrorSnackBar(message, scope, snackbarHostState)
                }
              }
            }
          }
        }
      }
    }
  }

  private fun showErrorSnackBar(
      message: String,
      scope: CoroutineScope,
      snackbarHostState: SnackbarHostState
  ) {
    if (message.isNotEmpty()) {
      scope.launch {
        snackbarHostState.showSnackbar(message = message)
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

