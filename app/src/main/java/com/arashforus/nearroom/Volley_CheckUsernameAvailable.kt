package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class Volley_CheckUsernameAvailable(val context: Context, val username: String, var callback: ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun isUsernameAvailable(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "CheckUsernameAvailable"

    // Checking is this username is exist in server Db or not //////////////////////////////////////
    fun check()  {
        val url = MyServerSide.checkUsername

        val body = JSONObject()
        body.put("username",username)

        val request :JsonObjectRequest = object :JsonObjectRequest(  Method.POST,  url, body ,
            Response.Listener { response ->
                Log.e(tag,response.toString())
                callback?.isUsernameAvailable(response)
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
            }) {
            }

        queue.add(request)
    }

}