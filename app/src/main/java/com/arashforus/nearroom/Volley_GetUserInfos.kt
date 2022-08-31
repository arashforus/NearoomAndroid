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

class Volley_GetUserInfos(val context: Context, val ids : ArrayList<Int>, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    companion object{
        lateinit var dynamicId : ArrayList<Int>
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue: RequestQueue = Volley.newRequestQueue(context)
    val tag = "GetUserInfo"
    private val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Function for Looping getting User info from server DB ///////////////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getUserInfos

        val body = JSONObject()
        dynamicId = ids
        var ja = JSONArray(dynamicId)
        body.put("ids",ja)

        timer.schedule(object : TimerTask() {
            override fun run() {
                ja = JSONArray(dynamicId)
                body.put("ids",ja)
                //println(" Dynamic Id is $dynamicId")
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener{
                        Log.e(tag,it.toString())
                        if ( it.getBoolean("isSuccess" ) ){
                            if ( !it.isNull("userinfos") ){
                                DB_UserInfos(context).saveUserInfos(it.getJSONObject("userinfos"))
                            }
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

    // Getting User info from server DB just one time //////////////////////////////////////////////
    fun getOnce()  {
        val url = MyServerSide.getUserInfos

        val body = JSONObject()
        val ja = JSONArray(ids)
        body.put("ids",ja)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener{
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess" ) ){
                    callback?.getSuccess(it)
                    if ( !it.isNull("userinfos") ){
                        DB_UserInfos(context).saveUserInfos(it.getJSONObject("userinfos"))
                    }
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)

    }

}