package com.technofreak.projetcv15.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.GalleryAdapter
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.databinding.ActivityMainBinding


import com.technofreak.projetcv15.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.wait


const val PERMISSIONS_REQUEST_CODE = 10
val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var galleryAdapter:GalleryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.setTitle("Gallery")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //BOTTOM NAVIGATION
        nav_view.selectedItemId=R.id.gallery_menu
        binding.navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            when (item)
            {
                R.id.flicker_menu->     startActivity(Intent(this, FlickerActivity::class.java))
                R.id.camera_menu->      startActivity(Intent(this, CameraActivity::class.java))
                R.id.dhgallery_menu->   startActivity(Intent(this, DHGalleryActivity::class.java))
                R.id.editor_menu->      startActivity(Intent(this, PhotoEditorActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener true
        }

        galleryAdapter = GalleryAdapter ()
        galleryAdapter.setOnClickListener { _ ,pos->
            startViewPager(pos)
        }

        binding.gallery.layoutManager = GridLayoutManager(this,3)
        binding.gallery.adapter = galleryAdapter
        binding.gallery.addItemDecoration(
            SpaceItemDecoration(
                10,10,6,6
            )
        )

        viewModel.images.observe(this, Observer<List<PhotoEntity>> { images ->
            galleryAdapter.submitList(images)
        })


        binding.openAlbum.setOnClickListener { GetPermissions() }
        binding.grantPermissionButton.setOnClickListener { GetPermissions() }
        if (!haveStoragePermission()) {
            binding.welcomeView.visibility = View.VISIBLE

        } else {

            showImages()
        }
    }

    private fun startViewPager(pos: Int) {
        val intent = Intent(this, ScreenSlidePagerActivity::class.java)
        intent.putExtra("position", pos)
        startActivity(intent)
    }
    private fun startVideoPlayer(uri: String) {
        val intent = Intent(this, VideoPlayerAvtivity::class.java)
        intent.putExtra("uri", uri)
        startActivity(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE-> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                   ) {

                    showImages()
                } else {
                    if( ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                     ){
                        showNoAccess()
                    } else {
                        goToSettings()
                    }
                }
                return
            }
        }
    }
    private fun showImages() {
        viewModel.loadVideos()
        viewModel.loadImages()
        binding.navView.visibility=View.VISIBLE
        binding.welcomeView.visibility = View.GONE
        binding.permissionRationaleView.visibility = View.GONE
    }


    private fun showNoAccess() {
        binding.welcomeView.visibility = View.GONE
        binding.permissionRationaleView.visibility = View.VISIBLE
    }

    private fun GetPermissions() {
        if (arePermissionsEnabled()) {
            showImages()
        } else {
            requestPermission()
        }
    }

    private fun goToSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    /**
     * method to check if  permission
     * has been granted to the app.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun arePermissionsEnabled(): Boolean {
        for (permission in PERMISSIONS_REQUIRED) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun haveStoragePermission() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (!haveStoragePermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.photo_video_menu, menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        when(item!!.title)
        {
            "Video" ->  {


                           item.setIcon(R.drawable.ic_camera_alt_black_24dp)
                            item.title = "Photo"
                            viewModel.videos.observe(this, Observer {
                                galleryAdapter.submitList(it)
                                 })
                            galleryAdapter.setOnClickListener { uri ,_->
                                startVideoPlayer(uri)
                                 }

                        }
            "Photo" -> {

                            item.setIcon(R.drawable.ic_videocam_black_24dp)
                            item.title = "Video"
                            viewModel.images.observe(this, Observer{
                                galleryAdapter.submitList(it)
                                    })
                            galleryAdapter.setOnClickListener { _ ,pos->
                                 startViewPager(pos)
                                 }
                        }
            "List" -> {
                            binding.gallery.layoutManager = LinearLayoutManager(this)
                            item.title = "Grid"
                            item.setIcon(R.drawable.grid_view_24dp)
                        }
            "Grid" -> {
                            binding.gallery.layoutManager = GridLayoutManager(this,3)
                            item.title = "List"
                            item.setIcon(R.drawable.list_view_24dp)
                      }
        }
       return super.onOptionsItemSelected(item)
            }
    override fun onBackPressed() {
        val home = Intent(Intent.ACTION_MAIN)
        home.addCategory(Intent.CATEGORY_HOME)
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(home)
        finish()
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        supportActionBar!!.setTitle("Gallery")
        nav_view.selectedItemId=R.id.gallery_menu
    }
}




