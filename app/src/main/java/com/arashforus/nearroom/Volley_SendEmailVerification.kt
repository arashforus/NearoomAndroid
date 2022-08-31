package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SendEmailVerification(val context: Context, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity ///////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ////////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SendEmailVerification"

    // Sending verification email to user and save verification code to DB /////////////////////////
    fun send() {
        val url = MyServerSide.sendEmailVerification
        val myID = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        val body = JSONObject()
        body.put("myID",myID)

        val request: JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener {
                Log.e(tag, it.toString())
                callback?.getSuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString())
                Toast.makeText(context, "Something wrong , Try again ...", Toast.LENGTH_SHORT).show()
            }) {
        }
        queue.cache.clear()
        queue.add(request)
    }
}