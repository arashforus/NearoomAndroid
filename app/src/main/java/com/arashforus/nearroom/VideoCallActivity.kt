package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityVideoCallBinding
import com.arashforus.nearroom.databinding.ActivityVoiceCallBinding

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.VideoCallActivityEndCall.setOnClickListener {
            finish()
        }
    }
}