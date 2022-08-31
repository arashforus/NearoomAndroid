package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arashforus.nearroom.databinding.ActivityGalleryMediaBinding
import java.util.*

class GalleryMediaActivity : AppCompatActivity() {

    lateinit var binding : ActivityGalleryMediaBinding
    private lateinit var bucketId : String
    private lateinit var bucketName : String
    private var title : String? = null
    private var subtitle : String? = null
    private var type : String? = null
    private var contactId : String? = null
    private var roomId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if ( intent.hasExtra("bucketId") ){
            bucketId = intent.getStringExtra("bucketId")!!
        }
        if ( intent.hasExtra("bucketName") ){
            bucketName = intent.getStringExtra("bucketName")!!
        }
        title = intent.getStringExtra("title")
        subtitle = intent.getStringExtra("subtitle")
        type = intent.getStringExtra("type")
        if ( intent.hasExtra("contactId") ){
            contactId = intent.getStringExtra("contactId")
        }
        if ( intent.hasExtra("roomId") ){
            roomId = intent.getStringExtra("roomId")
            //println("roomId is : $roomId")
        }

        initToolbar()
        initAdapter()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.GalleryMediaActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        if ( title == null || title == "" ){
            supportActionBar?.title = bucketName
        }else{
            supportActionBar?.title = title
        }
        if ( subtitle !== null && subtitle !== "" ){
            supportActionBar?.subtitle = subtitle
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun initAdapter(){
        val mediaList = when (type?.toUpperCase(Locale.ROOT)?.trim() ){
            "VIDEO" -> MyGalleryTools(this).getVideosFromAlbum(bucketId)
            "PICTURE" ->  MyGalleryTools(this).getPicturesFromAlbum(bucketId)
            "IMAGE" -> MyGalleryTools(this).getPicturesFromAlbum(bucketId)
            "AUDIO" -> MyGalleryTools(this).getAudiosFromAlbum(bucketId)
            "VIEWCONTACTPICTURE" -> DB_Messages(this).getAllImagesFromUsername(contactId!!.toInt())
            "VIEWCONTACTVIDEO" -> DB_Messages(this).getAllVideosFromUsername(contactId!!.toInt())
            "ROOMINFOPICTURE" -> DB_NearroomMessages(this).getAllImagesOfRoom(roomId!!.toInt())
            "ROOMINFOVIDEO" -> DB_NearroomMessages(this).getAllVideosOfRoom(roomId!!.toInt())
            else -> MyGalleryTools(this).getPicturesFromAlbum(bucketId)
        }
        val adapter = Adapter_GalleryMedias(mediaList)
        binding.GalleryMediaActivityRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.GalleryMediaActivityRecyclerView.setHasFixedSize(true)
        binding.GalleryMediaActivityRecyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}