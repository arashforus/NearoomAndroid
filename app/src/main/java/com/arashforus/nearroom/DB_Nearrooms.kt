package com.arashforus.nearroom

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import androidx.core.database.getStringOrNull
import org.json.JSONArray
import org.json.JSONObject

class DB_Nearrooms(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "Nearooms_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_SERVERID = "SERVERID"
    private val COLUMN_ROOMNAME = "ROOMNAME"
    private val COLUMN_ROOMPIC = "ROOMPIC"
    private val COLUMN_ROOMDESCRIPTION = "ROOMDESCRIPTION"
    private val COLUMN_ROOMCATEGORY = "ROOMCATEGORY"
    private val COLUMN_ROOMCAPACITY = "ROOMCAPACITY"
    private val COLUMN_ROOMJOINED = "ROOMJOINED"
    private val COLUMN_CREATEDTIME = "CREATEDTIME"
    private val COLUMN_CREATORID = "CREATORID"
    private val COLUMN_CREATORUSERNAME = "CREATORUSERNAME"
    private val COLUMN_ROOMLAT = "ROOMLAT"
    private val COLUMN_ROOMLON = "ROOMLON"
    private val COLUMN_DISTANCE = "DISTANCE"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_SERVERID + "' INTEGER, '" +
            COLUMN_ROOMNAME + "' TEXT, '" +
            COLUMN_ROOMPIC + "' TEXT, '" +
            COLUMN_ROOMDESCRIPTION + "' TEXT, '" +
            COLUMN_ROOMCATEGORY + "' TEXT, '" +
            COLUMN_ROOMCAPACITY + "' INTEGER, '" +
            COLUMN_ROOMJOINED + "' INTEGER, '" +
            COLUMN_CREATEDTIME + "' TEXT, '" +
            COLUMN_CREATORID + "' INTEGER, '" +
            COLUMN_CREATORUSERNAME + "' TEXT, '" +
            COLUMN_ROOMLAT + "' REAL, '" +
            COLUMN_ROOMLON + "' REAL, '" +
            COLUMN_DISTANCE + "' REAL)"

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

    private val idPos = 0
    private val roomNamePos = 1
    //private val roomNameDCPos = 2
    private val roomPicPos = 3
    //private val roomPicDCPos = 4
    private val roomDescriptionPos = 5
    //private val roomDescriptionDCPos = 6
    private val roomCategoryPos = 7
    //private val roomCategoryDCPos = 8
    private val roomCapacityPos = 9
    //private val roomCapacityDCPos = 10
    private val roomJoinedPos = 11
    private val createdTimePos = 12
    private val creatorIdPos = 13
    private val creatorUsernamePos = 14
    private val roomLatPos = 15
    private val roomLonPos = 16
    //private val roomIsRemovePos = 17
    //private val roomIsRemoveDCPos = 18

    fun saveRooms(result: JSONObject, lat: Double, lng : Double) {
        val db = this.writableDatabase
        db?.execSQL(SQL_DELETE_ENTRIES)
        db?.execSQL(SQL_CREATE_ENTRIES)
        val cv = ContentValues()
        if ( !result.isNull("rooms")){
            val nearooms : JSONArray = result.getJSONArray("rooms")
            for (i in 0 until nearooms.length()) {
                val chat = nearooms.getJSONArray(i)
                cv.clear()
                cv.put(COLUMN_SERVERID,chat.getInt(idPos))
                cv.put(COLUMN_ROOMNAME,chat.getString(roomNamePos))
                if ( !chat.isNull(roomPicPos) ){
                    cv.put(COLUMN_ROOMPIC,chat.getString(roomPicPos))
                }
                if ( !chat.isNull(roomDescriptionPos) ){
                    cv.put(COLUMN_ROOMDESCRIPTION,chat.getString(roomDescriptionPos))
                }
                cv.put(COLUMN_ROOMCATEGORY,chat.getString(roomCategoryPos))
                cv.put(COLUMN_ROOMCAPACITY,chat.getInt(roomCapacityPos))
                cv.put(COLUMN_ROOMJOINED,chat.getInt(roomJoinedPos))
                cv.put(COLUMN_CREATEDTIME,chat.getString(createdTimePos))
                cv.put(COLUMN_CREATORID,chat.getInt(creatorIdPos))
                cv.put(COLUMN_CREATORUSERNAME,chat.getString(creatorUsernamePos))
                cv.put(COLUMN_ROOMLAT,chat.getDouble(roomLatPos))
                cv.put(COLUMN_ROOMLON,chat.getDouble(roomLonPos))
                val results = FloatArray(1)
                Location.distanceBetween(lat,lng,chat.getDouble(roomLatPos),chat.getDouble(roomLonPos),results)
                cv.put(COLUMN_DISTANCE,results[0])
                db.insertWithOnConflict(TABLE_NAME,null,cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
        if (db.isOpen) db.close()
    }

    fun loadRooms() : ArrayList<Object_Nearoom>{
        val db = this.readableDatabase
        val nearoomsList : ArrayList<Object_Nearoom> = ArrayList()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' ORDER BY '$COLUMN_DISTANCE'",null)
        if ( cursor.moveToFirst() ) {
            do {
                val room = Object_Nearoom(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SERVERID)),
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

    /*
    fun getRoomDetail(roomId : Int) : Object_Nearoom? {
        val db = this.readableDatabase
        var nearoom : Object_Nearoom? = null
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_SERVERID = '$roomId' ",null)
        if (cursor.moveToFirst()) {
            nearoom = Object_Nearoom(
                cursor.getInt(cursor.getColumnIndex(COLUMN_SERVERID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMNAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMPIC)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMDESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ROOMCATEGORY)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMCAPACITY)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMJOINED)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE))
            )
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return nearoom
    }

     */

    companion object{
        const val DB_NAME: String = "Nearooms_DB"
        const val VERSION: Int = 3
    }


}