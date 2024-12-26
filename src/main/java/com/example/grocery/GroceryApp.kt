/*
Copyright 2022 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.example.grocery

import android.Manifest
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.example.grocery.R.string as AppText
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.grocery.common.composable.PermissionDialog
import com.example.grocery.common.composable.RationaleDialog
import com.example.grocery.common.snackbar.SnackbarManager
import com.example.grocery.screens.edit_task.EditTaskScreen
import com.example.grocery.screens.login.LoginScreen
import com.example.grocery.screens.settings.SettingsScreen
import com.example.grocery.screens.sign_up.SignUpScreen
import com.example.grocery.screens.splash.SplashScreen
import com.example.grocery.screens.stats.StatsScreen
import com.example.grocery.screens.tasks.TasksScreen
import com.example.grocery.theme.MakeItSoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope

@Composable
@ExperimentalMaterialApi
fun GroceryApp(photoUri: List<Uri>?, openLibrary: () -> Unit) {
  MakeItSoTheme {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      RequestNotificationPermissionDialog()
      RequestMediaPermissionDialog()
    }

    Surface(color = MaterialTheme.colors.background) {
      val appState = rememberAppState()

      Scaffold(
        snackbarHost = {
          SnackbarHost(
            hostState = it,
            modifier = Modifier.padding(8.dp),
            snackbar = { snackbarData ->
              Snackbar(snackbarData, contentColor = MaterialTheme.colors.onPrimary)
            }
          )
        },
        scaffoldState = appState.scaffoldState
      ) { innerPaddingModifier ->
        NavHost(
          navController = appState.navController,
          startDestination = SPLASH_SCREEN,
          modifier = Modifier.padding(innerPaddingModifier)
        ) {
          groceryGraph(photoUri = photoUri, openLibrary = openLibrary,appState)
        }
      }
    }
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog() {
  val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

  if (!permissionState.status.isGranted) {
    if (permissionState.status.shouldShowRationale) RationaleDialog()
    else PermissionDialog( text = AppText.request_notification_permission, onRequestPermission = { permissionState.launchPermissionRequest() })
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestMediaPermissionDialog() {
  val permissionState = rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)

  if (!permissionState.status.isGranted) {
    if (permissionState.status.shouldShowRationale) RationaleDialog()
    else PermissionDialog( text = AppText.request_media_permission, onRequestPermission = { permissionState.launchPermissionRequest() })
  }
}

@Composable
fun rememberAppState(
  scaffoldState: ScaffoldState = rememberScaffoldState(),
  navController: NavHostController = rememberNavController(),
  snackbarManager: SnackbarManager = SnackbarManager,
  resources: Resources = resources(),
  coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
  remember(scaffoldState, navController, snackbarManager, resources, coroutineScope) {
    GroceryAppState(scaffoldState, navController, snackbarManager, resources, coroutineScope)
  }

@Composable
@ReadOnlyComposable
fun resources(): Resources {
  LocalConfiguration.current
  return LocalContext.current.resources
}

@ExperimentalMaterialApi
fun NavGraphBuilder.groceryGraph(photoUri: List<Uri>?, openLibrary: () -> Unit, appState: GroceryAppState) {
  composable(SPLASH_SCREEN) {
    SplashScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
  }

  composable(SETTINGS_SCREEN) {
    SettingsScreen(
      restartApp = { route -> appState.clearAndNavigate(route) },
      openScreen = { route -> appState.navigate(route) }
    )
  }

  composable(STATS_SCREEN) {
    StatsScreen()
  }

  composable(LOGIN_SCREEN) {
    LoginScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
  }

  composable(SIGN_UP_SCREEN) {
    SignUpScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
  }

  composable(TASKS_SCREEN) { TasksScreen(openScreen = { route -> appState.navigate(route) }) }

  composable(
    route = "$EDIT_TASK_SCREEN$TASK_ID_ARG",
    arguments = listOf(navArgument(TASK_ID) {
      nullable = true
      defaultValue = null
    })
  ) {
    EditTaskScreen(
      photoUri = photoUri,
      openLibrary = openLibrary,
      popUpScreen = { appState.popUp() }
    )
  }
}
