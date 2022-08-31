package com.arashforus.nearroom

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import kotlin.text.StringBuilder

class MyAsyncTask : AsyncTask<String, Void, String>() {
    val progressdialoge  = ProgressDialog(ReceiveCodeActivity().applicationContext)
    var link = ""
    var number = "5155"
    var builder: StringBuilder? = null

    fun AsyncTaskCheckCde(link:String,number:String){
        this.link = link
        this.number = number
    }

    override fun doInBackground(vararg params: String?): String {
        try {

            val data :String = URLEncoder.encode("number","UTF8")+"="+URLEncoder.encode(number,"UTF8")
            val url:URL = URL(link)
            val connection: URLConnection =  url.openConnection()
            connection.doOutput = true
            val write:OutputStreamWriter = OutputStreamWriter(connection.getOutputStream())
            write.write(data)
            write.flush()

            val reader :BufferedReader = BufferedReader(InputStreamReader(connection.getInputStream()))
            builder = StringBuilder()
            var line: String? = null

            while ((reader.readLine()) != null){
                line = reader.readLine()
                builder!!.append(line)
            }

        }catch (e:Exception){

        }
        return  builder.toString()
    }

    override fun onPreExecute() {
        super.onPreExecute()
        progressdialoge.setTitle("Please Wait ...")
        progressdialoge.show()

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        progressdialoge.dismiss()
    }
}