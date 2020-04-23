package com.technofreak.projetcv15.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.databinding.ActivityCameraBinding
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.image_input_dialog.view.*
import java.io.File
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() ,LifecycleOwner {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewFinder: TextureView
    private var lensMode = CameraX.LensFacing.BACK
    private lateinit var captureButton: ImageButton
    private lateinit var videoCapture: VideoCapture
    private lateinit var viewModel: DHGalleryViewModel
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview:Preview
    private lateinit var sharedPref :SharedPreferences
    private lateinit var curentFile:File
    private var autoSave =0

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        viewModel = ViewModelProvider(this).get(DHGalleryViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        viewFinder = binding.viewFinder
        captureButton = binding.captureButton
        binding.viewImages.setOnClickListener {
            startActivity(Intent(this, DHGalleryActivity::class.java))
        }
        binding.switchCamera.setOnClickListener {
            if (lensMode == CameraX.LensFacing.BACK) {
                lensMode = CameraX.LensFacing.FRONT
            } else {
                lensMode = CameraX.LensFacing.BACK
            }
                bindCameraUseCases()
        }
        binding.captureButton.setOnClickListener() {
            photoCapture()
        }

        binding.cameraMode.setOnClickListener {

            if (camMode) {
                camMode = false
                binding.cameraMode.background = getDrawable(R.drawable.ic_camera_alt_black_24dp)
                captureButton.setOnClickListener(null)
                captureButton.setOnTouchListener { _, event ->
                    videoCapture(event)
                }
                viewFinder.post { startCamera() }
            } else {
                camMode = true
                binding.cameraMode.background = getDrawable(R.drawable.ic_videocam_black_24dp)
                captureButton.setOnTouchListener(null)
                captureButton.setOnClickListener {
                    photoCapture()
                }
                viewFinder.post { startCamera() }
            }
        }

        methodWithPermissions()


       sharedPref=this.getPreferences(Context.MODE_PRIVATE) ?: return
        autoSave = sharedPref.getInt("AUTO_SAVE", 0)
        if(autoSave==0)
            binding.autosave.background=getDrawable(R.drawable.ic_autosaveoff_black_24dp)
        else

            binding.autosave.background=getDrawable(R.drawable.autosaveon_24dp)
        binding.autosave.setOnClickListener {
            updateAutoSaveUI(autoSave)
        }



    }




    private val executor = Executors.newSingleThreadExecutor()
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    private fun startCamera() {
        val ratio = AspectRatio.RATIO_16_9
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensMode)
            setTargetAspectRatio(ratio)
        }.build()
        Log.i("Debug", "1")
        preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
        }
        CameraX.unbindAll()




        if (camMode) {
            val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
                setTargetAspectRatio(ratio)
                setTargetRotation(viewFinder.display.rotation)
                setLensFacing(lensMode)
            }.build()
            imageCapture = ImageCapture(imageCaptureConfig)

           // Toast.makeText(this, "Photo Mode", Toast.LENGTH_SHORT).show()
            binding.timerMode.text="Photo Mode"

            CameraX.bindToLifecycle(this, preview, imageCapture)
        } else {
            val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
                setTargetAspectRatio(ratio)
                setTargetRotation(viewFinder.display.rotation)
                setLensFacing(lensMode)

            }.build()
            videoCapture = VideoCapture(videoCaptureConfig)

          //  Toast.makeText(this, "Video Mode", Toast.LENGTH_SHORT).show()
            binding.timerMode.text="Video Mode"
            CameraX.bindToLifecycle(this, preview, videoCapture)
        }
            //CameraX.bindToLifecycle(this, preview,imageCapture)

    }

    fun changeCamMode()
    {
        CameraX.unbindAll()

    }

    @SuppressLint("RestrictedApi")
    fun videoCapture(event: MotionEvent): Boolean {
        binding.cameraUi.visibility=View.GONE
        val fileLoc = File(
            externalMediaDirs.first(),
            "${System.currentTimeMillis()}.mp4"
        )
        if (event.action == MotionEvent.ACTION_DOWN) {
            videoCapture.startRecording(
                fileLoc,
                executor,
                object : VideoCapture.OnVideoSavedListener {
                    override fun onVideoSaved(file: File) {
                        curentFile=file
                        viewFinder.post {
                            if(autoSave==0)
                                previewViewer()
                            else
                                autoSave()}
                    }

                    override fun onError(
                        videoCaptureError: VideoCapture.VideoCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        Log.i("tag", "Video Error: $message")
                    }
                })

        } else if (event.action == MotionEvent.ACTION_UP) {
            videoCapture.stopRecording()
            binding.cameraUi.visibility=ConstraintLayout.VISIBLE
        }
        return false
    }


    fun photoCapture() {
        val fileLoc = File(
            externalMediaDirs.first(),
            "${System.currentTimeMillis()}.jpg"
        )

        imageCapture.takePicture(fileLoc, executor,
            object : ImageCapture.OnImageSavedListener {
                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    exc: Throwable?
                )  {
                    val msg = "Photo capture failed: $message"
                    Log.e("CameraXApp", msg, exc)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(file: File) {
                    curentFile=file
                    viewFinder.post {
                        if(autoSave==0)
                          previewViewer()
                        else
                            autoSave()
                    }
                }
            })
    }

    fun previewViewer() {
        val file=curentFile
       binding.previewContainer.visibility=View.VISIBLE
        if(!camMode){
            binding.playButton.visibility = View.VISIBLE
            binding.playButton.setOnClickListener {
                binding.playButton.visibility = View.GONE
                val intent = Intent(this, VideoPlayerAvtivity::class.java)
                intent.putExtra("uri", file.absolutePath)
                startActivity(intent)
                openSaveDialog()
            }

        }
        else
            binding.playButton.visibility = View.GONE

        binding.previewImage.setImageURI(Uri.parse(file.absolutePath))
        Glide.with(binding.previewImage)
            .load(file.absolutePath)
            .into(binding.previewImage)
        binding.previewSave.setOnClickListener {
            openSaveDialog()
        }
        binding.previewClose.setOnClickListener {
            Log.i("DDDD","DELETED1")
            file.delete()
            binding.previewContainer.visibility=View.GONE
        }

        return
    }

    fun autoSave(){
        val file=curentFile
        val photoEntity = PhotoEntity(
            0,
            "Unamed",
            file.lastModified(),
            file.absolutePath
        )
        viewModel.insert(photoEntity)
    }



    fun openSaveDialog(){
        val file=curentFile
        val customLayout = LayoutInflater.from(this).inflate(R.layout.image_input_dialog, null)
        val image_title: TextInputLayout = customLayout.title
        val image_tags: TextInputLayout = customLayout.tags
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(customLayout)
            .setPositiveButton("Save") { dialogInterface, _ ->
                val title = image_title.editText!!.text.toString()
                val tags = image_tags.editText!!.text.toString()
                val uri = file.absolutePath
                val date = file.lastModified()
                val photoEntity = PhotoEntity(
                    0,
                    title,
                    date,
                    uri,
                    tags
                )
                viewModel.insert(photoEntity)
                dialogInterface.dismiss()
                binding.previewContainer.visibility=View.GONE


            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                Log.i("DDDD","DELETED2")
                file.delete()
                dialogInterface.cancel()
                binding.previewContainer.visibility=View.GONE

            }
        builder.show()
    }


    override fun onResume() {
        super.onResume()
        methodWithPermissions()
    }

    private fun updateAutoSaveUI(_autosave: Int) {
        if (_autosave == 1)
        {
            autoSave=0
            Toast.makeText(this,"AUTO SAVE OFF",Toast.LENGTH_SHORT).show()
            binding.autosave.background=getDrawable(R.drawable.ic_autosaveoff_black_24dp)
        }
        else
        {
            autoSave=1
            Toast.makeText(this,"AUTO SAVE ON",Toast.LENGTH_SHORT).show()
            binding.autosave.background=getDrawable(R.drawable.autosaveon_24dp)

        }
        with(sharedPref.edit()) {
            putInt("AUTO_SAVE", autoSave)
            commit()
        }

    }

    //Quick permission library
    fun methodWithPermissions() =
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
            viewFinder.post { startCamera() }
        }

    private fun bindCameraUseCases() {
    viewFinder.post { startCamera() }
    }

    override fun onBackPressed() {
        if(binding.previewContainer.visibility==View.GONE)
        {
            intent = Intent(this, DHGalleryActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        else {
            curentFile.delete()
            binding.previewContainer.visibility=View.GONE
        }
    }



    override fun onDestroy() {
        if(binding.previewContainer.visibility==View.VISIBLE) {
            curentFile.delete()
        }
        super.onDestroy()
    }

    companion object {
        private var camMode = true
    }



}
