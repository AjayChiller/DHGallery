package com.technofreak.projetcv15.repo

class GalleryReop (val fetchImages: FetchImages){

    fun getImages()=fetchImages.images
    fun loadImages()=fetchImages.loadImages()
}