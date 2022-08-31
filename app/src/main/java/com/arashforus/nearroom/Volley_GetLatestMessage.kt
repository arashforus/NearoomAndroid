package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class Volley_GetLatestMessage(val context: Context, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getsuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetLatestMessage"
    val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Looping getting Last messages from server DB ////////////////////////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getLatestMessage

        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        var lastMessageId = MySharedPreference(MySharedPreference.PreferenceApp,context).load(MySharedPreference.App_LastMessageIdGet,"int") as Int

        val body = JSONObject()
        body.put("myId",myId)
        body.put("lastMessageId",lastMessageId)
        timer.schedule(object : TimerTask() {
            override fun run() {
                lastMessageId = MySharedPreference(MySharedPreference.PreferenceApp,context).load(MySharedPreference.App_LastMessageIdGet,"int") as Int
                body.put("lastId",lastMessageId)
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener {
                        Log.e(tag,it.toString())
                        if (it.getBoolean("isSuccess")){
                            DB_Messages(context).saveToDB(it)
                            callback?.getsuccess(it)
                        }
                    },
                    Response.ErrorListener {
                        Log.e(tag, it.message.toString() )
                    }){
                }
                queue.add(request)
            }
        },timerDelay,timerPeriod)
        return timer
    }
}