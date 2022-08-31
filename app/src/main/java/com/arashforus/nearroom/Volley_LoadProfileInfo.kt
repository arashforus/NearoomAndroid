package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONObject

class Volley_LoadProfileInfo (val context: Context, val Username:String, private val PhoneNumber:String, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity ///////////////////////////////////////////
    interface ServerCallBack {
        fun loadSuccess(result : JSONObject)
    }

    // Set the global values ////////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "LoadProfileInfo"

    // Function for checking Verification code from DB //////////////////////////////////////////////
    fun load()  {
        val url = MyServerSide.loadProfile
        //val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        //progress.show()
        val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
        val body = JSONObject()
        body.put("username",Username)
        body.put("phonenumber",PhoneNumber)
        body.put("myId",myId)

        val request : JsonObjectRequest = object : JsonObjectRequest(Method.POST , url ,body,
        Response.Listener {
            Log.e(tag,it.toString())
            if (it.get("isSuccess") as Boolean) {
                saveToSharedPreference(it)
                callback?.loadSuccess(it)
            }
            //progress.dismiss()
        },
        Response.ErrorListener {
            Log.e(tag, it.message.toString() )
            //progress.dismiss()
        }){
        }
        queue.cache.clear()
        queue.add(request)
    }

    fun saveToSharedPreference(respond : JSONObject){
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_ID,respond.get("ID"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_DateJoined,respond.get("joinDate"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Username,respond.get("username"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Password,respond.get("password"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Email,respond.get("email"))
        if ( !respond.isNull("isEmailVerify") ){
            MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_IsEmailVerify,respond.get("isEmailVerify"))
        }else{
            MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_IsEmailVerify,0)
        }
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_PhoneNumber,respond.get("phoneNumber"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_PhoneNumberWithCode,respond.get("phoneNumberWithCode"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_PhoneNumberWithCodeFormatted,respond.get("phoneNumberWithCodeFormatted"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_FullName,respond.get("fullName"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Country,respond.get("country"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_CountryFlag,respond.get("countryFlag"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_BirthYear,respond.get("birthYear"))
        MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_TermsAccepted,respond.get("termsAccepted"))
        if ( !respond.isNull("profilePicture") ){
            MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_ProfilePic,respond.get("profilePicture"))
        }else{
            MySharedPreference(MySharedPreference.PreferenceProfile,context).delete(MySharedPreference.Profile_ProfilePic)
        }
        if ( !respond.isNull("status") ){
            MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_Status,respond.get("status"))
        }else{
            MySharedPreference(MySharedPreference.PreferenceProfile,context).delete(MySharedPreference.Profile_Status)
        }
        if ( !respond.isNull("blockedList") ){
            MySharedPreference(MySharedPreference.PreferenceProfile,context).save(MySharedPreference.Profile_BlockedUsers,respond.get("blockedList"))
        }else{
            MySharedPreference(MySharedPreference.PreferenceProfile,context).delete(MySharedPreference.Profile_BlockedUsers)
        }
    }


}