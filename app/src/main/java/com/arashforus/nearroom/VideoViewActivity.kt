package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityVideoViewBinding

class VideoViewActivity : AppCompatActivity() {

    lateinit var binding : ActivityVideoViewBinding
    lateinit var mediaController : android.widget.MediaController

    var title = ""
    var subtitle = ""
    var uri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if ( intent.hasExtra("title") ){
            title = intent.getStringExtra("title")!!
        }
        if ( intent.hasExtra("subtitle") ){
            subtitle = intent.getStringExtra("subtitle")!!
        }
        uri = intent.getStringExtra("uri")!!

        initToolbar()
        makeView()

    }

    private fun initToolbar() {
        setSupportActionBar(binding.VideoViewActivityToolbar)
        if ( title.isNotEmpty() ){
            supportActionBar?.title = title
        }
        if ( subtitle.isNotEmpty() ){
            supportActionBar?.subtitle = subtitle
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        mediaController = android.widget.MediaController(this)
        mediaController.setAnchorView(binding.VideoViewActivityVideoView)
        binding.VideoViewActivityVideoView.setMediaController(mediaController)

        if ( MyExternalStorage(this).isVideoAvailable(uri) ){
            binding.VideoViewActivityVideoView.setVideoURI(MyExternalStorage(this).getVideoUri(uri))
            binding.VideoViewActivityVideoView.start()
        }else{
            binding.VideoViewActivityProgressBar.visibility = View.VISIBLE
            binding.VideoViewActivityProgressText.visibility = View.VISIBLE
            AndroidNetworking.download( MyServerSide().getVideoUri(uri), MyExternalStorage(this).getVideoFolder(), uri)
                .setTag(uri)
                .setPriority(Priority.HIGH)
                .build()
                .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                    binding.VideoViewActivityProgressText.text = MyTools(this).getReadableSizeFromByte(totalBytes.toInt())
                    binding.VideoViewActivityProgressBar.max = totalBytes.toInt()
                    binding.VideoViewActivityProgressBar.progress = bytesDownloaded.toInt()
                }
                .startDownload(object : DownloadListener {
                    override fun onDownloadComplete() {
                        binding.VideoViewActivityProgressBar.visibility = View.GONE
                        binding.VideoViewActivityProgressText.visibility = View.GONE
                        binding.VideoViewActivityVideoView.setVideoURI(MyExternalStorage(this@VideoViewActivity).getVideoUri(uri))
                        binding.VideoViewActivityVideoView.start()
                    }
                    override fun onError(anError: ANError?) {
                        //println("download $logo  has error")
                    }
                })
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


}