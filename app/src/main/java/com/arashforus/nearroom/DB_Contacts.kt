package com.arashforus.nearroom

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.ContactsContract
import org.json.JSONException
import org.json.JSONObject

class DB_Contacts(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    private val TABLE_NAME = "Contacts_TABLE"
    private val COLUMN_ID = "ID"
    private val COLUMN_CONTACTID = "CONTACTID"
    private val COLUMN_PHOTOURI = "PHOROURI"
    private val COLUMN_FIRSTNAME = "FIRSTNAME"
    private val COLUMN_SURENAME = "SURENAME"
    private val COLUMN_FULLNAME = "FULLNAME"
    private val COLUMN_PHONENUMBERS = "PHONENUMBERS"
    private val COLUMN_PHONENUMBERWITHCODE = "PHONENUMBERWITHCODE"
    private val COLUMN_DATAVERSION = "DATAVERSION"
    private val COLUMN_JOINED = "JOINED"
    private val COLUMN_USERID = "USERID"

    private val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ( '" +
            COLUMN_ID + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
            COLUMN_CONTACTID + "' INTEGER, '" +
            COLUMN_PHOTOURI + "' TEXT, '" +
            COLUMN_FIRSTNAME + "' TEXT, '" +
            COLUMN_SURENAME + "' TEXT, '" +
            COLUMN_FULLNAME + "' TEXT, '" +
            COLUMN_PHONENUMBERS + "' TEXT, '" +
            COLUMN_PHONENUMBERWITHCODE + "' TEXT, '" +
            COLUMN_DATAVERSION + "' INTEGER, '" +
            COLUMN_JOINED + "' INTEGER, '" +
            COLUMN_USERID + "' INTEGER )"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS '$TABLE_NAME'"


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    /*fun deleteTable(){
        val db = this.writableDatabase
        db?.execSQL(SQL_DELETE_ENTRIES)
        if (db.isOpen){
            db.close()
        }
    }*/

    fun deleteDB(){
        context?.deleteDatabase(DB_NAME)
    }

    fun firstTimeInit(){
        val db = this.writableDatabase
        db?.execSQL(SQL_CREATE_ENTRIES)
        if (db.isOpen)   db.close()
    }

    fun insertAllToDb() {
        val db = this.writableDatabase
        db?.execSQL(SQL_CREATE_ENTRIES)
        val cv = ContentValues()
        val countryCode = MyTools(context!!).getCountryCodeFromMyNumber()
        //val uri = ContactsContract.Data.CONTENT_URI
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val selection = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " > 0 "
        val projection = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            //ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            //ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Data.DATA_VERSION
        )

        val cr: ContentResolver = context.contentResolver
        cr.query(uri, projection, selection, null, null).use{ cursor ->
            if (cursor?.moveToFirst() == true) {
                do {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID))
                    val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                    val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    //val firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                    //val firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GIVEN_NAME))
                    //val surname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    val phoneNumbers = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val dataVersion = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.DATA_VERSION))
                    var phoneNumberWithCode : String? = null
                    if ( !phoneNumbers.isNullOrEmpty()){
                        phoneNumberWithCode = MyTools(context).MakeNumberWithCountryCode(countryCode,phoneNumbers)
                    }

                    cv.clear()
                    cv.put(COLUMN_ID, id)
                    cv.put(COLUMN_CONTACTID, contactId)
                    cv.put(COLUMN_PHOTOURI, photoUri)
                    //cv.put(COLUMN_FIRSTNAME, firstName)
                    //cv.put(COLUMN_SURENAME, surname)
                    cv.put(COLUMN_FULLNAME, name)
                    cv.put(COLUMN_PHONENUMBERS, phoneNumbers)
                    cv.put(COLUMN_PHONENUMBERWITHCODE, phoneNumberWithCode)
                    cv.put(COLUMN_DATAVERSION, dataVersion)
                    //println(cv)
                    db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                } while (cursor.moveToNext())
            }
        }
        if (db.isOpen) db.close()
    }

    fun insertLastItemToDb() {
        val db = this.writableDatabase
        val cv = ContentValues()
        val countryCode = MyTools(context!!).getCountryCodeFromMyNumber()
        val uri = ContactsContract.Contacts.CONTENT_URI
        val cr: ContentResolver = context.contentResolver
        cr.query(uri, null, null, null, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP+" ASC").use{ cursor ->
            if (cursor != null && cursor.count > 0) {
                cursor.moveToLast()
                val lastid = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                //val lastname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                //val lasttimestamp = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))
                //println(" last id is $lastid and last name is $lastname and last timestamp is $lasttimestamp")
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0){
                    cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(lastid),null).use { scursor ->
                        scursor?.moveToFirst()
                        val id = scursor?.getString(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                        val contactId = scursor?.getLong(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                        val photoUri = scursor?.getString(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                        val name = scursor?.getString(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val phonenumbers = scursor?.getString(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val dataVersion = scursor?.getInt(scursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA_VERSION))
                        var phoneNumberWithCode :String? = null
                        if ( !phonenumbers.isNullOrEmpty() ){
                            phoneNumberWithCode = MyTools(context).MakeNumberWithCountryCode(countryCode,phonenumbers)
                        }
                        cv.clear()
                        cv.put(COLUMN_ID, id)
                        cv.put(COLUMN_CONTACTID, contactId)
                        cv.put(COLUMN_PHOTOURI, photoUri)
                        cv.put(COLUMN_FULLNAME, name)
                        cv.put(COLUMN_PHONENUMBERS, phonenumbers)
                        cv.put(COLUMN_PHONENUMBERWITHCODE, phoneNumberWithCode)
                        cv.put(COLUMN_DATAVERSION, dataVersion)
                        println(cv)
                        db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }
            }
        }
        if (db.isOpen) db.close()
    }

    /*
    fun readAllfromDB() : ArrayList<Object_Contact> {
        val dbhelper = DB_Contacts(context)
        val db = dbhelper.readableDatabase
        val contactslist : ArrayList<Object_Contact> = ArrayList()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                val contact = Object_Contact(
                    cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_CONTACTID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHOTOURI)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SURENAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHONENUMBERS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))
                )
                contactslist.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen() ) db.close()
        return contactslist
    }
     */

    /*
    // Read registered contacts from DB to show in contact list ///////////////////////////////////
    fun readRegisteredFromDB() : ArrayList<Object_Contact> {
        val db = this.readableDatabase
        val contactslist : ArrayList<Object_Contact> = ArrayList()
        val cursor: Cursor = db.rawQuery("SELECT * from '" + TABLE_NAME + "' WHERE " + COLUMN_JOINED + " = 1",null )
        if (cursor.moveToFirst()) {
            do {
                var photo:String?
                //println(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO)))
                if (!cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO)).isNullOrEmpty() ){
                    photo = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO))
                }else{
                    if ( !cursor.getString(cursor.getColumnIndex(COLUMN_PHOTOURI)).isNullOrEmpty() ){
                        photo = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTOURI))
                    }else{
                        photo = null
                    }
                }
                val contact = Object_Contact(
                    cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_CONTACTID)),
                    photo,
                    cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SURENAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHONENUMBERS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))
                )
                contactslist.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return contactslist
    }
        */

    fun getRegisteredUsersInfo() : ArrayList<Object_User> {
        val db = this.readableDatabase
        val idList = ArrayList<Int>()
        val contactsList = ArrayList<Object_User>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_JOINED = 1 AND $COLUMN_USERID IS NOT NULL",null )
        if ( cursor.count > 0 ){
            if (cursor.moveToFirst()) {
                do {
                    idList.add(cursor.getInt(cursor.getColumnIndex(COLUMN_USERID)))
                } while (cursor.moveToNext())
            }
        }
        idList.distinct().forEach {
            contactsList.add(DB_UserInfos(context).getUserInfo(it))
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return contactsList
    }

    fun getPhotoAndNames() : ArrayList<Object_Contact> {
        val db = this.readableDatabase
        val contactsList = ArrayList<Object_Contact>()
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERID IS NOT NULL ",null )
        if (cursor.moveToFirst()) {
            do {
                contactsList.add(Object_Contact(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTOURI)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_USERID))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return contactsList
    }

    // Make a JsonObject from DB and passed it to checkUpdates() ///////////////////////////////////
    private fun getDataVersionsFromDB() : JSONObject{
        val json = JSONObject()
        val db = this.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                json.put(cursor.getString(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex( COLUMN_DATAVERSION )))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return json
    }

    /*
    fun getProfilePhotosFromDB() : JSONObject{
        val json :JSONObject = JSONObject()
        val db = this.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO)) == null) {
                    json.put(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)), cursor.getString(cursor.getColumnIndex( COLUMN_PHOTOURI )))
                }else{
                    json.put(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)), cursor.getString(cursor.getColumnIndex( COLUMN_PROFILEPHOTO )))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return json
    }
     */

    /*
    fun getAllUsernames() : ArrayList<String> {
        val usernames = ArrayList<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERNAME IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                usernames.add(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return usernames
    }
        */

    fun getAllUserIds() : ArrayList<Int> {
        val userIds = ArrayList<Int>()
        val db = this.readableDatabase
        val cursor: Cursor =
            db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERID IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                userIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_USERID)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return userIds
    }

    /*
    fun getAllProfilePics() : ArrayList<String> {
        val profilePics = ArrayList<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_PROFILEPHOTO IS NOT NULL", null)
        if (cursor.moveToFirst()) {
            do {
                profilePics.add(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return profilePics
    }
     */


    // Check contacts update ones //////////////////////////////////////////////////////////////////
    fun checkUpdates(){
        val cv = ContentValues()
        val countryCode = MyTools(context!!).getCountryCodeFromMyNumber()
        val versions = getDataVersionsFromDB()
        val db = this.readableDatabase
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val selection = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " > 0 "
        val projection = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            //ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            //ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Data.DATA_VERSION
        )
        val cr: ContentResolver = context.contentResolver
        cr.query(uri, projection, selection, null, null).use{ cursor ->
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID))
                        val dataVersion = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.DATA_VERSION))
                        if (versions.has(id)){
                            if (dataVersion == versions.getInt(id)){
                                //println("contacts is duplicate")
                            }else{
                                val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                val phonenumbers = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                var phoneNumberWithCode : String? = null
                                if ( !phonenumbers.isNullOrEmpty() ){
                                    phoneNumberWithCode = MyTools(context).MakeNumberWithCountryCode(countryCode,phonenumbers)
                                }
                                cv.clear()
                                cv.put(COLUMN_ID, id)
                                cv.put(COLUMN_CONTACTID, contactId)
                                cv.put(COLUMN_PHOTOURI, photoUri)
                                cv.put(COLUMN_FULLNAME, name)
                                cv.put(COLUMN_PHONENUMBERS, phonenumbers)
                                cv.put(COLUMN_PHONENUMBERWITHCODE, phoneNumberWithCode)
                                cv.put(COLUMN_DATAVERSION, dataVersion)
                                println(cv.toString())
                                db.insertWithOnConflict( TABLE_NAME,null, cv,  SQLiteDatabase.CONFLICT_REPLACE )
                            }
                        }else{
                            val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                            val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            val phonenumbers = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            var phoneNumberWithCode : String? = null
                            if ( !phonenumbers.isNullOrEmpty() ){
                                phoneNumberWithCode = MyTools(context).MakeNumberWithCountryCode(countryCode,phonenumbers)
                            }
                            cv.clear()
                            cv.put(COLUMN_ID, id)
                            cv.put(COLUMN_CONTACTID, contactId)
                            cv.put(COLUMN_PHOTOURI, photoUri)
                            cv.put(COLUMN_FULLNAME, name)
                            cv.put(COLUMN_PHONENUMBERS, phonenumbers)
                            cv.put(COLUMN_PHONENUMBERWITHCODE, phoneNumberWithCode)
                            cv.put(COLUMN_DATAVERSION, dataVersion)
                            println(cv.toString())
                            db.insertWithOnConflict( TABLE_NAME,null, cv,  SQLiteDatabase.CONFLICT_REPLACE )
                        }

                    } while (cursor.moveToNext())
                }
            }
        }
        if (db.isOpen) db.close()
    }

    /*
    fun getArrayNumbers() : ArrayList<String> {
        val numbers = ArrayList<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                numbers.add(cursor.getString(cursor.getColumnIndex(COLUMN_PHONENUMBERS)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return numbers
    }
     */

    fun getArrayNumbersWithCountryCode() : ArrayList<String> {
        val numbers = ArrayList<String>()
        var number: String
        var numberWithCode: String
        val countryCode = MyTools(context!!).getCountryCodeFromMyNumber()
        val dbHelper = DB_Contacts(context)
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME'", null)
        if (cursor.moveToFirst()) {
            do {
                number = cursor.getString(cursor.getColumnIndex(COLUMN_PHONENUMBERS))
                numberWithCode = MyTools(context).MakeNumberWithCountryCode(countryCode,number)
                numbers.add(numberWithCode)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (db.isOpen) db.close()
        return numbers
    }

    fun setRegisteredContacts(result : JSONObject){
        val db = this.writableDatabase
        val cv = ContentValues()
        try {
            val registered = result.getJSONObject("registered")
            registered.keys().forEach {
                val userId = registered.getInt(it.toString())
                if ( userId > 0 ) {
                    cv.clear()
                    cv.put(COLUMN_JOINED, 1)
                    cv.put(COLUMN_USERID, userId)

                    db.update(TABLE_NAME, cv, " $COLUMN_PHONENUMBERWITHCODE = '$it' ", null)
                }
            }
        }catch (e : JSONException){
            println(e.message)
        }
    }

    fun getFullName( userId : Int ) : String?{
        var fullName : String? = null
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_USERID = '$userId'", null)
        if (cursor.moveToFirst()) {
            do {
                fullName = cursor.getString(cursor.getColumnIndex(COLUMN_FULLNAME))
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
        return fullName
    }


    /*
    fun downloadNewProfilePictures(){
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_JOINED = 1 ", null)
        if (cursor.moveToFirst()) {
            do {
                val profilephoto = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILEPHOTO))
                if (profilephoto !== null){
                    val wrapper = ContextWrapper(context)
                    var file = wrapper.getDir("images", Context.MODE_APPEND)
                    file = File(file, profilephoto)
                    if (file.exists()){
                        println("profile photo is exist")
                    }else{
                        Glide.with(context!!.applicationContext)
                            .asBitmap()
                            .load(MyServerSide().getProfileUri(profilephoto))
                            .listener(object : RequestListener<Bitmap> {
                                override fun onLoadFailed( e: GlideException?, model: Any?, target: Target<Bitmap>?,isFirstResource: Boolean): Boolean {
                                    println("load failed")
                                    return true
                                }
                                override fun onResourceReady( resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    println("load complete")
                                    if (resource != null) {
                                        MyInternalStorage(context).saveProfilePic(resource,profilephoto)
                                    }
                                    return true
                                }
                            })
                            .submit()
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        if ( db.isOpen) db.close()
    }
     */

    fun countRegistered() : Int{
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * from '$TABLE_NAME' WHERE $COLUMN_JOINED = 1 ", null)
        val number = cursor.count
        cursor.close()
        if ( db.isOpen) db.close()
        return number
    }

    companion object{
        const val DB_NAME: String = "Contacts_DB"
        const val VERSION: Int = 11
    }



}