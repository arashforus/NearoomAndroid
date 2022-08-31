package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SendNearoomMessageStatus(val context: Context, private val messageId:Int, val roomId:Int , val status:String)  {


    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "NearoomMessageStatus"

    // Function for checking Verification code from DB //////////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.setNearoomMessageStatus
        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        val body = JSONObject()
        body.put("messageId",messageId)
        body.put("myId",myId)
        body.put("roomId",roomId)
        body.put("status",status)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)
    }


}