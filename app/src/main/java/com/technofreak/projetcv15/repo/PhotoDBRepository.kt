/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.technofreak.projetcv15.repo

import android.util.Log
import android.util.Pair
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.technofreak.projetcv15.database.DhgalleryDatabaseDao
import com.technofreak.projetcv15.model.PhotoEntity
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoDBRepository(private val dhgalleryDatabaseDao: DhgalleryDatabaseDao) {


    val allPhotos: LiveData<List<PhotoEntity>> = dhgalleryDatabaseDao.getall()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(photoEntity: PhotoEntity) {
        dhgalleryDatabaseDao.insert(photoEntity)
    }

    val likedPhotos: LiveData<List<PhotoEntity>> = dhgalleryDatabaseDao.getliked()

    fun likeupdate(id: Long, like: Boolean) {
        dhgalleryDatabaseDao.likeupdate(id, like)
    }


}