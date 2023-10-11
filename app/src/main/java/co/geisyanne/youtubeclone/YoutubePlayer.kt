package co.geisyanne.youtubeclone

import android.content.Context
import android.net.Uri
import android.os.Handler


import android.view.SurfaceHolder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class YoutubePlayer(private val context: Context) : SurfaceHolder.Callback {

    private var mediaPlayer: SimpleExoPlayer? = null
    var youtubePlayerListener: YoutubePlayerListener? = null
    private lateinit var runnable: Runnable
    private val handler = Handler()

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer?.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mediaPlayer == null) {
            mediaPlayer = SimpleExoPlayer.Builder(context).build()
            mediaPlayer?.setVideoSurfaceHolder(holder)
        }
    }

    fun setUrl(url: String) {
        mediaPlayer?.let {
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "utube")
            )

            val videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

            it.prepare(videoSource)
            it.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        trackTime()
                    }


                }

            })
            play()
        }
    }

    private fun trackTime() {
        mediaPlayer?.let {
            youtubePlayerListener?.onTrackTime(
                it.currentPosition,
                it.currentPosition * 100 / it.duration
            )
            if (it.isPlaying) {
                runnable = Runnable {
                    trackTime()
                }
                handler.postDelayed(runnable, 1000)
            }
        }
    }

    fun play() {
        mediaPlayer?.playWhenReady = true
    }

    fun pause() {
        mediaPlayer?.playWhenReady = false
    }

    fun release() {
        mediaPlayer?.release()
    }

    fun seek(progress: Long) {
        if (progress > 0) {
            mediaPlayer?.let {
                val seek: Long = progress * it.duration / 100
                it.seekTo(seek)
            }
        }
    }

    interface YoutubePlayerListener {
        fun onPrepared(duration: Int)
        fun onTrackTime(currentPosition: Long, percent: Long)
    }

}





