package com.arashforus.nearroom

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import org.json.JSONArray
import org.json.JSONObject

class DB_NearoomInfos(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "NearoomInfos_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_ROOMNAME = "ROOMNAME"
    private val COLUMN_ROOMPIC = "ROOMPIC"
    private val COLUMN_ROOMDESCRIPTION = "ROOMDESCRIPTION"
    private val COLUMN_ROOMCATEGORY = "ROOMCATEGORY"
    private val COLUMN_ROOMCAPACITY = "ROOMCAPACITY"
    private val COLUMN_ROOMJOINED = "ROOMJOINED"
    private val COLUMN_CREATED = "ROOMCREATED"
    private val COLUMN_CREATORID = "CREATORID"
    private val COLUMN_CREATORUSERNAME = "CREATORUSERNAME"
    private val COLUMN_ROOMLAT = "ROOMLAT"
    private val COLUMN_ROOMLON = "ROOMLON"
    private val COLUMN_DISTANCE = "DISTANCE"
    private val COLUMN_JOIN = "JOIN"
    private val COLUMN_MUTE = "MUTE"


    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_ROOMNAME + "' TEXT, '" +
            COLUMN_ROOMDESCRIPTION + "' TEXT, '" +
            COLUMN_ROOMPIC + "' TEXT, '" +
            COLUMN_ROOMCATEGORY + "' TEXT, '" +
            COLUMN_ROOMCAPACITY + "' INTEGER, '" +
            COLUMN_ROOMJOINED + "' INTEGER, '" +
            COLUMN_CREATED + "' TEXT, '" +
            COLUMN_CREATORID + "' INTEGER, '" +
            COLUMN_CREATORUSERNAME + "' TEXT, '" +
            COLUMN_ROOMLAT + "' REAL, '" +
            COLUMN_ROOMLON + "' REAL, '" +
            COLUMN_DISTANCE + "' REAL, '" +
            COLUMN_JOIN + "' INTEGER, '" +
            COLUMN_MUTE + "' INTEGER)"

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

    // Find new nearooms in nearoom messages DB and insert in nearoom info DB //////////////////////
    fun init(){
        val db = DB_NearoomInfos(context).writableDatabase
        val cv = ContentValues()
        val newNearooms = ArrayList<Int>()
        val existNearooms = getAllNearooms()
        //println(" ------------------------ existNearooms : ${existNearooms.size}")
        val chatsNearooms = DB_NearroomMessages(context).findRooms()
        newNearooms.addAll(chatsNearooms)
        newNearooms.removeAll(existNearooms)
        val filteredNewNearooms = newNearooms.distinct()
        //println("=============== filterdnearooms : $filteredNewNearooms")
        filteredNewNearooms.forEach {
            cv.clear()
            cv.put(COLUMN_ID,it)
            db.insertWithOnConflict(TABLE_NAME,null,cv,SQLiteDatabase.CONFLICT_REPLACE)
        }
        if (db.isOpen) db.close()
    }

    @SuppressLint("Range")
    fun getAllNearooms() : ArrayList<Int> {
        val nearooms = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' ", null)
        if (cursor.moveToFirst()) {
            do {
                nearooms.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearooms
    }

    /*
    fun saveRooms(rooms: JSONObject, lat: Double, lng : Double) {
        val dbhelper = DB_NearoomInfos(context)
        val db = dbhelper.writableDatabase
        db?.execSQL(SQL_DELETE_ENTRIES)
        db?.execSQL(SQL_CREATE_ENTRIES)
        val cv = ContentValues()

        println(rooms.toString())
        val nearrooms : JSONArray = rooms.getJSONArray("rooms")
        if ( rooms.getBoolean("isSuccess")){
            for (i in 0..nearrooms.length()-1) {
                val chat = nearrooms.getJSONArray(i)
                cv.clear()
                //cv.put(COLUMN_ID,chat.getInt(0))
                cv.put(COLUMN_SERVERID,chat.getInt(0))
                cv.put(COLUMN_ROOMNAME,chat.getString(1))
                cv.put(COLUMN_ROOMCATEGORY,chat.getString(2))
                cv.put(COLUMN_ROOMCAPACITY,chat.getInt(3))
                cv.put(COLUMN_ROOMJOINED,chat.getInt(4))
                cv.put(COLUMN_CREATORID,chat.getInt(6))
                cv.put(COLUMN_CREATORUSERNAME,chat.getString(7))
                cv.put(COLUMN_ROOMLAT,chat.getDouble(8))
                cv.put(COLUMN_ROOMLON,chat.getDouble(9))
                val results = FloatArray(1)
                Location.distanceBetween(lat,lng,chat.getDouble(8),chat.getDouble(9),results)
                //println(results[0])
                cv.put(COLUMN_DISTANCE,results[0])
                db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
        if (db.isOpen) db.close()
        loadRooms()
    }
     */

    // Position of Json Array of response from server //////////////////////////////////////////////
    //private val idPos = 0
    private val roomNamePos = 1
    private val roomPicPos = 2
    private val roomDescriptionPos = 3
    private val roomCategoryPos = 4
    private val roomCapacityPos = 5
    private val roomJoinedPos = 6
    private val roomCreatedPos = 7
    private val roomCreatorIdPos = 8
    private val roomCreatorUsernamePos = 9
    private val roomLatPos = 10
    private val roomLonPos = 11

    fun saveNearoomInfo(jsonObject : JSONObject) {
        val db = this.writableDatabase
        val cv = ContentValues()
        jsonObject.keys().forEach {
            val ja = jsonObject.getJSONArray(it.toString())
            cv.clear()
            cv.put(COLUMN_ID,it.toString())
            cv.put(COLUMN_ROOMNAME,ja.getString(roomNamePos))
            if ( !ja.isNull(roomPicPos) ){
                cv.put(COLUMN_ROOMPIC,ja.getString(roomPicPos))
            }else{
                cv.putNull(COLUMN_ROOMPIC)
            }
            if ( !ja.isNull(roomDescriptionPos) ){
                cv.put(COLUMN_ROOMDESCRIPTION,ja.getString(roomDescriptionPos))
            }else{
                cv.putNull(COLUMN_ROOMDESCRIPTION)
            }
            cv.put(COLUMN_ROOMCATEGORY,ja.getString(roomCategoryPos))
            cv.put(COLUMN_ROOMCAPACITY,ja.getString(roomCapacityPos))
            cv.put(COLUMN_ROOMJOINED,ja.getString(roomJoinedPos))
            cv.put(COLUMN_CREATED,ja.getString(roomCreatedPos))
            cv.put(COLUMN_CREATORID,ja.getString(roomCreatorIdPos))
            cv.put(COLUMN_CREATORUSERNAME,ja.getString(roomCreatorUsernamePos))
            cv.put(COLUMN_ROOMLAT,ja.getString(roomLatPos))
            cv.put(COLUMN_ROOMLON,ja.getString(roomLonPos))
            db.updateWithOnConflict(TABLE_NAME,cv, "$COLUMN_ID = '$it'",null, SQLiteDatabase.CONFLICT_REPLACE)
        }
        if (db.isOpen) db.close()
    }

    /*
    fun loadRooms() : ArrayList<Object_Nearoom>{
        val db = this.readableDatabase
        val nearoomsList : ArrayList<Object_Nearoom> = ArrayList()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' ORDER BY '$COLUMN_DISTANCE'",null)
        if (cursor.moveToFirst() ) {
            do {
                val room = Object_Nearoom(
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMPIC)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMDESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMCATEGORY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMCAPACITY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMJOINED)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE))
                )
                nearoomsList.add(room)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearoomsList
    }
     */

    @SuppressLint("Range")
    fun getRoomDetail(roomId : Int) : Object_Nearoom? {
        val db = this.readableDatabase
        var nearoom : Object_Nearoom? = null
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$roomId' ",null)
        if (cursor.moveToFirst()) {
            nearoom = Object_Nearoom(
                cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMPIC)),
                cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMDESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMCATEGORY)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMCAPACITY)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMJOINED)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE)),
                cursor.getIntOrNull(cursor.getColumnIndex(COLUMN_MUTE))
            )
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearoom
    }

    @SuppressLint("Range")
    fun getAllNearoomPhotos() : ArrayList<String> {
        val nearoomPhotos = ArrayList<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMPIC IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                nearoomPhotos.add(cursor.getString(cursor.getColumnIndex(COLUMN_ROOMPIC)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearoomPhotos
    }

    @SuppressLint("Range")
    fun getRoomPicWithRoomIds() : JSONObject{
        val json = JSONObject()
        val db = this.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMPIC IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                json.put(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)).toString(), cursor.getString(cursor.getColumnIndex( COLUMN_ROOMPIC )))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return json
    }

    fun getJoinAndMute(roomId : Int) : JSONArray {
        val ja = JSONArray()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$roomId'", null)
        if (cursor.moveToFirst()) {
            val join = cursor.getIntOrNull(cursor.getColumnIndex(COLUMN_JOIN))
            val mute = cursor.getIntOrNull(cursor.getColumnIndex(COLUMN_MUTE))
            if ( join == null ){
                ja.put(0)
            }else{
                ja.put(join)
            }
            if ( mute == null ){
                ja.put(0)
            }else{
                ja.put(mute)
            }
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return ja
    }

    fun setJoin(roomId : Int , joinState : Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put(COLUMN_JOIN,joinState)
        db.updateWithOnConflict(TABLE_NAME,cv," $COLUMN_ID = '$roomId' ",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun setMute(roomId : Int , muteState : Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put(COLUMN_MUTE,muteState)
        db.updateWithOnConflict(TABLE_NAME,cv," $COLUMN_ID = '$roomId' ",null, SQLiteDatabase.CONFLICT_REPLACE)
        if (db.isOpen) db.close()
    }

    fun getCreatedTime(roomId: Int ) : String? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = '$roomId' ORDER BY $COLUMN_ID ASC", null)
        var createdTime : String? = null
        if (cursor.moveToFirst()){
            createdTime = cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_CREATED))
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return createdTime
    }

    @SuppressLint("Range")
    fun search(word : String, number : Int) : ArrayList<Object_Nearoom> {
        val nearooms = ArrayList<Object_Nearoom>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMNAME LIKE '%$word%' OR $COLUMN_ROOMDESCRIPTION LIKE '%$word%' LIMIT $number ", null)
        if ( cursor.moveToFirst() ){
            do {
                nearooms.add(Object_Nearoom(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMPIC)),
                    cursor.getStringOrNull(cursor.getColumnIndex(COLUMN_ROOMDESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMCATEGORY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMCAPACITY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMJOINED)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_MUTE))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearooms
    }

    fun searchCount( word : String ) : Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ROOMNAME LIKE '%$word%' OR $COLUMN_ROOMDESCRIPTION LIKE '%$word%' ", null)
        val count = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return count
    }

    fun isNearoomExist( nearoomId : Int ) : Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_ID = $nearoomId ", null)
        val count = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return count>0
    }

    companion object{
        const val DB_NAME: String = "NearoomInfos_DB"
        const val VERSION: Int = 2
    }


}