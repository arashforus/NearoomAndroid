package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityImageViewBinding
import com.bumptech.glide.Glide

class ImageViewActivity : AppCompatActivity() {

    lateinit var binding : ActivityImageViewBinding

    var title : String? = null
    var subtitle : String? = null
    var uri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
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
        setSupportActionBar(binding.ImageViewActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayUseLogoEnabled(true)
        if ( !title.isNullOrEmpty() ){
            supportActionBar?.title = title
        }
        if ( !subtitle.isNullOrEmpty() ){
            supportActionBar?.subtitle = MyDateTime().gmtToLocal(subtitle!!)
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        //binding.ImageViewActivityImage.setImageURI(MyInternalStorage(this).getPicUri(uri))
        if ( MyExternalStorage(this).isImageAvailable(uri) ){
            Glide.with(this)
                .load(MyExternalStorage(this).getImageString(uri))
                .error(R.drawable.noimage)
                .into(binding.ImageViewActivityImage)
        }else{
            if ( MyInternalStorage(this).isPicThumbAvailable(uri) ){
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbPicString(uri))
                    .error(R.drawable.noimage)
                    .into(binding.ImageViewActivityImage)
            }else{
                Glide.with(this)
                    .load(R.drawable.noimage)
                    .into(binding.ImageViewActivityImage)
            }
            AndroidNetworking.download( MyServerSide().getPhotoUri(uri), MyExternalStorage(this).getImageFolder(), uri)
                .setTag(uri)
                .setPriority(Priority.HIGH)
                .build()
                .startDownload(object : DownloadListener {
                    override fun onDownloadComplete() {
                        Glide.with(this@ImageViewActivity)
                            .load(MyExternalStorage(this@ImageViewActivity).getImageString(uri))
                            .error(R.drawable.noimage)
                            .into(binding.ImageViewActivityImage)
                    }

                    override fun onError(anError: ANError?) {
                        Toast.makeText(this@ImageViewActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                    }
                })
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}