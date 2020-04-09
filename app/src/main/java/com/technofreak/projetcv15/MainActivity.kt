package com.technofreak.projetcv15

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technofreak.projetcv15.adapter.GalleryAdapter
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.camera.CameraActivity
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.databinding.ActivityMainBinding
import com.technofreak.projetcv15.flicker.FlickerActivity
import com.technofreak.projetcv15.liked.LikedActivity


import com.technofreak.projetcv15.viewmodel.MainActivityViewModel
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity


const val PERMISSIONS_REQUEST_CODE = 10
val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET)
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //BOTTOM NAVIGATION
        binding.navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            if(item==R.id.gallery_menu)
            {
                startActivity(Intent(this,MainActivity::class.java))
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
            intent.putExtra("position", pos);
            startActivity(intent)
        }

        binding.gallery.layoutManager = GridLayoutManager(this,3)
        binding.gallery.adapter = galleryAdapter
        binding.gallery.addItemDecoration(
            SpaceItemDecoration(
                4
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

    override fun onResume() {
        super.onResume()
        Toast.makeText(this,"HII",Toast.LENGTH_SHORT).show()


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
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED  ) {

                    showImages()
                } else {
                    if( ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.INTERNET
                        )){
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
        this.toast("showimg")
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

        //Log.i("DDDD","CHECK EACH PERMISSION")
        for (permission in PERMISSIONS_REQUIRED) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }



    private fun haveStoragePermission() = PERMISSIONS_REQUIRED.all {

       // Log.i("DDDD","HAV STORAGE PERMISSION")
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
    Method to request permission.
     */
    private fun requestPermission() {

        //Log.i("DDDD","IN RP")
        if (!haveStoragePermission()) {

            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }



    inline fun Context.toast(message:String){
        Toast.makeText(this, message , Toast.LENGTH_SHORT).show()
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
}




