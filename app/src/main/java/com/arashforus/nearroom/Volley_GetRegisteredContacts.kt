package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class Volley_GetRegisteredContacts (val context: Context, private val Numbers : ArrayList<String>, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetRegisteredContacts"

    // Getting Registered Contacts from server /////////////////////////////////////////////////////
    fun get()  {
        val url = MyServerSide.getRegisteredContacts

        val body = JSONObject()
        val ja = JSONArray(Numbers)
        body.put("numbers",ja)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                if (it.getBoolean("isSuccess")){
                    //println("Get registered contacts success from server")
                    DB_Contacts(context).setRegisteredContacts(it)
                    callback?.getSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }

        queue.cache.clear()
        queue.add(request)
    }
}