package com.arashforus.nearroom

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityParticleBinding

class ParticleActivity : AppCompatActivity() {

    lateinit var binding : ActivityParticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticleBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}