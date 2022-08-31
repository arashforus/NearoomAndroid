package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_CreateNewRoom(val context: Context, val roomName: String, private val category:String,
                           private val roomCapacity: String, private val lat: Double, private val lon: Double, var callback: ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun isSuccessful(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "CreateNewRoom"
    val username = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Username,"string")

    // Creating new nearoom ////////////////////////////////////////////////////////////////////////
    fun check()  {
        val url = MyServerSide.createNewRoom
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()
        val body = JSONObject()
        body.put("roomname", roomName)
        body.put("category", category)
        body.put("roomcapacity", roomCapacity)
        body.put("locationlat", lat.toString())
        body.put("locationlon", lon.toString())
        body.put("creator", username.toString())
        val request :JsonObjectRequest = object :JsonObjectRequest( Method.POST, url, body,
            Response.Listener { response ->
                Log.e(tag,response.toString())
                callback?.isSuccessful(response)
                progress.dismiss()
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
                progress.dismiss()
            }){
        }
        queue.add(request)
    }

}