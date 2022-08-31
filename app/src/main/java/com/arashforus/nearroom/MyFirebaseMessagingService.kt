package com.arashforus.nearroom

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notifmanag : NotificationManagerCompat
    private lateinit var notifbuilder : NotificationCompat.Builder
    private val channelId = "Messages"

    var id : Int = 0
    private lateinit var type : String
    private var roomid : Int? = 0
    private var roomname : String? = null
    private var senderid : Int = 0
    private lateinit var sender : String
    private lateinit var to : String
    private var toid : Int? = 0
    private lateinit var msg : String
    lateinit var uri : String
    var size : Int? = 0
    private lateinit var timeStamp : String
    private var senderFullname : String? = null

    // On Data Notification Received ///////////////////////////////////////////////////////////////
    override fun onMessageReceived(datareceived: RemoteMessage) {
        super.onMessageReceived(datareceived)
        println("--------============--------------------------------========--------")
        println(datareceived.data)
        when( datareceived.data["type"].toString().toUpperCase(Locale.ROOT).trim() ){
            "ISTYPING" -> {
                //DB_UserInfos(this).saveIsTyping(datareceived.data["sender"].toString(), datareceived.data["timestamp"].toString())
            }
            "THUMB" -> {
                uri = datareceived.data["uri"].toString()
                AndroidNetworking.download( MyServerSide().getThumbPhotoUri(uri), MyInternalStorage(this).getThumbImageFolder(), uri)
                    .setTag(uri)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : com.androidnetworking.interfaces.DownloadListener {
                        override fun onDownloadComplete() {
                            println("download complete")
                        }

                        override fun onError(anError: ANError?) {
                            println("download error")
                        }

                    })
            }
            "THUMBVID" -> {
                uri = datareceived.data["uri"].toString()
                AndroidNetworking.download( MyServerSide().getThumbVideoUri(uri), MyInternalStorage(this).getThumbVideoFolder(), uri)
                    .setTag(uri)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : com.androidnetworking.interfaces.DownloadListener {
                        override fun onDownloadComplete() {
                            println("download complete")
                        }

                        override fun onError(anError: ANError?) {
                            println("download error")
                        }

                    })
            }
            else -> {
                id = datareceived.data["myid"]!!.toInt()
                type = datareceived.data["type"].toString()
                senderid = datareceived.data["senderid"]!!.toInt()
                sender = datareceived.data["sender"].toString()
                //senderpic = datareceived.data["senderpic"].toString()
                toid = datareceived.data["toid"]?.toIntOrNull()
                to = datareceived.data["to"].toString()
                roomname = datareceived.data["roomname"].toString()
                roomid = datareceived.data["roomid"]?.toIntOrNull()
                msg = datareceived.data["message"].toString()
                uri = datareceived.data["uri"].toString()
                size = datareceived.data["size"]?.toIntOrNull()
                timeStamp = datareceived.data["timestamp"].toString()
                senderFullname = DB_Contacts(this).getFullName(senderid)

                if ( roomname == "PRIVATE" || roomname?.isEmpty() == true || roomname == null || roomname == "null" ){
                    val status = DB_UserInfos(this).getMuteAndBlocked(senderid)
                    if ( status.length() >= 2 ){
                        if ( status[1] == 1 ){
                            // Sender is blocked ... So return /////////////////////////////////////////
                            return
                        }
                    }else{
                        // Sender is not blocked ... so continue ///////////////////////////////////
                        sendMessageStatus("DELIVERED")
                        if ( toid !== null ){
                            DB_Messages(this).saveOneMessage(id,type,senderid,sender,toid!!,to,timeStamp,msg,uri,size,1,1,0)
                        }
                    }
                }else{
                    val status = DB_NearoomInfos(this).getJoinAndMute(roomid!!)
                    if ( status.length() >= 1 ){
                        if ( status[0] == 1 ){
                            // joined the nearoom .... so continue /////////////////////////////////////
                            sendNearoomMessageStatus(id, roomname!!,"DELIVERED")
                            DB_NearroomMessages(this).saveOneMessage(id,type,senderid,sender,roomid!!,roomname!!,timeStamp,msg,uri,size,1,1,0)
                        }
                    }else{
                        // leave the nearoom .... so return ////////////////////////////////////////
                        return
                    }
                }
                println(" running activity is : ${MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_RunningActivity,"String")}")
                when (MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_RunningActivity,"String").toString().toUpperCase(
                    Locale.ROOT)){
                    "PRIVATE" -> if (MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_ChatTo,"Int") == senderid){
                        sendToActivity()
                    }else{
                        //showNotification()
                        DB_Notification(this).saveNotification(type,senderid,sender,roomid,roomname,msg,uri,timeStamp)
                        if ( DB_UserInfos(this).isIdExist(senderid) ){
                            DB_Notification(this).showNotifications()
                        }else{
                            Volley_GetUserInfos(this, arrayListOf(senderid),object : Volley_GetUserInfos.ServerCallBack{
                                override fun getSuccess(result: JSONObject) {
                                    DB_Notification(this@MyFirebaseMessagingService).showNotifications()
                                }
                            })
                        }

                    }

                    "ROOM"  -> if (MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_ChatTo,"Int") == roomid){
                        sendToActivity()
                    }else{
                        showNotification()
                    }

                    else -> {
                        //showNotification()
                        DB_Notification(this).saveNotification(type,senderid,sender,roomid,roomname,msg,uri,timeStamp)
                        if ( DB_UserInfos(this).isIdExist(senderid) ){
                            DB_Notification(this).showNotifications()
                        }else{
                            Volley_GetUserInfos(this, arrayListOf(senderid),object : Volley_GetUserInfos.ServerCallBack{
                                override fun getSuccess(result: JSONObject) {
                                    DB_Notification(this@MyFirebaseMessagingService).showNotifications()
                                }
                            })
                        }
                    }
                }
                //println(MySharedPreference("Activity",this).load("RunningActivity","String"))
                //println(MySharedPreference("Activity",this).load("ChatTo","String"))
                //println(" NEW MESSAGE RECEIVED ............")
            }
        }
        //super.onMessageReceived(datareceived)
    }


    private fun showNotification() {
        // Create notification Channel /////////////////////////////////////////////////////////////
        println("In show notification section ..................................")
        notifmanag = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notification", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Notification"
            notifmanag.createNotificationChannel(channel)
        }

        // Create notification /////////////////////////////////////////////////////////////////////
        notifbuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val intent: Intent

        if ( roomname == "PRIVATE" || roomname?.isEmpty() == true || roomname == null || roomname == "null" ){
            // Notification to private /////////////////////////////////////////////////////////////
            val messageNotifEnable = MySharedPreference(MySharedPreference.PreferenceMessageNotification,this).load(MySharedPreference.MessageNotification_Enable,"Boolean",true) as Boolean
            val status = DB_UserInfos(this).getMuteAndBlocked(senderid)
            if ( messageNotifEnable && status[0] == 0 ){
                intent = Intent(this, PrivateActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                intent.putExtra("toId",senderid)
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                notifbuilder.setSmallIcon(R.drawable.nearoom_icon2)
                    .setContentIntent(pendingIntent)

                if (senderFullname !== null){
                    notifbuilder.setContentTitle(senderFullname)
                }else{
                    notifbuilder.setContentTitle(sender)
                }

                when (type.toUpperCase(Locale.ROOT).trim()){
                    "TEXT" -> notifbuilder.setContentText(msg)
                    "PIC" -> notifbuilder.setContentText(" \ud83d\uddbc Photo")
                    "VID" -> notifbuilder.setContentText(" \ud83c\udfa6 Video")
                    else -> notifbuilder.setContentText(msg)
                }
                notifmanag.notify(id,notifbuilder.build())
            }
        }else{
            // Notification to room ////////////////////////////////////////////////////////////////
            val nearoomNotifEnable = MySharedPreference(MySharedPreference.PreferenceNearoomNotification,this).load(MySharedPreference.MessageNotification_Enable,"Boolean") as Boolean
            val status = DB_NearoomInfos(this).getJoinAndMute(roomid!!)
            if ( nearoomNotifEnable && status[1] == 0 ){
                intent = Intent(this, RoomActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                intent.putExtra("roomId",roomid)
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                notifbuilder.setContentTitle(roomname)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.nearoom_icon2)
                if ( senderFullname !== null ){
                    when (type.toUpperCase(Locale.ROOT).trim()){
                        "TEXT" -> notifbuilder.setContentText("$senderFullname : $msg")
                        "PIC" -> notifbuilder.setContentText("$senderFullname : \uD83D\uDDBC Photo ")
                        "VID" -> notifbuilder.setContentText("$senderFullname : \uD83C\uDFA6 Video ")
                        else -> notifbuilder.setContentText("$senderFullname : $msg")
                    }
                }else{
                    when (type.toUpperCase(Locale.ROOT).trim()){
                        "TEXT" -> notifbuilder.setContentText("$sender : $msg")
                        "PIC" -> notifbuilder.setContentText("$sender : \uD83D\uDDBC Photo ")
                        "VID" -> notifbuilder.setContentText("$sender : \uD83C\uDFA6 Video ")
                        else -> notifbuilder.setContentText("$sender : $msg")
                    }
                }
                notifmanag.notify(id,notifbuilder.build())

            }
        }
    }

    private fun sendToActivity() {
        val targetIntent = Intent("NewMessageFromFirebase")
        if ( roomname == "PRIVATE" || roomname?.isEmpty() == true || roomname == null || roomname == "null" ){
            sendMessageStatus("READ")
        }else{
            sendNearoomMessageStatus(id, roomname!!,"READ")
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(targetIntent)
    }

    private fun sendMessageStatus(status : String){
        Volley_SendMessageStatus(this,id ,toid!! ,senderid ,status).send()
    }

    private fun sendNearoomMessageStatus(messageId: Int, roomName: String, status: String) {
        Volley_SendNearoomMessageStatus(this,messageId,roomid!! ,status).send()
    }

    // On new Token Function called ////////////////////////////////////////////////////////////////
    override fun onNewToken(token: String) {
        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
        val firebaseId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FirebaseId,"string") as String
        println(token)
        Volley_UpdateFirebaseToken(this, myId ,firebaseId , token, object : Volley_UpdateFirebaseToken.ServerCallBack {
            override fun updateSuccess(result: JSONObject) {
                println("new token is updated in DB")
            }

        }).update()
    }

}
