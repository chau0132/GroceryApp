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

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalMaterialApi
class GroceryActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { PhotoPickerDemoScreen() }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhotoPickerDemoScreen() {
  //The URI of the photo that the user has picked
  var photoUri: List<Uri>? by remember { mutableStateOf(null) }

  //The launcher we will use for the PickVisualMedia contract.
  //When .launch()ed, this will display the photo picker.
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uri ->
      //When the user has selected a photo, its URI is returned here
      photoUri = uri
    }
  GroceryApp(photoUri = photoUri, openLibrary = {launcher.launch(PickVisualMediaRequest(
    ActivityResultContracts.PickVisualMedia.ImageAndVideo))})
}


