package com.arashforus.nearroom

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashforus.nearroom.databinding.ActivityGalleyAlbumBinding
import java.util.*

class GalleryAlbumActivity : AppCompatActivity() {

    lateinit var binding : ActivityGalleyAlbumBinding
    private val ReadExternalStoragePermissionCode = 9646
    private val WriteExternalStoragePermissionCode = 9647
    var title : String? = null
    var subtitle : String? = null
    var type : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleyAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = intent.getStringExtra("title")
        subtitle = intent.getStringExtra("subtitle")
        type = intent.getStringExtra("type")

        initToolbar()
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, ReadExternalStoragePermissionCode)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WriteExternalStoragePermissionCode)


    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this,arrayOf(permission), requestCode)
        } else {
            loadAlbums()
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == ReadExternalStoragePermissionCode ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                loadAlbums()
            }else{
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, ReadExternalStoragePermissionCode)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadAlbums(){
        val albums = when (type?.toUpperCase(Locale.ROOT)?.trim()  ){
            "PICTURE" -> {
                MyGalleryTools(this).getPictureAlbums()
            }
            "VIDEO" -> {
                MyGalleryTools(this).getVideosAlbums()
            }
            "AUDIO" -> {
                MyGalleryTools(this).getAudiosAlbums()
            }
            else -> {
                MyGalleryTools(this).getPictureAlbums()
            }
        }
        //val albums = MyGalleryTools(this).getPictureAlbums()
        //println(albums.toString())
        val adapter = Adapter_Albums(albums)

        binding.GalleryAlbumActivityRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.GalleryAlbumActivityRecyclerView.setHasFixedSize(true)
        binding.GalleryAlbumActivityRecyclerView.adapter = adapter
        binding.GalleryAlbumActivityRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //println("onScrolled")
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    //RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                    //AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL, AbsListView.OnScrollListener.SCROLL_STATE_FLING -> glide.pauseRequests()
                }
            }

        })

    }

    private fun initToolbar(){
        setSupportActionBar(binding.GalleryAlbumActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        println("title is $title and subtitle is $subtitle")
        if ( title == null || title == "" ){
            supportActionBar?.title = "Albums"
        }else{
            supportActionBar?.title = title
        }
        if ( subtitle !== null && subtitle !== "" ){
            supportActionBar?.subtitle = subtitle
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }
}