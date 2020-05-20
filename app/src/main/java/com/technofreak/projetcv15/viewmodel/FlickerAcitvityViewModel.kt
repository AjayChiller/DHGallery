package com.technofreak.projetcv15.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.technofreak.projetcv15.database.cachedb.getDatabase
import com.technofreak.projetcv15.repo.FlickerRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
class FlickerAcitvityViewModel ( application: Application) : AndroidViewModel(application) {
    private val context=application


    private val flikerRepository = FlickerRepo(
        getDatabase(
            application
        )
    )

    val flickerPhotos = flikerRepository.flickerphotos

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    fun funsearchText(searchtext:String)
    {
        if (isonnected())
            viewModelScope.launch {
                flikerRepository.searchImages(searchtext)
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
