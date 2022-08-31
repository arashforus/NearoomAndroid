package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONObject

class Volley_JoinNearoom (val context: Context, private val myId: Int, val username :String ,private val roomId: Int, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getResponse(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)
    val tag = "JoinNearoom"

    // Joining the nearoom /////////////////////////////////////////////////////////////////////////
    fun send()  {
        val url = MyServerSide.joinNearoom
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()
        val body = JSONObject()
        body.put("myId",myId)
        body.put("username",username)
        body.put("roomId",roomId)

        val request : JsonObjectRequest = object : JsonObjectRequest(Method.POST , url ,body,
        Response.Listener {
            Log.e(tag,it.toString())
            if ( it.getBoolean("isSuccess") ){
                DB_NearroomMessages(context).saveToDB(it)
                DB_NearoomsParticipant(context).saveToDB(it)
                if ( !it.isNull("roomInfo") ){
                    DB_NearoomInfos(context).saveNearoomInfo(it.getJSONObject("roomInfo"))
                }
            }
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