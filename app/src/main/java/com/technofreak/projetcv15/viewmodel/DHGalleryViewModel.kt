package com.technofreak.projetcv15.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.technofreak.projetcv15.database.DhgalleryDatabase
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.repo.PhotoDBRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DHGalleryViewModel (application: Application) : AndroidViewModel(application) {
    val app=application
    private val photoRepository: PhotoDBRepository
    var allPhotos: LiveData<List<PhotoEntity>>
    var likedPhotos: LiveData<List<PhotoEntity>>

    init {
        val dhgalleryDatabaseDao = DhgalleryDatabase.getDatabase(application).dhgalleryDatabaseDao()
        photoRepository = PhotoDBRepository(dhgalleryDatabaseDao)
        allPhotos = photoRepository.allPhotos
        likedPhotos=photoRepository.likedPhotos

    }

    fun insert(photoEntity: PhotoEntity) = viewModelScope.launch(Dispatchers.IO) {
        try {
            photoRepository.insert(photoEntity)
        }
        catch (e:Exception)
        {
            Log.i("DDDD","INSERT FAILED")
        }
    }

    fun update(photoEntity: PhotoEntity) = viewModelScope.launch(Dispatchers.IO) {
        try {
            photoRepository.liketoggle(photoEntity)
        }
        catch (e:Exception)
        {
            Log.i("DDDD","UPDATE FAILED")
        }
    }

    fun update2(id:Long,like:Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            photoRepository.likeupdate(id,like)
        }
        catch (e:Exception)
        {
            Log.i("DDDD","UPDATE FAILED")
        }
    }


}