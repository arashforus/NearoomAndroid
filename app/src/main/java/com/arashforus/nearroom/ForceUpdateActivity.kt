package com.arashforus.nearroom

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityForceUpdateBinding
import org.json.JSONObject

class ForceUpdateActivity : AppCompatActivity() {

    lateinit var binding : ActivityForceUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForceUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadParameters()
        makeListeners()
        updateLatestVersionInfo()

    }

    private fun loadParameters() {
        val thisVersion = MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Version,"String").toString()
        val latestVersion = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_Version,"String").toString()
        val latestDateRelease = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_DateReleased,"String").toString()
        val latestSize = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_Size,"Int") as Int
        val latestDescription = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_Description,"String").toString()

        binding.ForceUpdateActivityYourVersionNumber.text = thisVersion
        binding.ForceUpdateActivityLatestVersionNumber.text = latestVersion
        binding.ForceUpdateActivityReleaseDate.text = MyDateTime().gmtToLocal(latestDateRelease,"YYYY/MM/dd")
        binding.ForceUpdateActivitySizeNumber.text = MyTools(this).getReadableSizeFromByte(latestSize)
        binding.ForceUpdateActivityLatestDescription.text = latestDescription
    }

    private fun makeListeners() {
        binding.ForceUpdateActivityExitButton.setOnClickListener {
            finishAndRemoveTask()
        }
        binding.ForceUpdateActivityUpdateButton.setOnClickListener {
            val downloadLink = MySharedPreference(MySharedPreference.PreferenceAppVersion,this).load(MySharedPreference.AppVersion_DownloadLink,"String").toString()
            val browserIntent = if ( downloadLink.isEmpty() ){
                Intent(Intent.ACTION_VIEW, Uri.parse(MyServerSide.downloadApp))
            }else{
                Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink))
            }
            startActivity(browserIntent)
        }
    }

    private fun updateLatestVersionInfo() {
        Volley_CheckVersion(this, object : Volley_CheckVersion.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                loadParameters()
                makeListeners()
            }
        })
    }

}