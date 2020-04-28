package com.technofreak.projetcv15.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.exoplayer2.ui.PlayerView
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.utils.ComponentListener
import com.technofreak.projetcv15.viewmodel.VideoPlayerViewModel

class VideoPlayerAvtivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var mediaUri = ""
    private val viewModel: VideoPlayerViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player_avtivity)


        mediaUri = intent.getStringExtra("uri")

        viewModel.componentListener =
            ComponentListener()
        if(mediaUri.isEmpty())
        {
            Toast.makeText(this,"Video Cant be played",Toast.LENGTH_SHORT).show()
            Log.i("DDDDD"," ----emptyyyyyyy----" )
            onBackPressed()
        }else{
            viewModel.initializePlayer(mediaUri)
        }
    }


    public override fun onResume() {
        super.onResume()
        playerView = findViewById(R.id.video_view)
        hideSystemUi()
                viewModel.playerset()
        playerView?.player = viewModel.player

    }

    public override fun onPause() {
        super.onPause()
            viewModel.releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
            viewModel.releasePlayer()

    }
    


    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
            playerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

}
