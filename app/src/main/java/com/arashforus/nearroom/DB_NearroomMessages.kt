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

class DB_NearroomMessages(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "NearoomMessages_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_TYPE = "TYPE"
    private val COLUMN_FROMID = "FROMID"
    private val COLUMN_FROMUSERNAME = "FROMUSERNAME"
    private val COLUMN_ROOMID = "ROOMID"
    private val COLUMN_ROOMNAME = "ROOMNAME"
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
    private val COLUMN_ISDELETE = "ISDELETE"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_TYPE + "' TEXT, '" +
            COLUMN_FROMID + "' INTEGER, '" +
            COLUMN_FROMUSERNAME + "' TEXT, '" +
            COLUMN_ROOMID + "' INTEGER, '" +
            COLUMN_ROOMNAME + "' TEXT, '" +
            COLUMN_TIMESTAMP + "' TEXT, '" +
            COLUMN_MESSAGE + "' TEXT, '" +
            COLUMN_URI + "' TEXT, '" +
            COLUMN_SIZE + "' INTEGER, '" +
            COLUMN_SEND + "' INTEGER, '" +
            COLUMN_RECEIVED + "' INTEGER, '" +
            COLUMN_SEEN + "' INTEGER, '" +
            COLUMN_RECEIVEDTIME + "' TEXT, '" +
            COLUMN_SEENTIME + "' TEXT, '" +
            COLUMN_UNSEENCOUNT + "' INTEGER, '" +
            COLUMN_ISDELETE + "' INTEGER )"

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
        if (db.isOpen)    db.close()

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

    private val posId = 0
    private val posType = 1
    private val posRoomId = 2
    private val posRoomName = 3
    private val posFromId = 4
    private val posFromUsername = 5
    private val posMessage = 6
    private val posUri = 7
    private val posSize = 8
    private val posTimeStamp = 9
    private val posReceived = 10
    private val posReceivedTime = 11
    private val posSeen = 12
    private val posSeenTime = 13
    private val posIsDelete = 14
    //private val posIsDeleteTime = 15
    //private val posIsChange = 16

    fun saveToDB(respond : JSONObject){
        val db = this.writableDatabase
        val cv = ContentValues()
        var lastId = 0

        if (respond.get("chat").equals(null)){
            println("No messages yet")
        }else{
            val chats : JSONArray = respond.getJSONArray("chat")
            if ( respond.getBoolean("isSuccess")){
                for (i in 0 until chats.length()) {
                    val chat = chats.getJSONArray(i)
                    cv.clear()
                    cv.put(COLUMN_ID,chat.getInt(posId))
                    cv.put(COLUMN_TYPE,chat.getString(posType))
                    cv.put(COLUMN_FROMID,chat.getInt(posFromId))
                    cv.put(COLUMN_FROMUSERNAME,chat.getString(posFromUsername))
                    cv.put(COLUMN_ROOMID,chat.getInt(posRoomId))
                    cv.put(COLUMN_ROOMNAME,chat.getString(posRoomName))
                    cv.put(COLUMN_TIMESTAMP,chat.getString(posTimeStamp))
                    if (chat.getString(posMessage) !== null) {
                        cv.put(COLUMN_MESSAGE,chat.getString(posMessage))
                    }
                    if (chat.getString(posUri) !== null){
                        cv.put(COLUMN_URI,chat.getString(posUri))
                    }
                    if (!chat.isNull(posSize)){
                        cv.put(COLUMN_SIZE,chat.getInt(posSize))
                    }
                    cv.put(COLUMN_SEND,1)
                    cv.put(COLUMN_RECEIVED,chat.getInt(posReceived))
                    cv.put(COLUMN_SEEN,chat.getInt(posSeen))
                    if (chat.getString(posReceivedTime) !== null){
                        cv.put(COLUMN_RECEIVEDTIME,chat.getString(posReceivedTime))
                    }
                    if (chat.getString(posSeenTime) !== null){
                        cv.put(COLUMN_SEENTIME,chat.getString(posSeenTime))
                    }
                    cv.put(COLUMN_ISDELETE,chat.getInt(posIsDelete))
                    //if (!chat.isNull(14)){
                    //    cv.put(COLUMN_UNSEENCOUNT,chat.getInt(14))
                    //}
                    db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)
                    lastId = chat.getInt(0)
                }
            }
            if (db.isOpen) db.close()
            if (lastId > 0) {
                MySharedPreference(MySharedPreference.PreferenceApp,context!!).save(MySharedPreference.App_LastNearoomMessageIdGet,lastId)
            }
        }

    }


    fun loadMessages(roomId: Int) : ArrayList<Object_NearoomMessage>{
        val db = this.readableDatabase
        val messageList : ArrayList<Object_NearoomMessage> = ArrayList()
        val sql = "SELECT * from '$TABLE_NAME' WHERE ( $COLUMN_ROOMID = '$roomId' AND $COLUMN_ISDELETE = 0 ) ORDER BY $COLUMN_ID DESC"
        val cursor: Cursor = db.rawQuery(sql,null)
        if ( cursor.moveToFirst() ) {
            do {
                val message = Object_NearoomMessage(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)),
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

    fun loadChatListMessages(userId: Int) : ArrayList<Object_Chatlist>{
        var roomId : Int?
        var pic : String?
        var senderUsername : String?
        var senderId : Int?

        val db = DB_NearroomMessages(context).readableDatabase
        val chatLists : ArrayList<Object_Chatlist> = ArrayList()
        val rooms = findRooms()
        val roomPics = DB_NearoomInfos(context).getRoomPicWithRoomIds()
        rooms.forEach {
            val sql = "SELECT * FROM '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$it' ORDER BY $COLUMN_ID DESC LIMIT 1"
            val cursor: Cursor = db.rawQuery(sql,null)
            if (cursor.moveToFirst()) {
                do {
                    roomId = cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID))
                    pic = if ( roomPics.has(roomId.toString()) ){
                        roomPics.getString(roomId!!.toString())
                    }else{
                        null
                    }
                    senderId =  cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))
                    senderUsername = if ( senderId == userId ){
                        "You"
                    }else{
                        cursor.getString(cursor.getColumnIndex(COLUMN_FROMUSERNAME))
                    }
                    val chatlist = Object_Chatlist(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)),
                        pic,
                        senderUsername!!,
                        cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SEND)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_RECEIVED)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN))
                    )
                    chatLists.add(chatlist)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        if ( db.isOpen) db.close()
        return chatLists
    }

    // Find all rooms in DB ///////////////////////////////////////////////////////////////////////
    fun findRooms() : List<Int> {
        val rooms = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT DISTINCT $COLUMN_ROOMID from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                rooms.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return rooms.distinct()
    }

    fun saveOneMessage(id :Int, type :String, fromId:Int, fromUsername:String, roomId:Int, roomName:String, timestamp:String, message:String?, uri:String?, size:Int?, send:Int?, received:Int?, seen:Int?){
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.clear()
        cv.put(COLUMN_ID,id)
        cv.put(COLUMN_TYPE,type)
        cv.put(COLUMN_FROMID,fromId)
        cv.put(COLUMN_FROMUSERNAME,fromUsername)
        cv.put(COLUMN_ROOMID,roomId)
        cv.put(COLUMN_ROOMNAME,roomName)
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

    fun saveSeen(roomId : Int ,lastMessageId: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_SEEN,1)
        db.updateWithOnConflict(TABLE_NAME,cv, "$COLUMN_ROOMID = '$roomId' AND $COLUMN_ID <= $lastMessageId AND $COLUMN_SEEN = 0 ",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun getLastImagesOfRoom(roomId : Int , number : Int) : ArrayList<Object_Media> {
        val images = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        try {
            val sql = "SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'PIC%' ORDER BY $COLUMN_ID DESC "
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

    fun getAllImagesOfRoom(roomId : Int) : ArrayList<Object_Media> {
        val images = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'PIC%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
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

    fun getLastVideosOfRoom( roomId : Int , number : Int) : ArrayList<Object_Media> {
        val videos = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        try {
            val sql = "SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'VID%' ORDER BY $COLUMN_ID DESC "
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

    fun getAllVideosOfRoom(roomId : Int) : ArrayList<Object_Media> {
        val videos = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'VID%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
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

    fun getLastFilesOfRoom(roomId : Int , number : Int) : ArrayList<Object_Media> {
        val files = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        try {
            //val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE ($COLUMN_FROMUSERNAME = '$username' OR $COLUMN_TOUSERNAME = '$username' ) AND $COLUMN_TYPE = 'PIC' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
            val sql = "SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'FILE%' ORDER BY $COLUMN_ID DESC "
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

    fun getAllFilesOfRoom(roomId : Int) : ArrayList<Object_Media> {
        val files = ArrayList<Object_Media>()
        val db = DB_NearroomMessages(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_TYPE LIKE 'FILE%' AND $COLUMN_URI IS NOT NULL ORDER BY $COLUMN_ID DESC ", null)
        if (cursor.moveToFirst()) {
            do {
                cursor.getString(cursor.getColumnIndex(COLUMN_URI))
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

    fun countUnreadMessages( roomId : Int) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_SEEN = 0 ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun countMessagesSend( roomId: Int) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_FROMID = '$roomId' ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun countMessagesOfRoom( roomId: Int) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    fun search( word : String , number : Int) : ArrayList<Object_Chatlist> {
        var roomId : Int?
        var pic : String?
        var senderUsername : String?
        var senderId : Int?
        val userId = MySharedPreference(MySharedPreference.PreferenceProfile,context!!).load(MySharedPreference.Profile_ID,"Int") as Int

        val nearooms = ArrayList<Object_Chatlist>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_MESSAGE LIKE '%$word%' LIMIT $number ", null)
        val roomPics = DB_NearoomInfos(context).getRoomPicWithRoomIds()
        if ( cursor.moveToFirst() ){
            do {
                roomId = cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID))
                pic = if ( roomPics.has(roomId.toString()) ){
                    roomPics.getString(roomId.toString())
                }else{
                    null
                }
                senderId =  cursor.getInt(cursor.getColumnIndex(COLUMN_FROMID))
                senderUsername = if ( senderId == userId ){
                    "You"
                }else{
                    cursor.getString(cursor.getColumnIndex(COLUMN_FROMUSERNAME))
                }
                nearooms.add(Object_Chatlist(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)),
                    pic,
                    senderUsername!!,
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEND)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_RECEIVED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearooms
    }

    fun searchCount( word : String ) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_MESSAGE LIKE '%$word%' ", null)
        val count = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return count
    }

    fun deleteChats( roomId : Int , lastMessageId : Int ){
        val db = this.writableDatabase
        db.delete(TABLE_NAME," $COLUMN_ROOMID = '$roomId' AND $COLUMN_ID <= '$lastMessageId' ",null)
    }

    companion object{
        const val DB_NAME: String = "NearoomMessages_DB"
        const val VERSION: Int = 2
    }
}