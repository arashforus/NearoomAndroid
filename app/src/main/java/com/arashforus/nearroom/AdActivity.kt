package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.arashforus.nearroom.databinding.ActivityAdBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdActivity : AppCompatActivity() {

    lateinit var binding : ActivityAdBinding

    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "AdActivityTag"
    //val AD_UNIT_ID = "ca-app-pub-6649298806520307/8313895012"
    val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        initToolbar()
        initInterstitialAd()
        initBannerAd()


    }

    private fun initBannerAd() {
        //binding.AdActivityAdView1.adSize = AdSize.BANNER
        //binding.AdActivityAdView1.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        val adRequest = AdRequest.Builder().build()
        binding.AdActivityAdView1.loadAd(adRequest)
        binding.AdActivityAdView1.adListener = object : AdListener() {
            override fun onAdLoaded() {

            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
            }
            override fun onAdOpened() {
            }
            override fun onAdClicked() {
            }
            override fun onAdImpression() {
            }
            override fun onAdClosed() {
            }
        }

       // binding.AdActivityAdView2.adSize = AdSize.BANNER
        //binding.AdActivityAdView2.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        val adRequest2 = AdRequest.Builder().build()
        binding.AdActivityAdView2.loadAd(adRequest2)
    }

    private fun initInterstitialAd() {

        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                showAd()
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d(TAG, "Ad failed to show.")
                println(adError?.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.AdActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Ads"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun showAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}