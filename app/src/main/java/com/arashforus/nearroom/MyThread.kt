package com.arashforus.nearroom

import android.content.Context
import java.util.*

class MyThread(context: Context) {

    val userInfoTimerName = "userInfoTimer"
    val userInfoTimerPeriod : Long = 2000 // In millisecond


    fun usersInfos(username: String) : Timer{
        val timer = Timer(userInfoTimerName)
        timer.schedule(object : TimerTask() {
            override fun run() {
                TODO("Not yet implemented")
            }
        },100,userInfoTimerPeriod)
        return timer
    }


}