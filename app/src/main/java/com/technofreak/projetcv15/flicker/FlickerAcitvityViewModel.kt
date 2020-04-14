package com.technofreak.projetcv15.flicker

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast
import androidx.lifecycle.*
import com.technofreak.projetcv15.flicker.cachedb.getDatabase
import com.technofreak.projetcv15.repo.FlickerRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
class FlickerAcitvityViewModel ( application: Application) : AndroidViewModel(application) {
    private val context=application


    private val videosRepository = FlickerRepo(getDatabase(application))

    val flickerPhotos = videosRepository.flickerphotos

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {

    }

  fun funsearchText(searchtext:String)
    {
        if (isonnected())
          viewModelScope.launch {
            videosRepository.searchImages(searchtext)
        }
        else
        {
            Toast.makeText(context,"No Network",Toast.LENGTH_SHORT).show()
        }
    }

    fun isonnected():Boolean{
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }




    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }





























}

