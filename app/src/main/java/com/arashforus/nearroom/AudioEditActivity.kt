package com.arashforus.nearroom

import android.content.ContentUris
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.arashforus.nearroom.databinding.ActivityAudioEditBinding
import com.bumptech.glide.Glide
import java.util.*

class AudioEditActivity : AppCompatActivity() {

    lateinit var binding : ActivityAudioEditBinding
    var path : String? = null
    var toUsername : String? = null
    var size : Int = 0
    var albumId : String? = null
    var songName : String? = null

    var mediaPlayer = MediaPlayer()

    val height = MyTools(this).screenWidth()

    private val seekTimer = Timer("seekAudio")
    private val seekDelay : Long = 10
    private val seekPeriod : Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        path = intent.getStringExtra("path")
        size = intent.getIntExtra("size",0)
        toUsername = intent.getStringExtra("toUsername")
        albumId = intent.getStringExtra("albumId")
        songName = intent.getStringExtra("songName")

        mediaPlayer.setDataSource(path)
        mediaPlayer.prepare()

        initToolbar()
        initView()
        initSeekBar()
        makeListeners()

    }

    override fun onStop() {
        super.onStop()
        seekTimer.cancel()
        seekTimer.purge()
        mediaPlayer.release()
    }

    private fun makeListeners() {
        binding.AudioEditActivityPlayButtonLayout.setOnClickListener {
            mediaPlayer.start()
            makeSeekHandler()
        }
        binding.AudioEditActivityPauseButtonLayout.setOnClickListener {
            if ( mediaPlayer.isPlaying ){
                mediaPlayer.pause()
                seekTimer.cancel()
            }
        }
        binding.AudioEditActivityStopButtonLayout.setOnClickListener {
            mediaPlayer.stop()
            seekTimer.cancel()
        }

        binding.AudioEditActivityCancelButton.setOnClickListener {
            onBackPressed()
        }
        binding.AudioEditActivitySendButton.setOnClickListener {
            val targetIntent = if (intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT) == "ROOM"){
                Intent(this,RoomActivity::class.java)
            }else{
                Intent(this,PrivateActivity::class.java)
            }
            targetIntent.putExtra("type","Audio")
            targetIntent.putExtra("fromActivity",intent.getStringExtra("fromActivity"))
            targetIntent.putExtra("toId",intent.getIntExtra("toId",0))
            targetIntent.putExtra("roomId",intent.getIntExtra("roomId",0))
            targetIntent.putExtra("path",path)
            //targetIntent.putExtra("size",path)
            targetIntent.putExtra("duration",(mediaPlayer.duration/1000).toString() )
            targetIntent.putExtra("purpose","SendAudio")
            startActivity(targetIntent)
            finish()
        }
    }

    private fun initSeekBar() {
        binding.AudioEditActivityCurrentTimeText.text = "00:00"
        mediaPlayer.setOnPreparedListener {
            binding.AudioEditActivityFullTimeText.text = MyTools(this).secondToFullTime(it.duration/1000)
            binding.AudioEditActivitySeekBar.max = it.duration/1000
        }

        binding.AudioEditActivitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if ( fromUser ){
                    mediaPlayer.seekTo(progress * 1000)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initView() {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val path = if ( albumId !== null ){
            ContentUris.withAppendedId(artworkUri, albumId!!.toLong())
        }else{
            null
        }
        Glide.with(this)
            .load(path)
            .placeholder(R.drawable.noimage)
            .error(R.drawable.noimage)
            .override(height / 2 )
            .centerCrop()
            .into(binding.AudioEditActivityImage)
        binding.AudioEditActivityName.setText(songName)
    }

    private fun initToolbar() {
        setSupportActionBar(binding.AudioEditActivityToolbar)
        supportActionBar?.title = "Send audio to $toUsername"
        supportActionBar?.subtitle = "Size : ${MyTools(this).getReadableSizeFromByte(size)}"
    }

    private fun makeSeekHandler() {
        seekTimer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    binding.AudioEditActivitySeekBar.progress = mediaPlayer.currentPosition / 1000
                    binding.AudioEditActivityCurrentTimeText.text =  MyTools(this@AudioEditActivity).secondToFullTime(mediaPlayer.currentPosition / 1000)
                }
            }

        },seekDelay,seekPeriod)
    }
}