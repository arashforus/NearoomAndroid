package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_Signin(val context: Context, val username: String, private val phoneNumber: String, private  val password: String, var callback: ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "signIn"


    // Check sign in parameters and get access if true /////////////////////////////////////////////
    fun check()  {
        val url = MyServerSide.signIn
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()
        val body = JSONObject()
        body.put("username",username)
        body.put("phonenumber",phoneNumber)
        body.put("password",password)

        val request : JsonObjectRequest = object :JsonObjectRequest(Method.POST, url, body,
            Response.Listener {
                Log.e(tag,it.toString())
                callback?.getSuccess(it)
                progress.dismiss()
            },
            Response.ErrorListener{
                Log.e(tag, it.message.toString() )
                progress.dismiss()
            }) {
            }
        queue.add(request)
    }

}