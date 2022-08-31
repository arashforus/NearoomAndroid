package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityPreparingBinding

class PreparingActivity : AppCompatActivity() {

    lateinit var binding : ActivityPreparingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreparingBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}