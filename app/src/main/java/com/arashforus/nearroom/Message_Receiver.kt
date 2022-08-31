package com.arashforus.nearroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
//import io.socket.client.IO
//import io.socket.client.Socket
//import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException
import java.sql.Timestamp
import java.util.*

class Message_Receiver : Service() {

    //private var mSocket: Socket? = null
    lateinit var username : String
    lateinit var notifmanag : NotificationManagerCompat
    lateinit var notifbuilder : NotificationCompat.Builder
    val CHANNEL_ID = "Messages"

    lateinit var respondData : JSONObject
    lateinit var type : String
    lateinit var roomname : String
    lateinit var roompic : String
    lateinit var sender : String
    lateinit var senderpic : String
    lateinit var msg : String
    lateinit var timeStamp : String


    //fun getSocket() :Socket? {
    //    println(mSocket.toString())
    //    return mSocket
    //}


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags2: Int, startId: Int): Int {
        //Service().startForeground(11,notifbuilder.build())

        return super.onStartCommand(intent, flags2, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        //mSocket?.disconnect()
    }

    override fun onCreate() {
        super.onCreate()

        username = MySharedPreference("profile",this).load("username","string") as String

        // Notification preconfigurations //////////////////////////////////////////////////////////
        notifmanag = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Message_Channel", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Message_Channel"
            //notifmanag = getSystemService(Context.NOTIFICATION_SERVICE)
            notifmanag.createNotificationChannel(channel)
        }
        val intent2 = Intent(this, RoomActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent2, 0)

        //startForeground()

        // Conncect to socket //////////////////////////////////////////////////////////////////////
        /*
        try {
            mSocket = IO.socket("https://nearoom.iran.liara.run")
            println(mSocket.toString())
        } catch(e: URISyntaxException) {
            e.printStackTrace()
        }
        mSocket?.connect()
        mSocket?.emit("nickname", username)

         */

        // Listener for new messages ///////////////////////////////////////////////////////////////
        /*
        val onNewMessage = Emitter.Listener {
            respondData = it[0] as JSONObject
            type = respondData.getString("type")
            roomname = respondData.getString("roomname")
            roompic = respondData.getString("roompic")
            sender = respondData.getString("sender")
            senderpic = respondData.getString("senderpic")
            msg = respondData.getString("message")
            timeStamp = respondData.getString("timestamp")

            //println(RoomActivity().isTaskRoot)

            notifbuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            if (roomname == "PRIVATE"){
                notifbuilder.setContentTitle(username)
                            .setContentIntent(pendingIntent)
            }else{
                notifbuilder.setContentTitle(roomname)
                            .setContentIntent(pendingIntent)
            }
            when (type.toUpperCase(Locale.ROOT)){
                "TEXT" -> notifbuilder.setContentText(msg)
                    .setSmallIcon(R.drawable.nearoom_icon2)


                "PHOTO" -> notifbuilder.setContentText("Photo : " + msg)
                    .setSmallIcon(R.drawable.nearoom_icon2)


                "VIDEO" -> notifbuilder.setContentText("Video : " + msg)
                    .setSmallIcon(R.drawable.nearoom_icon2)
            }



            notifmanag.notify(12,notifbuilder.build())

        }

        mSocket?.on("new message",onNewMessage)



         */


    }


}

