package com.arashforus.nearroom

import android.os.Handler
import android.os.Looper
//import io.socket.client.Socket
//import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class MySocket {

    //private var mSocket: Socket? = null
    val handler: Handler = Handler(Looper.getMainLooper())

    fun oncreate(){
        //mSocket = Message_Receiver().getSocket()
        //println(mSocket.toString())
        //val isRoomActivity = "YES"

        // Connect to server and active lisener for msg ////////////////////////////////////////////
        try {
            //mSocket = IO.socket("https://nearoom.iran.liara.run")
        } catch(e: URISyntaxException) {
            e.printStackTrace()
        }
        //mSocket?.connect()
        //mSocket?.emit("nickname", "alaki")
        //mSocket?.on("new message",onNewMessage)
        //mSocket?.on("new message",onNewMessage)
    }

    fun ondestroy(){
        //mSocket?.disconnect()
    }

    fun sendingmessage(){
        //messagejson = JSONObject()
        //messagejson.put("type","TEXT")
        //messagejson.put("roomname","TestRoom")
        //messagejson.put("roompic","nothing")
        //messagejson.put("sender",username)
        //messagejson.put("senderpic","Null")
        //messagejson.put("message",messagee)
        //messagejson.put("timestamp",Timestamp(System.currentTimeMillis()))
        //mSocket?.emit("new message",messagejson)
    }

    /*
    val onNewMessage = Emitter.Listener { args ->
        handler.post(Runnable {
            val data = args[0] as JSONObject
            val message: String
            try {
                message = data.getString("message")
            } catch (e: JSONException) {
                return@Runnable
            }
            //addMessage(username, message);

        })
    }

     */
}