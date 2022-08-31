package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SendMessageStatus(val context: Context, private val messageId:Int, private val myId : Int, private val anotherId :Int, val status:String)  {


    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SendMessageStatus"

    // Sending delivered and seen status to server DB //////////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.sendMessageStatus
        val body = JSONObject()
        body.put("messageId",messageId)
        body.put("myId", myId)
        body.put("anotherId", anotherId)
        body.put("status",status)

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