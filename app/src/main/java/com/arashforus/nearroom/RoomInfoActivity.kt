package com.arashforus.nearroom

import android.app.ActivityOptions
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.trimmedLength
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityRoomInfoBinding
import com.bumptech.glide.Glide
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.json.JSONObject
import java.util.*

class RoomInfoActivity : AppCompatActivity() {

    lateinit var binding : ActivityRoomInfoBinding
    var roomId : Int = 0
    lateinit var roomInfo : Object_Nearoom
    var myId : Int = 0

    lateinit var naturalColor : ColorStateList
    lateinit var positiveColor : ColorStateList
    lateinit var negativeColor : ColorStateList
    var isChangeProfileEnable = false

    private lateinit var thumbNotificationConfig : UploadNotificationConfig
    private lateinit var profileNotificationConfig : UploadNotificationConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getIntExtra("roomId",0)
        naturalColor = this.resources.getColorStateList(R.color.colorPrimaryDark)
        positiveColor = this.resources.getColorStateList(R.color.buttonPositive)
        negativeColor = this.resources.getColorStateList(R.color.buttonNegetive)
        initToolbar()

        roomInfo = DB_NearoomInfos(this).getRoomDetail(roomId)!!
        myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int

        createNotificationChannel()
        createNotificationConfig()
        UploadServiceConfig.initialize(this.application, notificationChannelID, BuildConfig.DEBUG)

        if ( intent.hasExtra("purpose") ){
            if ( intent.getStringExtra("purpose")!!.toUpperCase(Locale.ROOT).trim() == "CHANGEPICTURE" ){
                val path = intent.getStringExtra("path")
                val thumbPath = intent.getStringExtra("thumbPath")
                //val imageName = intent.getStringExtra("imageName")
                uploadNearoomPic(path!!,thumbPath!!)
                //MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_ProfilePic, imageName)
            }
        }

        fetchRoomInfos()
        makeView()
        makeListeners()

    }

    private fun fetchRoomInfos() {
        Volley_GetNearoomInfos(this, arrayListOf(roomId), object : Volley_GetNearoomInfos.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                makeView()
            }
        }).getOnce()
    }

    private fun initToolbar(){
        setSupportActionBar(binding.RoomInfoActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Room Info"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        roomInfo = DB_NearoomInfos(this).getRoomDetail(roomId)!!
        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.RoomInfoActivityRoomNameValue.isEnabled = false
        binding.RoomInfoActivityCategorySpinner.visibility = View.GONE
        binding.RoomInfoActivityDescriptionValue.isEnabled = false
        binding.RoomInfoActivityCapacitySpinner.visibility = View.GONE
        binding.RoomInfoActivityCategoryValue.visibility = View.VISIBLE
        binding.RoomInfoActivityCapacityValue.visibility = View.VISIBLE

        if ( DB_NearoomsParticipant(this).isAdmin(roomId,myId) ){
            binding.RoomInfoActivityRemovePictureButton.visibility = View.VISIBLE
            binding.RoomInfoActivityChangePictureButton.visibility = View.VISIBLE
            binding.RoomInfoActivityChangeProfileButton.visibility = View.VISIBLE
            binding.RoomInfoActivityRemoveNearoomButton.visibility = View.VISIBLE
        }else{
            binding.RoomInfoActivityRemovePictureButton.visibility = View.GONE
            binding.RoomInfoActivityChangePictureButton.visibility = View.GONE
            binding.RoomInfoActivityChangeProfileButton.visibility = View.GONE
            binding.RoomInfoActivityRemoveNearoomButton.visibility = View.GONE
        }

        binding.RoomInfoActivityRoomNameValue.setText(roomInfo.roomname)
        binding.RoomInfoActivityCategoryValue.text = roomInfo.category
        if ( roomInfo.description.isNullOrEmpty()) {
            binding.RoomInfoActivityDescriptionValue.setText("No description")
            //binding.ViewContactActivityStatusValue.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.RoomInfoActivityDescriptionValue.alpha = 0.5f
        }else{
            binding.RoomInfoActivityDescriptionValue.setText(roomInfo.description)
        }

        binding.RoomInfoActivityCapacityValue.text = "${roomInfo.joined} / ${roomInfo.capacity}"

        if ( roomInfo.pic.isNullOrEmpty()){
            Glide.with(this)
                .load(R.drawable.default_nearoom)
                .centerInside()
                .into(binding.RoomInfoActivityRoomPic)
        }else{
            if (MyInternalStorage(this).isNearoomAvailable(roomInfo.pic!!)){
                Glide.with(this)
                    .load(MyInternalStorage(this).getNearoomPicString(roomInfo.pic!!))
                    .error(R.drawable.default_nearoom)
                    .centerInside()
                    .into(binding.RoomInfoActivityRoomPic)
            }else{
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbNearoomPicString(roomInfo.pic!!))
                    .error(R.drawable.default_nearoom)
                    .centerInside()
                    .into(binding.RoomInfoActivityRoomPic)
                AndroidNetworking.download( MyServerSide().getNearoomPicUri(roomInfo.pic!!), MyInternalStorage(this).getNearoomFolder(), roomInfo.pic)
                    .setTag(roomInfo.pic)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@RoomInfoActivity)
                                .load(MyInternalStorage(this@RoomInfoActivity).getNearoomPicString(roomInfo.pic!!))
                                .error(R.drawable.default_nearoom)
                                .centerInside()
                                .into(binding.RoomInfoActivityRoomPic)
                        }
                        override fun onError(anError: ANError?) {
                            //println("download $logo  has error")
                        }
                    })
            }
        }

        // Statics /////////////////////////////////////////////////////////////////////////////////
        val messagesSend = DB_NearroomMessages(this).countMessagesOfRoom(roomId)
        val daysJoined = DB_NearoomsParticipant(this).getJoinedTime(roomId , myId )
        val daysCreated = DB_NearoomInfos(this).getCreatedTime(roomId)
        binding.RoomInfoActivityMessagesSentNumber.text = messagesSend.toString()
        if ( daysJoined == null ){
            binding.RoomInfoActivityDaysJoinedNumber.text = "N/A"
        }else{
            binding.RoomInfoActivityDaysJoinedNumber.text = MyDateTime().DayDiff(daysJoined).toString()
            if ( MyDateTime().DayDiff(daysJoined) <= 1 ){
                binding.RoomInfoActivityDaysJoinedUnit.text = "day ago"
            }
        }
        if ( daysCreated == null ){
            binding.RoomInfoActivityDaysCreatedNumber.text = "N/A"
        }else{
            binding.RoomInfoActivityDaysCreatedNumber.text = MyDateTime().DayDiff(daysCreated).toString()
            if ( MyDateTime().DayDiff(daysCreated) <= 1 ){
                binding.RoomInfoActivityDaysCreatedUnit.text = "day ago"
            }
        }

        // Members /////////////////////////////////////////////////////////////////////////////////

        val membersJoined = DB_NearoomsParticipant(this).roomMembers(roomId)
        binding.RoomInfoActivityMemberNumberText.setText("${membersJoined.size} members")
        if ( membersJoined.size == 0 ){
            binding.RoomInfoActivityNoMemberText.visibility = View.VISIBLE
        }else{
            val membersAdapter = Adapter_RoomInfoMembers(membersJoined)
            binding.RoomInfoActivityMembersRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            //binding.ViewContactActivityFilesRecyclerView.setHasFixedSize(true)
            binding.RoomInfoActivityMembersRecyclerView.adapter = membersAdapter
        }

        // Actions /////////////////////////////////////////////////////////////////////////////////
        if ( roomInfo.mute == null ){
            roomInfo.mute = 0
        }
        binding.RoomInfoActivityMuteNotificationSwitch.isChecked = roomInfo.mute != 0

        // Medias //////////////////////////////////////////////////////////////////////////////////
        val images = DB_NearroomMessages(this).getLastImagesOfRoom(roomId,10)
        if (images.size == 0){
            binding.RoomInfoActivityNoImageText.visibility = View.VISIBLE
        }else{
            val imagesAdapter = Adapter_ViewContactMedias(images)
            binding.RoomInfoActivityImagesRecyclerView.layoutManager = GridLayoutManager(this, images.size)
            binding.RoomInfoActivityImagesRecyclerView.setHasFixedSize(true)
            binding.RoomInfoActivityImagesRecyclerView.adapter = imagesAdapter
        }

        val videos = DB_NearroomMessages(this).getLastVideosOfRoom(roomId,10)
        if ( videos.size == 0){
            binding.RoomInfoActivityNoVideoText.visibility = View.VISIBLE
        }else{
            val videosAdapter = Adapter_ViewContactMedias(videos)
            //println("Media size ........ "+images.size)
            binding.RoomInfoActivityVideosRecyclerView.layoutManager = GridLayoutManager(this, videos.size)
            binding.RoomInfoActivityVideosRecyclerView.setHasFixedSize(true)
            binding.RoomInfoActivityVideosRecyclerView.adapter = videosAdapter
        }

        val files = DB_NearroomMessages(this).getLastFilesOfRoom(roomId,10)
        if ( files.size == 0 ){
            binding.RoomInfoActivityNoFileText.visibility = View.VISIBLE
        }else{
            val filesAdapter = Adapter_ViewContactMedias(files)
            //println("Media size ........ "+images.size)
            binding.RoomInfoActivityFilesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            //binding.ViewContactActivityFilesRecyclerView.setHasFixedSize(true)
            binding.RoomInfoActivityFilesRecyclerView.adapter = filesAdapter
        }


    }

    private fun makeListeners() {
        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.RoomInfoActivityRoomPic.setOnClickListener {
            val intent = Intent(this,ProfilePicViewActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.RoomInfoActivityRoomPic,"pic"),
                Pair.create(binding.RoomInfoActivityRoomNameValue,"title"))
            intent.putExtra("roomId",roomId)
            startActivity(intent,options.toBundle())

        }

        binding.RoomInfoActivityChangePictureButton.setOnClickListener {
            val galleryIntent = Intent(this,GalleryAlbumActivity::class.java)
            galleryIntent.putExtra("title","Select nearoom pic")
            galleryIntent.putExtra("type","PICTURE")
            galleryIntent.putExtra("fromActivity","RoomInfo")
            galleryIntent.putExtra("roomId",roomId)
            startActivity(galleryIntent)
        }

        binding.RoomInfoActivityRemovePictureButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("You want to delete nearoom picture , are you sure ?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    Volley_DeleteNearoomPic(this, roomId, object : Volley_DeleteNearoomPic.ServerCallBack {
                        override fun getSuccess(result: JSONObject) {
                            //Toast.makeText( this@RoomInfoActivity,"Nearoom picture successfully removed", Toast.LENGTH_LONG ).show()
                            fetchRoomInfos()
                        }
                    }).send()
                    dialog.dismiss()
                    dialog.cancel()
                }
                .create()
                .show()
        }

        binding.RoomInfoActivityChangeProfileButton.setOnClickListener {
            if ( isChangeProfileEnable ){
                // Confirm /////////////////////////////////////////////////////////////////////////
                if ( binding.RoomInfoActivityRoomNameValue.text.length in MyRules.minRoomNameLength until MyRules.maxRoomNameLength){
                    Volley_ChangeNearoomInfo(this, myId, roomId,
                        binding.RoomInfoActivityRoomNameValue.text.toString(),
                        binding.RoomInfoActivityCategorySpinner.selectedItem.toString(),
                        binding.RoomInfoActivityDescriptionValue.text.toString(),
                        binding.RoomInfoActivityCapacitySpinner.selectedItem.toString(),
                        object : Volley_ChangeNearoomInfo.ServerCallBack {
                            override fun getSuccess(result: JSONObject) {
                                if (result.getBoolean("isSuccess")) {
                                    Toast.makeText(this@RoomInfoActivity, "Nearoom info changed successfully", Toast.LENGTH_SHORT).show()
                                    binding.RoomInfoActivityCancelProfileButton.visibility = View.GONE
                                    binding.RoomInfoActivityCancelProfileButton.backgroundTintList = naturalColor
                                    binding.RoomInfoActivityChangeProfileButton.text = "Change"
                                    binding.RoomInfoActivityChangeProfileButton.backgroundTintList = naturalColor

                                    isChangeProfileEnable = false
                                    fetchRoomInfos()
                                } else {
                                    if ( result.getBoolean("isRoomNameExist") ){
                                        Toast.makeText(this@RoomInfoActivity, "This room name is selected by another , select another one ...", Toast.LENGTH_LONG).show()
                                    }else{
                                        if ( result.getBoolean("isCapacityFit") ){
                                            Toast.makeText(this@RoomInfoActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                                        }else{
                                            Toast.makeText(this@RoomInfoActivity, "Room capacity is smaller than joined users , select more capacity ...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                        }).send()
                }else{
                    Toast.makeText(this, "Room name must be between ${MyRules.minRoomNameLength} and ${MyRules.maxRoomNameLength}", Toast.LENGTH_SHORT).show()
                }
            }else{
                // Change //////////////////////////////////////////////////////////////////////////
                binding.RoomInfoActivityCancelProfileButton.visibility = View.VISIBLE
                binding.RoomInfoActivityCancelProfileButton.backgroundTintList = negativeColor
                binding.RoomInfoActivityChangeProfileButton.text = "Confirm"
                binding.RoomInfoActivityChangeProfileButton.backgroundTintList = positiveColor

                when ( roomInfo.category?.toUpperCase(Locale.ROOT) ){
                    "GENERAL" -> binding.RoomInfoActivityCategorySpinner.setSelection(0)
                    "CAFE/RESTAURANT" -> binding.RoomInfoActivityCategorySpinner.setSelection(1)
                    "STORE/MALL" -> binding.RoomInfoActivityCategorySpinner.setSelection(2)
                    "CINEMA/THEATRE" -> binding.RoomInfoActivityCategorySpinner.setSelection(3)
                    "PARK" -> binding.RoomInfoActivityCategorySpinner.setSelection(4)
                    "HOUSE/APARTMENT" -> binding.RoomInfoActivityCategorySpinner.setSelection(5)
                    "ACTIVITY" -> binding.RoomInfoActivityCategorySpinner.setSelection(6)
                    "FRIENDHUB" -> binding.RoomInfoActivityCategorySpinner.setSelection(7)
                    "WORK" -> binding.RoomInfoActivityCategorySpinner.setSelection(8)
                    else -> binding.RoomInfoActivityCategorySpinner.setSelection(0)
                }

                when ( roomInfo.capacity ){
                    10 -> binding.RoomInfoActivityCapacitySpinner.setSelection(0)
                    20 -> binding.RoomInfoActivityCapacitySpinner.setSelection(1)
                    30 -> binding.RoomInfoActivityCapacitySpinner.setSelection(2)
                    40 -> binding.RoomInfoActivityCapacitySpinner.setSelection(3)
                    50 -> binding.RoomInfoActivityCapacitySpinner.setSelection(4)
                    60 -> binding.RoomInfoActivityCapacitySpinner.setSelection(5)
                    70 -> binding.RoomInfoActivityCapacitySpinner.setSelection(6)
                    80 -> binding.RoomInfoActivityCapacitySpinner.setSelection(7)
                    90 -> binding.RoomInfoActivityCapacitySpinner.setSelection(8)
                    100 -> binding.RoomInfoActivityCapacitySpinner.setSelection(9)
                    else -> binding.RoomInfoActivityCategorySpinner.setSelection(0)
                }

                binding.RoomInfoActivityCategorySpinner.visibility = View.VISIBLE
                binding.RoomInfoActivityCategoryValue.visibility = View.GONE
                binding.RoomInfoActivityCapacitySpinner.visibility = View.VISIBLE
                binding.RoomInfoActivityCapacityValue.visibility = View.GONE
                binding.RoomInfoActivityRoomNameValue.isEnabled = true
                binding.RoomInfoActivityDescriptionValue.isEnabled = true
                binding.RoomInfoActivityRoomNameValue.showSoftInputOnFocus = true
                binding.RoomInfoActivityRoomNameValue.requestFocus(View.FOCUS_UP)

                if ( roomInfo.description.isNullOrEmpty() ){
                    binding.RoomInfoActivityDescriptionValue.setText("")
                    binding.RoomInfoActivityDescriptionValue.alpha = 1.0f
                }

                isChangeProfileEnable = true
            }
        }

        binding.RoomInfoActivityCancelProfileButton.setOnClickListener {
            // Cancel //////////////////////////////////////////////////////////////////////////////
            binding.RoomInfoActivityRoomNameValue.setText(roomInfo.roomname)
            binding.RoomInfoActivityCategoryValue.text = roomInfo.category
            if ( roomInfo.description.isNullOrEmpty()) {
                binding.RoomInfoActivityDescriptionValue.setText("No description")
                //binding.ViewContactActivityStatusValue.textAlignment = View.TEXT_ALIGNMENT_CENTER
                binding.RoomInfoActivityDescriptionValue.alpha = 0.5f
            }else{
                binding.RoomInfoActivityDescriptionValue.setText(roomInfo.description)
            }
            binding.RoomInfoActivityCapacityValue.text = "${roomInfo.joined} / ${roomInfo.capacity}"

            binding.RoomInfoActivityCategorySpinner.visibility = View.GONE
            binding.RoomInfoActivityCategoryValue.visibility = View.VISIBLE
            binding.RoomInfoActivityCapacitySpinner.visibility = View.GONE
            binding.RoomInfoActivityCapacityValue.visibility = View.VISIBLE
            binding.RoomInfoActivityRoomNameValue.isEnabled = false
            binding.RoomInfoActivityDescriptionValue.isEnabled = false

            binding.RoomInfoActivityCancelProfileButton.visibility = View.GONE
            binding.RoomInfoActivityCancelProfileButton.backgroundTintList = naturalColor
            binding.RoomInfoActivityChangeProfileButton.text = "Change"
            binding.RoomInfoActivityChangeProfileButton.backgroundTintList = naturalColor

            isChangeProfileEnable = false
        }

        binding.RoomInfoActivityCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                ( parent?.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                ( parent.getChildAt(0) as TextView).textSize = 18f
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.RoomInfoActivityCapacitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                if ( parent?.getItemAtPosition(position).toString().toInt() < roomInfo.joined!! + 20 ){
                    Toast.makeText(this@RoomInfoActivity, "Capacity of nearoom must be more than members", Toast.LENGTH_SHORT).show()
                    when ( roomInfo.capacity ){
                        10 -> binding.RoomInfoActivityCapacitySpinner.setSelection(0)
                        20 -> binding.RoomInfoActivityCapacitySpinner.setSelection(1)
                        30 -> binding.RoomInfoActivityCapacitySpinner.setSelection(2)
                        40 -> binding.RoomInfoActivityCapacitySpinner.setSelection(3)
                        50 -> binding.RoomInfoActivityCapacitySpinner.setSelection(4)
                        60 -> binding.RoomInfoActivityCapacitySpinner.setSelection(5)
                        70 -> binding.RoomInfoActivityCapacitySpinner.setSelection(6)
                        80 -> binding.RoomInfoActivityCapacitySpinner.setSelection(7)
                        90 -> binding.RoomInfoActivityCapacitySpinner.setSelection(8)
                        100 -> binding.RoomInfoActivityCapacitySpinner.setSelection(9)
                        else -> binding.RoomInfoActivityCategorySpinner.setSelection(0)
                    }
                }
                ( parent?.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                ( parent.getChildAt(0) as TextView).textSize = 18f
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Actions /////////////////////////////////////////////////////////////////////////////////
        binding.RoomInfoActivityMuteNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if ( isChecked ){
                DB_NearoomInfos(this).setMute(roomId,1)
            }else{
                DB_NearoomInfos(this).setMute(roomId,0)
            }
        }


        binding.RoomInfoActivityReportButton.setOnClickListener {
            val targetIntent = Intent(this,ContactUsActivity::class.java)
            targetIntent.putExtra("purpose","reportRoom")
            targetIntent.putExtra("reportRoomId",roomId)
            startActivity(targetIntent)
        }

        binding.RoomInfoActivityLeaveButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Do you want to leave ${roomInfo.roomname} ?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    Volley_LeaveNearoom(this, roomId, myId, object : Volley_LeaveNearoom.ServerCallBack{
                        override fun getSuccess(result: JSONObject) {
                            if ( result.getBoolean("isSuccess") && !result.isNull("chat") ){
                                DB_NearroomMessages(this@RoomInfoActivity).saveToDB(result)
                                DB_NearoomsParticipant(this@RoomInfoActivity).setJoinedStatus(roomId , myId , 1)
                                Toast.makeText(this@RoomInfoActivity, "You leave this nearoom successfully", Toast.LENGTH_SHORT).show()
                            }else{
                                if ( result.getBoolean("isParticipant") ){
                                    Toast.makeText(this@RoomInfoActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(this@RoomInfoActivity, "You are no longer member of this nearoom", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }).send()
                    dialog.dismiss()
                    dialog.cancel()
                }
                .create()
                .show()

        }

        binding.RoomInfoActivityRemoveNearoomButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Do you want to remove ${roomInfo.roomname} ?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    Volley_RemoveNearoom(this , roomId , myId , object: Volley_RemoveNearoom.ServerCallBack{
                        override fun getSuccess(result: JSONObject) {
                            if ( result.getBoolean("isSuccess") ){
                                Toast.makeText(this@RoomInfoActivity, "Nearoom removed successfully", Toast.LENGTH_SHORT).show()
                                DB_NearroomMessages(this@RoomInfoActivity).saveToDB(result)
                                DB_NearoomsParticipant(this@RoomInfoActivity).setJoinedStatus(roomId , myId , 1)
                                DB_NearoomInfos(this@RoomInfoActivity).setJoin(roomId , 0)
                            }else{
                                if ( result.getBoolean("isAdmin") ){
                                    Toast.makeText(this@RoomInfoActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(this@RoomInfoActivity, "You aren't admin to remove this nearoom", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }).send()
                    dialog.dismiss()
                    dialog.cancel()
                }
                .create()
                .show()

        }

        // Medias //////////////////////////////////////////////////////////////////////////////////
        binding.RoomInfoActivityImagesText.setOnClickListener {
            val targetIntent = Intent(this,GalleryMediaActivity::class.java)
            targetIntent.putExtra("title","All Images")
            //targetIntent.putExtra("subtitle","ViewContact")
            targetIntent.putExtra("type","RoomInfoPicture")
            targetIntent.putExtra("fromActivity","RoomInfo")
            targetIntent.putExtra("roomId",roomId.toString())
            startActivity(targetIntent)
        }

        binding.RoomInfoActivityVideosText.setOnClickListener {
            val targetIntent = Intent(this,GalleryMediaActivity::class.java)
            targetIntent.putExtra("title","All Videos")
            //targetIntent.putExtra("subtitle","ViewContact")
            targetIntent.putExtra("type","RoomInfoVideo")
            targetIntent.putExtra("fromActivity","RoomInfo")
            targetIntent.putExtra("roomId",roomId.toString())
            startActivity(targetIntent)
        }

        binding.RoomInfoActivityFilesText.setOnClickListener {
            val targetIntent = Intent(this,GalleryFileActivity::class.java)
            //targetIntent.putExtra("title",media.path)
            //targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
            //targetIntent.putExtra("uri",media.path)
            targetIntent.putExtra("roomId",roomId)
            startActivity(targetIntent)
        }

    }

    private fun uploadNearoomPic(path : String , thumbPath : String ){

        MultipartUploadRequest(this,MyServerSide.saveThumbNearoomPic_address)
            .setMethod("POST")
            .addFileToUpload(thumbPath,"thumbNearoomPic")
            .setNotificationConfig { context, uploadId ->
                thumbNotificationConfig
            }
            .subscribe(this,this, object : RequestObserverDelegate {
                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {  }
                override fun onCompletedWhileNotObserving() {   }
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable ) { println(exception.message) }
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    //println(serverResponse.bodyString)
                }
            })

        MultipartUploadRequest(this,MyServerSide.saveNearoomPic_address)
            .setMethod("POST")
            .addFileToUpload(path,"nearoomPic")
            .addParameter("roomId", roomId.toString())
            .setNotificationConfig { context, uploadId ->
                profileNotificationConfig
            }
            .subscribe(this, this, object : RequestObserverDelegate {
                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                override fun onCompletedWhileNotObserving() {    }
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    //println(serverResponse.bodyString)
                    fetchRoomInfos()
                }
            })
    }

    private fun createNotificationConfig() {
        thumbNotificationConfig = UploadNotificationConfig( notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            )
        )

        profileNotificationConfig = UploadNotificationConfig(
            notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Upload complete" ,
                "Nearoom picture uploaded successfully",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading nearoom picture" ,
                "Nearoom picture isn't uploading ... try again",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                false,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading canceled" ,
                "Nearoom picture uploading is canceled by you ",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            )
        )
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel( notificationChannelID,"Upload Nearoom Picture", NotificationManager.IMPORTANCE_NONE )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if ( intent.hasExtra("fromActivity") ){
            if ( intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT).trim() == "MAINACTIVITY" ){
                val targetIntent = Intent(this  ,MainActivity::class.java)
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
                startActivity(targetIntent)
                finish()
            }else{
                val targetIntent = Intent(this  ,RoomActivity::class.java)
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
                startActivity(targetIntent)
                finish()
            }
        }


    }

    companion object {
        const val notificationChannelID = "UploadNearoomPicture"
    }
}