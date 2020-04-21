
package com.technofreak.projetcv15.flicker.networking

import com.technofreak.projetcv15.flicker.cachedb.FlickerPhoto

var method= "flickr.photos.search"
var api_key="b7578c6d6eeb50e755c8b2616cb2d91a"
var format="json"
var nojsoncallback=1
//Photoresponse to FlickerPhoto to add to db
suspend fun loadPhotos( searchText:String): List<FlickerPhoto> {
    val  searchResponse= FlickerApi.retrofitService.fetchImages(method, api_key,format, nojsoncallback,1,searchText)
    val photosList = searchResponse.photos.photo.map { photo ->
        FlickerPhoto(
            id = photo.id.toLong(),
            url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
            title = photo.title
        )
    }
    return(photosList)
}