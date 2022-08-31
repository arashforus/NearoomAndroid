package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_GetLastSponsorship(val context: Context, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "GetLastSponsorship"

    // Fetching last sponsorship information ///////////////////////////////////////////////////////
    fun get()  {
        val url = MyServerSide.getLastSponsorhship

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,null,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess") ) {
                    saveToSharedPref(it)
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

    private fun saveToSharedPref(jo: JSONObject?) {
        MySharedPreference(MySharedPreference.PreferenceSponsorship,context).save(MySharedPreference.Sponsorship_Logo,jo?.getString("Logo"))
        MySharedPreference(MySharedPreference.PreferenceSponsorship,context).save(MySharedPreference.Sponsorship_Name,jo?.getString("Name"))
        MySharedPreference(MySharedPreference.PreferenceSponsorship,context).save(MySharedPreference.Sponsorship_Description,jo?.getString("Description"))
        MySharedPreference(MySharedPreference.PreferenceSponsorship,context).save(MySharedPreference.Sponsorship_Link,jo?.getString("Link"))
    }
}