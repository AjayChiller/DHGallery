package com.technofreak.projetcv15.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.databinding.FragmentCameraBinding
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.ui.DHGalleryActivity
import com.technofreak.projetcv15.ui.PhotoEditorActivity
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.camera_ui.view.*
import java.io.File
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
    private var timeValue=5


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
            if (timeValue == 0)
                photoCapture()
            else
                startTimer()
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
            goToGallery()
        }
        if(autoSave==0)
            container.autosave.background=getDrawable(requireContext(),R.drawable.ic_autosaveoff_black_24dp)
        else

            container.autosave.background=getDrawable(requireContext(),R.drawable.autosaveon_24dp)
        container.autosave.setOnClickListener {
            updateAutoSaveUI(autoSave)
        }
        container.camera_mode.setOnClickListener { view->
            view.findNavController().navigate(R.id.action_cameraFragment_to_videoFragment)
        }
        timerChange()
        container.camera_timer.setOnClickListener{  timerChange()   }

    }



    @SuppressLint("RestrictedApi")
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
                .setTargetAspectRatio(ratio)
                .setTargetRotation(rotation)
                .build()





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

    /* private fun previewSavedImage(file :File) {
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

     */

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
        runWithPermissions(Manifest.permission.CAMERA) {
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

    private fun goToGallery()
    {
        val intent = Intent(requireContext(), DHGalleryActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun startTimer()
    {
        var rtime=timeValue
        container.camera_ui.visibility=View.GONE
        container.start_countdown.visibility=View.VISIBLE
        container.capture_button.visibility=View.GONE
        val startTimer = object: CountDownTimer((rtime*1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                container.start_countdown.text= rtime.toString()
                rtime--
            }
            override fun onFinish() {

                container.start_countdown.text=""
                container.start_countdown.visibility=View.GONE
                photoCapture()
                container.camera_ui.visibility=View.VISIBLE
                container.capture_button.visibility=View.VISIBLE

            }
        }
        startTimer.start()
    }


    private fun timerChange() {
       when(timeValue)
       {
           0    -> {
               timeValue = 3
               container.camera_timer.background =
                   AppCompatResources.getDrawable(requireContext(), R.drawable.ic_timer_black_24dp)
           }
           3    ->{
               timeValue = 5
               container.camera_timer.background =
                   AppCompatResources.getDrawable(requireContext(), R.drawable.ic_timer_black_24dp)
           }

           5    ->{
               timeValue = 0
               container.camera_timer.background =
                   AppCompatResources.getDrawable(requireContext(), R.drawable.ic_timer_off_black_24dp)
               container.timer_time.text=""
               return
           }

       }
        container.timer_time.text=timeValue.toString()+"s"
    }





}
