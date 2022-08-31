package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class Volley_SendOnline(val context: Context, private val myId : Int)  {

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    private val tag = "SendOnline"
    val timer : Timer = Timer(tag)
    private val checkPeriod = 3000L // in milliSeconds

    // send online time to server DB ///////////////////////////////////////////////////////////////
    fun send() : Timer {
        val url = MyServerSide.setLastSeen
        val body = JSONObject()
        body.put("myId",myId)

        timer.schedule(object : TimerTask() {
            override fun run() {
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener {
                        Log.e(tag,it.toString())
                    },
                    Response.ErrorListener {
                        Log.e(tag, it.message.toString() )
                    }){
                }
                queue.add(request)
            }
        },10,checkPeriod)
        return timer
    }
}