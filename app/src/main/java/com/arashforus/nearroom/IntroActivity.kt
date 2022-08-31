package com.arashforus.nearroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    lateinit var binding : ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (MySharedPreference("profile",this).load("isLogin","boolean") == false) {
            binding.IntroActivityStartButton.setOnClickListener {
                val nextIntent = Intent(this,LoginActivity::class.java)
                startActivity(nextIntent)
            }
        }else{
            val nextIntent = Intent(this,MainActivity::class.java)
            startActivity(nextIntent)
            finish()
        }

    }
}

