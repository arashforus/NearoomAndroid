package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_GetContacts (val context: Context, val ContactNumbers : JSONObject, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity
    interface ServerCallBack {
        fun isavailable(result : String)
    }


    // Set the global values
    val queue = Volley.newRequestQueue(context)
    val tag = "GetContacts"


    // Function for checking Verification code from DB
    fun check()  {
        val URL = "http://nearoom.galleriha.com/sms/getcontacts.php"

        val request : StringRequest = object : StringRequest(
            Method.POST,
            URL,
            Response.Listener<String> { response ->
                Log.e(tag,response.toString())
                println("correct" in response)
                callback?.isavailable(response.toString())
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
            }) {
            override fun  getParams() : Map<String, String> {
                val params = HashMap<String, String>()
                params.put("contacts", ContactNumbers.toString())

                return params
            }
        }
        queue.add(request)
    }

}