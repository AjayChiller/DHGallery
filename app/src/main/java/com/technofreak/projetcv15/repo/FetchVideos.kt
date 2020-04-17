package com.technofreak.projetcv15.repo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.technofreak.projetcv15.model.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FetchVideos"

class FetchVideos(val context : Context) {
    init{
        loadVideos()
    }

    private val _videos = MutableLiveData<List<PhotoEntity>>()
    val videos: LiveData<List<PhotoEntity>> get() = _videos


    fun loadVideos() {
        GlobalScope.launch {
            val videoList = queryVideos()
            _videos.postValue(videoList)
       }
    }

    private suspend fun queryVideos(): List<PhotoEntity> {
        val videos = mutableListOf<PhotoEntity>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN
            )



            val selection = null

            val selectionArgs = null

            val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
           context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {

                    // Here we'll use the column indexs that we found above.
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn)

                    //getting the actual uri by appending id to it
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()


                    val video = PhotoEntity(
                        id,
                        displayName,
                        dateTaken,
                        contentUri,
                        null
                    )
                    videos += video

                }
            }
        }

        return videos
    }


    companion object{
        private var  instance: FetchVideos? =null
        fun fetchVideoInstance(context: Context): FetchVideos {
            if (instance==null) {
                instance = FetchVideos(context)
            }
            return instance as FetchVideos
        }
    }

}


