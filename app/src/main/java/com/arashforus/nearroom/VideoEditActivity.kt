package com.arashforus.nearroom

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.arashforus.nearroom.databinding.ActivityVideoEditBinding
import com.video.trimmer.interfaces.OnCropVideoListener
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import java.util.*

class VideoEditActivity : AppCompatActivity() {

    lateinit var binding : ActivityVideoEditBinding
    var path : String? = null
    var toUsername : String? = null
    var size : Int = 0

    private var isTrimActive = false
    private var isCropActive = false
    private var isStarted = false
    private var isPlaying = false

    private val seekTimer = Timer("seekVideo")
    private val seekDelay : Long = 10
    private val seekPeriod : Long = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        path = intent.getStringExtra("path")
        size = intent.getIntExtra("size",0)
        toUsername = intent.getStringExtra("toUsername")

        initToolbar()
        setupVideoPlayer()
        setupVideoTrimmer()
        setupVideoCropper()

        makeListeners()
        makeSeekBar()

    }

    private fun makeSeekBar() {
        binding.VideoEditActivityVideoView.setOnPreparedListener {
            val fullLength = it.duration / 1000  // In sec
            binding.VideoEditActivitySeekBar.max = fullLength
            binding.VideoEditActivityFullTimeText.text = MyTools(this).secondToFullTime(fullLength )
            binding.VideoEditActivityCurrentTimeText.text = "00:00"
        }

        binding.VideoEditActivitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    binding.VideoEditActivityVideoView.seekTo(progress * 1000 )
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun makeListeners() {
        binding.VideoEditActivityCancelButton.setOnClickListener {
            onBackPressed()
        }

        binding.VideoEditActivitySendButon.setOnClickListener {
            if ( isTrimActive ){
                binding.VideoEditActivityVideoTrimmer.onSaveClicked()
            }else{
                if ( isCropActive ){
                    binding.VideoEditActivityVideoCropper.onSaveClicked()
                }else{
                    sendVideo()
                }
            }
        }

        binding.VideoEditActivityTrimLayout.setOnClickListener {
            makeView("Trim")
            isTrimActive = !isTrimActive
            isCropActive = false
        }

        binding.VideoEditActivityCropLayout.setOnClickListener {
            makeView("Crop")
            isCropActive = !isCropActive
            isTrimActive = false
        }

        binding.VideoEditActivityPlayButtonLayout.setOnClickListener {
            if ( !binding.VideoEditActivityVideoView.isPlaying ){
                if ( isStarted ){
                    binding.VideoEditActivityVideoView.start()
                }else{
                    binding.VideoEditActivityVideoView.start()
                    binding.VideoEditActivityVideoView.resume()
                }
                isStarted = true
                isPlaying = true
                makeSeekHandler()
            }

        }

        binding.VideoEditActivityPauseButtonLayout.setOnClickListener {
            if ( binding.VideoEditActivityVideoView.isPlaying ){
                binding.VideoEditActivityVideoView.pause()
                seekTimer.purge()
                //seekTimer.cancel()
                isPlaying = false
            }
        }

        binding.VideoEditActivityStopButtonLayout.setOnClickListener {
            if ( isStarted ){
                binding.VideoEditActivityVideoView.seekTo(50)
                binding.VideoEditActivityVideoView.stopPlayback()
                isStarted = false
                isPlaying = false
                seekTimer.purge()
                //seekTimer.cancel()
            }
        }

    }

    private fun sendVideo() {
        val targetIntent = if (intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT) == "ROOM"){
            Intent(this,RoomActivity::class.java)
        }else{
            Intent(this,PrivateActivity::class.java)
        }
        targetIntent.putExtra("type","Video")
        targetIntent.putExtra("fromActivity",intent.getStringExtra("fromActivity"))
        targetIntent.putExtra("toId",intent.getIntExtra("toId",0))
        targetIntent.putExtra("roomId",intent.getIntExtra("roomId",0))
        targetIntent.putExtra("path",path)
        targetIntent.putExtra("purpose","SendVideo")
        startActivity(targetIntent)
        finish()
    }

    private fun makeView(activity : String){
        val blackTint = ColorStateList.valueOf(Color.BLACK)
        val whiteTint = ColorStateList.valueOf(Color.WHITE)
        val whiteColor = Color.WHITE
        setDefaultView()
        when (activity.toUpperCase(Locale.ROOT) ){
            "CROP" -> {
                if ( isCropActive ){
                    binding.VideoEditActivityVideoView.visibility = View.VISIBLE
                    binding.VideoEditActivityVideoTrimmer.visibility = View.GONE
                    binding.VideoEditActivityVideoCropper.visibility = View.GONE
                    binding.VideoEditActivitySendButon.text = "Send"
                    binding.VideoEditActivityCropIcon.imageTintList = whiteTint
                    binding.VideoEditActivityCropText.setTextColor(whiteTint)
                    binding.VideoEditActivityCropLayout.setBackgroundColor(Color.TRANSPARENT)
                    binding.VideoEditActivitySeekBarLayout.visibility = View.VISIBLE
                    binding.VideoEditActivityVideoButtonsLayout.visibility = View.VISIBLE
                }else{
                    binding.VideoEditActivityVideoView.visibility = View.GONE
                    binding.VideoEditActivityVideoTrimmer.visibility = View.GONE
                    binding.VideoEditActivityVideoCropper.visibility = View.VISIBLE
                    binding.VideoEditActivitySendButon.text = "Crop"
                    binding.VideoEditActivityCropIcon.imageTintList = blackTint
                    binding.VideoEditActivityCropText.setTextColor(blackTint)
                    binding.VideoEditActivityCropLayout.setBackgroundColor(whiteColor)
                    binding.VideoEditActivitySeekBarLayout.visibility = View.GONE
                    binding.VideoEditActivityVideoButtonsLayout.visibility = View.GONE
                }
            }
            "TRIM" -> {
                if (isTrimActive) {
                    binding.VideoEditActivityVideoView.visibility = View.VISIBLE
                    binding.VideoEditActivityVideoTrimmer.visibility = View.GONE
                    binding.VideoEditActivityVideoCropper.visibility = View.GONE
                    binding.VideoEditActivitySendButon.text = "Send"
                    binding.VideoEditActivityTrimIcon.imageTintList = whiteTint
                    binding.VideoEditActivityTrimText.setTextColor(whiteTint)
                    binding.VideoEditActivityTrimLayout.setBackgroundColor(Color.TRANSPARENT)
                    binding.VideoEditActivitySeekBarLayout.visibility = View.VISIBLE
                    binding.VideoEditActivityVideoButtonsLayout.visibility = View.VISIBLE
                } else {
                    binding.VideoEditActivityVideoView.visibility = View.GONE
                    binding.VideoEditActivityVideoTrimmer.visibility = View.VISIBLE
                    binding.VideoEditActivityVideoCropper.visibility = View.GONE
                    binding.VideoEditActivitySendButon.text = "Trim"
                    binding.VideoEditActivityTrimIcon.imageTintList = blackTint
                    binding.VideoEditActivityTrimText.setTextColor(blackTint)
                    binding.VideoEditActivityTrimLayout.setBackgroundColor(whiteColor)
                    binding.VideoEditActivitySeekBarLayout.visibility = View.GONE
                    binding.VideoEditActivityVideoButtonsLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setDefaultView(){
        val whiteTint = ColorStateList.valueOf(Color.WHITE)
        binding.VideoEditActivityVideoTrimmer.visibility = View.GONE
        binding.VideoEditActivityVideoCropper.visibility =View.GONE
        binding.VideoEditActivityCropIcon.imageTintList = whiteTint
        binding.VideoEditActivityCropText.setTextColor(whiteTint)
        binding.VideoEditActivityCropLayout.setBackgroundColor(Color.TRANSPARENT)
        binding.VideoEditActivityTrimIcon.imageTintList = whiteTint
        binding.VideoEditActivityTrimText.setTextColor(whiteTint)
        binding.VideoEditActivityTrimLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setupVideoPlayer() {
        binding.VideoEditActivityVideoView.setVideoURI(Uri.parse(path))
        binding.VideoEditActivityVideoView.seekTo(1)
    }

    private fun setupVideoCropper() {
        binding.VideoEditActivityVideoCropper.setVideoURI(Uri.parse(path))
            .setOnCropVideoListener(onCropListener)
            .setMinMaxRatios(0.3f, 3f)
            .setDestinationPath(MyInternalStorage(this).getThumbVideoCropFolder())
    }

    private fun setupVideoTrimmer() {
        binding.VideoEditActivityVideoTrimmer
            .setOnTrimVideoListener(onTrimListener)
            .setOnVideoListener(onVideoListener)
            .setVideoURI(Uri.parse(path))
            .setVideoInformationVisibility(true)
            .setMaxDuration(600)
            .setMinDuration(2)
            .setDestinationPath(MyInternalStorage(this).getThumbVideoTrimFolder())
    }

    private fun initToolbar() {
        setSupportActionBar(binding.VideoEditActivityToolbar)
        supportActionBar?.title = "Send Video to $toUsername"
        supportActionBar?.subtitle = "Size : ${MyTools(this).getReadableSizeFromByte(size)}"
    }

    private val onTrimListener = object : OnTrimVideoListener {
        override fun cancelAction() {
        }
        override fun getResult(uri: Uri) {
            makeView("Trim")
            binding.VideoEditActivityVideoView.setVideoURI(Uri.parse(uri.path))
            binding.VideoEditActivityVideoView.seekTo(100)
        }
        override fun onError(message: String) {
        }
        override fun onTrimStarted() {
        }
    }

    private val onCropListener = object : OnCropVideoListener {
        override fun cancelAction() {
            makeView("Crop")
        }

        override fun getResult(uri: Uri) {
            //path = uri.path
            makeView("Crop")
            binding.VideoEditActivityVideoView.setVideoURI(Uri.parse(uri.path))
            binding.VideoEditActivityVideoView.seekTo(100)
        }
        override fun onCropStarted() {
        }
        override fun onError(message: String) {
        }
        override fun onProgress(progress: Float) {
        }
    }

    private val onVideoListener = object : OnVideoListener {
        override fun onVideoPrepared() {
        }
    }

    private fun makeSeekHandler() {
        seekTimer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    binding.VideoEditActivitySeekBar.progress = binding.VideoEditActivityVideoView.currentPosition / 1000
                    binding.VideoEditActivityCurrentTimeText.text =  MyTools(this@VideoEditActivity).secondToFullTime(binding.VideoEditActivityVideoView.currentPosition / 1000)
                }
            }

        },seekDelay,seekPeriod)
    }

}