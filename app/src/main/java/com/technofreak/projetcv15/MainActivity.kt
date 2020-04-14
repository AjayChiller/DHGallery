package com.technofreak.projetcv15

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.technofreak.projetcv15.adapter.GalleryAdapter
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.camera.CameraActivity
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.databinding.ActivityMainBinding
import com.technofreak.projetcv15.flicker.FlickerActivity
import com.technofreak.projetcv15.liked.LikedActivity


import com.technofreak.projetcv15.viewmodel.MainActivityViewModel
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.synthetic.main.activity_main.nav_view


const val PERMISSIONS_REQUEST_CODE = 10
val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.setTitle("Gallery")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //BOTTOM NAVIGATION
        nav_view.selectedItemId=R.id.gallery_menu
        binding.navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            if(item==R.id.gallery_menu)
            {
                val intent=Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
             //   startActivity(intent)
            }
            else if(item==R.id.camera_menu)
            {
                startActivity(Intent(this,CameraActivity::class.java))

            }
            else if(item==R.id.dhgallery_menu)
            {
                startActivity(Intent(this,DHGalleryActivity::class.java))
            }
            else if(item==R.id.liked_menu)
            {
                startActivity(Intent(this,LikedActivity::class.java))
            }
            else if(item==R.id.flicker_menu)
            {
                startActivity(Intent(this,FlickerActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener true
        }

        val galleryAdapter = GalleryAdapter ()
        galleryAdapter.setOnClickListener { image ,pos->
            val intent = Intent(this, ScreenSlidePagerActivity::class.java)
            intent.putExtra("position", pos)
            startActivity(intent)
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

      //  Log.i("DDDD","IN SI")
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
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.title == "List") {
                    binding.gallery.layoutManager = LinearLayoutManager(this)
                    item.title = "Grid"
                }
            else{
                    binding.gallery.layoutManager = GridLayoutManager(this,3)
                    item.title = "List"
                }
        return super.onOptionsItemSelected(item)
            }
    override fun onBackPressed() {
        super.onBackPressed()
       val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(a)
        finish()
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i("DDDD","mainnnnnn")
        supportActionBar!!.setTitle("Gallery")
        nav_view.selectedItemId=R.id.gallery_menu
    }
}




