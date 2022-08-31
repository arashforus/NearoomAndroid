package com.arashforus.nearroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityWallpaperEditBinding
import com.bumptech.glide.Glide
import com.isseiaoki.simplecropview.CropImageView

class WallpaperEditActivity : AppCompatActivity() {

    lateinit var binding : ActivityWallpaperEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        setupCropView()
        makeListeners()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.WallpaperEditActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Crop wallpaper picture"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun setupCropView() {
        val path = intent.getStringExtra("path")
        Glide.with(this)
            .load(path)
            .into(binding.WallpaperEditActivityCropImageView)
        //println("Width is : ${binding.WallpaperEditActivityMeasure.width}")
        //binding.WallpaperEditActivityCropImageView.setCustomRatio(binding.WallpaperEditActivityMeasure.width,binding.WallpaperEditActivityMeasure.height)
        binding.WallpaperEditActivityCropImageView.setCropMode(CropImageView.CropMode.CUSTOM)
        //MyTools(this).screenWidth()
        binding.WallpaperEditActivityMeasure.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.WallpaperEditActivityCropImageView.setCustomRatio(binding.WallpaperEditActivityMeasure.width,binding.WallpaperEditActivityMeasure.height,500)
        }
        binding.WallpaperEditActivityCropImageView.setInitialFrameScale(0.8f)
    }

    private fun makeListeners() {
        binding.WallpaperEditActivitySelectButton.setOnClickListener {
            val imagePath = MyInternalStorage(this).saveWallpaperPic(binding.WallpaperEditActivityCropImageView.croppedBitmap)
            MySharedPreference(MySharedPreference.PreferenceWallpaper,this).save(MySharedPreference.Wallpaper_Name,"Picture")
            MySharedPreference(MySharedPreference.PreferenceWallpaper,this).save(MySharedPreference.Wallpaper_ImagePath,imagePath)
            val targetIntent = Intent(this,SettingActivity::class.java)
            //targetIntent.putExtra("purpose","changeProfile")
            //targetIntent.putExtra("path",imageArray[0])
            startActivity(targetIntent)
            finish()

        }

        binding.WallpaperEditActivityCancelButton.setOnClickListener {
            startActivity(Intent(this,SettingActivity::class.java))
            finish()
        }

        binding.WallpaperEditActivityRotate.setOnClickListener {
            binding.WallpaperEditActivityCropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D,300)
        }
    }

}