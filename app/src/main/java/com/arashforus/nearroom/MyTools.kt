package com.arashforus.nearroom

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.util.Base64
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.transition.Explode
import java.lang.Exception
import java.util.ArrayList
import java.util.regex.Pattern

class MyTools(val context : Context) {

    fun screenWidth() : Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun screenHeight() : Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getReadableSize(size : Int) : String {
        val kbsize = size
        var returnsize : String = ""
        if (kbsize > 1024){
            returnsize = (kbsize / 1024).toString() + "MB"
        }else{
            returnsize = kbsize.toString() + "KB"
        }
        return returnsize
    }

    fun getReadableSizeFromByte(size : Int) : String {
        var returnsize: String = "0 KB"
        val ByteSize = size
        if (ByteSize < 1024){
            returnsize = "$ByteSize B"
        }else{
            if ( ByteSize < 1024*1024 ){
                returnsize = "${ByteSize/1024} KB"
            }else{
                if ( ByteSize < 1024*1024*1024 ){
                    returnsize = "${ByteSize/(1024*1024)} MB"
                }else{
                    returnsize = "${ByteSize/(1024*1024*1024)} GB"
                }
            }
        }
        return returnsize
    }

    fun changeFileType(filename : String , type : String) : String {
        return filename.replaceAfterLast(".",type)
    }

    fun findFileType(filename : String ) : String {
        return filename.substringAfterLast(".","***")
    }

    fun resizableWindow(activity : Activity){
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun forceHideKeyboard(activity : Activity){
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun closeKeyboard(){
        val activity = context as Activity
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun enableWindowContentTransition(activity: Activity , enterTransition : Transition? , exitTransition : Transition?){
        activity.window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        if ( enterTransition == null ){
            //
        }else{
            //activity.window.enterTransition = android.transition.Explode()
        }
        if ( exitTransition == null ){
            //
        }else{
            //activity.window.exitTransition = android.transition.Explode()
        }


    }

    fun getCountryCodeFromMyNumber() : String {
        val myNum = MySharedPreference("Profile",context).load("PhoneNumberWithCodeFormatted","String")
        return myNum.toString().substringBefore(" ","+")
    }

    fun MakeNumberWithCountryCode(countryCode : String , number: String ): String{
        var numberWithCode : String = number.trim()
        if (number.trim().startsWith("0")){
            numberWithCode = countryCode + number.drop(1).trim()
        }else{
            if (!number.trim().startsWith("+")){
                numberWithCode = "+" + number.trim()
            }
        }
        return numberWithCode.filter { !it.isWhitespace() }
    }

    fun addPlusToNumber( number: String ): String{
        var numberWithPlus : String = number.trim()
        if (!number.trim().startsWith("+")){
            numberWithPlus = "+" + number.trim()
        }
        return numberWithPlus.filter { !it.isWhitespace() }
    }

    fun measurePasswordStrength(password : String) : Array<Any> {
        var strength = ""
        var score = 0
        var color = R.color.passwordStrengthVeryWeak

        if ( password.length >= 6 ){
            score += 1
        }
        if ( password.length >= 8 ){
            score += 1
        }
        if ( password.length >= 10 ){
            score += 1
        }
        if ( password.length >= 12 ){
            score += 1
        }

        if ( Pattern.matches(".*[a-z].*",password ) ){
            score += 1
        }
        if ( Pattern.matches(".*[A-Z].*",password ) ){
            score += 1
        }
        if ( Pattern.matches(".*[0-9].*",password ) ){
            score += 1
        }
        if ( Pattern.matches(".*[ !@#$%^&*_?].*",password ) ){
            score += 1
        }
        when ( score ) {
            2 -> {strength = "Very Weak"
                color = R.color.passwordStrengthVeryWeak}
            3 -> {strength = "Weak"
                color = R.color.passwordStrengthWeak}
            4 -> {strength = "Medium"
                color = R.color.passwordStrengthMedium}
            5 -> {strength = "Strong"
                color = R.color.passwordStrengthStrong}
            6 -> {strength = "Strong"
                color = R.color.passwordStrengthStrong}
            7 -> {strength = "Very Strong"
                color = R.color.passwordStrengthVeryStrong}
            8 -> {strength = "Very Strong"
                color = R.color.passwordStrengthVeryStrong}
        }
        return arrayOf(strength,color)
    }

    fun decodeThumb(encodeText : String ): Bitmap? {
        val ba = Base64.decode(encodeText,Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(ba,0,ba.size)
        println("byte count is :  "+bitmap.allocationByteCount)
        return bitmap
    }

    fun secondToFullTime(seconds : Int): String {
        val min = seconds/60
        val sec = seconds - ( min * 60 )
        val minString = if ( min < 10 ){
            "0$min"
        }else{
            min
        }
        val secString = if ( sec < 10 ){
            "0$sec"
        }else{
            sec
        }
        return "$minString:$secString"
    }

    fun getDrawableId(name : String ) : Int {
        return try {
            context.resources.getIdentifier(name,"drawable",context.packageName)
        }catch ( e : Exception){
            println(e.message)
            -1
        }
    }

    fun changeBlockedList(id : String , add : Boolean) : String{
        var newBlockedList = ""
        val oldBlockedList = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_BlockedUsers,"String","") as String
        if ( add ){
            // add ID to blocked list //////////////////////////////////////////////////////////////
            if (oldBlockedList.isNotEmpty()){
                newBlockedList = "$oldBlockedList,$id"
            }else{
                newBlockedList = id
            }
        }else{
            // remove ID from blocked list /////////////////////////////////////////////////////////
            var blockedArray = oldBlockedList.split(",")
            if ( blockedArray.contains(id) ){
                blockedArray = blockedArray.minus(id)
                newBlockedList = blockedArray.joinToString(",","","")
            }else{
                newBlockedList = oldBlockedList
            }
        }
        return newBlockedList
    }

    companion object{
        val transitionExplode = android.transition.Explode()
        val transitionSlide = Slide()
        val transitionFade = Fade()
    }
}