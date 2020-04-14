package com.technofreak.projetcv15

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.technofreak.projetcv15.camera.CameraActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technofreak.projetcv15.adapter.DHGalleryAdapter
import com.technofreak.projetcv15.camera.CameraActivity
import com.technofreak.projetcv15.flicker.FlickerActivity
import com.technofreak.projetcv15.liked.LikedActivity
import com.technofreak.projetcv15.utils.TopSpacingItemDecoration
import com.technofreak.projetcv15.utils.backPress
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.synthetic.main.activity_d_h_gallery.*

class DHGalleryActivity : AppCompatActivity() {
    private lateinit var viewModel: CameraActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d_h_gallery)
        supportActionBar!!.setTitle("DH Gallery")
        val navView: BottomNavigationView = nav_view
        navView.selectedItemId=R.id.dhgallery_menu
        navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            if(item==R.id.gallery_menu)
            {
                startActivity(Intent(this,MainActivity::class.java))
            }
            else if(item==R.id.camera_menu)
            {
                startActivity(Intent(this, CameraActivity::class.java))
                return@setOnNavigationItemSelectedListener true
            }
            else if(item==R.id.dhgallery_menu)
            {
                //startActivity(Intent(this,DHGalleryActivity::class.java))
            }
            else if(item==R.id.liked_menu)
            {
                startActivity(Intent(this, LikedActivity::class.java))
            }
            else if(item==R.id.flicker_menu)
            {
                startActivity(Intent(this, FlickerActivity::class.java))
            }
           // ActivityCompat.finishAffinity(this)
            return@setOnNavigationItemSelectedListener true

        }
        viewModel= ViewModelProvider(this).get(CameraActivityViewModel::class.java)
        val dhGalleryAdapter = DHGalleryAdapter()
        val gallery=gallery
        gallery.layoutManager = LinearLayoutManager(this)
        gallery.adapter = dhGalleryAdapter
        gallery.addItemDecoration(
           TopSpacingItemDecoration(
                10
            )
        )

        viewModel.allPhotos.observe(this, Observer { photos ->
            if (photos.size>0)
                no_Image.visibility=View.GONE
            dhGalleryAdapter.submitList(photos)
        })

        dhGalleryAdapter.setOnClickListenerimage { pos->
            val intent = Intent(this, ScreenSlidePagerActivity::class.java)
            intent.putExtra("dhgallery", true)
            intent.putExtra("position", pos)
            startActivity(intent)
        }
        dhGalleryAdapter.setOnClickListenerlike{
                photoEntity->
            viewModel.update(photoEntity)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu!!.findItem(R.id.change_layout).title="Grid"

        return super.onCreateOptionsMenu(menu)
    }

      override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.title == "List") {
            gallery.layoutManager = LinearLayoutManager(this)
            item.title = "Grid"
        }
        else{
            gallery.layoutManager = GridLayoutManager(this,3)
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
        supportActionBar!!.setTitle("DH Gallery")
        nav_view.selectedItemId=R.id.dhgallery_menu
    }
}
