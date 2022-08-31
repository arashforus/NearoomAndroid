package com.arashforus.nearroom

import android.content.Context
import android.os.Handler
import android.provider.ContactsContract

class MyContactsChange( val context: Context) {

    fun register(){
        context.contentResolver.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,true,MyContentObserver( Handler(),context))
    }

    fun unregister(){
        context.contentResolver.unregisterContentObserver(MyContentObserver(Handler(),context))
    }


}