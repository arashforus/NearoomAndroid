package com.arashforus.nearroom

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.arashforus.nearroom.databinding.ActivitySettingBinding
import org.json.JSONObject

class SettingActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySettingBinding

    private val messageRingtoneCode = 1986
    private val nearoomRingtoneCode = 1987

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        loadParameters()
        makeButtonListeners()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.SettingActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Setting"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun loadParameters() {
        // Message Notifications ///////////////////////////////////////////////////////////////////
        binding.SettingActivityNotificationMessageEnable.isChecked =
            MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Enable,"Boolean",true) as Boolean
        val messageRingtoneName = MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Ringtone,"String","Default") as String
        if ( messageRingtoneName !== "Default" ){
            val ringtone = RingtoneManager.getRingtone(this,Uri.parse(messageRingtoneName))
            binding.SettingActivityNotificationMessageRingtoneSelected.text = ringtone.getTitle(this)
        }else{
            binding.SettingActivityNotificationMessageRingtoneSelected.text = messageRingtoneName
        }
        binding.SettingActivityNotificationMessageVibrate.isChecked =
            MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Vibrate,"Boolean",true) as Boolean
        binding.SettingActivityNotificationMessageRingtoneLayout.isEnabled = binding.SettingActivityNotificationMessageEnable.isChecked
        binding.SettingActivityNotificationMessageRingtoneText.isEnabled = binding.SettingActivityNotificationMessageEnable.isChecked
        binding.SettingActivityNotificationMessageRingtoneSelected.isEnabled = binding.SettingActivityNotificationMessageEnable.isChecked
        binding.SettingActivityNotificationMessageVibrate.isEnabled = binding.SettingActivityNotificationMessageEnable.isChecked

        // Nearoom Notifications ///////////////////////////////////////////////////////////////////
        binding.SettingActivityNotificationNearoomsEnable.isChecked =
            MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.NearoomNotification_Enable,"Boolean",true) as Boolean
        val nearoomRingtoneName = MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.NearoomNotification_Ringtone,"String","Default") as String
        if ( nearoomRingtoneName !== "Default" ){
            val ringtone = RingtoneManager.getRingtone(this,Uri.parse(nearoomRingtoneName))
            binding.SettingActivityNotificationNearoomsRingtoneSelected.text = ringtone.getTitle(this)
        }else{
            binding.SettingActivityNotificationNearoomsRingtoneSelected.text = nearoomRingtoneName
        }
        binding.SettingActivityNotificationNearoomsVibrate.isChecked =
            MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.NearoomNotification_Vibrate,"Boolean",true) as Boolean
        binding.SettingActivityNotificationNearoomsRingtoneLayout.isEnabled = binding.SettingActivityNotificationNearoomsEnable.isChecked
        binding.SettingActivityNotificationNearoomsRingtoneText.isEnabled = binding.SettingActivityNotificationNearoomsEnable.isChecked
        binding.SettingActivityNotificationNearoomsRingtoneSelected.isEnabled = binding.SettingActivityNotificationNearoomsEnable.isChecked
        binding.SettingActivityNotificationNearoomsVibrate.isEnabled = binding.SettingActivityNotificationNearoomsEnable.isChecked

        // Download ////////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityImageAutoDownload.isChecked =
            MySharedPreference(MySharedPreference.PreferenceDownloadSetting,this).load(MySharedPreference.DownloadSetting_ImageAutoDownload,"Boolean",false) as Boolean
        binding.SettingActivityVideoAutoDownload.isChecked =
            MySharedPreference(MySharedPreference.PreferenceDownloadSetting,this).load(MySharedPreference.DownloadSetting_VideoAutoDownload,"Boolean",false) as Boolean

        // App /////////////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityVersionNumber.text =
            MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_Version,"String") as String

    }

    private fun makeButtonListeners() {
        // Message Notification ////////////////////////////////////////////////////////////////////
        binding.SettingActivityNotificationMessageEnable.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).save(MySharedPreference.MessageNotification_Enable,isChecked)
            binding.SettingActivityNotificationMessageRingtoneLayout.isEnabled = isChecked
            binding.SettingActivityNotificationMessageRingtoneText.isEnabled = isChecked
            binding.SettingActivityNotificationMessageRingtoneSelected.isEnabled = isChecked
            binding.SettingActivityNotificationMessageVibrate.isEnabled = isChecked

        }
        binding.SettingActivityNotificationMessageRingtoneLayout.setOnClickListener {
            val currentTone = MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Ringtone,"String") as String
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            //intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, true)
            startActivityForResult(intent, messageRingtoneCode)
        }
        binding.SettingActivityNotificationMessageVibrate.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).save(MySharedPreference.MessageNotification_Vibrate,isChecked)
        }

        // Nearooms Notification ///////////////////////////////////////////////////////////////////
        binding.SettingActivityNotificationNearoomsEnable.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).save(MySharedPreference.NearoomNotification_Enable,isChecked)
            binding.SettingActivityNotificationNearoomsRingtoneLayout.isEnabled = isChecked
            binding.SettingActivityNotificationNearoomsRingtoneText.isEnabled = isChecked
            binding.SettingActivityNotificationNearoomsRingtoneSelected.isEnabled = isChecked
            binding.SettingActivityNotificationNearoomsVibrate.isEnabled = isChecked
        }
        binding.SettingActivityNotificationNearoomsRingtoneLayout.setOnClickListener {
            val currentTone = MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.NearoomNotification_Ringtone,"String") as String
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            startActivityForResult(intent, nearoomRingtoneCode)
        }
        binding.SettingActivityNotificationNearoomsVibrate.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).save(MySharedPreference.NearoomNotification_Vibrate,isChecked)
        }

        // Download ////////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityImageAutoDownload.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceDownloadSetting,this).save(MySharedPreference.DownloadSetting_ImageAutoDownload,isChecked)
        }
        binding.SettingActivityVideoAutoDownload.setOnCheckedChangeListener { _, isChecked ->
            MySharedPreference(MySharedPreference.PreferenceDownloadSetting,this).save(MySharedPreference.DownloadSetting_VideoAutoDownload,isChecked)
        }

        // Wallpaper ///////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityWallpaperSolidColor.setOnClickListener {
            startActivity(Intent(this,BackgroundSolidColorActivity::class.java))
        }
        binding.SettingActivityWallpaperLiveWallpaper.setOnClickListener {
            startActivity(Intent(this,BackgroundLiveWallpaperActivity::class.java))
        }
        binding.SettingActivityWallpaperMyPicture.setOnClickListener {
            val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
            targetIntent.putExtra("title","Select wallpaper picture")
            //targetIntent.putExtra("subtitle","Wallpaper")
            targetIntent.putExtra("type","PICTURE")
            targetIntent.putExtra("fromActivity","Wallpaper")
            startActivity(targetIntent)
        }

        // Support us //////////////////////////////////////////////////////////////////////////////
        binding.SettingActivitySponsorship.setOnClickListener {
            startActivity(Intent(this, SponsorshipActivity::class.java))
        }
        binding.SettingActivityDonate.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MyServerSide.donate_address)))
        }
        binding.SettingActivityWatchad.setOnClickListener {
            startActivity(Intent(this, AdActivity::class.java))
        }

        // Help ////////////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityFAQ.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MyServerSide.FAQ_address)))
        }
        binding.SettingActivityPrivacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MyServerSide.privacyPolicy_address)))
        }
        binding.SettingActivityContactUs.setOnClickListener {
            startActivity(Intent(this,ContactUsActivity::class.java))
        }

        // App /////////////////////////////////////////////////////////////////////////////////////
        binding.SettingActivityVersionLayout.setOnClickListener {
            startActivity(Intent(this,AppInfoActivity::class.java))
        }

        binding.SettingActivityCheckVersion.setOnClickListener {
            Volley_CheckVersion(this, object : Volley_CheckVersion.ServerCallBack {
                override fun getsuccess(result: JSONObject) {
                    if (result.getBoolean("isSuccess")){
                        val currentVersionNumber = MySharedPreference(MySharedPreference.PreferenceAppVersion,this@SettingActivity).load(MySharedPreference.AppVersion_Number,"Int") as Int
                        if (result.getInt("number") > currentVersionNumber){
                            val version = result.getString("version")
                            val downloadLink = result.getString("downloadlink")
                            if (result.getInt("minsupportnumber") > currentVersionNumber ){
                                AlertDialog.Builder(this@SettingActivity)
                                    .setTitle("New Version ( $version )")
                                    .setMessage("New version of nearoom is available , you should update if you want to continue using this application")
                                    .setCancelable(false)
                                    .setPositiveButton("download") { _, _ ->
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink.toString())))
                                    }
                                    .setNegativeButton("close app") { dialog, _ ->
                                        dialog.cancel()
                                        dialog.dismiss()
                                        finishAndRemoveTask()
                                    }
                                    .show()
                            }else{
                                AlertDialog.Builder(this@SettingActivity)
                                    .setTitle("New Version ( $version )")
                                    .setMessage("New version of nearoom is available to download")
                                    .setCancelable(true)
                                    .setPositiveButton("download") { _, _ ->
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink.toString())))
                                    }
                                    .setNegativeButton("cancel") { dialog, _ ->
                                        dialog.dismiss()
                                        dialog.cancel()
                                    }
                                    .show()
                            }
                        }else{
                            AlertDialog.Builder(this@SettingActivity)
                                .setTitle("Up to date")
                                .setMessage("You have the latest version of nearoom ")
                                .setCancelable(true)
                                .setPositiveButton("Ok") { dialog, _ ->
                                    dialog.dismiss()
                                    dialog.cancel()
                                }
                                .show()
                        }
                    }else{
                        AlertDialog.Builder(this@SettingActivity)
                            .setTitle("Error")
                            .setMessage("Something wrong , can't connect to server")
                            .setCancelable(true)
                            .setPositiveButton("Ok") { dialog, _ ->
                                dialog.dismiss()
                                dialog.cancel()
                            }
                            .show()
                    }
                }
            }).get()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            val uri : Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            //val ringtone = RingtoneManager.getRingtone(this,uri)
            if (requestCode == messageRingtoneCode){
                MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).save(MySharedPreference.MessageNotification_Ringtone,uri.toString())
                val messageRingtoneName = MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Ringtone,"String","Default") as String
                if ( messageRingtoneName !== "Default" ){
                    val ringtone = RingtoneManager.getRingtone(this,uri)
                    binding.SettingActivityNotificationMessageRingtoneSelected.text = ringtone.getTitle(this)
                }else{
                    binding.SettingActivityNotificationMessageRingtoneSelected.text = messageRingtoneName
                }
            }
            if ( requestCode == nearoomRingtoneCode){
                MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).save(MySharedPreference.NearoomNotification_Ringtone,uri.toString())
                val nearoomRingtoneName = MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.NearoomNotification_Ringtone,"String","Default") as String
                if ( nearoomRingtoneName !== "Default" ){
                    val ringtone = RingtoneManager.getRingtone(this,uri)
                    binding.SettingActivityNotificationMessageRingtoneSelected.text = ringtone.getTitle(this)
                }else{
                    binding.SettingActivityNotificationMessageRingtoneSelected.text = nearoomRingtoneName
                }
            }
            loadParameters()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val targetIntent = Intent(this  ,MainActivity::class.java)
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
        startActivity(targetIntent)
        finish()
    }
}