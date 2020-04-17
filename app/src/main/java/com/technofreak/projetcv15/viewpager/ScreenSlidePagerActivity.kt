package com.technofreak.projetcv15.viewpager

import ZoomOutPageTransformer
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.ScreenSlidePagerAdapter
import com.technofreak.projetcv15.camera.CameraActivityViewModel
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.videoplayer.VideoPlayerAvtivity
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
         screenSlidePagerAdapter = ScreenSlidePagerAdapter(this)

        val isDHGallery = intent.getBooleanExtra("dhgallery", false)
        if (isDHGallery == true) {
            val dhgalleryviewModel: CameraActivityViewModel by viewModels()


            dhgalleryviewModel.allPhotos.observe(this, Observer<List<PhotoEntity>> { images ->
                screenSlidePagerAdapter.setItem(images)
                screenSlidePagerAdapter.notifyDataSetChanged()
            })
                viewPager.adapter = screenSlidePagerAdapter
                setInitialPos()
                var doubleClickLastTime = 0L
                screenSlidePagerAdapter!!.setOnClickListener { image ->
                    if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                        if (image.liked) {
                            image.liked = false
                            layout.toast_like_button.visibility = View.GONE
                            layout.toast_unlikelike_button.visibility = View.VISIBLE
                            toast.show()
                            layout.toast_unlikelike_button.startAnimation(toastout)
                        } else {
                            image.liked = true
                            layout.toast_like_button.visibility = View.VISIBLE
                            layout.toast_unlikelike_button.visibility = View.GONE
                            toast.show()
                            layout.toast_like_button.startAnimation(toastin)
                        }
                        doubleClickLastTime = 0
                        dhgalleryviewModel.update2(image.id, image.liked)
                    } else {
                        doubleClickLastTime = System.currentTimeMillis()
                    }
                }

            screenSlidePagerAdapter.setOnClickListenerplay {
                val intent = Intent(this, VideoPlayerAvtivity::class.java)
                intent.putExtra("uri", it)
                startActivity(intent)
            }

        }

        else {

            val galleryviewModel: MainActivityViewModel by viewModels()
            galleryviewModel.images.observe(this, Observer<List<PhotoEntity>> { images ->
                screenSlidePagerAdapter.setItem(images)
                screenSlidePagerAdapter.notifyDataSetChanged()
            })
            viewPager.adapter = screenSlidePagerAdapter
            setInitialPos()
           screenSlidePagerAdapter.setOnClickListenerplay {
                val intent = Intent(this, VideoPlayerAvtivity::class.java)
                intent.putExtra("uri", it)
                startActivity(intent)
            }

            screenSlidePagerAdapter.setOnClickListener { image ->
                Toast.makeText(this, ""+image.displayName, Toast.LENGTH_SHORT).show()
            }

        }

    }

    fun setInitialPos() {
        val pos: Int = intent.getIntExtra("position", 0)
        if (pos != 0) {
            viewPager.postDelayed({ viewPager.setCurrentItem(pos, false) }, 100)
        }
    }


    fun dbTapLike(imaage:PhotoEntity, pos:Int)
    {

    }
}
