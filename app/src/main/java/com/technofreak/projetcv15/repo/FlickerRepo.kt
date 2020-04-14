/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.technofreak.projetcv15.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import com.technofreak.projetcv15.flicker.cachedb.FlickerPhoto
import com.technofreak.projetcv15.flicker.cachedb.FlickersDatabase
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.flicker.networking.loadPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * Repository for fetching devbyte videos from the network and storing them on disk
 */
class FlickerRepo(private val database: FlickersDatabase) {

    val dataSourceFactory =database.flickerPhotoDao.getPhotos()
    val flickerphotos=LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE).build()


    suspend fun searchImages(searchText:String) {
        withContext(Dispatchers.IO) {
            val photoList = loadPhotos(searchText)
            if (photoList.size != 0) {
                database.flickerPhotoDao.clearPhotos()
                database.flickerPhotoDao.insertAll(photoList)
            }
            else
            {
                Log.i("DDDD","NOTHING FETCHED")
            }
        }

    }


    companion object {
        private const val NETWORK_PAGE_SIZE = 50
        private const val DATABASE_PAGE_SIZE = 15
    }
}

