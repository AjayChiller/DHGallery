package com.technofreak.projetcv15.videoplayer

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class VideoPlayerViewModel  (application: Application) : AndroidViewModel(application) {
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    val context=application.baseContext

    var playbackPosition: Long = 0
    var currentWindow: Int = 0
    var playWhenReady = true
    public lateinit var player: SimpleExoPlayer
    var componentListener: ComponentListener? = null
     fun initializePlayer(uri: String) {
        if (uri.isEmpty()) {
            return
        }

        if (!::player.isInitialized) {
            // a factory to create an AdaptiveVideoTrackSelection
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
            player = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(getApplication()),
                DefaultTrackSelector(adaptiveTrackSelectionFactory),
                DefaultLoadControl()
            )

            player.addListener(componentListener)
            player.addAnalyticsListener(componentListener)
            player.addAnalyticsListener(componentListener)
        }



        player.playWhenReady = playWhenReady
        player.seekTo(currentWindow, playbackPosition)


        val firstSource = getMediaSource(uri)
        player.prepare(firstSource, true, false)
    }

    fun playerset()
    { player.setPlayWhenReady(true);
        player.getPlaybackState();

        player.seekTo(currentWindow, playbackPosition)
    }

    fun getMediaSource(uri : String):MediaSource{
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(getApplication(),"DGGallery"));
        return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource((Uri.parse(uri)))
    }

    fun isPlayerInitialized():Boolean
    {
        return !::player.isInitialized
    }

    fun releasePlayer() {
        playbackPosition = player.currentPosition
        currentWindow = player.currentWindowIndex
        playWhenReady = player.playWhenReady
        player.setPlayWhenReady(false)
        player.getPlaybackState()

            // player.removeListener(componentListener)
        //player.setVideoListener(null)
        //player.removeAnalyticsListener(componentListener)
        //player.removeAnalyticsListener(componentListener)
        //player.release()
        Log.i("DDDD","  "+playbackPosition)
    }





}