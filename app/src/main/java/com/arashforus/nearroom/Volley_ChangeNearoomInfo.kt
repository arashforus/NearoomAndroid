package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_ChangeNearoomInfo(val context: Context, private val myId : Int, val roomId: Int, val roomName :String, val category : String,
                               val description : String, private val capacity : String, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "ChangeNearoomInfo"

    // Change nearoom info in server DB ////////////////////////////////////////////////////////////
    fun send() {
        val url = MyServerSide.changeNearoomInfo
        val body = JSONObject()
        body.put("myId",myId)
        body.put("roomId",roomId)
        body.put("roomName",roomName)
        body.put("category",category)
        body.put("description",description)
        body.put("capacity",capacity)

        val request: JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener {
                Log.e(tag, it.toString())
                callback?.getSuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString())
            }) {
        }
        queue.cache.clear()
        queue.add(request)
    }
}