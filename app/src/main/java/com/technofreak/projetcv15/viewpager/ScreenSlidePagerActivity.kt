package com.technofreak.projetcv15.viewpager

import ZoomOutPageTransformer
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.technofreak.projetcv15.camera.CameraActivityViewModel
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.ScreenSlidePagerAdapter
import com.technofreak.projetcv15.flicker.FlickerAcitvityViewModel
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_screen_slide.*
import kotlinx.android.synthetic.main.toast_like.view.*



class ScreenSlidePagerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.toast_like, (this as Activity).findViewById(R.id.toastlikecontainer))
        val toast=Toast(this)
        val toastin: Animation = AnimationUtils.loadAnimation(
            this,
            R.anim.toastin
        )
        val toastout: Animation = AnimationUtils.loadAnimation(
            this,
            R.anim.toastout
        )
        toast.duration=(Toast.LENGTH_SHORT)
        toast.view=layout
        toast.setGravity(Gravity.CENTER, 0, 0)
        viewPager = pager
        viewPager.setPageTransformer(ZoomOutPageTransformer())


         var screenSlidePagerAdapter:ScreenSlidePagerAdapter

        val isDHGallery = intent.getBooleanExtra("dhgallery", false)
        if (isDHGallery == true) {
            val dhgalleryviewModel: CameraActivityViewModel by viewModels()
            dhgalleryviewModel.allPhotos.observe(this, Observer<List<PhotoEntity>> { images ->
                screenSlidePagerAdapter = ScreenSlidePagerAdapter(this, images)
                viewPager.adapter = screenSlidePagerAdapter
                var doubleClickLastTime = 0L
                screenSlidePagerAdapter.setOnClickListener { image, pos ->
                    if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                        if (image.liked) {
                            //  Log.i("DDDD","Liked = "+ image.displayName)
                            image.liked = false
                            layout.toast_like_button.visibility = View.GONE
                            layout.toast_unlikelike_button.visibility = View.VISIBLE
                            toast.show()
                            layout.toast_unlikelike_button.startAnimation(toastout)

                        } else {
                            //     Log.i("DDDD","un Liked = "+ image.displayName)
                            image.liked = true
                            layout.toast_like_button.visibility = View.VISIBLE
                            layout.toast_unlikelike_button.visibility = View.GONE
                            toast.show()
                            layout.toast_like_button.startAnimation(toastin)
                        }
                        doubleClickLastTime = 0

                        Log.i("DDDD", "current pos" + viewPager.currentItem)
                        dhgalleryviewModel.update2(image.id, image.liked)


                    } else {
                        doubleClickLastTime = System.currentTimeMillis()
                    }
                }
            })
        }
        else if(intent.getBooleanExtra("flicker", false))
        {/*
            val flickerViewModel: FlickerAcitvityViewModel by viewModels()
            flickerViewModel.flickerPhotos.observe(this, Observer<List<PhotoEntity>> { images ->
                screenSlidePagerAdapter = ScreenSlidePagerAdapter(this, images)
                viewPager.adapter = screenSlidePagerAdapter
                screenSlidePagerAdapter.setOnClickListener { image, pos ->
                        Toast.makeText(this, ""+image.displayName, Toast.LENGTH_SHORT).show()
                }
            })
            */

        }
        else {
            val galleryviewModel: MainActivityViewModel by viewModels()
            galleryviewModel.images.observe(this, Observer<List<PhotoEntity>> { images ->
                screenSlidePagerAdapter = ScreenSlidePagerAdapter(this, images)
                viewPager.adapter = screenSlidePagerAdapter
                screenSlidePagerAdapter.setOnClickListener { image, pos ->
                    Toast.makeText(this, ""+image.displayName, Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    fun setInitialPos() {
        val pos: Int = intent.getIntExtra("position", 0)
        Log.i("DDDD", "SET INITIALPOS= " + pos)
        if (pos != 0) {
            viewPager.setCurrentItem(pos, false)
        }
    }

}
