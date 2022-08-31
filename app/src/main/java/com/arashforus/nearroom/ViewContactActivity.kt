package com.arashforus.nearroom

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityViewContactBinding
import com.bumptech.glide.Glide
import org.json.JSONObject

class ViewContactActivity : AppCompatActivity() {

    lateinit var binding : ActivityViewContactBinding
    var id : Int = 0
    lateinit var userInfo : Object_User

    override fun onCreate(savedInstanceState: Bundle?) {
        MyTools(this).enableWindowContentTransition(this,MyTools.transitionExplode,MyTools.transitionExplode)
        super.onCreate(savedInstanceState)
        binding = ActivityViewContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra("userId",0)
        initToolbar()
        userInfo = DB_UserInfos(this).getUserInfo(id)
        makeView()
        makeListeners()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.ViewContactActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "View contact"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.ViewContactActivityUsernameValue.text = userInfo.username
        binding.ViewContactActivityFullnameValue.text = userInfo.fullName
        if ( userInfo.status.isNullOrEmpty()) {
            binding.ViewContactActivityStatusValue.text = "No status"
            //binding.ViewContactActivityStatusValue.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.ViewContactActivityStatusValue.alpha = 0.5f
        }else{
            binding.ViewContactActivityStatusValue.text = userInfo.status
        }

        binding.ViewContactActivityPhoneNumberValue.text = if ( DB_Contacts(this).getArrayNumbersWithCountryCode().contains(userInfo.phoneNumber) ){
            userInfo.phoneNumber
        }else{
            "Private"
        }

        if ( userInfo.profilePic.isNullOrEmpty()){
            Glide.with(this)
                .load(R.drawable.defaultprofile)
                .centerInside()
                .into(binding.ViewContactActivityProfilePic)
        }else{
            if (MyInternalStorage(this).isProfileAvailable(userInfo.profilePic!!)){
                Glide.with(this)
                    .load(MyInternalStorage(this).getProfilePicString(userInfo.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .centerInside()
                    .into(binding.ViewContactActivityProfilePic)
            }else{
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbProfilePicString(userInfo.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .centerInside()
                    .into(binding.ViewContactActivityProfilePic)
                AndroidNetworking.download( MyServerSide().getProfileUri(userInfo.profilePic!!), MyInternalStorage(this).getProfileFolder(), userInfo.profilePic)
                    .setTag(userInfo.profilePic)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@ViewContactActivity)
                                .load(MyInternalStorage(this@ViewContactActivity).getProfilePicString(userInfo.profilePic!!))
                                .error(R.drawable.defaultprofile)
                                .centerInside()
                                .into(binding.ViewContactActivityProfilePic)
                        }
                        override fun onError(anError: ANError?) {
                            //println("download $logo  has error")
                        }
                    })
            }
        }

        // Statics /////////////////////////////////////////////////////////////////////////////////
        val messagesSend = DB_Messages(this).countMessagesSendTo(userInfo.id!!)
        val messagesReceived = DB_Messages(this).countMessagesReceivedFrom(userInfo.id!!)
        val fromFirstMessage = DB_Messages(this).firstMessageTime(userInfo.id!!)
        binding.ViewContactActivityMessagesSentNumber.text = messagesSend.toString()
        binding.ViewContactActivityMessagesReceivedNumber.text = messagesReceived.toString()
        if (fromFirstMessage == null){
            binding.ViewContactActivityFromFirstMessageNumber.text = "N/A"
        }else{
            binding.ViewContactActivityFromFirstMessageNumber.text = MyDateTime().DayDiff(fromFirstMessage).toString()
        }
        if ( messagesSend <= 1 ){
            binding.ViewContactActivityMessagesSentUnit.text = "message"
        }
        if ( messagesReceived <= 1 ){
            binding.ViewContactActivityMessagesReceivedUnit.text = "message"
        }

        // Actions /////////////////////////////////////////////////////////////////////////////////
        binding.ViewContactActivityMuteNotificationSwitch.isChecked = userInfo.mute != 0
        if ( userInfo.blocked == 1 ){
            binding.ViewContactActivityBlockedButton.text = "Unblock"
        }

        // Medias //////////////////////////////////////////////////////////////////////////////////
        val images = DB_Messages(this).getLastImagesFromUsername(userInfo.id!!,10)
        if (images.size == 0){
            binding.ViewContactActivityNoImageText.visibility = View.VISIBLE
        }else{
            val imagesAdapter = Adapter_ViewContactMedias(images)
            binding.ViewContactActivityImagesRecyclerView.layoutManager = GridLayoutManager(this, images.size)
            binding.ViewContactActivityImagesRecyclerView.setHasFixedSize(true)
            binding.ViewContactActivityImagesRecyclerView.adapter = imagesAdapter
        }

        val videos = DB_Messages(this).getLastVideosFromUsername(userInfo.id!!,10)
        if ( videos.size == 0){
            binding.ViewContactActivityNoVideoText.visibility = View.VISIBLE
        }else{
            val videosAdapter = Adapter_ViewContactMedias(videos)
            //println("Media size ........ "+images.size)
            binding.ViewContactActivityVideosRecyclerView.layoutManager = GridLayoutManager(this, videos.size)
            binding.ViewContactActivityVideosRecyclerView.setHasFixedSize(true)
            binding.ViewContactActivityVideosRecyclerView.adapter = videosAdapter
        }

        val files = DB_Messages(this).getLastFilesFromUsername(userInfo.id!!,10)
        if ( files.size == 0 ){
            binding.ViewContactActivityNoFileText.visibility = View.VISIBLE
        }else{
            val filesAdapter = Adapter_ViewContactMedias(files)
            //println("Media size ........ "+images.size)
            binding.ViewContactActivityFilesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            //binding.ViewContactActivityFilesRecyclerView.setHasFixedSize(true)
            binding.ViewContactActivityFilesRecyclerView.adapter = filesAdapter
        }

        // Nearooms ////////////////////////////////////////////////////////////////////////////////
        //DB_NearoomsParticipant(this).firstTimeInit()
        val nearoomsJoined = DB_NearoomsParticipant(this).roomsJoinedByUsername(userInfo.id!!)
        if ( nearoomsJoined.size == 0 ){
            binding.ViewContactActivityNoNearoomText.visibility = View.VISIBLE
        }else{
            val nearoomsAdapter = Adapter_ViewContactNearoom(nearoomsJoined)
            binding.ViewContactActivityNearoomsRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            //binding.ViewContactActivityFilesRecyclerView.setHasFixedSize(true)
            binding.ViewContactActivityNearoomsRecyclerView.adapter = nearoomsAdapter
        }


    }

    private fun makeListeners() {
        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.ViewContactActivityProfilePic.setOnClickListener {
            val intent = Intent(this,ProfilePicViewActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.ViewContactActivityProfilePic,"pic"),
                Pair.create(binding.ViewContactActivityFullnameValue,"title"))
            intent.putExtra("userId",userInfo.id)
            //intent.putExtra("profilePic",userInfo.profilePic)
            startActivity(intent,options.toBundle())

        }

        // Actions /////////////////////////////////////////////////////////////////////////////////
        binding.ViewContactActivityMuteNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if ( isChecked ){
                DB_UserInfos(this).setMute(id,1)
            }else{
                DB_UserInfos(this).setMute(id,0)
            }
        }

        binding.ViewContactActivityBlockedButton.setOnClickListener {
            if ( userInfo.blocked == 1 ){
                AlertDialog.Builder(this)
                    .setTitle("Are you sure ?")
                    .setMessage("Do you want to unblock ${userInfo.username} ?")
                    .setCancelable(true)
                    .setPositiveButton("Yes , Unblock ${userInfo.username}") { dialog, _ ->
                        val newBlockedList = MyTools(this).changeBlockedList(userInfo.id.toString(),false)
                        Volley_ChangeBlockedUsers(this,newBlockedList , object : Volley_ChangeBlockedUsers.ServerCallBack{
                            override fun getSuccess(result: JSONObject) {
                                if ( result.getBoolean("isSuccess") ){
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@ViewContactActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                    DB_UserInfos(this@ViewContactActivity).syncBlocksFromSharedAndDB()
                                    userInfo.blocked = 0
                                    makeView()
                                    dialog.dismiss()
                                    dialog.cancel()
                                }else{
                                    Toast.makeText(this@ViewContactActivity, "Unblocking failed ... try again", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }).send()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        dialog.cancel()
                    }
                    .show()
            }else{
                AlertDialog.Builder(this)
                    .setTitle("Are you sure ?")
                    .setMessage("If you block this contact , you can't receive any message from this contact anymore . Do you want to block ${userInfo.username} ?")
                    .setCancelable(true)
                    .setPositiveButton("Yes , Block ${userInfo.username}") { dialog, _ ->
                        val newBlockedList = MyTools(this).changeBlockedList(userInfo.id.toString(),true)
                        Volley_ChangeBlockedUsers(this,newBlockedList , object : Volley_ChangeBlockedUsers.ServerCallBack{
                            override fun getSuccess(result: JSONObject) {
                                if ( result.getBoolean("isSuccess") ){
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@ViewContactActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                    DB_UserInfos(this@ViewContactActivity).syncBlocksFromSharedAndDB()
                                    userInfo.blocked = 1
                                    makeView()
                                    dialog.dismiss()
                                    dialog.cancel()
                                }else{
                                    Toast.makeText(this@ViewContactActivity, "Blocking failed ... try again", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }).send()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        dialog.cancel()
                    }
                    .show()
            }
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("If you block this contact , you can't receive any message from this contact anymore . Do you want to block ${userInfo.username} ?")
                .setCancelable(true)
                .setPositiveButton("Yes , Block ${userInfo.username}") { dialog, _ ->
                    val newBlockedList = MyTools(this).changeBlockedList(userInfo.id.toString(),true)
                    Volley_ChangeBlockedUsers(this,newBlockedList , object : Volley_ChangeBlockedUsers.ServerCallBack{
                        override fun getSuccess(result: JSONObject) {
                            if ( result.getBoolean("isSuccess") ){
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@ViewContactActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                DB_UserInfos(this@ViewContactActivity).syncBlocksFromSharedAndDB()
                                dialog.dismiss()
                                dialog.cancel()
                            }else{
                                Toast.makeText(this@ViewContactActivity, "Blocking failed ... try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }).send()

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    dialog.cancel()
                }
                .show()
        }

        binding.ViewContactActivityReportButton.setOnClickListener {
            val targetIntent = Intent(this,ContactUsActivity::class.java)
            targetIntent.putExtra("purpose","report")
            targetIntent.putExtra("reportUserId",userInfo.id)
            startActivity(targetIntent)
        }

        // Medias //////////////////////////////////////////////////////////////////////////////////
        binding.ViewContactActivityImagesText.setOnClickListener {
            val targetIntent = Intent(this,GalleryMediaActivity::class.java)
            targetIntent.putExtra("title","All Images")
            //targetIntent.putExtra("subtitle","ViewContact")
            targetIntent.putExtra("type","ViewContactPicture")
            targetIntent.putExtra("fromActivity","ViewContact")
            targetIntent.putExtra("contactId",userInfo.id.toString())
            startActivity(targetIntent)
        }

        binding.ViewContactActivityVideosText.setOnClickListener {
            val targetIntent = Intent(this,GalleryMediaActivity::class.java)
            targetIntent.putExtra("title","All Videos")
            //targetIntent.putExtra("subtitle","ViewContact")
            targetIntent.putExtra("type","ViewContactVideo")
            targetIntent.putExtra("fromActivity","ViewContact")
            targetIntent.putExtra("contactId",userInfo.id.toString())
            startActivity(targetIntent)
        }

        binding.ViewContactActivityFilesText.setOnClickListener {
            val targetIntent = Intent(this,GalleryFileActivity::class.java)
            //targetIntent.putExtra("title",media.path)
            //targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
            //targetIntent.putExtra("uri",media.path)
            targetIntent.putExtra("contactId",userInfo.id)
            startActivity(targetIntent)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}