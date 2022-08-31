package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Volley_GetLastSeen(val context: Context, val userIds:Array<Int>)  {

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetLastSeen"
    val timer : Timer = Timer(tag)

    // Function for checking Verification code from DB /////////////////////////////////////////////
    fun send() : Timer {
        val url = MyServerSide.getLastSeens
        val body = JSONObject()
        val ja = JSONArray(userIds)
        body.put("userIds",ja)
        //println(timer)
        timer.schedule(object : TimerTask() {
            override fun run() {
                // Get last seens every 3 seconds to server side ///////////////////////////////////
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener {
                        Log.e(tag,it.toString())
                        if ( it.getBoolean("isSuccess") && !it.isNull("lastseens") ){
                            DB_UserInfos(context).saveLastSeen(it.getJSONObject("lastseens"))
                        }

                    },
                    Response.ErrorListener {
                        Log.e(tag, it.message.toString() )
                    }){
                }
                queue.add(request)
            }
        },10,30000)
        return timer
    }
}