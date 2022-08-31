package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class Volley_GetLatestNearoomMessage(val context: Context, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getsuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetLatestNearoomMessage"
    val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Looping getting Last nearoom messages from server DB ////////////////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getLatestNearoomMessage

        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        var lastNearoomMessageId = MySharedPreference(MySharedPreference.PreferenceApp,context).load(MySharedPreference.App_LastNearoomMessageIdGet,"int",0) as Int

        val body = JSONObject()
        body.put("myId",myId)
        body.put("lastid",lastNearoomMessageId)
        timer.schedule(object : TimerTask() {
            override fun run() {
                lastNearoomMessageId = MySharedPreference(MySharedPreference.PreferenceApp,context).load(MySharedPreference.App_LastNearoomMessageIdGet,"int") as Int
                body.put("lastid",lastNearoomMessageId)
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener {
                        Log.e(tag,it.toString())
                        if (it.getBoolean("isSuccess")){
                            DB_NearroomMessages(context).saveToDB(it)
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