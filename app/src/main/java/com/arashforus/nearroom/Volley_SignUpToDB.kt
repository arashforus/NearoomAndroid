package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_SignUpToDB(val context: Context, var callback : ServerCallBack?)  {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun saveSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SignUpToDB"



    // Saving profile to server DB /////////////////////////////////////////////////////////////////
    fun save()  {
        val url = MyServerSide.signUp

        val username = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Username,"string").toString()
        val fullName = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_FullName,"string").toString()
        val email = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Email,"string").toString()
        val phoneNumber = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_PhoneNumber,"string").toString()
        val phoneNumberWithCode = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_PhoneNumberWithCode,"string").toString()
        val phoneNumberWithCodeFormatted = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_PhoneNumberWithCodeFormatted,"string").toString()
        val birthYear = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_BirthYear,"string").toString()
        val country = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Country,"string").toString()
        val countryFlag = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_CountryFlag,"string").toString()
        val password = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Password,"string").toString()
        val termsAccepted = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_TermsAccepted,"boolean").toString()

        val body = JSONObject()
        body.put("phonenumber",phoneNumber)
        body.put("username",username)
        body.put("fullname",fullName)
        body.put("email",email)
        body.put("birthyear",birthYear)
        body.put("country",country)
        body.put("countryflag",countryFlag)
        body.put("password",password)
        body.put("termschecked",termsAccepted)
        body.put("phonenumberwithcode",phoneNumberWithCode)
        body.put("phonenumberwithcodeformatted",phoneNumberWithCodeFormatted)
        body.put("brand",android.os.Build.BRAND)
        body.put("model",android.os.Build.MODEL)
        body.put("sdk",android.os.Build.VERSION.SDK_INT.toString())
        body.put("android",android.os.Build.VERSION.RELEASE)
        //println(body.toString())
        val progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()

        val request :JsonObjectRequest = object :JsonObjectRequest( Method.POST,  url, body ,
            Response.Listener { response ->
                Log.e(tag,response.toString())
                callback?.saveSuccess(response)
                progress.dismiss()
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
                progress.dismiss()
            }) {
            }
        queue.add(request)
    }

}