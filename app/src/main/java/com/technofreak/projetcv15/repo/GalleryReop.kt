package com.technofreak.projetcv15.repo

class GalleryReop (val fetchImages: FetchImages, val fetchVideos: FetchVideos){

    fun getImages()=fetchImages.images
    fun loadImages()=fetchImages.loadImages()
    fun getVideos()=fetchVideos.videos
    fun loadVideos()=fetchVideos.loadVideos()

}