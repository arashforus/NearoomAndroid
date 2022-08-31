package com.arashforus.nearroom

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityPrivateBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PrivateActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityPrivateBinding

    private var myId = 0
    private var toId : Int = 0
    private lateinit var toInfo : Object_User

    private var firstTimeAdapter = true
    private var firstItem = 0
    private var newestMessageId = 0
    private var isSearchMode = false
    private var resultPositions = ArrayList<Int>()
    private var searchWord : CharSequence = ""
    private var currentSearchResult = 0
    private var isBlocked = false
    private var isSendOptionVisible = false
    private lateinit var tempFileUri : Uri
    private lateinit var tempFilePath : String

    private lateinit var messages : ArrayList<Object_Message>
    private lateinit var adapter : Adapter_Messages

    private var lasttimetype : Array<Int> = arrayOf(0,0,0)
    private var timerUserInfo : Timer = Timer()
    private var timerOnline : Timer = Timer()
    private var timerUpdateMessages : Timer = Timer()

    private val cameraPermissionRequestCode = 1200
    private val readExternalStorageRequestCode = 1201

    private val showCameraRequestCode = 1500
    private val showGalleryForPicRequestCode = 1501
    private val showGalleryForVideoRequestCode = 1502
    private val showGalleryForSoundRequestCode = 1503
    private val showGalleryForFileRequestCode = 1504

    private lateinit var thumbNotificationConfig : UploadNotificationConfig
    private lateinit var uploadNotificationConfig : UploadNotificationConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        //onTransformationEndContainer(intent.getParcelableExtra("TransformationParams"))
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyTools(this).resizableWindow(this)
        myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
        toId = intent.getIntExtra("toId",0)
        toInfo = DB_UserInfos(this).getUserInfo(toId)

        if ( toInfo.blocked == 1){
            isBlocked = true
        }

        makeLiveWallpaper()
        updateBlockedUi()
        initToolbar()
        initAdapter()
        createNotificationChannel()
        createNotificationConfig()
        UploadServiceConfig.initialize(this.application, notificationChannelID, BuildConfig.DEBUG)
        MyContactsChange(this).register()
        makeListeners()
        makeResultFunctions()
    }

    private fun makeLiveWallpaper() {
        when ( MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_Name,"String").toString().toUpperCase(Locale.ROOT)){
            "SOLID" -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.GONE
                binding.PrivateActivityBackgroundBubble.visibility = View.GONE
                binding.PrivateActivityBackgroundRainbow.visibility = View.GONE
                binding.PrivateActivityBackgroundOther.visibility = View.VISIBLE
                val backColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_SolidColor,"Int") as Int
                binding.PrivateActivityBackgroundOther.setBackgroundColor(backColor)
            }
            "PARTICLE" -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.VISIBLE
                binding.PrivateActivityBackgroundBubble.visibility = View.GONE
                binding.PrivateActivityBackgroundRainbow.visibility = View.GONE
                binding.PrivateActivityBackgroundOther.visibility = View.GONE
                val particleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleNums,"Int",-1) as Int
                val particleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSpeed,"Int",-1) as Int
                val particleSize = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSize,"Int",-1) as Int
                val particleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleColor,"Int",-1) as Int
                val particleBackColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleBackgroundColor,"Int",-1) as Int
                if ( particleNum != -1 ){
                    binding.PrivateActivityBackgroundParticle.setNumbers(particleNum)
                }
                if ( particleSpeed != -1 ){
                    binding.PrivateActivityBackgroundParticle.setSpeed(particleSpeed)
                }
                if ( particleSize != -1 ){
                    binding.PrivateActivityBackgroundParticle.setSize(particleSize)
                }
                if ( particleColor != -1 ){
                    binding.PrivateActivityBackgroundParticle.setColor(particleColor)
                }
                if ( particleBackColor != -1 ){
                    binding.PrivateActivityBackgroundParticle.setBackground(particleBackColor)
                }
            }
            "BUBBLES" -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.GONE
                binding.PrivateActivityBackgroundBubble.visibility = View.VISIBLE
                binding.PrivateActivityBackgroundRainbow.visibility = View.GONE
                binding.PrivateActivityBackgroundOther.visibility = View.GONE
                val bubbleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesNums,"Int",-1) as Int
                val bubbleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesSpeed,"Int",-1) as Int
                val bubbleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesColor,"Int",-1) as Int
                val bubbleBackground = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_BubblesBackgroundColor,"Int",-1) as Int
                if ( bubbleNum != -1 ){
                    binding.PrivateActivityBackgroundBubble.setNumbers(bubbleNum)
                }
                if ( bubbleSpeed != -1 ){
                    binding.PrivateActivityBackgroundBubble.setSpeed(bubbleSpeed)
                }
                if ( bubbleColor != -1 ){
                    binding.PrivateActivityBackgroundBubble.setColor(bubbleColor)
                }
                if ( bubbleBackground != -1 ){
                    binding.PrivateActivityBackgroundBubble.setBackground(bubbleBackground)
                }
            }
            "RAINBOW" -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.GONE
                binding.PrivateActivityBackgroundBubble.visibility = View.GONE
                binding.PrivateActivityBackgroundRainbow.visibility = View.VISIBLE
                binding.PrivateActivityBackgroundOther.visibility = View.GONE
                val rainbowAlpha = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_RainbowAlpha,"Int",-1) as Int
                val rainbowSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_RainbowSpeed,"Int",-1) as Int
                if ( rainbowAlpha != -1 ){
                    binding.PrivateActivityBackgroundRainbow.setAlpha(rainbowAlpha)
                }
                if ( rainbowSpeed != -1 ){
                    binding.PrivateActivityBackgroundRainbow.setSpeed(rainbowSpeed)
                }
            }
            "PICTURE" -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.GONE
                binding.PrivateActivityBackgroundBubble.visibility = View.GONE
                binding.PrivateActivityBackgroundRainbow.visibility = View.GONE
                binding.PrivateActivityBackgroundOther.visibility = View.VISIBLE
                val picBackground = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ImagePath,"String") as String
                binding.PrivateActivityBackgroundOther.background = Drawable.createFromPath(picBackground)
            }
            else -> {
                binding.PrivateActivityBackgroundParticle.visibility =View.VISIBLE
                binding.PrivateActivityBackgroundBubble.visibility = View.GONE
                binding.PrivateActivityBackgroundRainbow.visibility = View.GONE
                binding.PrivateActivityBackgroundOther.visibility = View.GONE
                val particleNum = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleNums,"Int",-1) as Int
                val particleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSpeed,"Int",-1) as Int
                val particleSize = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleSize,"Int",-1) as Int
                val particleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleColor,"Int",-1) as Int
                val particleBackColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_ParticleBackgroundColor,"Int",-1) as Int
                if ( particleNum != -1 ){
                    binding.PrivateActivityBackgroundParticle.setNumbers(particleNum)
                }
                if ( particleSpeed != -1 ){
                    binding.PrivateActivityBackgroundParticle.setSpeed(particleSpeed)
                }
                if ( particleSize != -1 ){
                    binding.PrivateActivityBackgroundParticle.setSize(particleSize)
                }
                if ( particleColor != -1 ){
                    binding.PrivateActivityBackgroundParticle.setColor(particleColor)
                }
                if ( particleBackColor != -1 ){
                    binding.PrivateActivityBackgroundParticle.setBackground(particleBackColor)
                }
            }
        }
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


    }

    override fun onResume() {
        super.onResume()
        // Save active activity to shared pref /////////////////////////////////////////////////////
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_RunningActivity,"Private")
        MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_ChatTo,toId)

        // Delete notifications ////////////////////////////////////////////////////////////////////
        DB_Notification(this).deleteUserNotification(toId)

        // Active send online time to server ///////////////////////////////////////////////////////
        timerOnline = Volley_SendOnline(this, myId).send()

        // Active get user info from server ////////////////////////////////////////////////////////
        timerUserInfo = Volley_GetUserInfos(this, arrayListOf(toId), object : Volley_GetUserInfos.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                val status = MyStatus(this@PrivateActivity).find(myId ,toId)
                if (status == null ){
                    binding.PrivateActivityToolbarSubtitle.visibility = View.GONE
                }else{
                    binding.PrivateActivityToolbarSubtitle.visibility = View.VISIBLE
                    binding.PrivateActivityToolbarSubtitle.text = status
                }
            }
        }).get()

        // Active getting latest message and update DB /////////////////////////////////////////////
        timerUpdateMessages = Volley_GetLatestMessage(this, object : Volley_GetLatestMessage.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                if ( !result.isNull("chat") ){
                    updateMessages(false)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        MyContactsChange(this).unregister()
    }

    private fun makeResultFunctions() {
        if ( intent.hasExtra("purpose") ){
            when ( intent.getStringExtra("purpose")?.toUpperCase(Locale.ROOT)?.trim() ){
                "SENDIMAGE" -> {
                    val imagePath  = intent.getStringExtra("path")
                    val imageUri = Uri.fromFile(File(imagePath!!))
                    val newImage = MyInternalStorage(this).saveSendPic(imageUri!!, myId)
                    println(newImage[0] +"  "+ newImage[1] +"  "+ newImage[2])
                    val newMessage = Object_Message("PIC",myId, toInfo.id!!,null, newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .addParameter("type","THUMB")
                        //.addParameter("to",toUsername)
                        .setNotificationConfig { _, _ ->
                            thumbNotificationConfig
                        }
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
                        .addParameter("toId",toId.toString())
                        .addParameter("type","PIC")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {
                                println("Upload pic error :  ${exception.message} -- ${uploadInfo.files} -- ${exception.printStackTrace()} -- ${exception.stackTraceToString()}")  }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                "SENDVIDEO" -> {
                    val videoPath = intent.getStringExtra("path")
                    val videoUri = Uri.fromFile(File(videoPath!!))
                    val newVideo = MyInternalStorage(this).saveSendVideo(videoUri!!, myId)
                    val newMessage = Object_Message("VID",myId ,toId ,null,newVideo[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[1],"thumbvid")
                        .addParameter("type","THUMBVID")
                        .setNotificationConfig { _, _ ->
                            thumbNotificationConfig
                        }
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
                        .addParameter("toId",toId.toString())
                        .addParameter("type","VID")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
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
                    val newAudio = MyInternalStorage(this).saveSendAudio(audioUri!!, myId)
                    val newMessage = Object_Message("AUD",myId ,toId ,audioDuration,newAudio[1],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveAudio_address)
                        .setMethod("POST")
                        .addFileToUpload(newAudio[0],"aud")
                        .addParameter("fromId",myId.toString())
                        .addParameter("toId",toId.toString())
                        .addParameter("type","AUD")
                        .addParameter("duration",audioDuration!!)
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
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

    private fun makeListeners() {
        // Send button listener ////////////////////////////////////////////////////////////////////
        binding.PrivateActivitySendButtonLayout.setOnClickListener{
            if ( binding.PrivateActivityMessage2.text.toString().trim() !== "" && binding.PrivateActivityMessage2.text !== null ){
                sendMessage(binding.PrivateActivityMessage2.text.toString().trim())
            }
            binding.PrivateActivityMessage2.text.clear()

        }

        binding.PrivateActivityMessage2.addTextChangedListener {
            if (MyDateTime().secDiffLocalClock(MyDateTime().getLocalClock(),lasttimetype) > 3 ){
                lasttimetype = MyDateTime().getLocalClock()
                Volley_IsTyping(this,myId ,"P$toId" ).send()
            }
        }

        // Send Option Layout Show and gone state //////////////////////////////////////////////////
        //var sendOption = false
        binding.PrivateActivityOpenSendMenu.setOnClickListener {
            if ( !isSendOptionVisible ){
                binding.PrivateActivitySendOptionLayout.visibility = View.VISIBLE
                binding.PrivateActivitySendOptionLayout.minHeight = binding.PrivateActivitySendMessageLayout.height + 55
                binding.view7.visibility = View.VISIBLE
                ViewAnimationUtils.createCircularReveal(binding.PrivateActivitySendOptionLayout,0,binding.PrivateActivitySendOptionLayout.bottom,0f,binding.PrivateActivitySendOptionLayout.width.toFloat()).apply {
                    duration = 200
                }.start()
                binding.PrivateActivityOpenSendMenu.setImageResource(R.drawable.ic_baseline_arrow_downward)

            }else{

                ViewAnimationUtils.createCircularReveal(binding.PrivateActivitySendOptionLayout,0,binding.PrivateActivitySendOptionLayout.bottom,binding.PrivateActivitySendOptionLayout.width.toFloat(),0f).apply {
                    duration = 200
                    doOnEnd {
                        binding.PrivateActivitySendOptionLayout.visibility = View.GONE
                        binding.view7.visibility = View.GONE
                    }
                }.start()
                binding.PrivateActivityOpenSendMenu.setImageResource(R.drawable.ic_baseline_attach_file)
            }
            isSendOptionVisible = !isSendOptionVisible
        }

        binding.PrivateActivityCameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
                }
            } else {
                showCamera()
            }
        }

        binding.PrivateActivitySelectPicButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryPic()
            }
        }

        binding.PrivateActivitySelectVideoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryVideo()
            }
        }

        binding.PrivateActivitySelectSoundButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGallerySound()
            }
        }

        binding.PrivateActivitySelectFileButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), readExternalStorageRequestCode)
                }
            } else {
                showGalleryFile()
            }
        }

        // Search Bar Listeners ////////////////////////////////////////////////////////////////////
        binding.PrivateActivitySearchBar.SearchBarChatGoIcon.setOnClickListener {
            searchWord = binding.PrivateActivitySearchBar.SearchBarChatText.text.trim()
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
                binding.PrivateActivitySearchBar.SearchBarChatResult.visibility = View.VISIBLE
                if ( resultPositions.size > 0 ){
                    binding.PrivateActivitySearchBar.SearchBarChatResult.text = "1 of ${resultPositions.size} search found"
                    binding.PrivateActivitySearchBar.SearchBarChatUpIcon.visibility = View.VISIBLE
                    binding.PrivateActivitySearchBar.SearchBarChatDownIcon.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    currentSearchResult = 0
                    binding.PrivateActivityRecyclerView.smoothScrollToPosition(resultPositions[0])

                }else{
                    binding.PrivateActivitySearchBar.SearchBarChatResult.text = "Nothing found"
                }

            }else{
                binding.PrivateActivitySearchBar.SearchBarChatResult.visibility = View.VISIBLE
                binding.PrivateActivitySearchBar.SearchBarChatResult.text = "You should enter more than two letters for searching ..."
            }

        }

        binding.PrivateActivitySearchBar.SearchBarChatCloseIcon.setOnClickListener {
            binding.PrivateActivitySearchBar.SearchBarChatLayout.visibility = View.GONE
            isSearchMode = false
            binding.PrivateActivityButtomLayout.visibility = View.VISIBLE
            updateBlockedUi()
            updateMessages(false)
        }

        binding.PrivateActivitySearchBar.SearchBarChatDownIcon.setOnClickListener {
            if ( currentSearchResult > 0 ){
                currentSearchResult -= 1
                binding.PrivateActivitySearchBar.SearchBarChatResult.text = "${currentSearchResult+1} of ${resultPositions.size} search found"
                binding.PrivateActivityRecyclerView.smoothScrollToPosition(resultPositions[currentSearchResult])
            }
        }

        binding.PrivateActivitySearchBar.SearchBarChatUpIcon.setOnClickListener {
            if ( currentSearchResult < resultPositions.size - 1 ){
                currentSearchResult += 1
                binding.PrivateActivitySearchBar.SearchBarChatResult.text = "${currentSearchResult+1} of ${resultPositions.size} search found"
                binding.PrivateActivityRecyclerView.smoothScrollToPosition(resultPositions[currentSearchResult])
            }
        }

        // Blocked Listeners
        binding.PrivateActivityBlocked.BlockedStatusUnblockButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Do you want to unblock ${toInfo.username} ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, _ ->
                    val newBlockedList = MyTools(this).changeBlockedList(toId.toString(),false)
                    Volley_ChangeBlockedUsers(this,newBlockedList,object : Volley_ChangeBlockedUsers.ServerCallBack{
                        override fun getSuccess(result: JSONObject) {
                            if ( result.getBoolean("isSuccess") ){
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@PrivateActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                DB_UserInfos(this@PrivateActivity).syncBlocksFromSharedAndDB()
                                isBlocked = false
                                updateBlockedUi()
                                dialog.dismiss()
                                dialog.cancel()
                            }else{
                                Toast.makeText(this@PrivateActivity, "Unblocking failed ... try again", Toast.LENGTH_SHORT).show()
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

    }

    private fun makeMenuListener(menuPopup: PopupMenu) {
        menuPopup.setOnMenuItemClickListener {
            when ( it.itemId ){
                R.id.PrivateMenu_ViewContact -> {
                    val intent = Intent(this,ViewContactActivity::class.java)
                    intent.putExtra("userId",toId)
                    startActivity(intent)
                    true
                }
                R.id.PrivateMenu_Search -> {
                    binding.PrivateActivitySearchBar.SearchBarChatLayout.visibility = View.VISIBLE
                    binding.PrivateActivitySearchBar.SearchBarChatResult.visibility = View.GONE
                    binding.PrivateActivityButtomLayout.visibility = View.GONE
                    true
                }
                R.id.PrivateMenu_DeleteHistory -> {
                    AlertDialog.Builder(this)
                        .setTitle("Are you sure ?")
                        .setMessage("You select deleting history of chats , so after that you can't recover your deleted messages . Do you want to delete all chats with ${toInfo.username} ?")
                        .setCancelable(true)
                        .setPositiveButton("Yes , Delete all") { dialog, _ ->
                            Volley_DeleteChats( this, myId ,toId , adapter.messages[0].id, object : Volley_DeleteChats.ServerCallBack {
                                    override fun getResponse(result: JSONObject) {
                                        if (result.getBoolean("isSuccess")) {
                                            dialog.dismiss()
                                            dialog.cancel()
                                            DB_Messages(this@PrivateActivity).deleteChats( toId , adapter.messages[0].id )
                                            Toast.makeText(this@PrivateActivity,"All messages delete successfully",Toast.LENGTH_SHORT ).show()
                                            updateMessages(false)
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
                R.id.PrivateMenu_Block -> {
                    if ( isBlocked ){
                        AlertDialog.Builder(this)
                            .setTitle("Are you sure ?")
                            .setMessage("Do you want to unblock ${toInfo.username} ?")
                            .setCancelable(true)
                            .setPositiveButton("Yes") { dialog, _ ->
                                val newBlockedList = MyTools(this).changeBlockedList(toId.toString(),false)
                                Volley_ChangeBlockedUsers(this,newBlockedList,object : Volley_ChangeBlockedUsers.ServerCallBack{
                                    override fun getSuccess(result: JSONObject) {
                                        if ( result.getBoolean("isSuccess") ){
                                            MySharedPreference(MySharedPreference.PreferenceProfile,this@PrivateActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                            DB_UserInfos(this@PrivateActivity).syncBlocksFromSharedAndDB()
                                            isBlocked = false
                                            updateBlockedUi()
                                            dialog.dismiss()
                                            dialog.cancel()
                                        }else{
                                            Toast.makeText(this@PrivateActivity, "Unblocking failed ... try again", Toast.LENGTH_SHORT).show()
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
                            .setMessage("If you block this contact , you can't receive any message from this contact anymore . Do you want to block ${toInfo.username} ?")
                            .setCancelable(true)
                            .setPositiveButton("Yes , Block ${toInfo.username}") { dialog, _ ->
                                val newBlockedList = MyTools(this).changeBlockedList(toId.toString(),true)
                                Volley_ChangeBlockedUsers(this,newBlockedList , object : Volley_ChangeBlockedUsers.ServerCallBack{
                                    override fun getSuccess(result: JSONObject) {
                                        if ( result.getBoolean("isSuccess") ){
                                            MySharedPreference(MySharedPreference.PreferenceProfile,this@PrivateActivity).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                                            DB_UserInfos(this@PrivateActivity).syncBlocksFromSharedAndDB()
                                            //DB_UserInfos(this).setBlocked(toUsername,1)
                                            isBlocked = true
                                            updateBlockedUi()
                                            dialog.dismiss()
                                            dialog.cancel()
                                        }else{
                                            Toast.makeText(this@PrivateActivity, "Blocking failed ... try again", Toast.LENGTH_SHORT).show()
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

                    true
                }
                R.id.PrivateMenu_Report -> {
                    AlertDialog.Builder(this)
                        .setTitle("Are you sure ?")
                        .setMessage("This person hesitate you ? So let us know why ...")
                        .setCancelable(true)
                        .setPositiveButton("Yes") { dialog, _ ->
                            val targetIntent = Intent(this,ContactUsActivity::class.java)
                            targetIntent.putExtra("purpose","ReportUsername")
                            targetIntent.putExtra("reportUserId",toId)
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

    private fun initAdapter() {
        messages = DB_Messages(this).loadMessages( toId )
        adapter = Adapter_Messages(messages)
        val rvLayout = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        rvLayout.reverseLayout = true
        rvLayout.stackFromEnd = true
        rvLayout.scrollToPosition(0)
        binding.PrivateActivityRecyclerView.layoutManager = rvLayout
        binding.PrivateActivityRecyclerView.adapter = adapter
        if ( firstTimeAdapter ){
            binding.PrivateActivityRecyclerView.postDelayed({rvLayout.scrollToPosition(0)},500)
            firstItem = 0
            firstTimeAdapter = false
        }
        binding.PrivateActivityRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if ( bottom < oldBottom ) {
                binding.PrivateActivityRecyclerView.post {
                    //println(rvLayout.findFirstVisibleItemPosition())
                    if ( rvLayout.findFirstVisibleItemPosition() <= 7 ){
                        rvLayout.scrollToPosition(0)
                    }
                }
            }
        }

        // Check if new message received then send seen status to server ///////////////////////////
        //println("for sending seen status ... last id : ${messages[0].id} and newest message id is : $newestMessageId .....")
        if ( messages.size > 0 ){
            if ( messages[0].id > newestMessageId ){
                Volley_SendMessageStatus(this,messages[0].id, myId , toId ,"SEEN").send()
                newestMessageId = messages[0].id
                DB_Messages(this).setSeenStatus(toId)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when ( requestCode ){
                showCameraRequestCode -> {
                    val photoEditIntent = Intent(this,PhotoEditActivity::class.java)
                    photoEditIntent.putExtra("path",tempFilePath)
                    //photoEditIntent.putExtra("size",tempFilePath)
                    photoEditIntent.putExtra("type","Image")
                    photoEditIntent.putExtra("fromActivity","Private")
                    photoEditIntent.putExtra("toId",toId)
                    startActivity(photoEditIntent)

                    /*
                    val newImage = MyInternalStorage(this).saveSendPic(tempFileUri,myId )
                    //println(newImage.get(0)+"  "+newImage.get(1)+"  "+newImage.get(2))
                    val newMessage = Object_Message("PIC", myId ,toId ,null,newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .addParameter("type","THUMB")
                        //.addParameter("to",toUsername)
                        .setNotificationConfig { _, _ ->
                            thumbNotificationConfig
                        }
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
                        .addParameter("toId",toId.toString())
                        .addParameter("type","PIC")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })

                     */
                }
                showGalleryForPicRequestCode -> {
                    val image : Uri? = data?.data
                    val newImage = MyInternalStorage(this).saveSendPic(image!!, myId )
                    val newMessage = Object_Message("PIC", myId ,toId ,null,newImage[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbPicture_address)
                        .setMethod("POST")
                        .addFileToUpload(newImage[1],"thumbPic")
                        .addParameter("type","THUMB")
                        //.addParameter("to",toUsername)
                        .setNotificationConfig { _, _ ->
                            thumbNotificationConfig
                        }
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
                        .addParameter("toId",toId.toString())
                        .addParameter("type","PIC")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                showGalleryForVideoRequestCode -> {
                    val video : Uri? = data?.data
                    val newVideo = MyInternalStorage(this).saveSendVideo(video!!, myId)
                    val newMessage = Object_Message("VID",myId ,toId ,null,newVideo[2],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveThumbVideo_address)
                        .setMethod("POST")
                        .addFileToUpload(newVideo[1],"thumbvid")
                        //.addParameter("to",toUsername)
                        .addParameter("type","THUMBVID")
                        .setNotificationConfig { _, _ ->
                            thumbNotificationConfig
                        }
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
                        .addParameter("toId",toId.toString())
                        .addParameter("type","VID")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
                        .subscribe(this, this, object : RequestObserverDelegate {
                            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                            override fun onCompletedWhileNotObserving() {    }
                            override fun onError( context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                            override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                            override fun onSuccess( context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) { println(serverResponse.bodyString)  }
                        })
                }
                showGalleryForFileRequestCode -> {
                    val file : Uri? = data?.data
                    val newFile = MyInternalStorage(this).saveSendFile(file!!, myId)
                    val newMessage = Object_Message("FILE", myId ,toId ,null,newFile[1],0,
                        MyDateTime().getLocalFullTime(),1,0,0)
                    messages.add(0,newMessage)
                    adapter.notifyItemInserted(0)
                    binding.PrivateActivityRecyclerView.scrollToPosition(0)
                    MultipartUploadRequest(this,MyServerSide.saveFile_address)
                        .setMethod("POST")
                        .addFileToUpload(newFile[0],"file")
                        .addParameter("fromId",myId.toString())
                        .addParameter("toId",toId.toString())
                        .addParameter("type","FILE")
                        .setNotificationConfig { _, _ ->
                            uploadNotificationConfig
                        }
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

    private fun initToolbar(){
        setSupportActionBar(binding.PrivateActivityToolbar)
        // custom toolbar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        //supportActionBar?.setCustomView(R.layout.custom_toolbar)

        if ( !toInfo.profilePic.isNullOrEmpty() ){
            if ( MyInternalStorage(this).isProfileThumbAvailable(toInfo.profilePic!!) ){
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbProfilePicString(toInfo.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .circleCrop()
                    .into(binding.PrivateActivityToolbarPic)
            }else{
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbProfilePicString(toInfo.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .circleCrop()
                    .into(binding.PrivateActivityToolbarPic)
                AndroidNetworking.download( MyServerSide().getThumbProfileUri(toInfo.profilePic!!), MyInternalStorage(this).getThumbProfileFolder(), toInfo.profilePic)
                    .setTag(toInfo.profilePic)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@PrivateActivity)
                                .load(MyInternalStorage(this@PrivateActivity).getThumbProfilePicString(toInfo.profilePic!!))
                                .error(R.drawable.defaultprofile)
                                .circleCrop()
                                .into(binding.PrivateActivityToolbarPic)
                        }
                        override fun onError(anError: ANError?) {
                            println("download ${toInfo.profilePic}  has error")
                        }
                    })
            }
        }else{
            if ( !toInfo.phonePhotoUri.isNullOrEmpty() ){
                Glide.with(this)
                    .load(toInfo.phonePhotoUri)
                    .error(R.drawable.defaultprofile)
                    .circleCrop()
                    .into(binding.PrivateActivityToolbarPic)
            }else{
                Glide.with(this)
                    .load(R.drawable.defaultprofile)
                    .error(R.drawable.defaultprofile)
                    .circleCrop()
                    .into(binding.PrivateActivityToolbarPic)
            }
        }

        binding.PrivateActivityToolbarTitle.text = if ( toInfo.phoneFullname.isNullOrEmpty() ){
            toInfo.username
        }else{
            toInfo.phoneFullname
        }
        binding.PrivateActivityToolbarSubtitle.visibility = View.GONE

        // Toolbar Listeners ///////////////////////////////////////////////////////////////////////
        binding.PrivateActivityToolbarBackLayout.setOnClickListener {
            onBackPressed()
        }

        binding.PrivateActivityToolbarPic.setOnClickListener {
            val intent = Intent(this,ProfilePicViewActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.PrivateActivityToolbarPic,"pic"),
                Pair.create(binding.PrivateActivityToolbarTitle,"title"))
            intent.putExtra("userId",toId)
            startActivity(intent,options.toBundle())
        }

        binding.PrivateActivityToolbarTitleLayout.setOnClickListener {
            val intent = Intent(this,ViewContactActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(binding.PrivateActivityToolbarPic,"pic"),
                Pair.create(binding.PrivateActivityToolbarTitle,"title"))
            intent.putExtra("userId",toId)
            startActivity(intent,options.toBundle())
        }

        binding.PrivateActivityToolbarVoicecall.setOnClickListener {
            startActivity(Intent(this,VoiceCallActivity::class.java))
        }

        binding.PrivateActivityToolbarVideoCall.setOnClickListener {
            startActivity(Intent(this,VideoCallActivity::class.java))
        }

        val menuPopup = PopupMenu(this,binding.PrivateActivityToolbarMenu,Gravity.BOTTOM)
        menuPopup.inflate(R.menu.private_menu)
        makeMenuListener(menuPopup)
        binding.PrivateActivityToolbarMenu.setOnClickListener {
            menuPopup.show()
            if ( isBlocked ){
                menuPopup.menu.findItem(R.id.PrivateMenu_Block).title = "Unblock"
            }else{
                menuPopup.menu.findItem(R.id.PrivateMenu_Block).title = "Block"
            }

        }


    }

    private fun showCamera(){
        /*val tempFileName = "TEMP${System.currentTimeMillis()}.jpg"
        tempFileUri = Uri.parse(MyExternalStorage(this).getImageString(tempFileName))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,tempFileUri)
        println("tempfileuri : $tempFileUri")
        cameraIntent.clipData = ClipData.newRawUri(null,tempFileUri)
        startActivityForResult(cameraIntent,showCameraRequestCode)
         */

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    File.createTempFile("TEMP${System.currentTimeMillis()}",".jpg",storageDir ).also {
                        tempFilePath = it.absolutePath
                    }
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this,"com.arashforus.nearroom",it )
                    tempFileUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, showCameraRequestCode)
                }
            }
        }
    }

    private fun showGalleryPic(){
        //val galleryIntent = Intent(Intent.ACTION_PICK)
        //galleryIntent.type = "image/*"
        //startActivityForResult(galleryIntent,ShowGalleryForPicRequestCode)
        val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
        targetIntent.putExtra("title","Send Pic to")
        targetIntent.putExtra("subtitle",binding.PrivateActivityToolbarTitle.text)
        targetIntent.putExtra("type","PICTURE")
        targetIntent.putExtra("fromActivity","PRIVATE")
        targetIntent.putExtra("toId",toId)
        //targetIntent.putExtra("picProfile",profilePic)
        startActivity(targetIntent)
    }

    private fun showGalleryVideo(){
        //val galleryIntent = Intent(Intent.ACTION_PICK)
        //galleryIntent.type = "video/*"
        //startActivityForResult(galleryIntent,ShowGalleryForVideoRequestCode)
        val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
        targetIntent.putExtra("title","Send Video to")
        targetIntent.putExtra("subtitle",binding.PrivateActivityToolbarTitle.text)
        targetIntent.putExtra("type","VIDEO")
        targetIntent.putExtra("fromActivity","PRIVATE")
        targetIntent.putExtra("toId",toId)
        //targetIntent.putExtra("picProfile",profilePic)
        startActivity(targetIntent)
    }

    private fun showGallerySound(){
        if ( Build.VERSION.SDK_INT >= 29 ) {
            val targetIntent = Intent(this,GalleryAlbumActivity::class.java)
            targetIntent.putExtra("title","Send Sound to")
            targetIntent.putExtra("subtitle",binding.PrivateActivityToolbarTitle.text)
            targetIntent.putExtra("type","AUDIO")
            targetIntent.putExtra("fromActivity","PRIVATE")
            targetIntent.putExtra("toId",toId)
            //targetIntent.putExtra("picProfile",profilePic)
            startActivity(targetIntent)
        }else{
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "audio/*"
            startActivityForResult(galleryIntent,showGalleryForSoundRequestCode)
        }
    }

    private fun showGalleryFile(){
        val targetIntent = Intent(Intent.ACTION_GET_CONTENT)
        targetIntent.addCategory(Intent.CATEGORY_OPENABLE)
        targetIntent.type = "*/*"
        startActivityForResult(targetIntent,showGalleryForFileRequestCode)
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


    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("New Notification received from firebase to private activity ......")
            updateMessages(true)
        }
    }


    private fun sendMessage(messageText: String) {
        val newMessage = Object_Message("TEXT",myId ,toId ,messageText,MyDateTime().getGMTFullTime(),0,0,0)
        messages.add(0,newMessage)
        adapter.notifyItemInserted(0)
        binding.PrivateActivityRecyclerView.scrollToPosition(0)
        Volley_SendMessage(this,"TEXT", myId , toId ,null, messageText,null, object : Volley_SendMessage.ServerCallBack {
                override fun sendSuccess(result: JSONObject) {
                    println(" SENDING SUCCESSFUL")
                    if (result.getBoolean("isSuccess")){
                        val message : JSONObject = result.getJSONObject("newmessage")
                        val size = if (message.isNull("size")) {
                            null
                        }else{
                            message.getInt("size")
                        }
                        DB_Messages(this@PrivateActivity).saveOneMessage(message.getInt("myid"),
                                                                                message.getString("type"),
                                                                                message.getInt("senderid"),
                                                                                message.getString("sender"),
                                                                                message.getInt("toid"),
                                                                                message.getString("to"),
                                                                                message.getString("timestamp"),
                                                                                message.getString("message"),
                                                                                message.getString("uri"),
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
        val newMessages = DB_Messages(this).loadMessages(toId)
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
            binding.PrivateActivityRecyclerView.scrollToPosition(0)
        }
        // Check if new message received then send seen status to server ///////////////////////////
        if ( newMessages.size > 0 ){
            if ( newMessages[0].id > newestMessageId ){
                Volley_SendMessageStatus(this,newMessages[0].id,myId ,toId ,"SEEN").send()
                newestMessageId = newMessages[0].id
                DB_Messages(this).setSeenStatus(toId)
            }
        }

    }

    private fun updateBlockedUi(){
        if ( isBlocked ){
            binding.PrivateActivityBlocked.BlockedStatusLayout.visibility = View.VISIBLE
            binding.PrivateActivitySendMessageLayout.visibility = View.GONE

        }else{
            binding.PrivateActivityBlocked.BlockedStatusLayout.visibility = View.GONE
            binding.PrivateActivitySendMessageLayout.visibility = View.VISIBLE
        }
    }

    private fun createNotificationConfig() {
        thumbNotificationConfig = UploadNotificationConfig(
            notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading" ,
                "Uploading Media ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading" ,
                "Uploading Media ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading" ,
                "Uploading Media ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading" ,
                "Uploading Media ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            )
        )

        uploadNotificationConfig = UploadNotificationConfig(
            notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading" ,
                "Nearoom picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Upload complete" ,
                "Media uploaded successfully",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Error" ,
                "Media can't uploading ... try again",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                false,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading canceled" ,
                "Media uploading is canceled by you ",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                java.util.ArrayList(3),
                true,
                true,
                null
            )
        )
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel( notificationChannelID,"Upload Media Channel", NotificationManager.IMPORTANCE_LOW )
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
        val targetIntent = Intent(this  ,MainActivity::class.java)
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
        startActivity(targetIntent)
        finish()
    }

    companion object {
        const val notificationChannelID = "Upload Media"
    }
}