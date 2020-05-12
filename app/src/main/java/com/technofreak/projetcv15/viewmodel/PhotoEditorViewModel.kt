package com.technofreak.projetcv15.viewmodel

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.database.DhgalleryDatabase
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.repo.PhotoDBRepository
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.threadName
import java.io.File


class PhotoEditorViewModel (application: Application) : AndroidViewModel(application) {
    val app = application
    private val photoRepository: PhotoDBRepository
    val stickers=ArrayList<Bitmap>()
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


    val filterPair: MutableList<Pair<String, PhotoFilter>> =
        java.util.ArrayList()

    init {
        val dhgalleryDatabaseDao =
            DhgalleryDatabase.getDatabase(application).dhgalleryDatabaseDao()
        photoRepository = PhotoDBRepository(dhgalleryDatabaseDao)
        viewModelScope.launch {
            getStickers()
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


    suspend fun getStickers() {
       val asset:AssetManager=app.assets
        for( i in asset.list("stickers/")!!) {
            val ins = asset.open("stickers/" + i)
            val bitmap = BitmapFactory.decodeStream(ins)
            stickers.add(bitmap)
        }
    }


    suspend fun setupFilters()  {
        Log.i("DDDD","-------------------"+ Thread.currentThread())
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
                "filters/documentary.png",
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
                "filters/fish_eye.png",
                PhotoFilter.FISH_EYE
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
        filterPair.add(
            Pair(
                "filters/flip_horizental.png",
                PhotoFilter.FLIP_HORIZONTAL
            )
        )
        filterPair.add(
            Pair(
                "filters/flip_vertical.png",
                PhotoFilter.FLIP_VERTICAL
            )
        )
        filterPair.add(
            Pair(
                "filters/rotate.png",
                PhotoFilter.ROTATE
            )
        )
    }
}

