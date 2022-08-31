package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SendMessage(val context: Context, val type:String, private val fromId :Int, private val toId :Int?,
                         val roomId : Int?, val message : String?, val uri : String?, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun sendSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SendMessage"

    // Sending message from private or nearoom to server ///////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.sendMessage
        val body = JSONObject()
        body.put("type",type)
        body.put("fromId",fromId)
        body.put("toId",toId)
        body.put("roomId",roomId)
        body.put("message",message)
        body.put("uri",uri)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                if (it.getBoolean("isSuccess") ) {
                    callback?.sendSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)
    }


}