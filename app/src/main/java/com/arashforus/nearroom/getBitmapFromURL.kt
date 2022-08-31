package com.arashforus.nearroom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class getBitmapFromURL {

    fun load(src: String) : Bitmap? {
        try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val myBitmap : Bitmap = BitmapFactory.decodeStream(input)
            return myBitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

}