package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_ChangeProfile(val context: Context, private val myID : String, val fullName: String, val username :String, val email : String, val birthYear : String, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "ChangeProfile"

    // Changing user info in server DB /////////////////////////////////////////////////////////////
    fun send() {
        val url = MyServerSide.changeProfile
        val body = JSONObject()
        body.put("myID",myID)
        body.put("fullname",fullName)
        body.put("username",username)
        body.put("email",email)
        body.put("birthyear",birthYear)

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