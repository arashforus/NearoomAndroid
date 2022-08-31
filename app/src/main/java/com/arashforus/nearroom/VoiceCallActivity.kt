package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityPrivateBinding
import com.arashforus.nearroom.databinding.ActivityVoiceCallBinding

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.VoiceCallActivityEndCall.setOnClickListener {
            finish()
        }

    }
}