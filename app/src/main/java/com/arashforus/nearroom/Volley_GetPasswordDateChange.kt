package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_GetPasswordDateChange(val context: Context, val myId :Int, val callback:ServerCallBack)  {

    interface ServerCallBack {
        fun getsuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)
    val tag = "GetPasswordDateChange"

    // Fetching the date of change password ////////////////////////////////////////////////////////
    fun get()  {
        val url = MyServerSide.getPasswordDateChange
        val body = JSONObject()
        body.put("myId",myId)
        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess") ){
                    save(it.getString("passworddatechange"))
                }
                callback.getsuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.add(request)
    }

    private fun save(timeStamp : String) {
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_PasswordDateChanged,timeStamp)
    }
}