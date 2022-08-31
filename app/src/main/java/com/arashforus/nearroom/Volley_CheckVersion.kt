package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_CheckVersion(val context: Context, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getsuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "CheckVersion"

    // Checking the version of application /////////////////////////////////////////////////////////
    fun get()  {
        val url = MyServerSide.checkVersion
        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,null,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess") ) {
                    saveToSharedPref(it)
                }
                callback?.getsuccess(it)
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
            }){
        }
        queue.cache.clear()
        queue.add(request)
    }

    private fun saveToSharedPref(jo: JSONObject?) {
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_Number,jo?.getInt("number"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_Version,jo?.getString("version"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_Icon,jo?.getString("icon"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_Size,jo?.getInt("size"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_Description,jo?.getString("description"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_MinSupportNumber,jo?.getInt("minsupportnumber"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_DownloadLink,jo?.getString("downloadlink"))
        MySharedPreference(MySharedPreference.PreferenceAppVersion,context).save(MySharedPreference.AppVersion_DateReleased,jo?.getString("datereleased"))
    }
}