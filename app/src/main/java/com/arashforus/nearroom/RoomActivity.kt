package com.arashforus.nearroom

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Pair
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityRoomBinding
import com.bumptech.glide.Glide
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RoomActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRoomBinding

    private var myId = 0
    private var roomId : Int = 0
    lateinit var roomInfo : Object_Nearoom
    lateinit var roomInfoUpdate : Object_Nearoom

    private var firstTimeAdapter = true
    private var firstItem = 0
    private var newestMessageId = 0
    private var isSearchMode = false
    private var resultPositions = ArrayList<Int>()
    private var searchWord : CharSequence = ""
    private var currentSearchResult = 0
    //private var isBlocked = false

    private lateinit var memberIds : ArrayList<Int>
    private lateinit var messages : ArrayList<Object_NearoomMessage>
    private lateinit var adapter : Adapter_NearoomMessages

    private var lasttimetype : Array<Int> = arrayOf(0,0,0)
    private var timerUserInfo : Timer = Timer()
    private var timerOnline : Timer = Timer()
    private var timerUpdateMessages : Timer = Timer()
    private var timerUpdateNearoomParticipants : Timer = Timer()
    private var timerGetRoomInfo : Timer = Timer()

    private val cameraPermissionRequestCode = 1300
    private val readExternalStorageRequestCode = 1301

    private val requestCodeCamera = 1600
    private val requestCodePic = 1601
    private val requestCodeVid = 1602
    private val requestCodeAud = 1603
    private val requestCodeFile = 1604

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyTools(this).resizableWindow(this)
        myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
        roomId = intent.getIntExtra("roomId",0)
        roomInfo = DB_NearoomInfos(this).getRoomDetail(roomId)!!

        memberIds = DB_NearoomsParticipant(this).roomParticipantId(roomId)

        makeLiveWallpaper()
        initToolbar()
        initAdaptor()
        createNotificationChannel()
        UploadServiceConfig.initialize(this.application, notificationChannelID, BuildConfig.DEBUG)
        MyContactsChange(this).register()

        makeListeners()
        makeResultFunctions()

    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, IntentFilter("NewMessageFromFirebase"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver)
        Adapter_Messages.seekTimer.also {
            it.cancel()
            it.purge()
        }
        Adapter_Messages.mediaPlayer.also {
            it.release()
        }
        if ( adapter.messages.size > 0 ){
            DB_NearroomMessages(this).saveSeen(roomId,adapter.messages[0].id)
        }

    }

    override fun onResume() {
        super.onResume()
        // Save active activity to shared pref /////////////////////////////////////////////////////
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_RunningActivity,"Room")
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_ChatTo,roomId)

        // Active send online time to server ///////////////////////////////////////////////////////
        timerOnline = Volley_SendOnline(this, myId).send()

        // Active get user infos from server ///////////////////////////////////////////////////////
        timerUserInfo = Volley_GetUserInfos(this, memberIds, object : Volley_GetUserInfos.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                val status = MyStatus(this@RoomActivity).findForRooms(roomId,memberIds)
                if (status == null ){
                    binding.RoomActivityToolbarSubtitle.visibility = View.GONE
                }else{
                    binding.RoomActivityToolbarSubtitle.visibility = View.VISIBLE
                    binding.RoomActivityToolbarSubtitle.text = status
                }
            }
        }).get()

        // Active getting latest message and update DB /////////////////////////////////////////////
        timerUpdateMessages = Volley_GetLatestNearoomMessage(this, object : Volley_GetLatestNearoomMessage.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                if ( !result.isNull("chat") ){
                    updateMessages(false)
                }
            }
        }).get()

        // Active getting room participants and update DB //////////////////////////////////////////
        timerUpdateNearoomParticipants = Volley_GetNearoomParticipants(this, null , roomId , object : Volley_GetNearoomParticipants.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                memberIds = DB_NearoomsParticipant(this@RoomActivity).roomParticipantId(roomId)
            }
        }).get()

        // Active getting room information from server /////////////////////////////////////////////
        timerGetRoomInfo = Volley_GetNearoomInfos(this, arrayListOf(roomId),object : Volley_GetNearoomInfos.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                roomInfoUpdate = DB_NearoomInfos(this@RoomActivity).getRoomDetail(roomId)!!
                if ( roomInfoUpdate != roomInfo ){
                    roomInfo = roomInfoUpdate
                    initToolbar()
                }
            }
        }).get()
    }

    override fun onPause() {
        super.onPause()
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_RunningActivity,"Null")
        timerUserInfo.cancel()
        timerUserInfo.purge()
        timerOnline.cancel()
        timerOnline.purge()
        timerUpdateMessages.cancel()
        timerUpdateMessages.purge()
        timerUpdateNearoomParticipants.cancel()
        timerUpdateNearoomParticipants.purge()
        timerGetRoomInfo.cancel()
        timerGetRoomInfo.purge()
    }

    override fun onDestroy() {
        super.onDestroy()
        MyContactsChange(this).unregister()
    }

    private fun makeLiveWallpaper() {
        when ( MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_Name,"String").toString().toUpperCase(Locale.ROOT)){
            "SOLID" -> {
                binding.RoomActivityBackgroundParticle.visibility =View.GONE
                binding.RoomActivityBackgroundBubbles.visibility = View.GONE
                binding.RoomActivityBackgroundRainbow.visibility = View.GONE
                binding.RoomActivityBackgroundOther.visibility = View.VISIBLE
                val backColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_SolidColor,"Int") as Int
                binding.RoomActivityBackgroundOther.setBackgroundColor(backColor)
            }
            "PARTICLE" -> {
                binding.RoomActivityBackgroundParticle.visibility =View.VISIBLE
                binding.RoomActivityBackgroundBubbles.visibility = View.GONE
                binding.RoomActivityBackgroundRainbow.visibility = View.GONE
                binding.RoomActivityBackgroundOther.visibility = View.GONE
                val particleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleNums,"Int",-1) as Int
                val particleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSpeed,"Int",-1) as Int
                val particleSize = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSize,"Int",-1) as Int
                val particleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleColor,"Int",-1) as Int
                val particleBackColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleBackgroundColor,"Int",-1) as Int
                if ( particleNum != -1 ){
                    binding.RoomActivityBackgroundParticle.setNumbers(particleNum)
                }
                if ( particleSpeed != -1 ){
                    binding.RoomActivityBackgroundParticle.setSpeed(particleSpeed)
                }
                if ( particleSize != -1 ){
                    binding.RoomActivityBackgroundParticle.setSize(particleSize)
                }
                if ( particleColor != -1 ){
                    binding.RoomActivityBackgroundParticle.setColor(particleColor)
                }
                if ( particleBackColor != -1 ){
                    binding.RoomActivityBackgroundParticle.setBackground(particleBackColor)
                }
            }
            "BUBBLES" -> {
                binding.RoomActivityBackgroundParticle.visibility =View.GONE
                binding.RoomActivityBackgroundBubbles.visibility = View.VISIBLE
                binding.RoomActivityBackgroundRainbow.visibility = View.GONE
                binding.RoomActivityBackgroundOther.visibility = View.GONE
                val bubbleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesNums,"Int",-1) as Int
                val bubbleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesSpeed,"Int",-1) as Int
                val bubbleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesColor,"Int",-1) as Int
                val bubbleBackground = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesBackgroundColor,"Int",-1) as Int
                if ( bubbleNum != -1 ){
                    binding.RoomActivityBackgroundBubbles.setNumbers(bubbleNum)
                }
                if ( bubbleSpeed != -1 ){
                    binding.RoomActivityBackgroundBubbles.setSpeed(bubbleSpeed)
                }
                if ( bubbleColor != -1 ){
                    binding.RoomActivityBackgroundBubbles.setColor(bubbleColor)
                }
                if ( bubbleBackground != -1 ){
                    binding.RoomActivityBackgroundBubbles.setBackground(bubbleBackground)
                }
            }
            "RAINBOW" -> {
                binding.RoomActivityBackgroundParticle.visibility =View.GONE
                binding.RoomActivityBackgroundBubbles.visibility = View.GONE
                binding.RoomActivityBackgroundRainbow.visibility = View.VISIBLE
                binding.RoomActivityBackgroundOther.visibility = View.GONE
                val rainbowAlpha = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_RainbowAlpha,"Int",-1) as Int
                val rainbowSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_RainbowSpeed,"Int",-1) as Int
                if ( rainbowAlpha != -1 ){
                    binding.RoomActivityBackgroundRainbow.setAlpha(rainbowAlpha)
                }
                if ( rainbowSpeed != -1 ){
                    binding.RoomActivityBackgroundRainbow.setSpeed(rainbowSpeed)
                }
            }
            "PICTURE" -> {
                binding.RoomActivityBackgroundParticle.visibility =View.GONE
                binding.RoomActivityBackgroundBubbles.visibility = View.GONE
                binding.RoomActivityBackgroundRainbow.visibility = View.GONE
                binding.RoomActivityBackgroundOther.visibility = View.VISIBLE
                val picBackground = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ImagePath,"String") as String
                binding.RoomActivityBackgroundOther.background = Drawable.createFromPath(picBackground)
            }
            else -> {
                binding.RoomActivityBackgroundParticle.visibility =View.VISIBLE
                binding.RoomActivityBackgroundBubbles.visibility = View.GONE
                binding.RoomActivityBackgroundRainbow.visibility = View.GONE
                binding.RoomActivityBackgroundOther.visibility = View.GONE
                val particleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleNums,"Int",-1) as Int
                val particleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSpeed,"Int",-1) as Int
                val particleSize = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSize,"Int",-1) as Int
                val particleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleColor,"Int",-1) as Int
                val particleBackColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleBackgroundColor,"Int",-1) as Int
                if ( particleNum != -1 ){
                    binding.RoomActivityBackgroundParticle.setNumbers(particleNum)
                }
                if ( particleSpeed != -1 ){
                    binding.RoomActivityBackgroundParticle.setSpeed(particleSpeed)
                }
                if ( particleSize != -1 ){
                    binding.RoomActivityBackgroundParticle.setSize(particleSize)
                }
                if ( particleColor != -1 ){
                    binding.RoomActivityBackgroundParticle.setColor(particleColor)
                }
                if ( particleBackColor != -1 ){
                    binding.RoomActivityBackgroundParticle.setBackground(particleBackColor)
                }
            }
        }
    }

    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("New Notification received from firebase to room activity ......")
            updateMessages(true)
        }
    }

    private fun initToolbar(){
        setSupportActionBar(binding.RoomActivityToolbar)
        // custom toolbar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)

        Glide.with(this)
            .load(if ( roomInfo.pic.isNullOrEmpty() ){
                R.drawable.default_nearoom
            }else{
                if ( MyInternalStorage(this).isNearoomThumbAvailable(roomInfo.pic!!) ){
                    MyInternalStorage(this).getThumbNearoomPicString(roomInfo.pic!!)
                }else{
                    R.drawable.default_nearoom
                    AndroidNetworking.download( MyServerSide().getThumbNearoomUri(roomInfo.pic!!), MyInternalStorage(this).getThumbNearoomFolder(), roomInfo.pic)
                        .setTag(roomInfo.pic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(this@RoomActivity)
                                    .load(MyInternalStorage(this@RoomActivity).getThumbNearoomPicString(roomInfo.pic!!))
                                    .error(R.drawable.default_nearoom)
                                    .circleCrop()
                                    .into(binding.RoomActivityToolbarPic)
                            }
                            override fun onError(anError: ANError?) {
                                println("download ${roomInfo.pic}  has error")
                            }
                        })
                }
            })
            .error(R.drawable.default_nearoom)
            .circleCrop()
            .into(binding.RoomActivityToolbarPic)
        binding.RoomActivityToolbarTitle.text = roomInfo.roomname
        if ( roomInfo.description.isNullOrEmpty() ){
            binding.RoomActivityToolbarSubtitle.visibility = View.GONE
        }else{
            binding.RoomActivityToolbarSubtitle.visibility = View.VISIBLE
            binding.RoomActivityToolbarSubtitle.text = roomInfo.description
        }

        // Toolbar Listeners ///////////////////////////////////////////////////////////////////////
        binding.RoomActivityToolbarBackLayout.setOnClickListener {
            onBackPressed()
        }

        binding.RoomActivityToolbarPic.setOnClickListener {
            val intent = Intent(this,ProfilePicViewActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.RoomActivityToolbarPic,"pic"),
                Pair.create(binding.RoomActivityToolbarTitle,"title"))
            intent.putExtra("roomId",roomId)
            startActivity(intent,options.toBundle())
        }

        binding.RoomActivityToolbarTitleLayout.setOnClickListener {
            val intent = Intent(this,RoomInfoActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.RoomActivityToolbarPic,"pic"),
                Pair.create(binding.RoomActivityToolbarTitle,"title"))
            intent.putExtra("roomId",roomId)
            startActivity(intent,options.toBundle())
        }

        binding.RoomActivityToolbarVoicecall.setOnClickListener {
            startActivity(Intent(this,VoiceCallActivity::class.java))
        }

        binding.RoomActivityToolbarVideoCall.setOnClickListener {
            startActivity(Intent(this,VideoCallActivity::class.java))
        }

        val menuPopup = PopupMenu(this,binding.RoomActivityToolbarMenu, Gravity.BOTTOM)
        menuPopup.inflate(R.menu.room_menu)
        makeMenuListener(menuPopup)
        binding.RoomActivityToolbarMenu.setOnClickListener {
            menuPopup.show()
        }
    }

    private fun initAdaptor(){
        messages = DB_NearroomMessages(this).loadMessages(roomId)
        adapter = Adapter_NearoomMessages(messages)
        val rvLayout = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        rvLayout.reverseLayout = true
        rvLayout.stackFromEnd = true
        rvLayout.scrollToPosition(0)
        binding.RoomActivityRecyclerView.layoutManager = rvLayout
        binding.RoomActivityRecyclerView.adapter = adapter

        if ( firstTimeAdapter ){
            binding.RoomActivityRecyclerView.postDelayed({rvLayout.scrollToPosition(0)},500)
            firstItem = 0
            firstTimeAdapter = false
        }
        binding.RoomActivityRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if ( bottom < oldBottom ) {
                binding.RoomActivityRecyclerView.post {
                    //println(rvLayout.findFirstVisibleItemPosition())
                    if ( rvLayout.findFirstVisibleItemPosition() <= 7 ){
                        rvLayout.scrollToPosition(0)
                    }
                }
            }
        }

        // Check if new message received then send seen status to server ///////////////////////////
        if ( messages.size > 0 ){
            if ( messages[0].id > newestMessageId ){
                Volley_SendNearoomMessageStatus(this,messages[0].id,roomId,"SEEN").send()
                DB_NearroomMessages(this).saveSeen(roomId,messages[0].id)
                newestMessageId = messages[0].id
            }
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel( PrivateActivity.notificationChannelID,"Room Channel", NotificationManager.IMPORTANCE_HIGH )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun makeListeners(){
        // Send button listener ////////////////////////////////////////////////////////////////////
        binding.RoomActivitySendButtonLayout.setOnClickListener{
            if ( binding.RoomActivityMessage2.text.toString().trim() !== "" && binding.RoomActivityMessage2.text !== null ){
                sendMessage(binding.RoomActivityMessage2.text.toString().trim())
            }
            binding.RoomActivityMessage2.text.clear()

        }

        binding.RoomActivityMessage2.addTextChangedListener {
            if (MyDateTime().secDiffLocalClock(MyDateTime().getLocalClock(),lasttimetype) > 3 ){
                lasttimetype = MyDateTime().getLocalClock()
                Volley_IsTyping(this, myId ,"R$roomId").send()
            }
        }

        // Send Option Layout Show and gone state //////////////////////////////////////////////////
        var sendOption = false
        binding.RoomActivityOpenSendMenu.setOnClickListener {
            if ( !sendOption ){
                binding.RoomActivitySendOptionLayout.visibility = View.VISIBLE
                binding.RoomActivitySendOptionLayout.minHeight = binding.RoomActivitySendMessageLayout.height + 55
                binding.view7.visibility = View.VISIBLE
                ViewAnimationUtils.createCircularReveal(binding.RoomActivitySendOptionLayout,0,binding.RoomActivitySendOptionLayout.bottom,0f,binding.RoomActivitySendOptionLayout.width.toFloat()).apply {
                    duration = 200
                }.start()
                binding.RoomActivityOpenSendMenu.setImageResource(R.drawable.ic_baseline_arrow_downward)

            }else{
                binding.view7.visibility = View.GONE
                ViewAnimationUtils.createCircularReveal(binding.RoomActivitySendOptionLayout,0,binding.RoomActivitySendOptionLayout.bottom,binding.RoomActivitySendOptionLayout.width.toFloat(),0f).apply {
                    duration = 200
                    doOnEnd {
                        binding.RoomActivitySendOptionLayout.visibility = View.GONE
                    }
                }.start()
                binding.RoomActivityOpenSendMenu.setImageResource(R.drawable.ic_baseline_attach_file)
            }
            sendOption = !sendOption
        }

        binding.RoomActivityCameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
                }
            } else {
                showCamera()
            }
        }

        binding.RoomActivitySelectPicButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryPic()
            }
        }

        binding.RoomActivitySelectVideoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryVideo()
            }
        }

        binding.RoomActivitySelectSoundButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGallerySound()
            }
        }

        binding.RoomActivitySelectFileButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryFile()
            }
        }

        // Search Bar Listeners ////////////////////////////////////////////////////////////////////
        binding.RoomActivitySearchBar.SearchBarChatGoIcon.setOnClickListener {
            searchWord = binding.RoomActivitySearchBar.SearchBarChatText.text.trim()
            if ( searchWord.trim().length > 2 ){
                resultPositions.clear()
                var forCount = 0
                isSearchMode = true
                adapter.messages.forEach {
                    if ( !it.message.isNullOrEmpty() ){
                        if ( it.message!!.contains(searchWord) ){
                            it.selected = true
                            resultPositions.add(forCount)
                        }else{
                            it.selected = false
                        }
                    }
                    forCount += 1
                }
                binding.RoomActivitySearchBar.SearchBarChatResult.visibility = View.VISIBLE
                if ( resultPositions.size > 0 ){
                    binding.RoomActivitySearchBar.SearchBarChatResult.text = "1 of ${resultPositions.size} search found"
                    binding.RoomActivitySearchBar.SearchBarChatUpIcon.visibility = View.VISIBLE
                    binding.RoomActivitySearchBar.SearchBarChatDownIcon.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    currentSearchResult = 0
                    binding.RoomActivityRecyclerView.smoothScrollToPosition(resultPositions[0])

                }else{
                    binding.RoomActivitySearchBar.SearchBarChatResult.text = "Nothing found"
                }

            }else{
                binding.RoomActivitySearchBar.SearchBarChatResult.visibility = View.VISIBLE
                binding.RoomActivitySearchBar.SearchBarChatResult.text = "You should enter more than two letters for searching ..."
            }

        }

        binding.RoomActivitySearchBar.SearchBarChatCloseIcon.setOnClickListener {
            binding.RoomActivitySearchBar.SearchBarChatLayout.visibility = View.GONE
            isSearchMode = false
            binding.RoomActivityButtomLayout.visibility = View.VISIBLE
            updateMessages(false)
        }

        binding.RoomActivitySearchBar.SearchBarChatDownIcon.setOnClickListener {
            if ( currentSearchResult > 0 ){
                currentSearchResult -= 1
                binding.RoomActivitySearchBar.SearchBarChatResult.text = "${currentSearchResult+1} of ${resultPositions.size} search found"
                binding.RoomActivityRecyclerView.smoothScrollToPosition(resultPositions[currentSearchResult])
            }
        }

        binding.RoomActivitySearchBar.SearchBarChatUpIcon.setOnClickListener {
            if ( currentSearchResult < resultPositions.size - 1 ){
                currentSearchResult += 1
                binding.RoomActivitySearchBar.SearchBarChatResult.text = "${currentSearchResult+1} of ${resultPositions.size} search found"
                binding.RoomActivityRecyclerView.smoothScrollToPosition(resultPositions[currentSearchResult])
            }
        }

    }

    private fun makeMenuListener(menuPopup: PopupMenu) {
        menuPopup.setOnMenuItemClickListener {
            when ( it.itemId ){
                R.id.RoomMenu_RoomInfo -> {
                    val intent = Intent(this,RoomInfoActivity::class.java)
                    intent.putExtra("roomId",roomId)
                    startActivity(intent)
                    true
                }
                R.id.RoomMenu_Search -> {
                    binding.RoomActivitySearchBar.SearchBarChatLayout.visibility = View.VISIBLE
                    binding.RoomActivitySearchBar.SearchBarChatResult.visibility = View.GONE
                    binding.RoomActivityButtomLayout.visibility = View.GONE
                    true
                }
                R.id.RoomMenu_DeleteHistory -> {
                    AlertDialog.Builder(this)
                        .setTitle("Are you sure ?")
                        .setMessage("You select deleting history of chats , so after that you can't recover your deleted messages . Do you want to delete all chats in ${roomInfo.roomname} ?")
                        .setCancelable(true)
                        .setPositiveButton("Yes , Delete all") { dialog, _ ->
                            Volley_DeleteRoomChats(this,myId , roomId ,adapter.messages[0].id , object : Volley_DeleteRoomChats.ServerCallBack{
                                override fun getResponse(result: JSONObject) {
                                    if ( result.getBoolean("isSuccess") ){
                                        dialog.dismiss()
                                        dialog.cancel()
                                        DB_NearroomMessages(this@RoomActivity).deleteChats( roomId, adapter.messages[0].id )
                                        Toast.makeText(this@RoomActivity,"All messages delete successfully",Toast.LENGTH_SHORT ).show()
                                        updateMessages(false)
                                    }else{
                                        Toast.makeText(this@RoomActivity,"Something wrong ...",Toast.LENGTH_SHORT ).show()
                                    }
                                }

                            }).send()

                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            dialog.cancel()
                        }
                        .show()
                    true
                }
                R.id.RoomMenu_LeaveRoom -> {
                    AlertDialog.Builder(this)
                        .setTitle("Are you sure ?")
                        .setMessage("You want to leave ${roomInfo.roomname} ?")
                        .setCancelable(true)
                        .setPositiveButton("Yes , Leave") { dialog, _ ->
                            Volley_LeaveNearoom(this, roomId, myId, object : Volley_LeaveNearoom.ServerCallBack{
                                override fun getSuccess(result: JSONObject) {
                                    if ( result.getBoolean("isSuccess") && !result.isNull("chat") ){
                                        DB_NearroomMessages(this@RoomActivity).saveToDB(result)
                                        DB_NearoomsParticipant(this@RoomActivity).setJoinedStatus(roomId , myId , 1)
                                        Toast.makeText(this@RoomActivity, "You leave this nearoom successfully", Toast.LENGTH_SHORT).show()
                                    }else{
                                        if ( result.getBoolean("isParticipant") ){
                                            Toast.makeText(this@RoomActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                                        }else{
                                            Toast.makeText(this@RoomActivity, "You are no longer member of this nearoom", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                            }).send()
                            //DB_NearoomInfos(this).setJoin(roomname!!,0)
                            dialog.dismiss()
                            dialog.cancel()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            dialog.cancel()
                        }
                        .show()
                    true
                }
                R.id.PrivateMenu_Report -> {
                    AlertDialog.Builder(this)
                        .setTitle("Are you sure ?")
                        .setMessage("This room hesitate you ? So let us know why ...")
                        .setCancelable(true)
                        .setPositiveButton("Yes") { dialog, _ ->
                            val targetIntent = Intent(this,ContactUsActivity::class.java)
                            targetIntent.putExtra("purpose","ReportRoom")
                            targetIntent.putExtra("roomId",roomId)
                            dialog.dismiss()
                            dialog.cancel()
                            startActivity(targetIntent)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            dialog.cancel()
                        }
                        .show()
                    true
                }
                else -> true
            }
        }
    }

    private fun makeResultFunctions() {
        if ( intent.hasExtra("purpose") ){
            when ( intent.getStringExtra("purpose")?.toUpperCase(Locale.ROOT)?.trim() ){
                "SENDIMAGE" -> {
                    val imagePath  = intent.getStringExtra("path")
                    val imageUri = Uri.fromFile(File(imagePath!!))
                    val newImage = MyInternalStorage(this).saveSendPic(imageUri!!, myId )
                    //println(newImage.get(0)+"  "+newImage.get(1)+"  "+newImage.get(2))
                    val newMessage = Object_NearoomMessage("PIC", myId , roomId ,null,newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0,null,null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .subscribe(this,this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {  }
                            override fun onCompletedWhileNotObserving() {   }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) { println(exception.message) }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString) }
                        })
                    MultipartUploadRequest(this,MyServerSide.savePicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[0],"pic")
                        .addParameter("fromId",myId.toString())
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","PIC")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                "SENDVIDEO" -> {
                    val videoPath = intent.getStringExtra("path")
                    val videoUri = Uri.fromFile(File(videoPath!!))
                    val newVideo = MyInternalStorage(this).saveSendVideo(videoUri!!, myId)
                    val newMessage = Object_NearoomMessage("VID", myId , roomId ,null,newVideo[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0, null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[1],"thumbvid")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                    MultipartUploadRequest(this,MyServerSide.saveVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[0],"vid")
                        .addParameter("fromId",myId.toString())
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","VID")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                "SENDAUDIO" -> {
                    val audioPath = intent.getStringExtra("path")
                    val audioDuration = intent.getStringExtra("duration")
                    val audioUri = Uri.fromFile(File(audioPath!!))
                    val newAudio = MyInternalStorage(this).saveSendAudio(audioUri!!,myId)
                    val newMessage = Object_NearoomMessage("AUD", myId ,roomId ,audioDuration,newAudio[1],0,
                        MyDateTime().getLocalFullTime(),1,0,0, null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveAudio_address)
                        .setMethod("POST")
                        .addFileToUpload(newAudio[0],"aud")
                        .addParameter("fromId",myId.toString())
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","AUD")
                        .addParameter("duration",audioDuration!!)
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                                println("===========================================================================")
                                println(serverResponse.bodyString)
                                println("===========================================================================")
                            }
                        })

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when ( requestCode ){
                requestCodeCamera -> {
                    val image : Bitmap = data?.extras?.get("data") as Bitmap
                    val newImage = MyInternalStorage(this).saveSendPic(image,myId )
                    val newMessage = Object_NearoomMessage("PIC",myId ,roomId ,null,newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0 , null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .subscribe(this,this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {  }
                            override fun onCompletedWhileNotObserving() {   }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) { println(exception.message) }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString) }
                        })

                    MultipartUploadRequest(this,MyServerSide.savePicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[0],"pic")
                        .addParameter("fromId", myId.toString())
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","PIC")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                requestCodePic -> {
                    val image : Uri? = data?.data
                    val newImage = MyInternalStorage(this).saveSendPic(image!!, myId )
                    val newMessage = Object_NearoomMessage("PIC", myId, roomId ,null,newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0, null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .subscribe(this,this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {  }
                            override fun onCompletedWhileNotObserving() {   }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) { println(exception.message) }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString) }
                        })
                    MultipartUploadRequest(this,MyServerSide.savePicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[0],"pic")
                        .addParameter("fromId",myId.toString() )
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","PIC")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                requestCodeVid -> {
                    val video : Uri? = data?.data
                    val newVideo = MyInternalStorage(this).saveSendVideo(video!!,myId )
                    val newMessage = Object_NearoomMessage("VID",myId ,roomId ,null,newVideo[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0 , null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[1],"thumbvid")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                    MultipartUploadRequest(this,MyServerSide.saveVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[0],"vid")
                        .addParameter("fromId",myId.toString() )
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","VID")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                requestCodeFile -> {
                    val file : Uri? = data?.data
                    val newFile = MyInternalStorage(this).saveSendFile(file!!, myId )
                    val newMessage = Object_NearoomMessage("FILE", myId ,roomId ,null,newFile[1],0,
                        MyDateTime().getLocalFullTime(),1,0,0 , null , null)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.RoomActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveFile_address)
                        .setMethod("POST")
                        .addFileToUpload(newFile[0],"file")
                        .addParameter("fromId", myId.toString())
                        .addParameter("roomId",roomId.toString())
                        .addParameter("type","FILE")
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println("+++++++++++++++++++++"+serverResponse.bodyString)  }
                        })

                }
            }

        }
    }

    private fun showCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent,requestCodeCamera)
    }

    private fun showGalleryPic(){
        //val galleryIntent = Intent(Intent.ACTION_PICK)
        //galleryIntent.type = "image/*"
        //startActivityForResult(galleryIntent,ShowGalleryForPicRequestCode)
        val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
        targetIntent.putExtra("title","Send Pic to")
        targetIntent.putExtra("subtitle",roomInfo.roomname)
        targetIntent.putExtra("type","PICTURE")
        targetIntent.putExtra("fromActivity","ROOM")
        //targetIntent.putExtra("toUsername",roomname)
        //targetIntent.putExtra("picProfile",roomPic)
        startActivity(targetIntent)
    }

    private fun showGalleryVideo(){
        //val galleryIntent = Intent(Intent.ACTION_PICK)
        //galleryIntent.type = "video/*"
        //startActivityForResult(galleryIntent,ShowGalleryForVideoRequestCode)
        val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
        targetIntent.putExtra("title","Send Video to")
        targetIntent.putExtra("subtitle",roomInfo.roomname)
        targetIntent.putExtra("type","VIDEO")
        targetIntent.putExtra("fromActivity","ROOM")
        //targetIntent.putExtra("toUsername",roomname)
        //targetIntent.putExtra("picProfile",roomPic)
        startActivity(targetIntent)
    }

    private fun showGallerySound(){
        if ( Build.VERSION.SDK_INT >= 29 ) {
            val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
            targetIntent.putExtra("title","Send Sound to")
            targetIntent.putExtra("subtitle",roomInfo.roomname)
            targetIntent.putExtra("type","AUDIO")
            targetIntent.putExtra("fromActivity","ROOM")
            //targetIntent.putExtra("toUsername",roomname)
            //targetIntent.putExtra("picProfile",roomPic)
            startActivity(targetIntent)
        }else{
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "audio/*"
            startActivityForResult(galleryIntent,requestCodeAud)
        }
    }

    private fun showGalleryFile(){
        val targetIntent = Intent(Intent.ACTION_GET_CONTENT)
        targetIntent.addCategory(Intent.CATEGORY_OPENABLE)
        targetIntent.type = "*/*"
        startActivityForResult(targetIntent,requestCodeFile)
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == cameraPermissionRequestCode ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                showCamera()
            }else{
                Toast.makeText(this, "Camera Access Denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun sendMessage(messageText: String) {
        val newMessage = Object_NearoomMessage("TEXT", myId , roomId ,messageText,null,null,MyDateTime().getGMTFullTime(),0,0,0,null,null)
        messages.add(0,newMessage)
        adapter.notifyItemInserted(0)
        binding.RoomActivityRecyclerView.scrollToPosition(0)
        Volley_SendMessage(this,"TEXT", myId, null, roomId, messageText,null, object : Volley_SendMessage.ServerCallBack {
            override fun sendSuccess(result: JSONObject) {
                println(" SENDING SUCCESSFUL")
                if (result.getBoolean("isSuccess")){
                    val message : JSONObject = result.getJSONObject("newmessage")
                    val size = if (message.isNull("size")) {
                        null
                    }else{
                        message.getInt("size")
                    }
                    val uri = if (message.isNull("uri")) {
                        null
                    }else{
                        message.getString("uri")
                    }
                    DB_NearroomMessages(this@RoomActivity).saveOneMessage(message.getInt("myid"),
                        message.getString("type"),
                        message.getInt("senderid"),
                        message.getString("sender"),
                        message.getInt("roomid"),
                        message.getString("roomname"),
                        message.getString("timestamp"),
                        message.getString("message"),
                        uri,
                        size,
                        1,
                        0,
                        0,
                    )
                    updateMessages(true)
                }
            }

        }).send()

    }


    fun updateMessages( scrollToDown : Boolean ){
        messages.clear()
        val newMessages = DB_NearroomMessages(this).loadMessages(roomId)
        if ( isSearchMode ){
            var forCount = 0
            resultPositions.clear()
            newMessages.forEach {
                if ( !it.message.isNullOrEmpty() ){
                    if ( it.message!!.contains(searchWord) ){
                        it.selected = true
                        resultPositions.add(forCount)
                    }else{
                        it.selected = false
                    }
                }
                forCount += 1
            }
        }
        messages.addAll(newMessages)
        adapter.notifyDataSetChanged()
        if ( scrollToDown && !isSearchMode){
            binding.RoomActivityRecyclerView.scrollToPosition(0)
        }
        // Check if new message received then send seen status to server ///////////////////////////
        if ( newMessages.size > 0 ){
            if ( newMessages[0].id > newestMessageId ){
                Volley_SendNearoomMessageStatus(this,newMessages[0].id,roomId,"SEEN").send()
                newestMessageId = newMessages[0].id
            }
        }

    }


    companion object {
        const val notificationChannelID = "RoomChannel"
    }

}