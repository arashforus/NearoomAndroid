package com.arashforus.nearroom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arashforus.nearroom.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Runnable
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    var myId : Int = 0
    private val permissionCodeReadContacts = 1652
    private var permissionRequestCounter = 0
    private var isGetUserInfoComplete = false
    private var isGetContactsInfoComplete = false
    private var isGetNearoomInfoComplete = false

    private var timerOnline = Timer()

    private lateinit var myAlertDialog : AlertDialog
    private lateinit var myAlert : AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        //onTransformationStartContainer()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int

        checkPermission(Manifest.permission.READ_CONTACTS, permissionCodeReadContacts)

        /*binding.appBar.fab.setOnClickListener { //view ->
            val createRoom = Intent(this,CreateRoomActivity::class.java)
            val fab = this.findViewById<TransformationLayout>(R.id.Fab_TransformationLayout)
            val bundle = fab.withActivity(this,"myTransitionName")
            createRoom.putExtra("TransformationParams", fab.getParcelableParams())
            startActivity(createRoom,bundle)

        }*/
    }

    override fun onResume() {
        super.onResume()
        timerOnline = Volley_SendOnline(this, myId ).send()
    }

    override fun onPause() {
        super.onPause()
        timerOnline.cancel()
        timerOnline.purge()
    }

    override fun onDestroy() {
        super.onDestroy()
        MyContactsChange(this).unregister()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.MainMenu_Search -> {
                startActivity(Intent(this,SearchActivity::class.java))
                true
            }
            R.id.menu_my_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
                true
            }
            R.id.menu_setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.menu_log_out -> {
                AlertDialog.Builder(this)
                    .setTitle("Are you sure ?")
                    .setMessage("if you logged out you cant access this account anymore ,unless you log in again")
                    .setCancelable(true)
                    .setPositiveButton("Yes , log me out") { dialog, _ ->
                        //val firebaseId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FirebaseId,"String") as String
                        //val firebaseToken = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FirebaseToken,"String") as String
                        Volley_DeleteFirebaseToken(this, myId ,object : Volley_DeleteFirebaseToken.ServerCallBack{
                            override fun updateSuccess(result: JSONObject) {
                                if ( result.getBoolean("isSuccess") ){
                                    MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_Login,false)
                                    MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_FirstTimeInit,false)
                                    MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_LastMessageIdGet,0)
                                    MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_LastNearoomMessageIdGet,0)
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MainActivity).deleteAll()
                                    DB_Contacts(this@MainActivity).deleteDB()
                                    DB_Messages(this@MainActivity).deleteDB()
                                    DB_NearoomInfos(this@MainActivity).deleteDB()
                                    DB_NearoomsParticipant(this@MainActivity).deleteDB()
                                    DB_NearoomUserInfos(this@MainActivity).deleteDB()
                                    DB_NearroomMessages(this@MainActivity).deleteDB()
                                    DB_Nearrooms(this@MainActivity).deleteDB()
                                    DB_Notification(this@MainActivity).deleteDB()
                                    DB_UserInfos(this@MainActivity).deleteDB()
                                    dialog.dismiss()
                                    dialog.cancel()
                                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                    finish()
                                }else{
                                    Toast.makeText(this@MainActivity,"Something wrong , Try again", Toast.LENGTH_LONG).show()
                                }
                            }
                        }).update()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        dialog.cancel()
                    }
                    .create()
                    .show()

                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun startNormal(){
        initToolbar()
        checkFirebaseToken()
        MyContactsChange(this).register()

        //updateMessageDB()
        // Update and find new username for user info //////////////////////////////////////////////
        DB_UserInfos(this).init(myId)
        // Update user info from contacts name and pictures ////////////////////////////////////////
        DB_UserInfos(this).getPhoneInfo()

        initFragment()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.appBar.toolbar)
    }

    private fun checkFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            //println("TOKEN FROM FIREBASEINSTALLIONS :   $it")
            val firebaseId = it.substringBefore(":")
            val firebaseToken = it
            if (MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_FirstTimeInit,"boolean",false) == false ){
                println(" First time ... want to update firebase token")
                Volley_UpdateFirebaseToken(this, myId , firebaseId, firebaseToken, updateFirebaseTokenCallBack(firebaseId,firebaseToken) ).update()
            }else{
                val oldToken = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FirebaseToken,"string")
                if ( oldToken != firebaseToken) {
                    println(" want to update firebase token")
                    Volley_UpdateFirebaseToken(this, myId , firebaseId, firebaseToken, updateFirebaseTokenCallBack(firebaseId,firebaseToken) ).update()
                }else{
                    println(" Firebase Token is similar to old one")
                }
            }
        }
    }

    private fun updateFirebaseTokenCallBack( firebaseId : String, firebaseToken : String) = object : Volley_UpdateFirebaseToken.ServerCallBack {
        override fun updateSuccess(result: JSONObject) {
            if (result.getBoolean("isSuccess") ){
                println("Firebase token is updated in server database")
                MySharedPreference(MySharedPreference.PreferenceProfile,this@MainActivity).save(MySharedPreference.Profile_FirebaseId,firebaseId)
                MySharedPreference(MySharedPreference.PreferenceProfile,this@MainActivity).save(MySharedPreference.Profile_FirebaseToken,firebaseToken)
            }else{
                println("Firebase token has a problem in saving to server database")
                Volley_UpdateFirebaseToken(this@MainActivity, myId , firebaseId, firebaseToken,this).update()
            }
        }
    }

    private fun initFragment() {
        val tabTitles = arrayOf("Contacts", "Chats" , "Nearooms")
        binding.appBar.content.ContentMainViewPager.adapter = FragmentsAdaptor(this)
        // Connect Tab layout to Viewpager for titles //////////////////////////////////////////////
        TabLayoutMediator(binding.appBar.AppbarTabLayout,binding.appBar.content.ContentMainViewPager){tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
            binding.appBar.content.ContentMainViewPager.setCurrentItem(1, false)
        }.attach()
    }

    /*private fun updateMessageDB(){
        Volley_GetAllMessages(this, username, object : Volley_GetAllMessages.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                println("Update Message DB from server successfull ...................")
                if (result.getBoolean("isSuccess")){
                    val MessageDbIntent = Intent("MyBroadcast")
                    MessageDbIntent.putExtra("type","MessageDBUpdate")
                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(MessageDbIntent)
                }
            }
        }).get()
    }*/


    // Check permission for CONTACTS ///////////////////////////////////////////////////////////////
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED ) {
            if ( permissionRequestCounter < 1 ) {
                ActivityCompat.requestPermissions(this,arrayOf(permission), requestCode)
                permissionRequestCounter += 1
            }else{
                makePermissionAlertDialogue()
            }
        } else {
            checkFirstTimeInit()
        }
    }

    private fun checkFirstTimeInit(){
        if (MySharedPreference(MySharedPreference.PreferenceApp,this).load(MySharedPreference.App_FirstTimeInit,"boolean") == false ){
            firstTimeInit()
        }else{
            if ( myId > 0){
                startNormal()
            }else{
                Toast.makeText(this, "Something wrong , Logging again", Toast.LENGTH_LONG).show()
                MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_Login,false)
                MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_FirstTimeInit,false)
                MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_LastMessageIdGet,0)
                MySharedPreference(MySharedPreference.PreferenceApp,this@MainActivity).save(MySharedPreference.App_LastNearoomMessageIdGet,0)
                MySharedPreference(MySharedPreference.PreferenceProfile,this@MainActivity).deleteAll()
                DB_Contacts(this@MainActivity).deleteDB()
                DB_Messages(this@MainActivity).deleteDB()
                DB_NearoomInfos(this@MainActivity).deleteDB()
                DB_NearoomsParticipant(this@MainActivity).deleteDB()
                DB_NearoomUserInfos(this@MainActivity).deleteDB()
                DB_NearroomMessages(this@MainActivity).deleteDB()
                DB_Nearrooms(this@MainActivity).deleteDB()
                DB_Notification(this@MainActivity).deleteDB()
                DB_UserInfos(this@MainActivity).deleteDB()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionCodeReadContacts ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                checkFirstTimeInit()
            }else{
                checkPermission(Manifest.permission.READ_CONTACTS, permissionCodeReadContacts)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun makePermissionAlertDialogue() {
        // Permission Alert Dialogue Configuration ////////////////////////////////////////////////////////
        myAlert = AlertDialog.Builder(this)
        myAlert.setTitle("Permission")
        myAlert.setMessage("For continue using this application you should accept reading your contacts from your phone.")
        myAlert.setCancelable(false)
        myAlert.setPositiveButton("Give Access") { _, _ ->
            val detailSetting = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            detailSetting.setData(Uri.fromParts("package",this.packageName,null))
            startActivity(detailSetting)
        }
        myAlert.setNegativeButton("Exit Application") { dialog, _ ->
            //startActivity(Intent(this@CreateRoomActivity, MainActivity::class.java))
            dialog.dismiss()
            dialog.cancel()
            onBackPressed()
        }
        myAlertDialog = myAlert.create()
        myAlertDialog.show()
    }

    private fun firstTimeInit(){
        val progress = MyProgressDialog().Constructor(this,"Please Wait for first configuration ...")
        progress.setCancelable(false)
        progress.show()

        Thread(Runnable {
            // Creating All databases and tables ///////////////////////////////////////////////////
            DB_Contacts(this).firstTimeInit()
            DB_Messages(this).firstTimeInit()
            DB_NearoomInfos(this).firstTimeInit()
            DB_NearoomsParticipant(this).firstTimeInit()
            DB_NearoomUserInfos(this).firstTimeInit()
            DB_NearroomMessages(this).firstTimeInit()
            DB_Nearrooms(this).firstTimeInit()
            DB_Notification(this).firstTimeInit()
            DB_UserInfos(this).firstTimeInit()

            // Read all contacts from phone book and save in local DB //////////////////////////////
            println("First time inserting contacts to db is started ................")
            DB_Contacts(this).insertAllToDb()
            println("First time inserting contacts to db is finished ................")

            // fetch messages from server //////////////////////////////////////////////////////////
            Volley_GetAllMessages(this, myId ,object : Volley_GetAllMessages.ServerCallBack{
                override fun getSuccess(result: JSONObject) {
                    println("Messages received and saved for first time ...................")

                    DB_UserInfos(this@MainActivity).init(myId)

                    val ids = DB_UserInfos(this@MainActivity).getAllIds()
                    Volley_GetUserInfos(this@MainActivity, ids , object : Volley_GetUserInfos.ServerCallBack {
                        override fun getSuccess(result: JSONObject) {
                            println("get user infos and saved for first time ......................")
                            isGetUserInfoComplete = true
                            checkForStartNormal(progress)
                        }
                    }).getOnce()

                }
            }).get()

            Volley_GetAllNearoomMessages(this, myId ,object : Volley_GetAllNearoomMessages.ServerCallBack{
                override fun getSuccess(result: JSONObject) {
                    println("Nearoom Messages received and saved for first time ...................")

                    DB_NearoomInfos(this@MainActivity).init()

                    val nearooms = DB_NearoomInfos(this@MainActivity).getAllNearooms()
                    Volley_GetNearoomInfos(this@MainActivity, nearooms , object : Volley_GetNearoomInfos.ServerCallBack {
                        override fun getSuccess(result: JSONObject) {
                            println("get nearoom infos and saved for first time ......................")
                            isGetNearoomInfoComplete = true
                            checkForStartNormal(progress)
                        }
                    }).getOnce()

                }
            }).get()

            // Fetch registered contacts from server ///////////////////////////////////////////////
            val numbers = DB_Contacts(this).getArrayNumbersWithCountryCode()

            Volley_GetRegisteredContacts(this, numbers, object : Volley_GetRegisteredContacts.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    println("get registered contacts and saved for first time ......................")

                    DB_UserInfos(this@MainActivity).init(myId)

                    val ids = DB_UserInfos(this@MainActivity).getAllIds()
                    Volley_GetUserInfos(this@MainActivity, ids , object : Volley_GetUserInfos.ServerCallBack {
                        override fun getSuccess(result: JSONObject) {
                            println("get user infos and saved for first time ......................")
                            isGetContactsInfoComplete = true
                            checkForStartNormal(progress)
                        }
                    }).getOnce()

                }
            }).get()

            /*
            // Find all chat and rooms that has a chat with them ///////////////////////////////////
            DB_UserInfos(this).init(myId)
            DB_NearoomInfos(this).init()

            // Fetch all user info and nearoom info and save to local DB ///////////////////////////
            val ids = DB_UserInfos(this).getAllIds()
            Volley_GetUserInfos(this, ids , object : Volley_GetUserInfos.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    println("get user infos and saved for first time ......................")
                }
            }).getOnce()

            val nearooms = DB_NearoomInfos(this).getAllNearooms()
            Volley_GetNearoomInfos(this, nearooms , object : Volley_GetNearoomInfos.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    println("get nearoom infos and saved for first time ......................")
                }
            }).getOnce()

             */

            // Update Firebase Token in server DB //////////////////////////////////////////////////
            checkFirebaseToken()

            // Check for available medias and download neccessory ones /////////////////////////////
            MyMediaDownloader(this).updateUserInfosPicThumb()
            MyMediaDownloader(this).updateNearoomInfosPicThumb()
            //MyMediaDownloader(this).updateContactsProfileThumb()
            MyMediaDownloader(this).updateImagesThumb()
            MyMediaDownloader(this).updateVideosThumb()

        }).start()


    }

    private fun checkForStartNormal(progress : AlertDialog){
        //println("get user info :: $isGetUserInfoComplete")
        //println("get nearoom info :: $isGetNearoomInfoComplete")
        //println("get contacts :: $isGetContactsInfoComplete")
        if ( isGetUserInfoComplete && isGetNearoomInfoComplete && isGetContactsInfoComplete ){
            progress.dismiss()
            MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_FirstTimeInit,true)

            runOnUiThread {
                startNormal()
            }
        }
    }

}

