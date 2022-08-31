package com.arashforus.nearroom

import android.content.Context
import android.util.Log
import androidx.core.text.trimmedLength
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Volley_CheckAndSendResetPasswordLink(val context: Context, val number : String, val username : String, var callback : ServerCallBack?) {

    // Interface for returning result in calling activity //////////////////////////////////////////
    interface ServerCallBack {
        fun resultFailed(result : String)
        fun resultSuccess(result : String)
    }


    val queue = Volley.newRequestQueue(context)!!
    val tag = "resetPasswordLink"
    private val apiKey = MySmsServer.API_KEY
    private val secretKey = MySmsServer.secretKey
    var tokenKey: String = ""
    var realPhoneNUmber = ""
    var realUsername = ""

    lateinit var progress : androidx.appcompat.app.AlertDialog


    // Checking is username or phoneNumber exist in server /////////////////////////////////////////
    fun check()  {
        val url = MyServerSide.checkIsExist
        progress = MyProgressDialog().Constructor(context,"Please Wait ...")
        progress.show()

        val body = JSONObject()
        body.put("username",username)
        if ( number.trimmedLength() > 0 ){
            body.put("phonenumber",MyTools(context).addPlusToNumber(number))
        }else{
            body.put("phonenumber","")
        }

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body ,
            Response.Listener {
                Log.e(tag,it.toString())
                //progress.dismiss()
                if (it.getBoolean("isSuccess")) {
                    realPhoneNUmber = it.getString("phonenumber")
                    realUsername = it.getString("username")
                    getToken()
                }else{
                    callback?.resultFailed("Entered number or username doesn't exist in nearoom")
                    progress.dismiss()
                }
            },
            Response.ErrorListener{
                Log.e(tag, it.message.toString() )
                callback?.resultFailed("Failed ... try again!")
                progress.dismiss()
            }) {
        }

        queue.add(request)
    }

    // Make a random alphanumeric //////////////////////////////////////////////////////////////////
    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ( '1'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    // Get token code from SMS Api /////////////////////////////////////////////////////////////////
    fun getToken() {
        val url = MySmsServer.tokenUrl
        val body = JSONObject()
        body.put("UserApiKey",apiKey)
        body.put("SecretKey",secretKey)

        val request = JsonObjectRequest( Request.Method.POST, url,  body,
            { response ->
                Log.e(tag,response.toString())
                if ( response.getBoolean("IsSuccessful") ) {
                    tokenKey = response.getString("TokenKey")
                    send()
                }else{
                    callback?.resultFailed("Failed ... try again!")
                    progress.dismiss()
                }
            },
            { error ->
                Log.e(tag, error.toString())
                progress.dismiss()
                callback?.resultFailed("Failed ... try again!")
            })
        queue.add(request)

    }

    // Function for sending text message to user ///////////////////////////////////////////////////
    fun send(){
        val url = MySmsServer.sendSmsUrl
        val phoneNumber = listOf(realPhoneNUmber)
        val resetLink = getRandomString(15) + realPhoneNUmber.substringBeforeLast(" ").takeLast(4) + getRandomString(15)
        val smsText = listOf(("Nearoom password reset link: \nhttp://nearoom.galleriha.com/sms/checkresetlink.php?resetlink=$resetLink"))

        val bodyMap : LinkedHashMap<String,Any> = LinkedHashMap()
        bodyMap["Messages"] = smsText
        bodyMap["MobileNumbers"] = phoneNumber

        val body = JSONObject(bodyMap as Map<*, *>)
        body.put("LineNumber",MySmsServer.number)
        body.put("SendDateTime","")
        body.put("CanContinueInCaseOfError","false")

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST, url, body,
            Response.Listener{ response ->
                Log.e(tag, response.toString())
                if ( response.getBoolean("IsSuccessful") ) {
                    saveToDB(resetLink)
                }else{
                    callback?.resultFailed("Failed ... try again!")
                    progress.dismiss()
                }
            },
            Response.ErrorListener{ error ->
                Log.e(tag, error.message.toString() )
                progress.dismiss()
                callback?.resultFailed("Failed ... try again!")
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["x-sms-ir-secure-token"] = tokenKey
                return headers
            }
        }
        //queue.getCache().clear()
        queue.add(request)
    }

    // Save the Password reset link to server DB ///////////////////////////////////////////////////
    private fun saveToDB(resetLink : String) {
        val url = MyServerSide.addResetPasswordLink

        val body = JSONObject()
        body.put("phonenumber",realPhoneNUmber)
        body.put("username",realUsername)
        body.put("passresetlink",resetLink)

        val request : JsonObjectRequest = object : JsonObjectRequest( Method.POST,  url, body ,
            Response.Listener {
                Log.e(tag,it.toString())
                if ( it.getBoolean("isSuccess") ){
                    callback?.resultSuccess("Password reset link send to your phone number")
                    progress.dismiss()
                }else{
                    callback?.resultFailed("Failed ... try again!")
                    progress.dismiss()
                }
            },
            Response.ErrorListener{
                Log.e(tag, it.message.toString() )
                callback?.resultFailed("Failed ... try again!")
            }) {
        }

        queue.add(request)
    }

}