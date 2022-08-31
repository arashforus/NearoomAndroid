package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_IsTyping(val context: Context, private val fromId :Int, private val toId :String )  {

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "IsTyping"

    // Sending is typing parameters to server DB ///////////////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.isTyping
        val body = JSONObject()
        body.put("fromId",fromId)
        body.put("toId",toId)

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

}