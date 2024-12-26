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

package com.example.grocery.model.service.impl

import android.net.Uri
import android.util.Log
import com.example.grocery.model.Images
import com.example.grocery.model.Priority
import com.example.grocery.model.Task
import com.example.grocery.model.service.AccountService
import com.example.grocery.model.service.StorageService
import com.example.grocery.model.service.trace
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await

class StorageServiceImpl @Inject constructor(
  private val firestore: FirebaseFirestore,
  private val auth: AccountService,
  ) : StorageService {

  private val storage: FirebaseStorage = Firebase.storage
  private val collection get() = firestore.collection(TASK_COLLECTION)
    .whereEqualTo(USER_ID_FIELD, auth.currentUserId)

  override suspend fun fileUpload(image: Images) {
    val storageRef = storage.reference
    if (image.uri != null) {
      for (ur in image.uri) {
        val riversRef = storageRef.child("image/" + auth.currentUserId + "/" + image.filePath + "/" + "${ur.lastPathSegment}" + ".jpg")
        val uploadTask = riversRef.putFile(ur)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener { exception ->
          // Handle unsuccessful uploads
          Log.e("UploadError", "Upload failed", exception)
        }.addOnSuccessListener { taskSnapshot ->
          // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
          // ...
          val metadata = taskSnapshot.metadata
          Log.d("UploadSuccess", "Upload succeeded: ${metadata.toString()}")
        }
      }
    }
  }
    override suspend fun getUri(filePath: String): List<Uri> {

        val storageRef = storage.reference
        val uriPath = storageRef.child("image/" + auth.currentUserId + "/" + filePath)
        val uris = mutableListOf<Uri>()
        uriPath.listAll().addOnSuccessListener { listResult ->
          listResult.items.map { it.downloadUrl.addOnSuccessListener {uri ->
            uris.add(uri)
            }
          }
          Log.d("Uploaddddd",uris.toString())
        }
          .addOnFailureListener{
            Log.d("Failure","")
          }
        return uris
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override val tasks: Flow<List<Task>>
    get() =
      auth.currentUser.flatMapLatest { user ->
        firestore
          .collection(TASK_COLLECTION)
          .whereEqualTo(USER_ID_FIELD, user.id)
          .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
          .dataObjects()
      }

  override suspend fun getTask(taskId: String): Task? =
    firestore.collection(TASK_COLLECTION).document(taskId).get().await().toObject()

  override suspend fun save(task: Task): String =
    trace(SAVE_TASK_TRACE) {
     val updatedTask = task.copy(userId = auth.currentUserId)
      firestore.collection(TASK_COLLECTION).add(updatedTask).await().id
    }

  override suspend fun update(task: Task): Unit =
    trace(UPDATE_TASK_TRACE) {
      firestore.collection(TASK_COLLECTION).document(task.id).set(task).await()
    }

  override suspend fun delete(taskId: String) {
    firestore.collection(TASK_COLLECTION).document(taskId).delete().await()
  }

  override suspend fun getCompletedTasksCount(): Int {
    val query = collection.whereEqualTo(COMPLETED_FIELD, true).count()
    return query.get(AggregateSource.SERVER).await().count.toInt()
  }

  override suspend fun getImportantCompletedTasksCount(): Int {
    val query = collection.where(
      Filter.and(
        Filter.equalTo(COMPLETED_FIELD, true),
        Filter.or(
          Filter.equalTo(PRIORITY_FIELD, Priority.High.name),
          Filter.equalTo(FLAG_FIELD, true)
        )
      )
    )

    return query.count().get(AggregateSource.SERVER).await().count.toInt()
  }

  override suspend fun getMediumHighTasksToCompleteCount(): Int {
    val query = collection
      .whereEqualTo(COMPLETED_FIELD, false)
      .whereIn(PRIORITY_FIELD, listOf(Priority.Medium.name, Priority.High.name)).count()

    return query.get(AggregateSource.SERVER).await().count.toInt()
  }

  companion object {
    private const val USER_ID_FIELD = "userId"
    private const val COMPLETED_FIELD = "completed"
    private const val PRIORITY_FIELD = "priority"
    private const val FLAG_FIELD = "flag"
    private const val CREATED_AT_FIELD = "createdAt"
    private const val TASK_COLLECTION = "tasks"
    private const val SAVE_TASK_TRACE = "saveTask"
    private const val UPDATE_TASK_TRACE = "updateTask"
  }
}
