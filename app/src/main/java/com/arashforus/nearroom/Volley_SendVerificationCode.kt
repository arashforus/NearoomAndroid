package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.random.Random

class Volley_SendVerificationCode(val context: Context, private val Number : String, var callback : ServerCallBack?)  {

    interface ServerCallBack {
        fun sendSuccess(result : JSONObject)
    }

    // Set the global values ///////////////////////////////////////////////////////////////////////
    private val apiKey = MySmsServer.API_KEY
    private var secretKey: String = MySmsServer.secretKey
    var tokenKey: String = ""
    val queue = Volley.newRequestQueue(context)!!
    val tag = "SendVerificationCode"
    private val phoneNumber = listOf(Number)
    private val verificationCode = Random.nextInt(111111,999999)
    private val smsText = listOf(("Nearoom Verification Code : $verificationCode"))

    val progress = MyProgressDialog().Constructor(context,"Please Wait ...")

    fun check() {
        val url = MyServerSide.checkIsExist
        val body = JSONObject()
        body.put("phonenumber",Number)

        progress.show()
        val request = JsonObjectRequest(Request.Method.POST, url, body,
            { response ->
                Log.e(tag,response.toString())
                if ( response.getBoolean("isSuccess") ){
                    if ( response.isNull("username") ){
                        if ( response.getInt("verificationAttempt") < 5 ){
                            getToken()
                        }else{
                            response.put("isExceedAttempt",true)
                            callback?.sendSuccess(response)
                            progress.dismiss()
                        }
                    }else{
                        response.put("isExist",true)
                        callback?.sendSuccess(response)
                        progress.dismiss()
                    }
                }else{
                    if (response.getBoolean("notExist")){
                        getToken()
                    }
                }
            },
            { error ->
                Log.e(tag, error.toString())
                progress.dismiss()
            })
        queue.add(request)

    }

    // Getting token code and start sending text message ///////////////////////////////////////////
    private fun getToken() {
        val url = MySmsServer.tokenUrl
        val body = JSONObject()
        body.put("UserApiKey",apiKey)
        body.put("SecretKey",secretKey)

        progress.show()
        val request = JsonObjectRequest(Request.Method.POST, url, body,
            { response ->
                Log.e(tag,response.toString())
                tokenKey = response.getString("TokenKey")
                sendCode()
            },
            { error ->
                Log.e(tag, error.toString())
                progress.dismiss()
            })
        queue.add(request)

    }

    // Sending text message to user ////////////////////////////////////////////////////////////////
    private fun sendCode(){
        val url = MySmsServer.sendSmsUrl

        val bodyMap : LinkedHashMap<String,Any> = LinkedHashMap()
        bodyMap["Messages"] = smsText
        bodyMap["MobileNumbers"] = phoneNumber

        val body = JSONObject(bodyMap as Map<*, *>)
        body.put("LineNumber",MySmsServer.number)
        body.put("SendDateTime","")
        body.put("CanContinueInCaseOfError","false")

        val request :JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, body,
            Response.Listener { response ->
                Log.e(tag, response.toString())
                saveCodeToDB()
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
                progress.dismiss()
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["x-sms-ir-secure-token"] = tokenKey
                return headers
        }
    }
        queue.cache.clear()
        queue.add(request)
    }

    // Save the verification code to server DB /////////////////////////////////////////////////////
    private fun saveCodeToDB() {
        val url = MyServerSide.addVerificationCode
        val body = JSONObject()
        body.put("phonenumber",Number)
        body.put("verificationcode",verificationCode.toString())
        println(body.toString())

        val request :JsonObjectRequest = object :JsonObjectRequest( Method.POST, url, body ,
            Response.Listener { response ->
                Log.e(tag,response.toString())
                callback?.sendSuccess(response)
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