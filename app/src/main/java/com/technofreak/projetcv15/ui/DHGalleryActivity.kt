package com.technofreak.projetcv15.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.DHGalleryAdapter
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.utils.backPress
import com.technofreak.projetcv15.viewmodel.DHGalleryViewModel
import kotlinx.android.synthetic.main.activity_d_h_gallery.*

class DHGalleryActivity : AppCompatActivity() {
    private lateinit var viewModel: DHGalleryViewModel
    private lateinit var  dhGalleryAdapter:DHGalleryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d_h_gallery)
        supportActionBar!!.setTitle("DH Gallery")
        val navView: BottomNavigationView = nav_view
        navView.selectedItemId= R.id.dhgallery_menu
        navView.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            when (item)
            {
                R.id.gallery_menu ->     startActivity(Intent(this, MainActivity::class.java))
                R.id.camera_menu ->      startActivity(Intent(this, CameraActivity::class.java))
                R.id.flicker_menu ->   startActivity(Intent(this, FlickerActivity::class.java))
            }
           // ActivityCompat.finishAffinity(this)
            return@setOnNavigationItemSelectedListener true
        }
        viewModel= ViewModelProvider(this@DHGalleryActivity).get(DHGalleryViewModel::class.java)
        dhGalleryAdapter = DHGalleryAdapter()
        val gallery=gallery
        gallery.layoutManager = GridLayoutManager(this,2)
        gallery.adapter = dhGalleryAdapter

        gallery.addItemDecoration(
           SpaceItemDecoration(
               10,10,6,6
            )
        )

        viewModel.allPhotos.observe(this, Observer {
            setInfoText(it.size)
            dhGalleryAdapter.submitList(it)
        })

        dhGalleryAdapter.setOnClickListenerimage { pos->
                onClickImage(pos)
        }
        dhGalleryAdapter.setOnClickListenerplay{ uri->
           val intent = Intent(this, VideoPlayerAvtivity::class.java)
            intent.putExtra("uri", uri)
            startActivity(intent)
        }
        dhGalleryAdapter.setOnClickListenerlike{
                photoEntity->
            viewModel.updateLike(photoEntity.id,photoEntity.liked)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.liked_dh_menu, menu)
        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

      override fun onOptionsItemSelected(item: MenuItem?): Boolean {


          when(item!!.title) {
              "Liked" -> {
                              supportActionBar!!.setTitle("DH Gallery")
                              item.setIcon(R.drawable.ic_like_black)
                              item.title = "DH"
                              dhGalleryAdapter.submitList(null)
                              if( viewModel.likedPhotos.hasActiveObservers())
                                  viewModel.likedPhotos.removeObservers(this@DHGalleryActivity)

                              viewModel.allPhotos.observe(this@DHGalleryActivity, Observer {
                                  setInfoText(it.size)
                                  dhGalleryAdapter.submitList(it)
                                  dhGalleryAdapter.notifyDataSetChanged()
                                    })
                              dhGalleryAdapter.setOnClickListenerimage { pos ->
                                  onClickImage(pos)
                                     }
                          }
              "DH" -> {
                          supportActionBar!!.setTitle("DH Gallery")
                          item.setIcon(R.drawable.ic_stars_black_24dp)
                          item.title = "Liked"
                          dhGalleryAdapter.submitList(null)
                          if( viewModel.allPhotos.hasActiveObservers())
                                viewModel.allPhotos.removeObservers(this@DHGalleryActivity)

                          viewModel.likedPhotos.observe(this@DHGalleryActivity, Observer {
                              setInfoText(it.size,false)
                              dhGalleryAdapter.submitList(it)
                              dhGalleryAdapter.notifyDataSetChanged()
                                })
                          dhGalleryAdapter.setOnClickListenerimage {pos->
                              onClickImage(pos,true)
                                }
                    }
              "List" -> {
                          gallery.layoutManager = LinearLayoutManager(this)
                          item.title = "Grid"
                          item.setIcon(R.drawable.grid_view_24dp)
                       }
              "Grid" -> {
                          gallery.layoutManager = GridLayoutManager(this,2)
                          item.title = "List"
                          item.setIcon(R.drawable.list_view_24dp)
                        }
          }
        return super.onOptionsItemSelected(item)
    }

    fun onClickImage(pos:Int,like:Boolean=false)
    {
        val intent = Intent(this, ScreenSlidePagerActivity::class.java)
        intent.putExtra("dhgallery", true)
        intent.putExtra("position", pos)
        if(like)
            intent.putExtra("likeGallery", true)
        startActivity(intent)
    }

    fun setInfoText(size: Int, flag:Boolean=true)
    {
        if( size > 0)
            no_Image.visibility=View.GONE
        else{
            no_Image.visibility=View.VISIBLE
            if(flag)
                no_Image.setText(getString(R.string.camera_activity_info))
            else
                no_Image.setText(getString(R.string.liked_info))
        }




    }

    override fun onBackPressed() {
        backPress(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        supportActionBar!!.setTitle("DH Gallery")
        nav_view.selectedItemId= R.id.dhgallery_menu
    }
}




































