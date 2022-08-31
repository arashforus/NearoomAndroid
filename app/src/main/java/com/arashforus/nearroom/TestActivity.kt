package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    lateinit var binding : ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        //binding.startImage.setOnClickListener {
        //    MyAnimations(this).zoomImageFromThumb(R.drawable.defaultprofile,
        //    binding.startImage,binding.finalImage)
        //}


        
    }
}