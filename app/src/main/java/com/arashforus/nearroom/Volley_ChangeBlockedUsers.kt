package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_ChangeBlockedUsers(val context: Context, private val blockedIDList : String, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result: JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "ChangeBlockedUsers"

    // Change blocked ID list in server DB /////////////////////////////////////////////////////////
    fun send() {
        val url = MyServerSide.changeBlockList
        val myID = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        //val blockedIDList = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_BlockedUsers,"String","") as String
        val body = JSONObject()
        body.put("myID",myID)
        body.put("blockedIDList",blockedIDList)

        val request: JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener {
                Log.e(tag, it.toString())
                if ( it.getBoolean("isSuccess") ) {

                    callback?.getSuccess(it)
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString())
            }) {
        }
        queue.cache.clear()
        queue.add(request)
    }
}