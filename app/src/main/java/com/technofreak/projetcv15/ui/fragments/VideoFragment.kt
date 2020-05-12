package com.technofreak.projetcv15.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.*
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.databinding.FragmentVideoBinding
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.ui.DHGalleryActivity
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import kotlinx.android.synthetic.main.camera_ui.view.*
import kotlinx.android.synthetic.main.fragment_video.view.*
import java.io.File
import java.util.concurrent.Executors


class VideoFragment : Fragment() {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var viewFinder: PreviewView
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var container: ConstraintLayout
    private lateinit var viewModel: DHGalleryViewModel
    private lateinit var videoCapture: VideoCapture
    private lateinit var sharedPref : SharedPreferences
    private var autoSave =0
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preview : Preview
    private lateinit var camera: Camera
    private var timetValue=15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DHGalleryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate<FragmentVideoBinding>(inflater,R.layout.fragment_video,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.preview_view)

        sharedPref=requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        autoSave = sharedPref.getInt("AUTO_SAVE", 0)

    }


    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        viewFinder.post {
            methodWithPermissions()
        }
    }

    private fun updateCameraUi() {
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(requireContext(), R.layout.camera_ui, container)

        controls.capture_button.setOnClickListener {
              startTimer()
        }

        container.findViewById<ImageButton>(R.id.switch_camera).setOnClickListener {
                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                // Re-bind use cases to update selected camera
                bindCameraUseCases()
            }

            container.findViewById<ImageButton>(R.id.view_images).setOnClickListener {
                goToGallery()
            }

            if (autoSave == 0)
                container.autosave.background = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_autosaveoff_black_24dp)
            else
                container.autosave.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.autosaveon_24dp)

            container.autosave.setOnClickListener {
                updateAutoSaveUI(autoSave)
            }
            container.camera_mode.background = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_camera_alt_black_24dp
            )
            container.camera_mode.setOnClickListener { view ->
                view.findNavController().navigate(R.id.action_videoFragment_to_cameraFragment)
            }
            container.camera_timer.background=AppCompatResources.getDrawable(requireContext(),R.drawable.ic_timer_black_24dp)
            timerChange()
        }


    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases() {
        viewFinder=binding.previewView

        val ratio= AspectRatio.RATIO_16_9

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
                .build()
            videoCapture = VideoCaptureConfig.Builder()
                .setTargetAspectRatio(ratio)
                .build()

            cameraProvider.unbindAll()
            try {
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,  videoCapture)
                // Attach the viewfinder's surface provider to preview use case
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))
            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }



    private fun startTimer()
    {
        var rtime=3
        container.camera_ui.visibility=View.GONE
        container.start_countdown.visibility=View.VISIBLE
        container.capture_button.setOnClickListener{    null    }
        val startTimer = object: CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                container.start_countdown.text= rtime.toString()
                rtime--
            }
            override fun onFinish() {
                container.start_countdown.text=""
                container.start_countdown.visibility=View.GONE
                videoCapture()
            }
        }
        startTimer.start()
    }

    private fun videoTimer(): CountDownTimer {
        var rtime=timetValue
        val videoTimer = object: CountDownTimer((rtime*1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.videoTimer.text= String.format("%02d:%02d", rtime / 60, rtime % 60)
                rtime--
            }
            @SuppressLint("RestrictedApi")
            override fun onFinish() {
                container.camera_ui.visibility=View.VISIBLE
                binding.videoTimer.text=""
                container.capture_button.background=null
                videoCapture.stopRecording()

                container.capture_button.setOnClickListener{
                    startTimer()
                }
                this.cancel()
            }
        }
        return videoTimer
    }


    @SuppressLint("RestrictedApi")
    private fun videoCapture() {
            val videoTimer=videoTimer()
            videoTimer.start()
            val videoFile = File(
                requireContext().externalCacheDir,
                "${System.currentTimeMillis()}.mp4")

            container.capture_button.background=
                AppCompatResources.getDrawable(requireContext(), R.drawable.circle_bg)

            container.capture_button.setOnClickListener{
                videoTimer.onFinish()
            }
                videoCapture.startRecording(videoFile,executor,object :VideoCapture.OnVideoSavedCallback{

                    override fun onVideoSaved(file: File) {
                    Log.i("DDDD","CAPTURED")
                    viewFinder.post {
                       // if(autoSave==0)
                       //     previewViewer()
                       //   else
                        autoSave(file)}
                    }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                   Log.i("DDDD","ERROR")
                }
            })
    }

    private fun previewViewer()
    {

    }

    private fun autoSave(videoFile:File){
        val file=videoFile
        val photoEntity = PhotoEntity(
            0,
            "Unamed",
            file.lastModified(),
            file.absolutePath
        )
        viewModel.insert(photoEntity)
        Toast.makeText(requireContext(),"SAVED",Toast.LENGTH_SHORT).show()
    }



    fun methodWithPermissions()=
        runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
            updateCameraUi()
            bindCameraUseCases()
        }




    private fun updateAutoSaveUI(_autosave: Int) {
        if (_autosave == 1)
        {
            autoSave=0
            Toast.makeText(requireContext(),"AUTO SAVE OFF", Toast.LENGTH_SHORT).show()
            container.autosave.background= AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_autosaveoff_black_24dp
            )
        }
        else
        {
            autoSave=1
            Toast.makeText(requireContext(),"AUTO SAVE ON", Toast.LENGTH_SHORT).show()
            container.autosave.background=
                AppCompatResources.getDrawable(requireContext(), R.drawable.autosaveon_24dp)

        }
        with(sharedPref.edit()) {
            putInt("AUTO_SAVE", autoSave)
            commit()
        }
    }

    private fun timerChange()
    {
        if(timetValue==15)
            timetValue=30
        else
            timetValue=15

        container.timer_time.text=timetValue.toString()+"s"
    }


    private fun goToGallery()
    {
        val intent = Intent(requireContext(), DHGalleryActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }



}

