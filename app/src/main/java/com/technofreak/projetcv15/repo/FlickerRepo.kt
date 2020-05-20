
package com.technofreak.projetcv15.repo

import android.util.Log
import androidx.paging.LivePagedListBuilder
import com.technofreak.projetcv15.database.cachedb.FlickersDatabase
import com.technofreak.projetcv15.flicker.networking.loadPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class FlickerRepo(private val database: FlickersDatabase) {

    val dataSourceFactory =database.flickerPhotoDao.getPhotos()
    val flickerphotos=LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE).build()

    val stickers=database.flickerPhotoDao.getStickers()


    suspend fun reloadStickers() {
        withContext(Dispatchers.IO) {
            val stickerList = loadPhotos("logo")
            if (stickerList.size != 0) {
                database.flickerPhotoDao.clearPhotos()
                database.flickerPhotoDao.insertAll(stickerList)
                Log.i("DDDD"," FETCHINGG")
            }
            else
            {
                Log.i("DDDD","NOTHING FETCHED")
            }
        }
    }


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
        private const val DATABASE_PAGE_SIZE = 25
    }
}
