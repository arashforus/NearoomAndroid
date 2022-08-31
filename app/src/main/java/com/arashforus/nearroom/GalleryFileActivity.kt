package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashforus.nearroom.databinding.ActivityGalleryFileBinding

class GalleryFileActivity : AppCompatActivity() {

    lateinit var binding : ActivityGalleryFileBinding

    private var contactId = 0
    private var roomId = 0
    private var fileList = arrayListOf<Object_Media>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactId = intent.getIntExtra("contactId",0)
        roomId = intent.getIntExtra("roomId",0)

        initToolbar()
        initAdapter()
    }

    private fun initAdapter() {
        if ( contactId > 0 ){
            fileList = DB_Messages(this).getAllFilesFromUsername(contactId)
        }else{
            if ( roomId > 0 ){
                fileList = DB_NearroomMessages(this).getAllFilesOfRoom(roomId)
            }
        }

        val adapter = Adapter_GalleryFiles(fileList)
        binding.GalleryFileActivityRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.GalleryFileActivityRecyclerView.adapter = adapter
    }

    private fun initToolbar() {
        setSupportActionBar(binding.GalleryFileActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "All Files"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}