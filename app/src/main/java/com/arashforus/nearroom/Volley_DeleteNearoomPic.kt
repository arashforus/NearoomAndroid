package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_DeleteNearoomPic(val context: Context, val roomId :Int, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "DeleteProfilePic"

    // Deleting nearoom picture from server DB /////////////////////////////////////////////////////
    fun send() {
        val url = MyServerSide.deleteNearoomPic
        val body = JSONObject()
        body.put("roomId",roomId)

        val request: JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener {
                Log.e(tag, it.toString())
                if ( it.getBoolean("isSuccess") ) {
                    callback?.getSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString())
            }) {
        }
        queue.cache.clear()
        queue.add(request)
    }
}