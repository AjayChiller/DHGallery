package com.technofreak.projetcv15.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.technofreak.projetcv15.DHGalleryActivity
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.databinding.ActivityCameraBinding
import kotlinx.android.synthetic.main.activity_d_h_gallery.*
import kotlinx.android.synthetic.main.image_input_dialog.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors



class CameraActivity : AppCompatActivity() ,LifecycleOwner {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewFinder: TextureView
    private var lensMode = CameraX.LensFacing.BACK
    private lateinit var captureButton: ImageButton
    private lateinit var videoCapture: VideoCapture
    private lateinit var viewModel: DHGalleryViewModel
    private lateinit var imageCapture: ImageCapture
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
                startCamera()
            } else {
                camMode = true
                binding.cameraMode.background = getDrawable(R.drawable.ic_videocam_black_24dp)
                captureButton.setOnTouchListener(null)
                captureButton.setOnClickListener {
                    photoCapture()
                }
                startCamera()
            }
        }
        methodWithPermissions()

    }//end of oncreate



    private val executor = Executors.newSingleThreadExecutor()
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensMode)
        }.build()
        Log.i("Debug", "1")

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
        }
        CameraX.unbindAll()
        if (camMode) {
            val imageCaptureConfig = ImageCaptureConfig.Builder()
                .build()
            imageCapture = ImageCapture(imageCaptureConfig)
            Toast.makeText(this, "Photo Mode", Toast.LENGTH_SHORT).show()
            CameraX.bindToLifecycle(this, preview, imageCapture)
        } else {
            val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
                setTargetRotation(viewFinder.display.rotation)
            }.build()
            videoCapture = VideoCapture(videoCaptureConfig)
            Toast.makeText(this, "Video Mode", Toast.LENGTH_SHORT).show()
            CameraX.bindToLifecycle(this, preview, videoCapture)
        }
    }

    @SuppressLint("RestrictedApi")
    fun videoCapture(event: MotionEvent): Boolean {
        Log.i("DDDDx", "9")
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
                        Log.i("tag", "Video File : $file")
                        viewFinder.post {
                            get_Input(file)
                            Toast.makeText(baseContext, " " + file.name, Toast.LENGTH_SHORT).show()
                        }
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
            Toast.makeText(this, "STOPED", Toast.LENGTH_SHORT).show()
            Log.i("tag", "Video File stopped")
        }
        return false
    }


    fun photoCapture() {
        Log.i("DDDDx", "10")
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

    fun get_Input(image_file: File) {
        val customLayout = LayoutInflater.from(this).inflate(R.layout.image_input_dialog, null)
        val image_title: TextInputLayout = customLayout.title
        val image_tags: TextInputLayout = customLayout.tags
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(customLayout)
            .setPositiveButton("Submit") { dialogInterface, _ ->
                val title = image_title.editText!!.text.toString()
                val tags = image_tags.editText!!.text.toString()
                val uri = image_file.absolutePath
                val date = image_file.lastModified()
                val photoEntity = PhotoEntity(
                    0,
                    title,
                    date,
                    uri,
                    tags
                )
                Log.i("DDDD"," "+photoEntity.contentUri)
                viewModel.insert(photoEntity)
                dialogInterface.dismiss()
                Toast.makeText(this, title, Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                image_file.delete()
                dialogInterface.cancel()
            }
        builder.show()
        return
    }

    //Quick permission library
    fun methodWithPermissions() =
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
            viewFinder.post { startCamera() }
        }

    private fun bindCameraUseCases() {
        CameraX.unbindAll()
        viewFinder.post { startCamera() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent = Intent(this, DHGalleryActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        methodWithPermissions()
        startCamera()
    }

    companion object {
        private var camMode = true
    }


}