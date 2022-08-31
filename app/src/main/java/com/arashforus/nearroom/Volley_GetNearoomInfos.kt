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

class Volley_GetNearoomInfos(val context: Context, private val nearooms : ArrayList<Int>, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    companion object{
        lateinit var dynamicNearooms : ArrayList<Int>
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue: RequestQueue = Volley.newRequestQueue(context)
    val tag = "GetNearoomInfos"
    private val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Function for Looping getting User info from server DB ///////////////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getNearoomInfos

        val body = JSONObject()
        dynamicNearooms = nearooms
        var ja = JSONArray(dynamicNearooms)
        body.put("nearooms",ja)

        timer.schedule(object : TimerTask() {
            override fun run() {
                ja = JSONArray(dynamicNearooms)
                body.put("ids",ja)
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener{
                        Log.e(tag,it.toString())
                        if (it.getBoolean("isSuccess" ) ){
                            if ( !it.isNull("roominfos") ){
                                DB_NearoomInfos(context).saveNearoomInfo(it.getJSONObject("roominfos"))
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

    // getting User info from server DB just one time //////////////////////////////////////////////
    fun getOnce()  {
        val url = MyServerSide.getNearoomInfos

        val body = JSONObject()
        val ja = JSONArray(nearooms)
        body.put("nearooms",ja)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener{
                Log.e(tag,it.toString())
                if (it.getBoolean("isSuccess" ) ){
                    if ( !it.isNull("roominfos") ){
                        DB_NearoomInfos(context).saveNearoomInfo(it.getJSONObject("roominfos"))
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

}