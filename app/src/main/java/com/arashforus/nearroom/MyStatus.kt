package com.arashforus.nearroom

import android.content.Context

class MyStatus(val context : Context) {

    private val delayTimeForLastSeen = 4
    private val delayTimeForIsTyping = 4

    fun find( myId : Int , toId : Int) : String? {
        val ja = DB_UserInfos(context).getLastSeenAndIsTyping(toId)
        var lastSeen : String? = null
        var isTypingTime : String? = null
        var isTypingTo : String? = null
        if ( !ja.isNull(0) ){
            lastSeen = ja.getString(0)
        }
        if ( !ja.isNull(1) ){
            isTypingTime = ja.getString(1)
        }
        if ( !ja.isNull(2) ){
            isTypingTo = ja.getString(2)
        }

        var resultText : String? = null
        //println("second diff is ........ "+MyDateTime().SecondDiff(lastSeen!!) )
        //println(" last seen : $lastSeen ----- isTypingTime : $isTypingTime ----- isTypingTo: $isTypingTo")

        if ( isTypingTime.isNullOrEmpty() || isTypingTo.isNullOrEmpty()){
            if (!lastSeen.isNullOrEmpty()){
                resultText = if ( MyDateTime().secondDiff(lastSeen) <= delayTimeForLastSeen ){
                    "Online"
                }else{
                    "Last seen " + MyDateTime().gmtToLocal(lastSeen,"HH:mm")
                }
            }
        }else{
            if ( isTypingTo == "P$myId" && MyDateTime().secondDiff(isTypingTime) <= delayTimeForIsTyping ){
                resultText = "Typing..."
            }else{
                if (!lastSeen.isNullOrEmpty()){
                    resultText = if ( MyDateTime().secondDiff(lastSeen) <= delayTimeForLastSeen ){
                        "Online"
                    }else{
                        "Last seen " + MyDateTime().gmtToLocal(lastSeen,"HH:mm")
                    }
                }
            }
        }
        return resultText
    }

    fun findForRooms(roomId : Int , membersId : ArrayList<Int>) : String? {
        val onlineList = ArrayList<Int>()
        val typingList = ArrayList<Int>()
        membersId.forEach {
            val ja = DB_UserInfos(context).getLastSeenAndIsTyping(it)
            var lastSeen : String? = null
            var isTypingTime : String? = null
            var isTypingTo : String? = null
            if ( !ja.isNull(0) ){
                lastSeen = ja.getString(0)
                if ( !ja.isNull(1) ){
                    isTypingTime = ja.getString(1)
                }
                if ( !ja.isNull(2) ){
                    isTypingTo = ja.getString(2)
                }
            }

            // Check if online add it to onlineList ////////////////////////////////////////////////
            if ( !lastSeen.isNullOrEmpty() ){
                if ( MyDateTime().secondDiff(ja.getString(0)) <= delayTimeForLastSeen ){
                    onlineList.add(it)
                }
            }
            // Check if is typing to room add it to typingList /////////////////////////////////////
            if ( !isTypingTime.isNullOrEmpty() && !isTypingTo.isNullOrEmpty() ){
                if ( isTypingTo == "R$roomId" && MyDateTime().secondDiff(isTypingTime) <= delayTimeForIsTyping ){
                    typingList.add(it)
                }
            }
        }

        var resultText : String? = ""
        if ( typingList.size > 0 ){
            // Some one is typing //////////////////////////////////////////////////////////////////
            var count = 1
            typingList.forEach {
                resultText = if ( count == 1 ){
                    "$it "
                }else{
                    "$resultText,$it "
                }
                count += count
            }
            resultText += "is typing ..."
        }else{
            // No one is typing ////////////////////////////////////////////////////////////////////
            resultText = if ( onlineList.size > 0 ){
                // Some one is online //////////////////////////////////////////////////////////////
                "${onlineList.size} member is online"
            }else{
                // No one is online ////////////////////////////////////////////////////////////////
                "Tap here for more info"
            }
        }

        return resultText
    }


}
