package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONObject

class Volley_DeleteRoomChats (val context: Context, private val myId: Int , private val roomId: Int , private val lastMessageId : Int, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getResponse(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "DeleteNearoomChats"

    // Deleting the nearoom chats in server DB /////////////////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.deleteNearoomChats
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()
        val body = JSONObject()
        body.put("myId",myId)
        body.put("roomId",roomId)
        body.put("lastMessageId",lastMessageId)

        val request : JsonObjectRequest = object : JsonObjectRequest(Method.POST , url ,body,
        Response.Listener {
            Log.e(tag,it.toString())
            callback?.getResponse(it)
            progress.dismiss()
        },
        Response.ErrorListener {
            Log.e(tag, it.message.toString() )
            progress.dismiss()
        }){}
        queue.add(request)
    }

}