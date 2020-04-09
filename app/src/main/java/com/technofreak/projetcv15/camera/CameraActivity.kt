package com.technofreak.projetcv15.camera

import android.content.Intent
import com.technofreak.projetcv15.R
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.technofreak.projetcv15.DHGalleryActivity
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.databinding.ActivityCameraBinding
import java.io.File
import java.util.concurrent.Executors



class CameraActivity : AppCompatActivity() ,LifecycleOwner{
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewFinder: TextureView
    private   lateinit var captureButton: ImageButton
    private lateinit var viewModel: CameraActivityViewModel
    private  var lensMode=CameraX.LensFacing.BACK
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        viewModel= ViewModelProvider(this).get(CameraActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        viewFinder = binding.viewFinder
        captureButton = binding.captureButton
        viewFinder.post { startCamera() }
        display()
        binding.imageView.setOnClickListener{
            startActivity(Intent(this,DHGalleryActivity::class.java))
        }
        binding.switchCamera.setOnClickListener{
            if(lensMode == CameraX.LensFacing.BACK) {
                lensMode = CameraX.LensFacing.FRONT

            }
            else
            {
                lensMode=CameraX.LensFacing.BACK

            }
            try {
                // Only bind use cases if we can query a camera with this orientation

                bindCameraUseCases()
            } catch (exc: Exception) {
                // Do nothing
            }
        }



    }//end of oncreate

    private fun bindCameraUseCases() {

        CameraX.unbindAll()


        startCamera()    }


    fun display()
    {
        viewModel.allPhotos.observe(this, Observer { photos ->
            // Update the cached copy of the words in the adapter.
            for(i in photos) {
                Log.i(
                    "DDDD SQL",
              //      "id = " + i.id + "---name = " + i.displayName + "---DateTaken = " + i.dateTaken + "---tags = " + i.tags + "---liked = " + i.liked
                    "id = " + i.id + "---name = " + i.displayName +  "---liked = " + i.liked
                )
            }
            })
        viewModel.likedPhotos.observe(this, Observer { photos ->
            // Update the cached copy of the words in the adapter.
            for(i in photos) {
                Log.i(
                    "DDDD  LIKED",
                    //      "id = " + i.id + "---name = " + i.displayName + "---DateTaken = " + i.dateTaken + "---tags = " + i.tags + "---liked = " + i.liked
                    "id = " + i.id + "---name = " + i.displayName +  "---liked = " + i.liked
                )
            }
        })
    }
    private val executor = Executors.newSingleThreadExecutor()

    private fun startCamera() {

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensMode)

        }.build()
        Log.i("Debug","1")

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture

        }


        // viewFinder.surfaceTexture = it.surfaceTexture
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        binding.captureButton.setOnClickListener {
            val file = File(externalMediaDirs.first(),
                "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(

                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        exc: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Log.e("CameraXApp", msg, exc)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }



                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("CameraXApp", msg)

                        viewFinder.post {
                            get_Input(file)
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

/*
    fun get_Input(image_file : File) {
        Log.i("DDDD", "Got")
        val title = "test"
        val tags = "tag1,tag2,tag3,testing"
        val uri = image_file.absolutePath
        val date = image_file.lastModified()
        val photoEntity = PhotoEntity(
            0,
            title,
            date,
            uri,
            tags
        )
        viewModel.insert(photoEntity)
    }
*/


    fun get_Input(image_file : File) {
        val customLayout = LayoutInflater.from(this).inflate(R.layout.image_input_dialog, null)
        val image_title: TextInputLayout = customLayout.findViewById(R.id.title)
        val image_tags: TextInputLayout = customLayout.findViewById(R.id.tags)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(customLayout)
            .setPositiveButton("Submit") { dialogInterface, _ ->
                val title =image_title.editText!!.text.toString()
                val tags = image_tags.editText!!.text.toString()
                val uri=image_file.absolutePath
                val date=image_file.lastModified()
                val photoEntity= PhotoEntity(
                    0,
                    title,
                    date,
                    uri,
                    tags
                )
                viewModel.insert(photoEntity)
                dialogInterface.dismiss()

                Toast.makeText(this, title, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                image_file.delete()
                dialogInterface.cancel() }
        builder.show()

        return
    }


}





