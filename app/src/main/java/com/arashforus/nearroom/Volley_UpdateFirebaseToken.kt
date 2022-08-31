package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_UpdateFirebaseToken (val context: Context, val userId : Int, private val firebaseId:String, private val firebaseToken :String, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun updateSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "UpdateFirebaseToken"

    // Function for checking Verification code from DB /////////////////////////////////////////////
    fun update()  {
        val url = MyServerSide.updateTokenId
        val body = JSONObject()
        body.put("userId",userId)
        body.put("id",firebaseId)
        body.put("token",firebaseToken)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess") ) {
                    callback?.updateSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)
    }

}