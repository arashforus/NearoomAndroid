package com.arashforus.nearroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityProfileEditBinding
import com.bumptech.glide.Glide
import com.isseiaoki.simplecropview.CropImageView
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    lateinit var binding : ActivityProfileEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        setupCropView()
        makeListeners()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.ProfileEditActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Crop profile picture"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun setupCropView() {
        val path = intent.getStringExtra("path")
        Glide.with(this)
            .load(path)
            .into(binding.ProfileEditActivityCropImageView)
        binding.ProfileEditActivityCropImageView.setCropMode(CropImageView.CropMode.SQUARE)
        binding.ProfileEditActivityCropImageView.setInitialFrameScale(1f)
    }

    private fun makeListeners() {
        binding.ProfileEditActivitySelectButton.setOnClickListener {
            when ( intent.getStringExtra("fromActivity")?.toUpperCase(Locale.ROOT)  ){
                "PROFILE" -> {
                    val myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
                    val imageArray = MyInternalStorage(this).saveProfilePic(binding.ProfileEditActivityCropImageView.croppedBitmap, myId)

                    val targetIntent = Intent(this,MyProfileActivity::class.java)
                    targetIntent.putExtra("purpose","changeProfile")
                    targetIntent.putExtra("path",imageArray[0])
                    targetIntent.putExtra("thumbPath",imageArray[1])
                    targetIntent.putExtra("imageName",imageArray[2])
                    startActivity(targetIntent)
                    finish()
                }
                "ROOMINFO" -> {
                    val myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
                    val imageArray = MyInternalStorage(this).saveProfilePic(binding.ProfileEditActivityCropImageView.croppedBitmap, myId)

                    val targetIntent = Intent(this,RoomInfoActivity::class.java)
                    println("room Id is ${intent.getIntExtra("roomId",0)}")
                    targetIntent.putExtra("roomId",intent.getIntExtra("roomId",0))
                    targetIntent.putExtra("purpose","changePicture")
                    targetIntent.putExtra("path",imageArray[0])
                    targetIntent.putExtra("thumbPath",imageArray[1])
                    targetIntent.putExtra("imageName",imageArray[2])
                    startActivity(targetIntent)
                    finish()
                }
            }


        }

        binding.ProfileEditActivityCancelButton.setOnClickListener {
            startActivity(Intent(this,MyProfileActivity::class.java))
            finish()
        }

        binding.ProfileEditActivityRotate.setOnClickListener {
            binding.ProfileEditActivityCropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D,300)
        }
    }
}