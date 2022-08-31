package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_GetAllMessages(val context: Context, val id :Int , var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetAllMessages"

    // Getting all messages for username ///////////////////////////////////////////////////////////
    fun get()  {
        val url = MyServerSide.getLatestMessage
        val body = JSONObject()
        body.put("myId",id)
        body.put("lastMessageId",0)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess")) {
                    DB_Messages(context).saveToDB(it)
                }
                callback?.getSuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        //queue.cache.clear()
        queue.add(request)
    }
}