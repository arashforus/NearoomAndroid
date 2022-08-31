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

class Volley_GetNearoomParticipants(val context: Context, private val myId : Int? , private val roomId : Int? , var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue: RequestQueue = Volley.newRequestQueue(context)
    val tag = "GetNearoomParticipants"
    private val timer : Timer = Timer(tag)
    private val timerDelay : Long = 10
    private val timerPeriod : Long = 3000

    // Function for Looping getting User info from server DB ///////////////////////////////////////
    fun get() : Timer {
        val url = MyServerSide.getNearoomParticipant

        val body = JSONObject()
        //val ja = JSONArray(ids)
        body.put("myId",myId)
        body.put("roomId",roomId)

        timer.schedule(object : TimerTask() {
            override fun run() {
                val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
                    Response.Listener{
                        Log.e(tag,it.toString())
                        if (it.getBoolean("isSuccess" ) && !it.isNull("participant")){
                            DB_NearoomsParticipant(context).saveToDB(it.getJSONObject("participant"))
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
        //val ja = JSONArray(ids)
        body.put("myId",myId)
        body.put("roomId",roomId)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener{
                Log.e(tag,it.toString())
                if (it.getBoolean("isSuccess" ) && !it.isNull("participant")){
                    DB_NearoomsParticipant(context).saveToDB(it.getJSONObject("participant"))
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