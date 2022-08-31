package com.arashforus.nearroom

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivitySponsorshipBinding
import com.bumptech.glide.Glide
import org.json.JSONObject

class SponsorshipActivity : AppCompatActivity() {

    lateinit var binding: ActivitySponsorshipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySponsorshipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        checkLogo()
        loadSponsor()
        updateSponsor()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.SponsorshipActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Sponsorship"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun checkLogo() {
        MyMediaDownloader(this).updateSponsosorshipLogo(object : MyMediaDownloader.MyCallback{
            override fun downloadSuccess() {
                loadSponsor()
            }
        })
    }

    private fun loadSponsor() {
        // fetch details from DB ///////////////////////////////////////////////////////////////////
        val name = MySharedPreference(MySharedPreference.PreferenceSponsorship,this).load(MySharedPreference.Sponsorship_Name,"String") as String
        val logo = MySharedPreference(MySharedPreference.PreferenceSponsorship,this).load(MySharedPreference.Sponsorship_Logo,"String") as String
        val desc = MySharedPreference(MySharedPreference.PreferenceSponsorship,this).load(MySharedPreference.Sponsorship_Description,"String") as String
        val link = MySharedPreference(MySharedPreference.PreferenceSponsorship,this).load(MySharedPreference.Sponsorship_Link,"String") as String

        if (name !== "Null"){
            binding.SponsorshipActivityTitle.text = name
            binding.sponsorshipActivityDesc.text = desc
            binding.sponsorshipActivityButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            }
            Glide.with(this)
                .load(MyInternalStorage(this).getSponsorshipLogoString(logo))
                .into(binding.SponsorshipActivityLogo)
        }else{
            binding.SponsorshipActivityTitle.text = "Nearoom"
            binding.sponsorshipActivityDesc.text = "Get connected with your neareset soulmates"
            binding.sponsorshipActivityButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nearoom.com")))
            }
            Glide.with(this)
                .load(R.drawable.nearoom_small)
                .into(binding.SponsorshipActivityLogo)
        }

        binding.SponsorshipActivityNextSponsorship.setOnClickListener {
            val targetIntent = Intent(this,ContactUsActivity::class.java)
            targetIntent.putExtra("purpose","Sponsorship")
            startActivity(targetIntent)
        }

    }

    private fun updateSponsor() {
        // fetch sponsorship detail from server and update db and view /////////////////////////////
        Volley_GetLastSponsorship(this, object : Volley_GetLastSponsorship.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                if (result.getBoolean("isSuccess")){
                    checkLogo()
                    loadSponsor()
                }
            }
        }).get()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}