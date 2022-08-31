package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Volley_GetNearoomUserInfos(val context: Context, private val ids : ArrayList<Int>, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue: RequestQueue = Volley.newRequestQueue(context)
    val tag = "GetNearoomUserInfo"
    private val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Function for Looping getting Nearoom User info from server DB ///////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getUserInfos

        val body = JSONObject()
        val ja = JSONArray(ids)
        body.put("ids",ja)

        timer.schedule(object : TimerTask() {
            override fun run() {
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener{
                        Log.e(tag,it.toString())
                        if (it.getBoolean("isSuccess" ) && !it.isNull("userinfos")){
                            DB_NearoomUserInfos(context).saveUserInfos(it.getJSONObject("userinfos"))
                            callback?.getSuccess(it)
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

    // Getting Nearoom User info from server DB just one time //////////////////////////////////////
    fun getOnce()  {
        val url = MyServerSide.getUserInfos

        val body = JSONObject()
        val ja = JSONArray(ids)
        body.put("ids",ja)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener{
                Log.e(tag,it.toString())
                if (it.getBoolean("isSuccess" ) && !it.isNull("userinfos")){
                    DB_NearoomUserInfos(context).saveUserInfos(it.getJSONObject("userinfos"))
                    callback?.getSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)

    }

}