package com.arashforus.nearroom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arashforus.nearroom.databinding.ActivitySplashBinding
import com.bumptech.glide.Glide
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {

    lateinit var binding : ActivitySplashBinding
    private val delayTime : Long = 1000
    private val writeExternalRequestCode = 52223

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val trans : AnimationDrawable = binding.SplashActivityInnerLayout.background as AnimationDrawable
        trans.setEnterFadeDuration(200)
        trans.setExitFadeDuration(500)
        trans.start()

        setAppInfos()
        setViewElements()
        checkSponsorshipLogoAvailable()
        fetchNewSponsorship()
        fetchLatestVersion()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),writeExternalRequestCode )
        }else{
            MyExternalStorage(this).checkAndMakeFolders()
            forwardToNextActivity()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray ) {
        super .onRequestPermissionsResult(requestCode,permissions,grantResults)
        if (requestCode == writeExternalRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                MyExternalStorage(this).checkAndMakeFolders()
                forwardToNextActivity()
            } else {
                Toast.makeText(this,"Permission Denied by user, You should get access to this app to continue using this", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),writeExternalRequestCode )
            }
        }
    }

    private fun forwardToNextActivity(){
        val minSupportNumber = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_MinSupportNumber,"Int",0) as Int
        val currentNumber = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Number,"Int",1) as Int
        if ( currentNumber < minSupportNumber ){
            val nextIntent = Intent(this@SplashActivity,ForceUpdateActivity::class.java)
            startActivity(nextIntent)
            finish()
        }else{
            Handler().postDelayed({
                if (MySharedPreference("app",this@SplashActivity).load("introShowed","boolean") == true) {
                    // Intro Sliders Showed before /////////////////////////////////////////////////////
                    if (MySharedPreference("app",this@SplashActivity).load("login","boolean") == false) {
                        // No one is login /////////////////////////////////////////////////////////////
                        val nextIntent = Intent(this@SplashActivity,LoginActivity::class.java)
                        startActivity(nextIntent)
                        finish()
                    }else{
                        // Someone is login ////////////////////////////////////////////////////////////

                        val nextIntent = Intent(this@SplashActivity,MainActivity::class.java)
                        startActivity(nextIntent)
                        finish()
                    }
                }else{
                    // Intro Sliders Doesn't Showed before /////////////////////////////////////////////
                    val nextIntent = Intent(this@SplashActivity,IntroSliderActivity::class.java)
                    startActivity(nextIntent)
                    finish()
                }
            },delayTime)
        }
    }

    private fun setAppInfos() {
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_Number,MyAppInfos.number)
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_Version,MyAppInfos.version)
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_Icon,MyAppInfos.icon)
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_Description,MyAppInfos.description)
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_DateReleased,MyAppInfos.releaseDate)
    }

    private fun setViewElements() {
        // Set Version text ////////////////////////////////////////////////////////////////////////
        val currentVer = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Version,"String")as String
        val currentNum = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Number,"Int") as Int
        val latestVer = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_Version,"String") as String
        val latestNum = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_Number,"Int") as Int
        if (latestNum > currentNum){
            binding.SplashActivityVersion.text = "Ver $currentVer ( new version $latestVer available )"

        }else{
            binding.SplashActivityVersion.text = "Ver $currentVer"
        }

        // Set Sponsorship Logo ////////////////////////////////////////////////////////////////////
        val sponsorLogo = MySharedPreference(MySharedPreference.PreferenceSponsorship,this).load(MySharedPreference.Sponsorship_Logo,"String") as String
        if (MyInternalStorage(this).isSponsorshipLogoAvailable(sponsorLogo)){
            Glide.with(this)
                .load(MyInternalStorage(this).getSponsorshipLogoString(sponsorLogo))
                .centerInside()
                .into(binding.SplashActivitySponserLogo)
        }

    }

    private fun checkSponsorshipLogoAvailable() {
        MyMediaDownloader(this).updateSponsosorshipLogo(object : MyMediaDownloader.MyCallback{
            override fun downloadSuccess() {
                // Nothing
            }

        })
    }

    private fun fetchNewSponsorship(){
        Volley_GetLastSponsorship(this, object : Volley_GetLastSponsorship.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                //println("New sponsorship information fetch successfully")
            }
        }).get()
    }

    private fun fetchLatestVersion() {
        Volley_CheckVersion(this, object : Volley_CheckVersion.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                //println("Fetch latest version information successfully")
            }
        }).get()
    }
}