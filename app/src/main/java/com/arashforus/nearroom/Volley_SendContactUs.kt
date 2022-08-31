package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SendContactUs(val context: Context, val fullName:String, val phoneNumber:String, val email:String, val title:String, val message:String,
                           private val reportUserId : Int, private val reportRoomId : Int, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SendContactUs"

    // Sending contact us parameters to server /////////////////////////////////////////////////////
    fun send() {
        val url = MyServerSide.sendContactUs
        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int")
        val body = JSONObject()
        body.put("myID", myId)
        body.put("fullName", fullName)
        body.put("phoneNumber", phoneNumber)
        body.put("email", email)
        body.put("subject", title)
        body.put("message", message)
        if ( reportUserId > 0 ){
            body.put("reportUserId", reportUserId)
        }
        if ( reportRoomId > 0 ){
            body.put("reportRoomId", reportRoomId)
        }

        val request: JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener{
                Log.e(tag, it.toString())
                callback?.getSuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString())
                callback?.getSuccess(JSONObject())
            }) {
        }
        queue.cache.clear()
        queue.add(request)
    }
}