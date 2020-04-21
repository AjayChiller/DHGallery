package com.technofreak.projetcv15.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.technofreak.projetcv15.repo.FetchImages.Companion.fetchImageInstance
import com.technofreak.projetcv15.repo.FetchVideos
import com.technofreak.projetcv15.repo.GalleryReop
class MainActivityViewModel ( application: Application) : AndroidViewModel(application) {

    val galleryReop=GalleryReop(fetchImageInstance(application), FetchVideos.fetchVideoInstance(application))
    val images=galleryReop.getImages()
    fun loadImages()=galleryReop.loadImages()
    val videos=galleryReop.getVideos()
    fun loadVideos()=galleryReop.loadVideos()



}


