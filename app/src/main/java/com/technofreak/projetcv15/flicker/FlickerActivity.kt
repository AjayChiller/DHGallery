package com.technofreak.projetcv15.flicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.DHGalleryActivity
import com.technofreak.projetcv15.MainActivity
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.FlickerAdapter
import com.technofreak.projetcv15.adapter.GalleryAdapter
import com.technofreak.projetcv15.camera.CameraActivity
import com.technofreak.projetcv15.flicker.cachedb.FlickerPhoto
import com.technofreak.projetcv15.liked.LikedActivity
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.utils.backPress
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.synthetic.main.activity_d_h_gallery.*
import kotlinx.android.synthetic.main.activity_flicker.*
import kotlinx.android.synthetic.main.activity_flicker.nav_view
import kotlinx.android.synthetic.main.flicker_image.*
import kotlinx.android.synthetic.main.gallery_layout.*

class FlickerActivity : AppCompatActivity() {
    private val viewModel: FlickerAcitvityViewModel by viewModels()

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flicker)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
        supportActionBar!!.setTitle("Flicker")
        nav_view.selectedItemId=R.id.flicker_menu
        nav_view.setOnNavigationItemSelectedListener() {
            val item=it.itemId
            when (item)
            {
                R.id.gallery_menu->     startActivity(Intent(this, MainActivity::class.java))
                R.id.camera_menu->      startActivity(Intent(this, CameraActivity::class.java))
                R.id.dhgallery_menu->   startActivity(Intent(this, DHGalleryActivity::class.java))
                R.id.liked_menu->       startActivity(Intent(this, LikedActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener true
        }


        val galleryAdapter = FlickerAdapter()

        galleryAdapter.setOnClickListener {
           zoomImageFromThumb(it.url)
        }
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        flickerRecyclerView.addItemDecoration(
            SpaceItemDecoration(
                10,10,6,6
            )
        )
        flickerRecyclerView.layoutManager=GridLayoutManager(this,3)
        flickerRecyclerView.adapter=galleryAdapter

        viewModel.flickerPhotos.observe(this, Observer<PagedList<FlickerPhoto>> { images ->
          if(images.size>0)
              search_photo.visibility=View.GONE
           galleryAdapter.submitList(images)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu,menu)
        menuInflater.inflate(R.menu.main_menu,menu)

        val searchItem: MenuItem = menu.findItem(R.id.action_search)
            val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
            searchView.setOnCloseListener(object : SearchView.OnCloseListener {
                override fun onClose(): Boolean {
                    return true
                }
            })
            val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            searchPlate.hint = "Search here"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
            Toast.makeText(applicationContext, query, Toast.LENGTH_SHORT).show()
                    viewModel.funsearchText(query!!)
                    return false
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            val searchManager =
                getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.title == "List") {
            flickerRecyclerView.layoutManager = LinearLayoutManager(this)
            item.title = "Grid"
        }
        else{
            flickerRecyclerView.layoutManager = GridLayoutManager(this,3)
            item.title = "List"
        }
        return super.onOptionsItemSelected(item)
    }


    private fun zoomImageFromThumb( imageResId: String) {
        currentAnimator?.cancel()

        val expandedImageView: ImageView = findViewById(R.id.expanded_image)

        Glide.with(expandedImageView)
            .load(imageResId)
            .into(expandedImageView)

        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        placeholder.getGlobalVisibleRect(startBoundsInt)
        findViewById<View>(R.id.container2)
            .getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        expandedImageView.visibility = View.VISIBLE

        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                expandedImageView,
                View.X,
                startBounds.left,
                finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, 0f, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, 0f, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, 0f))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, 0f))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
            backPress(this)
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i("DDDD","FLICKERRRR")
        supportActionBar!!.setTitle("Flicker")
        nav_view.selectedItemId=R.id.flicker_menu
    }
}


