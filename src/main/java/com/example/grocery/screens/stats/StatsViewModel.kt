/*
Copyright 2023 Google LLC

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

package com.example.grocery.screens.stats

import androidx.compose.runtime.mutableStateOf
import com.example.grocery.model.service.LogService
import com.example.grocery.model.service.StorageService
import com.example.grocery.screens.GroceryAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
  logService: LogService,
  private val storageService: StorageService
) : GroceryAppViewModel(logService) {
  val uiState = mutableStateOf(StatsUiState())

  init {
    launchCatching { loadStats() }
  }

  private suspend fun loadStats() {
    val updatedUiState = StatsUiState(
      completedTasksCount = storageService.getCompletedTasksCount(),
      importantCompletedTasksCount = storageService.getImportantCompletedTasksCount(),
      mediumHighTasksToCompleteCount = storageService.getMediumHighTasksToCompleteCount()
    )

    uiState.value = updatedUiState
  }
}
