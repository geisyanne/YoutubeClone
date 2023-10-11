package co.geisyanne.youtubeclone

import android.os.Bundle
import android.view.Menu
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.geisyanne.youtubeclone.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var viewLayer: View
    private lateinit var youtubePlayer: YoutubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        val videos = mutableListOf<Video>()
        videoAdapter = VideoAdapter(videos) { video ->
            showOverlayView(video)
        }

        viewLayer = findViewById(R.id.view_layer)
        viewLayer.alpha = 0f

        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.adapter = videoAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val res = async { getVideo() }
            val listVideo = res.await()
            withContext(Dispatchers.Main) {
                listVideo?.let {
                    videos.clear()
                    videos.addAll(listVideo.data)
                    videoAdapter.notifyDataSetChanged()
                    binding.motionContainer.removeView(binding.progressRecycler)
                    //binding.progressRecycler.visibility = View.GONE
                }
            }
        }

        findViewById<SeekBar>(R.id.seek_bar_player).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    youtubePlayer.seek(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        preparePlayer()

    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayer.release()
    }

    override fun onPause() {
        super.onPause()
        youtubePlayer.pause()
    }

    private fun preparePlayer() {
        youtubePlayer = YoutubePlayer(this)
        youtubePlayer.youtubePlayerListener = object : YoutubePlayer.YoutubePlayerListener {
            override fun onPrepared(duration: Int) {

            }

            override fun onTrackTime(currentPosition: Long, percent: Long) {
                findViewById<SeekBar>(R.id.seek_bar_player).progress = percent.toInt()
                findViewById<TextView>(R.id.current_time_player).text = currentPosition.formatTime()

            }
        }

        findViewById<SurfaceView>(R.id.surface_player).holder.addCallback(youtubePlayer)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showOverlayView(video: Video) {
        viewLayer.animate().apply {
            duration = 400
            alpha(0.5f)
        }

        binding.motionContainer.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {

                if (progress > 0.5f) viewLayer.alpha = 1.0f - progress
                else viewLayer.alpha = 0.5f

            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {

            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {

            }

        })

        findViewById<ImageView>(R.id.video_player).visibility = View.GONE
        youtubePlayer.setUrl(video.videoUrl)

        val videoSimilarAdapter = VideoSimilarAdapter(videos())
        val rvSimilar = findViewById<RecyclerView>(R.id.content_rv_similar)
        rvSimilar.layoutManager = LinearLayoutManager(this)
        rvSimilar.adapter = videoSimilarAdapter

        findViewById<TextView>(R.id.content_channel).text = video.publisher.name
        findViewById<TextView>(R.id.content_title).text = video.title
        Picasso.get().load(video.publisher.pictureProfileUrl)
            .into(findViewById<ImageView>(R.id.img_channel))

        videoSimilarAdapter.notifyDataSetChanged()

    }

    private fun getVideo(): ListVideo? {
        val client = OkHttpClient.Builder()
            .build()

        val request = Request.Builder()
            .get()
            .url("https://tiagoaguiar.co/api/youtube-videos")
            .build()

        return try {
            val respose = client.newCall(request).execute()

            if (respose.isSuccessful) {
                GsonBuilder().create()
                    .fromJson(respose.body()?.string(), ListVideo::class.java)
            } else {
                null
            }

        } catch (e: Exception) {
            null
        }


    }

}