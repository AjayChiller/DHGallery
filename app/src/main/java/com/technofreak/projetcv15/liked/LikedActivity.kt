package com.technofreak.projetcv15.liked

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technofreak.projetcv15.DHGalleryActivity
import com.technofreak.projetcv15.MainActivity
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.DHGalleryAdapter
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.camera.CameraActivity
import com.technofreak.projetcv15.flicker.FlickerActivity
import com.technofreak.projetcv15.utils.backPress
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.synthetic.main.activity_d_h_gallery.nav_view
import kotlinx.android.synthetic.main.activity_liked.*
import kotlinx.android.synthetic.main.activity_main.*


class LikedActivity : AppCompatActivity() {
    private lateinit var viewModel: DHGalleryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked)
        supportActionBar!!.setTitle("Liked")
        val navView: BottomNavigationView = nav_view
        navView.selectedItemId=R.id.liked_menu
        navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            when (item)
            {
                R.id.gallery_menu->     startActivity(Intent(this, MainActivity::class.java))
                R.id.camera_menu->      startActivity(Intent(this, CameraActivity::class.java))
                R.id.dhgallery_menu->   startActivity(Intent(this, DHGalleryActivity::class.java))
                R.id.flicker_menu->       startActivity(Intent(this, FlickerActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener true

        }

        viewModel= ViewModelProvider(this).get(DHGalleryViewModel::class.java)

        val likedAdapter=DHGalleryAdapter ()
        val gallery=galleryliked
        gallery.layoutManager = GridLayoutManager(this,3)
        gallery.adapter = likedAdapter
        gallery.addItemDecoration(
            SpaceItemDecoration(
                10,10,6,6
            )
        )


        viewModel.likedPhotos.observe(this, Observer { photos ->
            if(photos.size>0)
                no_Liked.visibility=View.GONE
            likedAdapter.submitList(photos)
        })

        likedAdapter.setOnClickListenerimage { pos->
            val intent = Intent(this, ScreenSlidePagerActivity::class.java)
            intent.putExtra("dhgallery", true)
            intent.putExtra("position", pos)
            startActivity(intent)
        }
        likedAdapter.setOnClickListenerlike { photoEntity ->
            viewModel.update(photoEntity)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.title == "List") {
            galleryliked.layoutManager = LinearLayoutManager(this)
            item.title = "Grid"
        }
        else{
            galleryliked.layoutManager = GridLayoutManager(this,3)
            item.title = "List"
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        backPress(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        supportActionBar!!.setTitle("Liked")
        nav_view.selectedItemId=R.id.liked_menu
    }
}