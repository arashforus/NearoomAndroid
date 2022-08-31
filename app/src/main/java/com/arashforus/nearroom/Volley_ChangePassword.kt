package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Volley_ChangePassword(val context: Context, val myId: Int , private val currentPassword : String, private val newPassword : String, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun getSuccess(result : JSONObject)
        fun getError()
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "ChangePassword"

    // Change password in server DB ////////////////////////////////////////////////////////////////
    fun check()  {
        val url = MyServerSide.changePassword
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()
        val body = JSONObject()
        body.put("myId",myId)
        body.put("currentPassword",currentPassword)
        body.put("newPassword",newPassword)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST , url ,body,
            Response.Listener {
                Log.e(tag,it.toString())
                progress.dismiss()
                try {
                    if (it.get("isSuccess") as Boolean) {
                        saveToSharedPref(it)
                        callback?.getSuccess(it)
                    }else{
                        callback?.getError()
                    }
                }catch (e:JSONException){
                    e.printStackTrace()
                    callback?.getError()
                }
            },
            Response.ErrorListener {
                Log.e(tag, it.message.toString() )
                callback?.getError()
                progress.dismiss()
            }){
        }
        queue.cache.clear()
        queue.add(request)
    }

    private fun saveToSharedPref(jo: JSONObject?) {
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Password,newPassword)
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_PasswordDateChanged,jo?.getString("dateChanged"))

    }
}