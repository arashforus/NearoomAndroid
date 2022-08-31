package com.arashforus.nearroom

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull
import org.json.JSONArray
import org.json.JSONObject

class DB_NearoomsParticipant (val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "NearoomsParticipant_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_ROOMID = "ROOMID"
    private val COLUMN_ROOMNAME = "ROOMNAME"
    private val COLUMN_USERID = "USERID"
    private val COLUMN_USERNAME = "USERNAME"
    private val COLUMN_JOINEDTIME = "JOINEDTIME"
    private val COLUMN_STARTMESSAGEID = "STARTMESSAGEID"
    private val COLUMN_ISADMIN = "ISADMIN"
    private val COLUMN_ISLEFT = "ISLEFT"


    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_ROOMID + "' INTEGER, '" +
            COLUMN_ROOMNAME + "' TEXT, '" +
            COLUMN_USERID + "' INTEGER, '" +
            COLUMN_USERNAME + "' TEXT, '" +
            COLUMN_JOINEDTIME + "' TEXT, '" +
            COLUMN_STARTMESSAGEID + "' INTEGER, '" +
            COLUMN_ISADMIN + "' INTEGER, '" +
            COLUMN_ISLEFT + "' INTEGER)"

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
        if (db.isOpen)      db.close()
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
    private val posRoomId = 1
    private val posRoomName = 2
    private val posUserId = 3
    private val posUsername = 4
    private val posJoinedTime = 5
    private val posStartMessageId = 6
    private val posIsAdmin = 7
    private val posIsLeft = 8

    fun saveToDB(respond : JSONObject){
        val db = this.writableDatabase
        val cv = ContentValues()

        if (respond.get("participant").equals(null)){
            println("No Participant")
        }else{
            val participants : JSONArray = respond.getJSONArray("participant")
            if ( respond.getBoolean("isSuccess")){
                for (i in 0 until participants.length()) {
                    val chat = participants.getJSONArray(i)
                    cv.clear()
                    cv.put(COLUMN_ID,chat.getInt(posId))
                    cv.put(COLUMN_ROOMID,chat.getInt(posRoomId))
                    cv.put(COLUMN_ROOMNAME,chat.getString(posRoomName))
                    cv.put(COLUMN_USERID,chat.getInt(posUserId))
                    cv.put(COLUMN_USERNAME,chat.getString(posUsername))
                    cv.put(COLUMN_JOINEDTIME,chat.getString(posJoinedTime))
                    cv.put(COLUMN_STARTMESSAGEID,chat.getInt(posStartMessageId))
                    cv.put(COLUMN_ISADMIN,chat.getInt(posIsAdmin))
                    cv.put(COLUMN_ISLEFT,chat.getInt(posIsLeft))

                    db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
            if (db.isOpen) db.close()
        }

    }

    fun roomsJoinedByUsername(id : Int) : ArrayList<Object_Nearoom>{
        val db = DB_NearoomsParticipant(context).readableDatabase
        val nearoomsList = ArrayList<Object_Nearoom>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERID = '$id' ",null)
        if (cursor.moveToFirst()) {
            do {
                val nearoom = DB_NearoomInfos(context).getRoomDetail(cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMID)))
                if ( nearoom !== null ){
                    nearoomsList.add(nearoom)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearoomsList
    }

    fun roomParticipant(roomId : Int) : ArrayList<String>{
        val db = this.readableDatabase
        val usernameList = ArrayList<String>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' ",null)
        if (cursor.moveToFirst()) {
            do {
                usernameList.add(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return usernameList
    }

    fun roomParticipantId(roomId : Int ) : ArrayList<Int>{
        val db = this.readableDatabase
        val idList = ArrayList<Int>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' ",null)
        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getInt(cursor.getColumnIndex(COLUMN_USERID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return idList
    }

    fun roomAdminIds (roomId : Int) : ArrayList<Int>{
        val db = this.readableDatabase
        val idList = ArrayList<Int>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_ISADMIN = 1 ",null)
        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getInt(cursor.getColumnIndex(COLUMN_USERID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return idList
    }

    fun isAdmin (roomId : Int , userId : Int ) : Boolean {
        val db = this.readableDatabase
        var isAdmin = false
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_USERID = '$userId' AND $COLUMN_ISLEFT = 0 ",null)
        if (cursor.moveToFirst()) {
            if ( cursor.getInt(cursor.getColumnIndex(COLUMN_ISADMIN)) == 1 ){
                isAdmin = true
            }
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return isAdmin
    }

    fun isMember (roomId : Int , userId : Int ) : Boolean {
        val db = this.readableDatabase
        var isMember = false
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_USERID = '$userId' AND $COLUMN_ISLEFT = 0 ",null)
        if (cursor.count > 0) {
            isMember = true
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return isMember
    }

    fun roomMembers( roomId : Int ) : ArrayList<Object_User>{
        val db = this.readableDatabase
        var memberId = 0
        val membersList = ArrayList<Object_User>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_ISLEFT = 0 ",null)
        if (cursor.moveToFirst()) {
            do {
                memberId = cursor.getInt(cursor.getColumnIndex(COLUMN_USERID))
                DB_UserInfos(context).getUserInfo(memberId)
                membersList.add(DB_UserInfos(context).getUserInfo(memberId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return membersList
    }

    fun getJoinedTime(roomId: Int , userId : Int) : String? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMID = '$roomId' AND $COLUMN_USERID = '$userId' ORDER BY $COLUMN_ID ASC", null)
        var joinedTime : String? = null
        if (cursor.moveToFirst()){
            joinedTime = cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_JOINEDTIME))
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return joinedTime
    }

    fun setJoinedStatus(roomId: Int , userId : Int , isLeft : Int)  {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_ISLEFT,isLeft)
        db.updateWithOnConflict(TABLE_NAME,cv,"$COLUMN_ROOMID = '$roomId' AND $COLUMN_USERID = '$userId' AND $COLUMN_ISLEFT = 0",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun findJoinedIds() : ArrayList<Int>{
        val db = this.readableDatabase
        val idList = ArrayList<Int>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ISLEFT = 0 ",null)
        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getInt(cursor.getColumnIndex(COLUMN_USERID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return idList
    }


    companion object{
        const val DB_NAME: String = "NearoomsParticipant_DB"
        const val VERSION: Int = 2
    }


}