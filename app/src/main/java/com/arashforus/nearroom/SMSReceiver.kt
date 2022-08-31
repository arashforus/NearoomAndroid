package com.arashforus.nearroom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast


class SMSReceiver(val callback:ICallback?)  : BroadcastReceiver() {

    interface ICallback {
        fun updateUI(value:String)
    }

    override fun onReceive(context: Context?, intent: Intent?)  {
        val bundle = intent!!.extras
        try {
            if (bundle != null) {
                val pdusObj = bundle["pdus"] as Array<*>?
                for (i in pdusObj!!.indices) {
                    val currentMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray)
                    val phoneNumber = currentMessage.displayOriginatingAddress
                    val message = currentMessage.displayMessageBody

                    if (phoneNumber == MySmsServer.numberWithCode) {
                        //Toast.makeText(context,"senderNum: $phoneNumber, message: $message",Toast.LENGTH_LONG).show()
                        callback?.updateUI(message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver : $e")
        }
    }
}
