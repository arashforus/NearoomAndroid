package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_DeleteFirebaseToken (val context: Context, val id :Int , var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun updateSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "DeleteFirebaseToken"

    // Deleting firebase token from server DB //////////////////////////////////////////////////////
    fun update()  {
        val url = MyServerSide.deleteTokenId
        val body = JSONObject()
        body.put("id",id)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                callback?.updateSuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)
    }


}