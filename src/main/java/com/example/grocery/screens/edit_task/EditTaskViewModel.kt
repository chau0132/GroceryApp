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

package com.example.grocery.screens.edit_task

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.grocery.TASK_ID
import com.example.grocery.common.ext.idFromParameter
import com.example.grocery.model.Images
import com.example.grocery.model.Task
import com.example.grocery.model.service.LogService
import com.example.grocery.model.service.StorageService
import com.example.grocery.screens.GroceryAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  logService: LogService,
  private val storageService: StorageService,
) : GroceryAppViewModel(logService) {
  val task = mutableStateOf(Task())
  val image = mutableStateOf(Images())
  private val _imageUris = MutableLiveData<List<Uri>>()
  private val imageUris: LiveData<List<Uri>> get() = _imageUris

  init {
    val taskId = savedStateHandle.get<String>(TASK_ID)
    if (taskId != null) {
      launchCatching {
        task.value = storageService.getTask(taskId.idFromParameter()) ?: Task()
      }
    }
  }

  fun onTitleChange(newValue: String) {
    task.value = task.value.copy(title = newValue)
  }

  fun onDescriptionChange(newValue: String) {
    task.value = task.value.copy(description = newValue)
  }

  fun onUrlChange(newValue: String) {
    task.value = task.value.copy(url = newValue)
  }

  fun onUriChange(newValue: List<Uri>?) {
    image.value = image.value.copy(uri = newValue)
    val currTime = Date().toString()
    task.value = task.value.copy(uriFile = currTime)
    image.value = image.value.copy(filePath = currTime)
  }

  fun onDateChange(newValue: Long) {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC))
    calendar.timeInMillis = newValue
    val newDueDate = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).format(calendar.time)
    task.value = task.value.copy(dueDate = newDueDate)
  }

  fun onTimeChange(hour: Int, minute: Int) {
    val newDueTime = "${hour.toClockPattern()}:${minute.toClockPattern()}"
    task.value = task.value.copy(dueTime = newDueTime)
  }

  fun onFlagToggle(newValue: String) {
    val newFlagOption = EditFlagOption.getBooleanValue(newValue)
    task.value = task.value.copy(flag = newFlagOption)
  }

  fun onPriorityChange(newValue: String) {
    task.value = task.value.copy(priority = newValue)
  }

  fun getImage(): List<Uri>? {
    launchCatching {
      _imageUris.postValue(storageService.getUri(task.value.uriFile))
    }
    return imageUris.value
  }

  fun onDoneClick(popUpScreen: () -> Unit) {
    launchCatching {
      storageService.fileUpload(image.value)
      val editedTask = task.value
      if (editedTask.id.isBlank()) {
        storageService.save(editedTask)
      } else {
        storageService.update(editedTask)
      }

      popUpScreen()
    }
  }

  private fun Int.toClockPattern(): String {
    return if (this < 10) "0$this" else "$this"
  }

  companion object {
    private const val UTC = "UTC"
    private const val DATE_FORMAT = "EEE, d MMM yyyy"
  }
}
