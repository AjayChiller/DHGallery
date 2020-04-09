package com.technofreak.projetcv15.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.technofreak.projetcv15.repo.FetchImages.Companion.fetchInstance
import com.technofreak.projetcv15.repo.GalleryReop
import java.util.*
class MainActivityViewModel ( application: Application) : AndroidViewModel(application) {

    val galleryReop=GalleryReop(fetchInstance(application))
    val images=galleryReop.getImages()
    fun loadImages()=galleryReop.loadImages()

}


