package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class Volley_CheckVerificationCode(val context: Context, private val Number : String, Code : String, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun isCorrect(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "CheckVerificationCode"
    private val verificationCode = Code

    // Checking SMS verification code from server //////////////////////////////////////////////////
    fun check()  {
        val url = MyServerSide.checkVerificationCode
        val body = JSONObject()
        body.put("phonenumber",Number)
        body.put("verificationcode",verificationCode)
        println(body.toString())
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()

        val request :JsonObjectRequest = object :JsonObjectRequest( Method.POST,  url, body ,
            Response.Listener{ response ->
                Log.e(tag,response.toString())
                callback?.isCorrect(response)
                progress.dismiss()
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
                progress.dismiss()
            }) {
            }
        queue.add(request)
    }

}