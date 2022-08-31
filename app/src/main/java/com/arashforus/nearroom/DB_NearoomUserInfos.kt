package com.arashforus.nearroom

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import org.json.JSONArray
import org.json.JSONObject

class DB_NearoomUserInfos(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "NearoomUserInfos_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_USERNAME = "USERNAME"
    private val COLUMN_PHONENUMBER = "PHONENUMBER"
    private val COLUMN_FULLNAME = "FULLNAME"
    private val COLUMN_CONTACTNAME = "CONTACTNAME"
    private val COLUMN_PROFILEPIC = "PROFILEPIC"
    private val COLUMN_CONTACTPIC = "CONTACTPIC"
    private val COLUMN_STATUS = "STATUS"
    private val COLUMN_LASTSEEN = "LASTSEEN"
    private val COLUMN_ISTYPINGTO = "ISTYPINGTO"
    private val COLUMN_ISTYPINGTIME = "ISTYPINGTIME"
    private val COLUMN_MUTE = "MUTE"
    private val COLUMN_BLOCKED = "BLOCKED"
    private val COLUMN_FAVOURITE = "FAVOURITE"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_USERNAME + "' TEXT, '" +
            COLUMN_PHONENUMBER + "' TEXT, '" +
            COLUMN_FULLNAME + "' TEXT, '" +
            COLUMN_CONTACTNAME + "' TEXT, '" +
            COLUMN_PROFILEPIC + "' TEXT, '" +
            COLUMN_CONTACTPIC + "' TEXT, '" +
            COLUMN_STATUS + "' TEXT, '" +
            COLUMN_LASTSEEN + "' TEXT, '" +
            COLUMN_ISTYPINGTO + "' TEXT, '" +
            COLUMN_ISTYPINGTIME + "' TEXT, '" +
            COLUMN_MUTE + "' INTEGER, '" +
            COLUMN_BLOCKED + "' INTEGER, '" +
            COLUMN_FAVOURITE + "' INTEGER)"

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

    // Find new users in nearoom participant DB and insert in userinfo DB //////////////////////////
    fun init(){
        val db = DB_NearoomUserInfos(context).writableDatabase
        val cv = ContentValues()
        val newUserIds = ArrayList<Int>()
        val existUserIds = getAllIds()
        val nearoomUserIds = DB_NearoomsParticipant(context).findJoinedIds()
        newUserIds.addAll(nearoomUserIds)
        newUserIds.removeAll(existUserIds)
        val filteredNewUserIds = newUserIds.distinct()
        for (i in filteredNewUserIds.indices){
            cv.clear()
            cv.put(COLUMN_ID,filteredNewUserIds[i])
            db.insertWithOnConflict(TABLE_NAME,null,cv,SQLiteDatabase.CONFLICT_REPLACE)
        }
        if (db.isOpen) db.close()
    }


    fun getUsername( id : Int) : String {
        var username = ""
        val db = DB_NearoomUserInfos(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$id' ", null)
        if (cursor.moveToFirst()) {
            do {
                username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return username
    }

    fun getAllIds() : ArrayList<Int> {
        val ids = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' ", null)
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return ids
    }

    fun getAllProfilePhotos() : ArrayList<String> {
        val profilePhotos = ArrayList<String>()
        val db = DB_NearoomUserInfos(context).readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_PROFILEPIC IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                profilePhotos.add(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPIC)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return profilePhotos
    }

    // Position of Json Array of response from server //////////////////////////////////////////////
    private val idPos = 0
    private val usernamePos = 1
    private val phoneNumberPos = 2
    private val fullNamePos = 3
    private val profilePhotoPos = 4
    private val lastSeenPos = 5
    private val isTypingTimePos = 6
    private val isTypingToPos = 7
    private val statusPos = 8

    fun saveUserInfos(jsonObject : JSONObject) {
        val db = this.writableDatabase
        val cv = ContentValues()
        jsonObject.keys().forEach {
            val ja = jsonObject.getJSONArray(it.toString())
            cv.clear()
            cv.put(COLUMN_ID,ja.getString(idPos))
            cv.put(COLUMN_USERNAME,ja.getString(usernamePos))
            cv.put(COLUMN_PHONENUMBER,ja.getString(phoneNumberPos))
            cv.put(COLUMN_FULLNAME,ja.getString(fullNamePos))

            if ( !ja.isNull(profilePhotoPos) ){
                cv.put(COLUMN_PROFILEPIC,ja.getString(profilePhotoPos))
            }else{
                cv.putNull(COLUMN_PROFILEPIC)
            }

            if ( !ja.isNull(lastSeenPos) ){
                cv.put(COLUMN_LASTSEEN,ja.getString(lastSeenPos))
            }else{
                cv.putNull(COLUMN_LASTSEEN)
            }

            if ( !ja.isNull(isTypingToPos) ){
                cv.put(COLUMN_ISTYPINGTO,ja.getString(isTypingToPos))
            }else{
                cv.putNull(COLUMN_ISTYPINGTO)
            }

            if ( !ja.isNull(isTypingTimePos) ){
                cv.put(COLUMN_ISTYPINGTIME,ja.getString(isTypingTimePos))
            }else{
                cv.putNull(COLUMN_ISTYPINGTIME)
            }

            if ( !ja.isNull(statusPos) ){
                cv.put(COLUMN_STATUS,ja.getString(statusPos))
            }else{
                cv.putNull(COLUMN_STATUS)
            }

            db.updateWithOnConflict(TABLE_NAME,cv,COLUMN_ID+" = '"+it as String+"'",null, SQLiteDatabase.CONFLICT_REPLACE)
        }
        if (db.isOpen) db.close()
    }

    fun getLastSeenAndIsTyping( id : Int) : JSONArray {
        val ja = JSONArray()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$id'", null)
        if (cursor.moveToFirst()) {
            do {
                ja.put(cursor.getString(cursor.getColumnIndex(COLUMN_LASTSEEN)))
                ja.put(cursor.getString(cursor.getColumnIndex(COLUMN_ISTYPINGTIME)))
                ja.put(cursor.getString(cursor.getColumnIndex(COLUMN_ISTYPINGTO)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return ja
    }

    fun getUserInfo(id : Int) : Object_User {
        val info : Object_User
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$id'", null)
        cursor.moveToFirst()
        info = Object_User(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
            cursor.getInt(cursor.getColumnIndex(COLUMN_SERVERID)),
            cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
            cursor.getString(cursor.getColumnIndex(COLUMN_PHONENUMBER)),
            cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME)),
            cursor.getString(cursor.getColumnIndex(COLUMN_CONTACTNAME)),
            cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_PROFILEPIC)),
            cursor.getString(cursor.getColumnIndex(COLUMN_CONTACTPIC)),
            cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)),
            cursor.getString(cursor.getColumnIndex(COLUMN_LASTSEEN)),
            cursor.getString(cursor.getColumnIndex(COLUMN_ISTYPINGTO)),
            cursor.getString(cursor.getColumnIndex(COLUMN_ISTYPINGTIME)),
            cursor.getInt(cursor.getColumnIndex(COLUMN_MUTE)),
            cursor.getInt(cursor.getColumnIndex(COLUMN_BLOCKED)),
            cursor.getInt(cursor.getColumnIndex(COLUMN_FAVOURITE)))

        cursor.close()
        if ( db.isOpen) db.close()
        return info
    }

    fun getUserId(username : String) : String {
        var id = 0
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME = '$username'", null)
        cursor.moveToFirst()
        id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
        cursor.close()
        if ( db.isOpen) db.close()
        return id.toString()
    }

    fun getBlocksObject() : ArrayList<Object_Blocked> {
        val blocks : ArrayList<Object_Blocked> = ArrayList()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_BLOCKED = 1 ", null)
        if (cursor.moveToFirst()) {
            do {
                blocks.add(Object_Blocked(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPIC)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return blocks
    }

    fun setBlocked(username : String , blockState : Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put(COLUMN_BLOCKED,blockState)
        db.updateWithOnConflict(TABLE_NAME,cv," $COLUMN_USERNAME = '$username' ",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun unblockAllUsers() {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put(COLUMN_BLOCKED,0)
        db.updateWithOnConflict(TABLE_NAME,cv," $COLUMN_BLOCKED = 1 ",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun countBlocks() : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_BLOCKED = 1 ", null)
        val num = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return num
    }

    fun syncBlocksFromSharedAndDB() {
        unblockAllUsers()
        val db = DB_NearoomUserInfos(context).writableDatabase
        val cv = ContentValues()
        val allIds = getAllIds()
        val blocksIdFromShared = MySharedPreference(MySharedPreference.PreferenceProfile,context!!).load(MySharedPreference.Profile_BlockedUsers,"String","") as String
        if (blocksIdFromShared.isNotEmpty()){
            val blockIds = blocksIdFromShared.split(",")
            blockIds.forEach {
                if ( allIds.contains(it.toInt())){
                    cv.clear()
                    cv.put(COLUMN_BLOCKED,1)
                    db.updateWithOnConflict(TABLE_NAME,cv," $COLUMN_ID = $it ",null, SQLiteDatabase.CONFLICT_REPLACE)
                }else{
                    cv.clear()
                    cv.put(COLUMN_ID,it)
                    cv.put(COLUMN_BLOCKED,1)
                    db.insertWithOnConflict(TABLE_NAME,null,cv,SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
        }
        if (db.isOpen) db.close()
    }

    fun getMuteAndBlocked(username : String) : JSONArray {
        val ja = JSONArray()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME = '$username'", null)
        if (cursor.moveToFirst()) {
            val mute = cursor.getIntOrNull(cursor.getColumnIndex(COLUMN_MUTE))
            val block = cursor.getIntOrNull(cursor.getColumnIndex(COLUMN_BLOCKED))
            if ( mute == null ){
                ja.put(0)
            }else{
                ja.put(mute)
            }
            if ( block == null ){
                ja.put(0)
            }else{
                ja.put(block)
            }
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return ja
    }

    fun search( word : String , number : Int) : ArrayList<Object_User> {
        val users = ArrayList<Object_User>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME LIKE '%$word%' OR $COLUMN_CONTACTNAME LIKE '%$word%' OR $COLUMN_FULLNAME LIKE '%$word%' LIMIT $number", null)
        if ( cursor.moveToFirst() ){
            do {
                users.add(Object_User(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SERVERID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_PHONENUMBER)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_FULLNAME)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_CONTACTNAME)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_PROFILEPIC)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_CONTACTPIC)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_STATUS)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_LASTSEEN)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ISTYPINGTO)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ISTYPINGTIME)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_MUTE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_BLOCKED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_FAVOURITE))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return users
    }

    fun searchCount( word : String ) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME LIKE '%$word%' OR $COLUMN_CONTACTNAME LIKE '%$word%' OR $COLUMN_FULLNAME LIKE '%$word%' ", null)
        val count = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return count
    }

    companion object{
        const val DB_NAME: String = "NearoomUserInfos_DB"
        const val VERSION: Int = 1
    }


}