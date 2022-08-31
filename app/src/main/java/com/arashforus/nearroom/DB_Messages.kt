package com.arashforus.nearroom

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

class DB_Messages(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "Messages_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_TYPE = "TYPE"
    private val COLUMN_FROMID = "FROMID"
    private val COLUMN_FROMUSERNAME = "FROMUSERNAME"
    private val COLUMN_TOID = "TOID"
    private val COLUMN_TOUSERNAME = "TOUSERNAME"
    private val COLUMN_TIMESTAMP = "TIMESTAMP"
    private val COLUMN_MESSAGE = "MESSAGE"
    private val COLUMN_URI = "URI"
    private val COLUMN_SIZE = "SIZE"
    private val COLUMN_SEND = "SEND"
    private val COLUMN_RECEIVED = "RECEIVED"
    private val COLUMN_SEEN = "SEEN"
    private val COLUMN_RECEIVEDTIME = "RECEIVEDTIME"
    private val COLUMN_SEENTIME = "SEENTIME"
    private val COLUMN_UNSEENCOUNT = "UNSEENCOUNT"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_TYPE + "' TEXT, '" +
            COLUMN_FROMID + "' INTEGER, '" +
            COLUMN_FROMUSERNAME + "' TEXT, '" +
            COLUMN_TOID + "' INTEGER, '" +
            COLUMN_TOUSERNAME + "' TEXT, '" +
            COLUMN_TIMESTAMP + "' TEXT, '" +
            COLUMN_MESSAGE + "' TEXT, '" +
            COLUMN_URI + "' TEXT, '" +
            COLUMN_SIZE + "' INTEGER, '" +
            COLUMN_SEND + "' INTEGER, '" +
            COLUMN_RECEIVED + "' INTEGER, '" +
            COLUMN_SEEN + "' INTEGER, '" +
            COLUMN_RECEIVEDTIME + "' TEXT, '" +
            COLUMN_SEENTIME + "' TEXT, '" +
            COLUMN_UNSEENCOUNT + "' INTEGER)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS '$TABLE_NAME'"


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    /*
    fun deleteTable(){
        val db = this.writableDatabase
        db?.execSQL(SQL_DELETE_ENTRIES)
        if (db.isOpen){
            db.close()
        }
    }
     */

    fun deleteDB(){
        context?.deleteDatabase(DB_NAME)
    }

    fun firstTimeInit(){
        val db = this.writableDatabase
        db?.execSQL(SQL_CREATE_ENTRIES)
        if (db.isOpen)   db.close()
    }

    // Position of Json Array of response from server //////////////////////////////////////////////
    private val idPos = 0
    private val typePos = 1
    private val fromIdPos = 2
    private val fromUsernamePos = 3
    private val toIdPos = 4
    private val toUsernamePos = 5
    private val timeStampPos = 6
    private val messagePos = 7
    private val uriPos = 8
    private val sizePos = 9
    private val receivedPos = 10
    private val seenPos = 11
    private val unseenCountPos = 12
    private val receivedTimePos = 13
    private val seenTimePos = 14

    fun saveToDB(respond : JSONObject){
        val db = DB_Messages(context).writableDatabase
        val cv = ContentValues()
        var lastId = 0

        if (respond.get("chat").equals(null)){
            println("No messages yet")
        }else{
            val chats : JSONArray = respond.getJSONArray("chat")
            if ( respond.getBoolean("isSuccess")){
                for (i in 0 until chats.length()) {
                    val chat = chats.getJSONArray(i)
                    if ( DB_UserInfos(context).getMuteAndBlocked(chat.getInt(fromIdPos)).getInt(1) == 0 ){
                        // User isn't blocked , so message save on DB //////////////////////////////
                        cv.clear()
                        cv.put(COLUMN_ID,chat.getInt(idPos))
                        cv.put(COLUMN_TYPE,chat.getString(typePos))
                        cv.put(COLUMN_FROMID,chat.getInt(fromIdPos))
                        cv.put(COLUMN_FROMUSERNAME,chat.getString(fromUsernamePos))
                        cv.put(COLUMN_TOID,chat.getInt(toIdPos))
                        cv.put(COLUMN_TOUSERNAME,chat.getString(toUsernamePos))
                        cv.put(COLUMN_TIMESTAMP,chat.getString(timeStampPos))
                        if (chat.getString(messagePos) !== null) {
                            cv.put(COLUMN_MESSAGE,chat.getString(messagePos))
                        }
                        if (chat.getString(uriPos) !== null){
                            cv.put(COLUMN_URI,chat.getString(uriPos))
                        }
                        if (!chat.isNull(sizePos)){
                            cv.put(COLUMN_SIZE,chat.getInt(sizePos))
                        }
                        cv.put(COLUMN_SEND,1)
                        cv.put(COLUMN_RECEIVED,chat.getInt(receivedPos))
                        cv.put(COLUMN_SEEN,chat.getInt(seenPos))
                        if (!chat.isNull(unseenCountPos)){
                            cv.put(COLUMN_UNSEENCOUNT,chat.getInt(unseenCountPos))
                        }
                        if (chat.getString(receivedTimePos) !== null){
                            cv.put(COLUMN_RECEIVEDTIME,chat.getString(receivedTimePos))
                        }
                        if (chat.getString(seenTimePos) !== null){
                            cv.put(COLUMN_SEENTIME,chat.getString(seenTimePos))
                        }

                        db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)
                    }
                    lastId = chat.getInt(idPos)
                }
            }
            if (db.isOpen) db.close()
            if (lastId > 0) {
                //println("Last id in DB Messages is $lastId =========")
                MySharedPreference(MySharedPreference.PreferenceApp,context!!).save(MySharedPreference.App_LastMessageIdGet,lastId)
            }
        }
    }

    fun loadMessages( userId : Int) : ArrayList<Object_Message>{
        val db = DB_Messages(context).readableDatabase
        val messageList : ArrayList<Object_Message> = ArrayList()
        val sql = "SELECT * from '$TABLE_NAME' WHERE ( $COLUMN_FROMID = '$userId' OR $COLUMN_TOID = '$userId' ) AND ( $COLUMN_TYPE != 'DELETE' ) ORDER BY $COLUMN_ID DESC"
        val cursor: Cursor = db.rawQuery(sql,null)
        if (cursor.moveToFirst()) {
            do {
                val message = Object_Message(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_TOID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEND)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_RECEIVED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_RECEIVEDTIME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SEENTIME))
                )
                messageList.add(message)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return messageList
    }

    fun loadChatListMessages(userId: Int) : ArrayList<Object_Chatlist> {
        var toUsername: String?
        var toId: Int
        var toSavedName: String? = null
        var toSavedPic: String? = null
        //var pic : String?
        val db = DB_Messages(context).readableDatabase
        val chatLists : ArrayList<Object_Chatlist> = ArrayList()
        val users = findUniqueUserIds(userId)
        users.forEach {
            val sql = "SELECT * FROM '$TABLE_NAME' WHERE ( $COLUMN_FROMID = '$it' OR $COLUMN_TOID = '$it' ) AND ( $COLUMN_TYPE != 'DELETE' ) ORDER BY $COLUMN_ID DESC LIMIT 1"
            val cursor: Cursor = db.rawQuery(sql,null)
            if (cursor.moveToFirst()) {
                if (userId == cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))){
                    toUsername = cursor.getString(cursor.getColumnIndex(COLUMN_TOUSERNAME))
                    toId = cursor.getInt(cursor.getColumnIndex(COLUMN_TOID))
                }else{
                    toUsername = cursor.getString(cursor.getColumnIndex(COLUMN_FROMUSERNAME))
                    toId = cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))
                }

                val userInfo = DB_UserInfos(context).getUserInfo(toId)
                // set saved name if exist /////////////////////////////////////////////////////////
                if ( !userInfo?.phoneFullname.isNullOrEmpty() ){
                    toSavedName = userInfo?.phoneFullname
                }
                if ( !userInfo?.phonePhotoUri.isNullOrEmpty() ){
                    toSavedPic = userInfo?.phonePhotoUri
                }
                val chatlist = Object_Chatlist(
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    toUsername!!,
                    toId,
                    toSavedName,
                    toSavedPic,
                    userInfo?.profilePic,
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEND)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_RECEIVED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_UNSEENCOUNT))  )
                chatLists.add(chatlist)
            }
            cursor.close()
        }
        if ( db.isOpen) db.close()

        return chatLists
    }

    fun deleteChats( toId : Int , lastMessageId : Int ){
        val db = this.writableDatabase
        db.delete(TABLE_NAME," ( $COLUMN_FROMID = '$toId' OR $COLUMN_TOID = '$toId' ) AND $COLUMN_ID <= '$lastMessageId' ",null)
    }

    // Find unique users from sender and receiver DB ///////////////////////////////////////////////
    fun findUniqueUserIds(userId : Int) : List<Int> {
        val userIds = ArrayList<Int>()
        val db = DB_Messages(context).readableDatabase

        var cursor: Cursor = db.rawQuery("SELECT DISTINCT $COLUMN_FROMID from '$TABLE_NAME' WHERE $COLUMN_TYPE != 'DELETE' ", null)
        if (cursor.moveToFirst()) {
            do {
                userIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        cursor = db.rawQuery("SELECT DISTINCT $COLUMN_TOID from '$TABLE_NAME' WHERE $COLUMN_TYPE != 'DELETE' ", null)
        if (cursor.moveToFirst()) {
            do {
                userIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_TOID)))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (db.isOpen) db.close()
        return userIds.distinct().minusElement(userId)
    }

    fun getAllImages() : ArrayList<String> {
        val images = ArrayList<String>()
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_TYPE = 'PIC' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                images.add(cursor.getString(cursor.getColumnIndex(COLUMN_URI)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return images
    }

    fun getAllImagesFromUsername(id : Int) : ArrayList<Object_Media> {
        val images = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'PIC%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                //cursor.getString(cursor.getColumnIndex(COLUMN_URI))
                images.add(Object_Media("PIC",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return images
    }

    fun getLastImagesFromUsername(id : Int , number : Int) : ArrayList<Object_Media> {
        val images = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        try {
            //val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMUSERNAME = '$username' OR $COLUMN_TOUSERNAME = '$username' ) AND $COLUMN_TYPE = 'PIC' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
            val sql = "SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'PIC%' ORDER BY $COLUMN_ID DESC "
            val cursor: Cursor = db.rawQuery(sql, null)
            //println("cursor count is ... ${cursor.count}")
            cursor.moveToFirst()
            val min = min( number , cursor.count )
            for ( i in 0 until min){
                images.add(Object_Media("PIC",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
                cursor.moveToNext()
            }
            cursor.close()
        }catch (e: SQLException){
            e.printStackTrace()
        }
        if ( db.isOpen) db.close()
        return images
    }

    fun getLastVideosFromUsername( id : Int , number : Int) : ArrayList<Object_Media> {
        val videos = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        try {
            //val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMUSERNAME = '$username' OR $COLUMN_TOUSERNAME = '$username' ) AND $COLUMN_TYPE = 'PIC' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
            val sql = "SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'VID%' ORDER BY $COLUMN_ID DESC "
            val cursor: Cursor = db.rawQuery(sql, null)
            //println("cursor count is ... ${cursor.count}")
            cursor.moveToFirst()
            val min = min( number , cursor.count )
            for ( i in 0 until min){
                videos.add(Object_Media("VID",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
                cursor.moveToNext()
            }
            cursor.close()
        }catch (e: SQLException){
            e.printStackTrace()
        }
        if ( db.isOpen) db.close()
        return videos
    }

    fun getLastFilesFromUsername(id : Int , number : Int) : ArrayList<Object_Media> {
        val files = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        try {
            //val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMUSERNAME = '$username' OR $COLUMN_TOUSERNAME = '$username' ) AND $COLUMN_TYPE = 'PIC' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
            val sql = "SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'FILE%' ORDER BY $COLUMN_ID DESC "
            val cursor: Cursor = db.rawQuery(sql, null)
            //println("cursor count is ... ${cursor.count}")
            cursor.moveToFirst()
            val min = min( number , cursor.count )
            for ( i in 0 until min){
                files.add(Object_Media("FILE",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
                cursor.moveToNext()
            }
            cursor.close()
        }catch (e: SQLException){
            e.printStackTrace()
        }
        if ( db.isOpen) db.close()
        return files
    }

    fun getAllVideos() : ArrayList<String> {
        val videos = ArrayList<String>()
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_TYPE = 'VID' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                videos.add(cursor.getString(cursor.getColumnIndex(COLUMN_URI)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return videos
    }

    fun getAllVideosFromUsername(id : Int) : ArrayList<Object_Media> {
        val videos = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'VID%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                //cursor.getString(cursor.getColumnIndex(COLUMN_URI))
                videos.add(Object_Media("VID",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return videos
    }

    fun getAllFilesFromUsername(id : Int) : ArrayList<Object_Media> {
        val files = ArrayList<Object_Media>()
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMID = '$id' OR $COLUMN_TOID = '$id' ) AND $COLUMN_TYPE LIKE 'FILE%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                //cursor.getString(cursor.getColumnIndex(COLUMN_URI))
                files.add(Object_Media("FILE",null,null,
                    null,
                    null,
                    cursor.getString(cursor.getColumnIndex(COLUMN_URI)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)),
                    null,
                    null,
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return files
    }

    fun countMessagesSend( toId: Int) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_FROMID = '$toId' ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun countMessagesSendTo( toId : Int) : Int {
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_TOID = '$toId'", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun countMessagesReceivedFrom( toId : Int) : Int {
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_FROMID = '$toId' ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun countUnreadMessages( toId : Int) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_FROMID = '$toId' AND $COLUMN_SEEN = 0 ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun firstMessageTime( userId: Int) : String? {
        val db = DB_Messages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_FROMID = '$userId' OR $COLUMN_TOID = '$userId' ORDER BY $COLUMN_ID ASC", null)
        var firstTime : String? = null
        if (cursor.moveToFirst()){
            firstTime = cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_TIMESTAMP))
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return firstTime
    }

    fun saveOneMessage(id :Int, type :String, fromId:Int, fromUsername:String, toId:Int, toUsername:String, timestamp:String, message:String?, uri:String?, size:Int?, send:Int?, received:Int?, seen:Int?){
        val db = DB_Messages(context).writableDatabase
        val cv = ContentValues()

        cv.clear()
        cv.put(COLUMN_ID,id)
        cv.put(COLUMN_TYPE,type)
        cv.put(COLUMN_FROMID,fromId)
        cv.put(COLUMN_FROMUSERNAME,fromUsername)
        cv.put(COLUMN_TOID,toId)
        cv.put(COLUMN_TOUSERNAME,toUsername)
        cv.put(COLUMN_TIMESTAMP,timestamp)
        cv.put(COLUMN_MESSAGE,message)
        cv.put(COLUMN_URI,uri)
        cv.put(COLUMN_SIZE,size)
        cv.put(COLUMN_SEND,send)
        cv.put(COLUMN_RECEIVED,received)
        cv.put(COLUMN_SEEN,seen)
        db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)

        if (db.isOpen) db.close()
    }

    fun search( word : String , number : Int ) : ArrayList<Object_Chatlist> {
        var toUsername: String?
        var toId: Int
        var toSavedName: String? = null
        var toSavedPic: String? = null
        val userId = MySharedPreference(MySharedPreference.PreferenceProfile,context!!).load(MySharedPreference.Profile_ID,"Int") as Int
        val messages = ArrayList<Object_Chatlist>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_MESSAGE LIKE '%$word%' LIMIT $number ", null)
        if ( cursor.moveToFirst() ){
            do {
                if (userId == cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))){
                    toUsername = cursor.getString(cursor.getColumnIndex(COLUMN_TOUSERNAME))
                    toId = cursor.getInt(cursor.getColumnIndex(COLUMN_TOID))
                }else{
                    toUsername = cursor.getString(cursor.getColumnIndex(COLUMN_FROMUSERNAME))
                    toId = cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))
                }
                val userInfo = DB_UserInfos(context).getUserInfo(toId)
                if ( !userInfo?.phoneFullname.isNullOrEmpty() ){
                    toSavedName = userInfo?.phoneFullname
                }
                if ( !userInfo?.phonePhotoUri.isNullOrEmpty() ){
                    toSavedPic = userInfo?.phonePhotoUri
                }
                messages.add(Object_Chatlist(
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    toUsername!!,
                    toId,
                    toSavedName,
                    toSavedPic,
                    userInfo?.profilePic,
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEND)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_RECEIVED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_UNSEENCOUNT))  ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return messages
    }

    fun searchCount( word : String ) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_MESSAGE LIKE '%$word%' ", null)
        val count = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return count
    }

    fun setSeenStatus( fromId : Int ) {
        val db = this.readableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_SEEN,1)
        cv.put(COLUMN_SEENTIME,MyDateTime().getGMTFullTime())
        db.update(TABLE_NAME,cv,"$COLUMN_FROMID = $fromId AND $COLUMN_SEEN = 0 ",null)
        if ( db.isOpen) db.close()
    }

    companion object{
        const val DB_NAME: String = "Messages_DB"
        const val VERSION: Int = 4
    }
}