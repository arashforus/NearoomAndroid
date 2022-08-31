package com.arashforus.nearroom

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityAppInfoBinding
import com.bumptech.glide.Glide
import org.json.JSONObject

class AppInfoActivity : AppCompatActivity() {

    lateinit var binding : ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initToolbar()
        loadParameters()
        checkNewVersion()
    }

    private fun checkNewVersion() {
        Volley_CheckVersion(this, object : Volley_CheckVersion.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                if (result.getBoolean("isSuccess")){
                    val currentNumber = MySharedPreference(MySharedPreference.PreferenceApp,this@AppInfoActivity).load(MySharedPreference.App_Number,"Int") as Int
                    val lastNumber = MySharedPreference(MySharedPreference.PreferenceAppVersion,this@AppInfoActivity).load(MySharedPreference.AppVersion_Number,"Int") as Int
                    if ( lastNumber > currentNumber){
                        binding.AppInfoActivityNewVersionButton.text = "New version available to download"
                        binding.AppInfoActivityNewVersionButton.setOnClickListener {
                            val downloadLink = MySharedPreference(MySharedPreference.PreferenceAppVersion,this@AppInfoActivity).load(MySharedPreference.AppVersion_DownloadLink,"String") as String
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink)))
                        }
                    }else{
                        binding.AppInfoActivityNewVersionButton.text = "You have the latest version"
                    }
                }
            }
        }).get()
    }

    private fun loadParameters() {
        val logo = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Icon,"String").toString()
        val version = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Version,"String").toString()
        val description = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Description,"String").toString()
        val releaseDate = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_DateReleased,"String").toString()
        binding.AppInfoActivityTitle.text = "Nearoom V$version"
        binding.AppInfoActivityReleasedDate.text = MyDateTime().gmtToLocal(releaseDate,"yy/mm/dd")
        binding.AppInfoActivityDescription.text = description
        if ( MyInternalStorage(this).isAppLogoAvailable(logo) ){
            Glide.with(this)
                .load(MyInternalStorage(this).getAppLogoString(logo))
                .into(binding.AppInfoActivityLogo)
        }else{
            AndroidNetworking.download( MyServerSide().getAppLogoUri(logo), MyInternalStorage(this).getAppLogoFolder(), logo)
                .setTag(logo)
                .setPriority(Priority.MEDIUM)
                .build()
                .startDownload(object : DownloadListener {
                    override fun onDownloadComplete() {
                        loadParameters()
                    }
                    override fun onError(anError: ANError?) {
                        //println("download $logo  has error")
                    }
                })
        }

    }

}