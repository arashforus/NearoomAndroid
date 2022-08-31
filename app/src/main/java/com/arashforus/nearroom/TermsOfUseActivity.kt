package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityTermsOfUseBinding

class TermsOfUseActivity : AppCompatActivity() {

    lateinit var binding : ActivityTermsOfUseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsOfUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.TermsOfUseActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Terms of use"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
        //supportActionBar?.subtitle = "Tap here for group info"
    }
}