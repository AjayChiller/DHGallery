package com.technofreak.projetcv15.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.AndroidViewModel
import com.technofreak.projetcv15.database.DhgalleryDatabase
import com.technofreak.projetcv15.database.cachedb.FlickerPhoto
import com.technofreak.projetcv15.database.cachedb.getStickerDatabase
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.repo.FlickerRepo
import com.technofreak.projetcv15.repo.PhotoDBRepository
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File


class PhotoEditorViewModel (application: Application) : AndroidViewModel(application) {
    val app = application
    private val flikerRepository = FlickerRepo(
        getStickerDatabase(
            application
        )
    )
    val stickersItem = flikerRepository.stickers
    lateinit var stickers:List<FlickerPhoto>
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val photoRepository: PhotoDBRepository

    lateinit var emoji: ArrayList<String>
    var currentText=""
    var colorCode= Color.BLACK
    var bgcolorCode= Color.WHITE

     lateinit var fileUri: Uri
     var OnTop = -1
     var isCaptured=false
     lateinit var photoFile: File
     var textFont: Typeface?=null
     var prevFilter=PhotoFilter.NONE
     var isLensFacingBack=true


    var multiFileList=ArrayList<Uri>()
    var isMultiPhoto=true

    var currentView: View?=null
    var currentUri:Uri?=null
    lateinit var rootView: ConstraintLayout


    val filterPair: MutableList<Pair<String, PhotoFilter>> =        java.util.ArrayList()

    init {
        val dhgalleryDatabaseDao =    DhgalleryDatabase.getDatabase(application).dhgalleryDatabaseDao()
        photoRepository = PhotoDBRepository(dhgalleryDatabaseDao)
        viewModelScope.launch {
            reloadStickers()
            getEmoji()
            setupFilters()
        }
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

    suspend private fun getEmoji() {
        emoji=PhotoEditor.getEmojis(app)
    }

    fun reloadStickers()
    {
        if (isonnected())
            viewModelScope.launch {
                flikerRepository.reloadStickers()
            }
        else
        {
            //   Toast.makeText(app,"No Network", Toast.LENGTH_SHORT).show()
        }
    }

    fun isonnected():Boolean{
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    suspend fun setupFilters()  {
        filterPair.add(
            Pair(
                "filters/original.jpg",
                PhotoFilter.NONE
            )
        )
        filterPair.add(
            Pair(
                "filters/auto_fix.png",
                PhotoFilter.AUTO_FIX
            )
        )
        filterPair.add(
            Pair(
                "filters/brightness.png",
                PhotoFilter.BRIGHTNESS
            )
        )
        filterPair.add(
            Pair(
                "filters/contrast.png",
                PhotoFilter.CONTRAST
            )
        )
        filterPair.add(
            Pair(
                "filters/document.png",
                PhotoFilter.DOCUMENTARY
            )
        )
        filterPair.add(
            Pair(
                "filters/dual_tone.png",
                PhotoFilter.DUE_TONE
            )
        )
        filterPair.add(
            Pair(
                "filters/fill_light.png",
                PhotoFilter.FILL_LIGHT
            )
        )

        filterPair.add(
            Pair(
                "filters/grain.png",
                PhotoFilter.GRAIN
            )
        )
        filterPair.add(
            Pair(
                "filters/gray_scale.png",
                PhotoFilter.GRAY_SCALE
            )
        )
        filterPair.add(
            Pair(
                "filters/lomish.png",
                PhotoFilter.LOMISH
            )
        )
        filterPair.add(
            Pair(
                "filters/negative.png",
                PhotoFilter.NEGATIVE
            )
        )
        filterPair.add(
            Pair(
                "filters/posterize.png",
                PhotoFilter.POSTERIZE
            )
        )
        filterPair.add(
            Pair(
                "filters/saturate.png",
                PhotoFilter.SATURATE
            )
        )
        filterPair.add(
            Pair(
                "filters/sepia.png",
                PhotoFilter.SEPIA
            )
        )
        filterPair.add(
            Pair(
                "filters/sharpen.png",
                PhotoFilter.SHARPEN
            )
        )
        filterPair.add(
            Pair(
                "filters/temprature.png",
                PhotoFilter.TEMPERATURE
            )
        )
        filterPair.add(
            Pair(
                "filters/tint.png",
                PhotoFilter.TINT
            )
        )
        filterPair.add(
            Pair(
                "filters/vignette.png",
                PhotoFilter.VIGNETTE
            )
        )
        filterPair.add(
            Pair(
                "filters/cross_process.png",
                PhotoFilter.CROSS_PROCESS
            )
        )
        filterPair.add(
            Pair(
                "filters/b_n_w.png",
                PhotoFilter.BLACK_WHITE
            )
        )

    }
}

