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

private const val TAG = "FetchImages"

class FetchImages(val context : Context) {

    private val _images = MutableLiveData<List<PhotoEntity>>()
    val images: LiveData<List<PhotoEntity>> get() = _images


    fun loadImages() {
        GlobalScope.launch {
            val imageList = queryImages()
            _images.postValue(imageList)
       }
    }

    private suspend fun queryImages(): List<PhotoEntity> {
        val images = mutableListOf<PhotoEntity>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
            )


            val selection = null

            val selectionArgs = null

            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
           context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {

                    // Here we'll use the column indexs that we found above.
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val displayName = cursor.getString(displayNameColumn)

                    //getting the actual uri by appending id to it
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()


                    val image = PhotoEntity(
                        id,
                        displayName,
                        dateTaken,
                        contentUri,
                        null
                    )
                    images += image

                }
            }
        }

        return images
    }


    companion object{
        private var  instance: FetchImages? =null
        fun fetchInstance(context: Context): FetchImages {
            if (instance==null) {
                instance = FetchImages(context)
            }
            return instance as FetchImages
        }
    }

}


