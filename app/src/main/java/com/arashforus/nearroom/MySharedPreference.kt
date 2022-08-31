package com.arashforus.nearroom

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.util.*

class MySharedPreference( PREFERENCE_name : String, val context : Context) {

    private val PREFERENCE_NAME = PREFERENCE_name.toUpperCase(Locale.ROOT)
    private val sharedPreference : SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE)

    companion object{
        const val PreferenceApp = "App"
        const val PreferenceTempProfile = "TempProfile"
        const val PreferenceProfile = "Profile"
        const val PreferenceSponsorship = "Sponsorship"
        const val PreferenceAppVersion = "AppVersion"
        const val PreferenceWallpaper = "Wallpaper"
        const val PreferenceMessageNotification = "MessageNotification"
        const val PreferenceNearoomNotification = "NearoomNotification"
        const val PreferenceDownloadSetting = "DownloadSetting"

        const val App_Login = "Login"
        const val App_IntroShowed = "IntroShowed"
        const val App_FirstTimeInit = "FirstTimeInit"
        const val App_RunningActivity = "RunningActivity"
        const val App_ChatTo = "ChatTo"
        const val App_LastMessageIdGet = "LastMessageIdGet"
        const val App_LastNearoomMessageIdGet = "LastNearoomMessageIdGet"
        const val App_Number = "AppNumber"
        const val App_Version = "AppVersion"
        const val App_Icon = "AppIcon"
        const val App_Description = "AppDescription"
        const val App_DateReleased = "AppDateReleased"

        const val TempProfile_Country = "Country"
        const val TempProfile_CountryFlag = "CountryFlag"
        const val TempProfile_PhoneNumber = "PhoneNumber"
        const val TempProfile_PhoneNumberWithCode = "PhoneNumberWithCode"
        const val TempProfile_PhoneNumberWithCodeFormatted = "PhoneNumberWithCodeFormatted"

        const val Profile_ID = "ID"
        const val Profile_Username = "Username"
        const val Profile_FullName = "FullName"
        const val Profile_Email = "Email"
        const val Profile_IsEmailVerify = "IsEmailVerify"
        const val Profile_BirthYear = "BirthYear"
        const val Profile_Status = "Status"
        const val Profile_Password = "Password"
        const val Profile_PasswordDateChanged = "PasswordDateChanged"
        const val Profile_ProfilePic = "ProfilePic"
        const val Profile_Country = "Country"
        const val Profile_CountryFlag = "CountryFlag"
        const val Profile_PhoneNumber = "PhoneNumber"
        const val Profile_PhoneNumberWithCode = "PhoneNumberWithCode"
        const val Profile_PhoneNumberWithCodeFormatted = "PhoneNumberWithCodeFormatted"
        const val Profile_TermsAccepted = "TermsAccepted"
        const val Profile_BlockedUsers = "BlockedUsers"

        const val Profile_DateJoined = "DateJoined"
        const val Profile_FirebaseId = "FirebaseId"
        const val Profile_FirebaseToken = "FirebaseToken"

        const val Sponsorship_Logo = "Logo"
        const val Sponsorship_Name = "Name"
        const val Sponsorship_Description = "Description"
        const val Sponsorship_Link = "Link"

        const val AppVersion_Number = "Number"
        const val AppVersion_Version = "Version"
        const val AppVersion_Icon = "Icon"
        const val AppVersion_Size = "Size"
        const val AppVersion_Description = "Description"
        const val AppVersion_MinSupportNumber = "MinSupportNumber"
        const val AppVersion_DownloadLink = "DownloadLink"
        const val AppVersion_DateReleased = "DateReleased"

        const val Wallpaper_Name = "Name"
        const val Wallpaper_SolidColor = "SolidColor"
        const val Wallpaper_ImagePath = "ImagePath"
        const val Wallpaper_ParticleNums = "ParticleNums"
        const val Wallpaper_ParticleSize = "ParticleSize"
        const val Wallpaper_ParticleSpeed = "ParticleSpeed"
        const val Wallpaper_ParticleBackgroundColor = "ParticleBackgroundColor"
        const val Wallpaper_ParticleColor = "ParticleColor"
        const val Wallpaper_BubblesNums = "BubblesNums"
        const val Wallpaper_BubblesSpeed = "BubblesSpeed"
        const val Wallpaper_BubblesBackgroundColor = "BubblesBackgroundColor"
        const val Wallpaper_BubblesColor = "BubblesColor"
        const val Wallpaper_RainbowAlpha = "RainbowAlpha"
        const val Wallpaper_RainbowSpeed = "RainbowSpeed"

        const val MessageNotification_Enable = "Enable"
        const val MessageNotification_Ringtone = "Ringtone"
        const val MessageNotification_Vibrate = "Vibrate"

        const val NearoomNotification_Enable = "Enable"
        const val NearoomNotification_Ringtone = "Ringtone"
        const val NearoomNotification_Vibrate = "Vibrate"

        const val DownloadSetting_ImageAutoDownload = "ImageAutoDownload"
        const val DownloadSetting_VideoAutoDownload = "VideoAutoDownload"

    }


    fun save(id: String, value: Any?) {
        val editor = sharedPreference.edit()
        val ID = id.toUpperCase(Locale.ROOT)

        when(value) {
            is String -> editor.putString(ID, value)
            is Int -> editor.putInt(ID, value)
            is Boolean -> editor.putBoolean(ID, value)
            is Long -> editor.putLong(ID, value)
            is Float -> editor.putFloat(ID, value)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
        editor.apply()
    }

    fun delete(id: String) {
        val editor = sharedPreference.edit()
        val ID = id.toUpperCase(Locale.ROOT)
        editor.remove(ID)
        editor.apply()
    }

    /*fun saveStringSet(id: String, value: MutableSet<String>?) {
        val editor = sharedPreference.edit()
        val ID = id.toUpperCase(Locale.ROOT)
        editor.putStringSet(ID, value)
        editor.apply()
    }

    fun saveJSONArray(id: String, value: String) {
        val editor = sharedPreference.edit()
        val ID = id.toUpperCase(Locale.ROOT)
        val jarray = JSONArray()
        try {
            jarray.put(value)
        }catch (e : JSONException){
            e.printStackTrace()
        }
        editor.putString(ID, jarray.toString())
        editor.apply()
    }*/

    fun load(id : String,type : String) : Any? {
        val ID = id.uppercase(Locale.ROOT)
        val value:Any?
        value = when(type.uppercase(Locale.ROOT)) {
            "STRING" -> sharedPreference.getString(ID, "Null")
            "INT" -> sharedPreference.getInt(ID, 0)
            "BOOLEAN" -> sharedPreference.getBoolean(ID, false)
            "LONG" -> sharedPreference.getLong(ID, 0)
            "FLOAT" -> sharedPreference.getFloat(ID, 0F)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
        return value
    }


    fun load(id : String,type : String, defaultValue : Any) : Any? {
        val ID = id.uppercase(Locale.ROOT)
        val value:Any?
        value = when(type.uppercase(Locale.ROOT)) {
            "STRING" -> sharedPreference.getString(ID, defaultValue.toString())
            "INT" -> sharedPreference.getInt(ID, defaultValue as Int)
            "BOOLEAN" -> sharedPreference.getBoolean(ID, defaultValue as Boolean)
            "LONG" -> sharedPreference.getLong(ID, defaultValue as Long)
            "FLOAT" -> sharedPreference.getFloat(ID, defaultValue as Float)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
        return value
    }

    //fun loadStringSet(id: String): MutableSet<String>? {
    //    val ID = id.toUpperCase(Locale.ROOT)
    //    val value = sharedPreference.getStringSet(ID,null)
    //    return value
    //}

    fun loadJSONArray(id: String): JSONArray {
        val ID = id.toUpperCase(Locale.ROOT)
        val value = sharedPreference.getString(ID,null)
        var ja = JSONArray()
        if ( value !== null){
            ja = JSONArray(value)
        }
        return ja
    }

    fun deleteAll(){
        sharedPreference.all.clear()
        sharedPreference.edit().clear().apply()
    }


}