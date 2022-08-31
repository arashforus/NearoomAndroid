package com.arashforus.nearroom

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import androidx.core.app.ActivityCompat

class MyContentObserver( handler: Handler , val context : Context) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (!selfChange) {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    DB_Contacts(context).insertLastItemToDb()
                }
            } catch ( e : Exception) {
                e.printStackTrace()
            }
        }
    }


    //override fun onChange(selfChange: Boolean) {
   //     super.onChange(selfChange,null)
    //}
}