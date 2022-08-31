package com.arashforus.nearroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

class DB_Notification(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "Notification_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_TYPE = "TYPE"
    private val COLUMN_SENDERID = "SENDERID"
    private val COLUMN_SENDER = "SENDER"
    private val COLUMN_ROOMID = "ROOMID"
    private val COLUMN_ROOMNAME = "ROOMNAME"
    private val COLUMN_MESSAGE = "MESSAGE"
    private val COLUMN_URI = "URI"
    private val COLUMN_TIMESTAMP = "TIMESTAMP"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_TYPE + "' TEXT, '" +
            COLUMN_SENDERID + "' INTEGER, '" +
            COLUMN_SENDER + "' TEXT, '" +
            COLUMN_ROOMID + "' INTEGER, '" +
            COLUMN_ROOMNAME + "' TEXT, '" +
            COLUMN_MESSAGE + "' TEXT, '" +
            COLUMN_URI + "' TEXT, '" +
            COLUMN_TIMESTAMP + "' TEXT)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS '$TABLE_NAME'"


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun deleteDB(){
        context?.deleteDatabase(DB_NAME)
    }

    fun firstTimeInit(){
        val db = this.writableDatabase
        db?.execSQL(SQL_CREATE_ENTRIES)
        if (db.isOpen)   db.close()
    }

    fun saveNotification(type : String , senderId : Int?, sender : String? ,roomId : Int? , roomName : String? ,message : String? , uri: String?, timeStamp : String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put(COLUMN_TYPE,type)
        if ( senderId !== null ){
            cv.put(COLUMN_SENDERID,senderId)
        }
        if ( sender !== null ){
            cv.put(COLUMN_SENDER,sender)
        }
        if ( roomId !== null ){
            cv.put(COLUMN_ROOMID,roomId)
        }
        if ( roomName !== null ){
            cv.put(COLUMN_ROOMNAME,roomName)
        }
        if ( message !== null ){
            cv.put(COLUMN_MESSAGE,message)
        }
        if ( uri !== null ){
            cv.put(COLUMN_URI,uri)
        }
        cv.put(COLUMN_TIMESTAMP,timeStamp)
        db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)

        if (db.isOpen) db.close()
    }

    fun deleteUserNotification(userId : Int){
        val db = this.writableDatabase
        db.delete(TABLE_NAME,"$COLUMN_SENDERID = '$userId' ",null)
        if (db.isOpen) db.close()
        showNotifications()
    }

    fun deleteRoomNotification(roomId : Int){
        val db = this.writableDatabase
        db.delete(TABLE_NAME,"$COLUMN_ROOMID = '$roomId' ",null)
        if (db.isOpen) db.close()
    }

    fun showNotifications(){
        val userIds = findUniqueUserIds()
        val roomIds = findUniqueRoomIds()
        val db = this.readableDatabase

        var notificationId = 66554265

        val notificationManager = NotificationManagerCompat.from(context!!)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notification", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Notification"
            notificationManager.createNotificationChannel(channel)
        }
        var intent: Intent
        notificationManager.cancelAll()

        // Notification to private /////////////////////////////////////////////////////////////
        val messageNotifEnable = MySharedPreference(MySharedPreference.PreferenceMessageNotification,context).load(MySharedPreference.MessageNotification_Enable,"Boolean",true) as Boolean
        if ( messageNotifEnable ){
            userIds.forEach {
                var status = DB_UserInfos(context).getMuteAndBlocked(it)
                if ( status.length() < 2 ){
                    status = JSONArray().put(0).put(0)
                }
                val userInfo = DB_UserInfos(context).getUserInfo(it)
                if ( status[0] == 0 ){
                    intent = Intent(context, PrivateActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    intent.putExtra("toId",it)
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                    val notificationBuilder1 = NotificationCompat.Builder(context, channelId)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(pendingIntent)
                        .setGroup(groupKey)
                    var personName = "Unkown"
                    if (userInfo.phoneFullname !== null){
                        notificationBuilder1.setContentTitle(userInfo.phoneFullname)
                        personName = userInfo.phoneFullname!!
                    }else{
                        notificationBuilder1.setContentTitle(userInfo.username)
                        personName = userInfo.username
                    }

                    if ( userInfo.profilePic !== null ){
                        notificationBuilder1.setLargeIcon(Glide.with(context)
                            .asBitmap()
                            .load(MyInternalStorage(context).getThumbProfilePicString(userInfo.profilePic!!))
                            .circleCrop()
                            .submit()
                            .get()
                            )
                    }else{
                        if ( userInfo.phonePhotoUri !== null ){
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(userInfo.phonePhotoUri)))
                            } else {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(userInfo.phonePhotoUri))
                            }
                            //notificationBuilder1.setLargeIcon(bitmap)
                            notificationBuilder1.setLargeIcon(Glide.with(context)
                                .asBitmap()
                                .load(bitmap)
                                .circleCrop()
                                .submit()
                                .get()
                            )
                        }else{
                            //notificationBuilder1.setLargeIcon(context.resources.getDrawable(R.drawable.defaultprofile).toBitmap())
                            notificationBuilder1.setLargeIcon(Glide.with(context)
                                .asBitmap()
                                .load(R.drawable.defaultprofile)
                                .circleCrop()
                                .submit()
                                .get()
                            )
                        }
                    }
                    // Inbox Style ////////////////////////////////
                    //val style = NotificationCompat.InboxStyle()
                    //val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_SENDERID = '$it' ORDER BY $COLUMN_ID DESC ", null)
                   // if (cursor.moveToFirst()) {
                    //    do {
                   //         when (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)).toUpperCase(Locale.ROOT).trim()){
                   //             "TEXT" -> style.addLine(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)))
                   //             "PIC" -> style.addLine(" \ud83d\uddbc Photo")
                   //             "VID" -> style.addLine(" \ud83c\udfa6 Video")
                    //            else -> style.addLine(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)))
                    //        }
                    //    } while (cursor.moveToNext())
                    //}

                    // Messaging Style ////////////////////////////////
                    val person = Person.Builder().setName(personName)
                        .build()
                    val style = NotificationCompat.MessagingStyle(person)
                    style.conversationTitle = "New messages"
                    val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_SENDERID = '$it' ORDER BY $COLUMN_ID ASC ", null)
                    if (cursor.moveToFirst()) {
                        do {
                            when (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)).toUpperCase(Locale.ROOT).trim()){
                                "TEXT" -> style.addMessage(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),MyDateTime().getUnix(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))),person)
                                "PIC" -> style.addMessage(" \ud83d\uddbc Photo",MyDateTime().getUnix(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))),person)
                                "VID" -> style.addMessage(" \ud83c\udfa6 Video",MyDateTime().getUnix(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))),person)
                                else -> style.addMessage(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),MyDateTime().getUnix(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))),person)
                            }
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                    //notificationBuilder1.build()
                    notificationBuilder1.setStyle(style)
                    //println("Notification Id is : $notificationId ===========")
                    notificationManager.notify(notificationId,notificationBuilder1.build())
                    notificationId += 1

                }
            }


        }



    }

    private fun findUniqueUserIds() : List<Int> {
        val userIds = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT DISTINCT $COLUMN_SENDERID from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                userIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_SENDERID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return userIds.distinct()
    }

    private fun findUniqueRoomIds() : List<Int> {
        val roomIds = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT DISTINCT $COLUMN_ROOMID from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                roomIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return roomIds.distinct()
    }



    companion object{
        const val DB_NAME: String = "Notification_DB"
        const val VERSION: Int = 1

        const val channelId = "Notification"
        const val groupKey = "Notification"
    }


}