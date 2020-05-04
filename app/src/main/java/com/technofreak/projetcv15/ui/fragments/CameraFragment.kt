package com.technofreak.projetcv15.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.icu.util.TimeUnit
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.camera.core.*
import androidx.camera.core.impl.ImageAnalysisConfig
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.databinding.FragmentCameraBinding
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.ui.DHGalleryActivity
import com.technofreak.projetcv15.ui.PhotoEditorActivity
import com.technofreak.projetcv15.ui.VideoPlayerAvtivity
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.camera_ui.view.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class CameraFragment : Fragment(),LifecycleOwner{
    private lateinit var binding: FragmentCameraBinding
    private lateinit var previewView: PreviewView
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var container: ConstraintLayout
    private lateinit var viewModel: DHGalleryViewModel
    private lateinit var imageCapture: ImageCapture
    private var displayId: Int = -1
    private lateinit var sharedPref : SharedPreferences
    private var autoSave =0
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preview : Preview
    private lateinit var camera:Camera


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate<FragmentCameraBinding>(inflater,R.layout.fragment_camera,container,false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DHGalleryViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout

        previewView = container.findViewById(R.id.preview_view)
        displayManager.registerDisplayListener(displayListener, null)

        previewView.post {

            // Keep track of the display in which this view is attached
            displayId = previewView.display.displayId
            // Build UI controls
            methodWithPermissions()
        }

        sharedPref=requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        autoSave = sharedPref.getInt("AUTO_SAVE", 0)
    }

    private fun updateCameraUi() {
        // Remove previous UI if any
           container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(requireContext(), R.layout.camera_ui, container)

        controls.capture_button.setOnClickListener {
            photoCapture()
        }

        controls.findViewById<ImageButton>(R.id.switch_camera).setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            // Re-bind use cases to update selected camera
            bindCameraUseCases()
        }

        controls.findViewById<ImageButton>(R.id.view_images).setOnClickListener {
            startActivity(Intent(requireContext(), DHGalleryActivity::class.java))
        }
        if(autoSave==0)
            container.autosave.background=getDrawable(requireContext(),R.drawable.ic_autosaveoff_black_24dp)
        else

            container.autosave.background=getDrawable(requireContext(),R.drawable.autosaveon_24dp)
        container.autosave.setOnClickListener {
            updateAutoSaveUI(autoSave)
        }

        }

    private fun bindCameraUseCases() {
        previewView=binding.previewView

        val ratio=AspectRatio.RATIO_16_9

        val rotation = previewView.display.rotation
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(ratio)
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                .setTargetAspectRatio(ratio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()


            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(ratio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->

            })





            cameraProvider.unbindAll()

            // Choose the camera by requiring a lens facing

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

                // Attach the viewfinder's surface provider to preview use case
                preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
            } catch(exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun photoCapture() {


        imageCapture.let { imageCapture ->


            // Create output file to hold the image
            val photoFile = File(
                requireContext().externalCacheDir,
                "${System.currentTimeMillis()}.jpg")
            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Log.i("DDDD","savedlocation "+photoFile.absolutePath)
                        savePhoto(output,photoFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.i("DDDD","DDDDDDDDD"+exception)
                        TODO("Not yet implemented")
                    }
                })

        }

    }

        private fun savePhoto(output: ImageCapture.OutputFileResults,file:File)
         {
             val savedUri = output.savedUri ?: Uri.fromFile(file)
             // so if you only target API level 24+ you can remove this statement
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                 requireActivity().sendBroadcast(
                     Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                 )
             }
             // We can only change the foreground Drawable using API level 23+ API
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 // Display flash animation to indicate that photo was captured
                 container.postDelayed({
                     container.foreground = ColorDrawable(Color.WHITE)
                     container.postDelayed(
                         { container.foreground = null }, 50L)
                 }, 100L)
         }
             if(autoSave==0)
                 //previewView.post{    previewSavedImage(photoFile)}
                startEditor(file.absolutePath)
             else
                 autoSaveImage(file)
    }

    private fun previewSavedImage(file :File) {
       container.preview_container.visibility=View.VISIBLE

        container.preview_image.setImageURI(Uri.parse(file.absolutePath))

        container.preview_save.setOnClickListener {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)

            val title = container.title.editText!!.text.toString()
            val tags = container.tags.editText!!.text.toString()
            if (title == "") {
                container.title.editText!!.setHint("Enter title")
            } else {
                val photoEntity = PhotoEntity(
                    0,
                    title,
                    file.lastModified(),
                    file.absolutePath,
                    tags
                )
                viewModel.insert(photoEntity)
                container.preview_container.visibility = View.GONE
            }
        }
        container.preview_close.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
            file.delete()
            container.preview_container.visibility=View.GONE
        }

    }

    private fun autoSaveImage(file:File)
    {
        val compressor= Compressor(requireContext())
        compressor.setDestinationDirectoryPath(requireContext().externalMediaDirs.first().absolutePath)

        val photoFile=  compressor.compressToFile(file)
        Log.i("DDDD","COMPRESSED AND SAVED TO "+photoFile.absolutePath)
        val photoEntity = PhotoEntity(
            0,
            "Unamed",
            photoFile.lastModified(),
            photoFile.absolutePath
        )
        viewModel.insert(photoEntity)
        file.delete()
    }





    fun methodWithPermissions()=
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
            updateCameraUi()
            // Bind use cases
               bindCameraUseCases()
        }


    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Log.d("TAG", "Rotation changed: ${view.display.rotation}")
                imageCapture.targetRotation = view.display.rotation
            }
        } ?: Unit
    }


    private fun updateAutoSaveUI(_autosave: Int) {
        if (_autosave == 1)
        {
            autoSave=0
            Toast.makeText(requireContext(),"AUTO SAVE OFF",Toast.LENGTH_SHORT).show()
            container.autosave.background=getDrawable(requireContext(),R.drawable.ic_autosaveoff_black_24dp)
        }
        else
        {
            autoSave=1
            Toast.makeText(requireContext(),"AUTO SAVE ON",Toast.LENGTH_SHORT).show()
            container.autosave.background=getDrawable(requireContext(),R.drawable.autosaveon_24dp)

        }
        with(sharedPref.edit()) {
            putInt("AUTO_SAVE", autoSave)
            commit()
        }
    }



    private fun startEditor(uri:String)
    {
        val intent = Intent(requireContext(),PhotoEditorActivity::class.java)
        intent.putExtra("uri", uri)
        startActivity(intent)
    }




}
