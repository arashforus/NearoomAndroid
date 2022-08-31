package com.arashforus.nearroom

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityMyProfileBinding
import com.bumptech.glide.Glide
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private val galleryRequestCode = 156

    private var isStatusEditEnable = false
    private var isProfileEditEnable = false
    private var isRemoveBlockedEnable = false
    private var isProfilePicCircle = true

    lateinit var username : String
    var myID : Int = 0
    lateinit var naturalColor : ColorStateList
    lateinit var positiveColor : ColorStateList
    lateinit var negativeColor : ColorStateList

    private lateinit var thumbNotificationConfig : UploadNotificationConfig
    private lateinit var profileNotificationConfig : UploadNotificationConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Username,"string") as String
        myID = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int
        naturalColor = this.resources.getColorStateList(R.color.colorPrimaryDark)
        positiveColor = this.resources.getColorStateList(R.color.buttonPositive)
        negativeColor = this.resources.getColorStateList(R.color.buttonNegetive)

        MyTools(this).forceHideKeyboard(this)
        initToolbar()
        createNotificationChannel()
        createNotificationConfig()
        UploadServiceConfig.initialize(this.application, notificationChannelID, BuildConfig.DEBUG)

        if ( intent.hasExtra("purpose") ){
            if ( intent.getStringExtra("purpose")!!.toUpperCase(Locale.ROOT).trim() == "CHANGEPROFILE" ){
                val path = intent.getStringExtra("path")
                val thumbPath = intent.getStringExtra("thumbPath")
                val imageName = intent.getStringExtra("imageName")
                uploadProfilePic(path!!,thumbPath!!)
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_ProfilePic, imageName)
            }
        }

        makeView()
        fetchProfileInfos()
        makeListeners()

        Volley_GetPasswordDateChange(this, myID, object : Volley_GetPasswordDateChange.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                makeView()
            }
        }).get()

    }

    override fun onResume() {
        super.onResume()
        makeView()
    }


    private fun initToolbar(){
        setSupportActionBar(binding.MyProfileActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "My Profile"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView(){
        val profilePic = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ProfilePic,"string").toString()

        // Profile Pic /////////////////////////////////////////////////////////////////////////////
        if ( profilePic == "" || profilePic.toUpperCase(Locale.ROOT) == "NULL" ){
            Glide.with(this)
                .load(R.drawable.defaultprofile)
                .circleCrop()
                .override(500)
                .error(R.drawable.defaultprofile)
                .dontAnimate()
                .into(binding.MyProfileActivityProfilePicture)
            Glide.with(this)
                .load(R.drawable.defaultprofile)
                .centerCrop()
                .override(500)
                .error(R.drawable.defaultprofile)
                .dontAnimate()
                .into(binding.MyProfileActivityProfilePictureSquare)
        }else{
            if ( MyInternalStorage(this).isProfileAvailable(profilePic) ){
                Glide.with(this)
                    .load(MyInternalStorage(this).getProfilePicString(profilePic))
                    .circleCrop()
                    .override(500)
                    .error(R.drawable.defaultprofile)
                    .dontAnimate()
                    .into(binding.MyProfileActivityProfilePicture)
                Glide.with(this)
                    .load(MyInternalStorage(this).getProfilePicString(profilePic))
                    .centerCrop()
                    .override(500)
                    .error(R.drawable.defaultprofile)
                    .dontAnimate()
                    .into(binding.MyProfileActivityProfilePictureSquare)
            }else{
                if ( MyInternalStorage(this).isProfileThumbAvailable(profilePic) ){
                    Glide.with(this)
                        .load(MyInternalStorage(this).getThumbProfilePicString(profilePic))
                        .circleCrop()
                        .override(500)
                        .error(R.drawable.defaultprofile)
                        .dontAnimate()
                        .into(binding.MyProfileActivityProfilePicture)
                    Glide.with(this)
                        .load(MyInternalStorage(this).getThumbProfilePicString(profilePic))
                        .centerCrop()
                        .override(500)
                        .error(R.drawable.defaultprofile)
                        .dontAnimate()
                        .into(binding.MyProfileActivityProfilePictureSquare)

                }else{
                    Glide.with(this)
                        .load(R.drawable.defaultprofile)
                        .circleCrop()
                        .override(500)
                        .error(R.drawable.defaultprofile)
                        .dontAnimate()
                        .into(binding.MyProfileActivityProfilePicture)
                    Glide.with(this)
                        .load(R.drawable.defaultprofile)
                        .centerCrop()
                        .override(500)
                        .error(R.drawable.defaultprofile)
                        .dontAnimate()
                        .into(binding.MyProfileActivityProfilePictureSquare)
                }
                AndroidNetworking.download( MyServerSide().getProfileUri(profilePic), MyInternalStorage(this).getProfileFolder(), profilePic)
                    .setTag(profilePic)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@MyProfileActivity)
                                .load(MyInternalStorage(this@MyProfileActivity).getProfilePicString(profilePic))
                                .circleCrop()
                                .override(500)
                                .error(R.drawable.defaultprofile)
                                .dontAnimate()
                                .into(binding.MyProfileActivityProfilePicture)
                            Glide.with(this@MyProfileActivity)
                                .load(MyInternalStorage(this@MyProfileActivity).getProfilePicString(profilePic))
                                .centerCrop()
                                .override(500)
                                .error(R.drawable.defaultprofile)
                                .dontAnimate()
                                .into(binding.MyProfileActivityProfilePictureSquare)
                        }
                        override fun onError(anError: ANError?) {
                            println("download $profilePic  has error")
                        }
                    })
            }
        }


        // Statics /////////////////////////////////////////////////////////////////////////////////
        val dateJoined = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_DateJoined,"String") as String
        if ( dateJoined.uppercase(Locale.ROOT).trim() !== "NULL" ){
            binding.MyProfileActivityDatesJoinedNumber.text = MyDateTime().DayDiff(dateJoined).toString()
        }else {
            Volley_GetDateJoined(this, myID, object : Volley_GetDateJoined.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    makeView()
                }
            }).get()
            binding.MyProfileActivityDatesJoinedNumber.text = "N/A"
        }
        val privateSend = DB_Messages(this).countMessagesSend(myID)
        val roomSend = DB_NearroomMessages(this).countMessagesSend(myID)
        binding.MyProfileActivityMessagesSentNumber.text = ( privateSend + roomSend ).toString()
        binding.MyProfileActivityContactsJoinedNumber.text = DB_Contacts(this).countRegistered().toString()

        // Status //////////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityStatusValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Status,"String","No status").toString())

        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityUsernameValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Username,"string").toString())
        binding.MyProfileActivityFullnameValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FullName,"string").toString())
        binding.MyProfileActivityEmailValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Email,"string").toString())
        binding.MyProfileActivityBirthYearValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_BirthYear,"int").toString())
        if ( !isProfileEditEnable ){
            binding.MyProfileActivityUsernameValue.isEnabled = false
            binding.MyProfileActivityFullnameValue.isEnabled = false
            binding.MyProfileActivityEmailValue.isEnabled = false
            binding.MyProfileActivityBirthYearValue.isEnabled = false
        }
        val emailVerify = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_IsEmailVerify,"Int") as Int
        if ( emailVerify == 1 ){
            binding.MyProfileActivityEmailVerify.visibility = View.GONE
        }else{
            binding.MyProfileActivityEmailVerify.visibility = View.VISIBLE
        }

        // Phone number ////////////////////////////////////////////////////////////////////////////
        //val flag = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_CountryFlag,"String") as String
        //val flagInt = Integer.parseInt(flag)
        val country = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Country,"string") as String
        //binding.MyProfileActivityCountryflag.setImageResource(flagInt)
        binding.MyProfileActivityCountryflag.setImageResource( MyTools(this).getDrawableId("flag_${country.toLowerCase(Locale.ROOT)}") )
        binding.MyProfileActivityCountryValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Country,"string").toString())
        binding.MyProfileActivityPhoneNumberValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_PhoneNumberWithCodeFormatted,"string").toString())
        binding.MyProfileActivityCountryValue.isEnabled = false
        binding.MyProfileActivityPhoneNumberValue.isEnabled = false

        // Password and Security ///////////////////////////////////////////////////////////////////
        val password = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Password,"String") as String
        val dateChange = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_PasswordDateChanged,"String","") as String
        var diffday = "N/A"
        if ( dateChange !== "Null" && dateChange !== "" ){
            diffday = MyDateTime().DayDiff(dateChange).toString()
        }
        binding.MyProfileActivityPasswordStrengthValue.setText(MyTools(this).measurePasswordStrength(password)[0].toString())
        binding.MyProfileActivityLastChangePasswordValue.setText("$diffday days ago")
        binding.MyProfileActivityPasswordStrengthValue.isEnabled = false
        binding.MyProfileActivityLastChangePasswordValue.isEnabled = false

        // Blocked List ////////////////////////////////////////////////////////////////////////////
        //val blockedUsers = MySharedPreference(MySharedPreference.PreferenceProfile,this).loadJSONArray(MySharedPreference.Profile_BlockedUsers)
        val blockedCount = DB_UserInfos(this).countBlocks()
        when ( blockedCount ){
            0 -> binding.MyProfileActivityBlockedListValue.text = "no user blocked"
            1 -> binding.MyProfileActivityBlockedListValue.text = "1 user blocked"
            else -> binding.MyProfileActivityBlockedListValue.text = "$blockedCount users blocked"
        }
        //binding.MyProfileActivityBlockedListValue.isEnabled = false

    }

    private fun makeListeners() {
        // Profile Pic /////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityProfilePicture.setOnClickListener {
            if ( isProfilePicCircle ){
                binding.MyProfileActivityProfilePictureSquare.visibility = View.VISIBLE
                ViewAnimationUtils.createCircularReveal(binding.MyProfileActivityProfilePictureSquare,
                binding.MyProfileActivityProfilePictureSquare.width/2,
                binding.MyProfileActivityProfilePictureSquare.height/2,
                binding.MyProfileActivityProfilePictureSquare.width/3f,
                binding.MyProfileActivityProfilePictureSquare.width/1f).apply {
                    this.duration = 500
                    //this.interpolator = DecelerateInterpolator()
                    this.doOnEnd {
                        binding.MyProfileActivityProfilePictureSquare.visibility = View.VISIBLE
                        isProfilePicCircle = false
                    }
                }.start()
                isProfilePicCircle = false
            }else{
                ViewAnimationUtils.createCircularReveal(binding.MyProfileActivityProfilePictureSquare,
                binding.MyProfileActivityProfilePictureSquare.width/2,
                binding.MyProfileActivityProfilePictureSquare.height/2,
                binding.MyProfileActivityProfilePictureSquare.width/1f,
                binding.MyProfileActivityProfilePictureSquare.width/3f).apply {
                    this.duration = 500
                    //this.interpolator = DecelerateInterpolator()
                    this.doOnEnd {
                        binding.MyProfileActivityProfilePictureSquare.visibility = View.GONE
                        isProfilePicCircle = true
                    }
                }.start()
                isProfilePicCircle = true

            }
        }

        binding.MyProfileActivityChangePicButton.setOnClickListener {
            //val galleryintent = Intent(Intent.ACTION_PICK)
            //galleryintent.type = "image/*"
            //startActivityForResult(galleryintent,galleryRequestCode)
            val galleryIntent = Intent(this,GalleryAlbumActivity::class.java)
            galleryIntent.putExtra("title","Select profile pic")
            galleryIntent.putExtra("type","PICTURE")
            galleryIntent.putExtra("fromActivity","Profile")
            startActivity(galleryIntent)
        }

        binding.MyProfileActivityRemovePicButton.setOnClickListener {
            // set profile pic in sharedPref and server to null
            AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("You want to delete your profile picture , are you sure ?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    Volley_DeleteProfilePic(this, myID, object : Volley_DeleteProfilePic.ServerCallBack {
                            override fun getSuccess(result: JSONObject) {
                                MySharedPreference( MySharedPreference.PreferenceProfile,this@MyProfileActivity).save(MySharedPreference.Profile_ProfilePic, "")
                                Toast.makeText( this@MyProfileActivity,"Profile picture successfully removed", Toast.LENGTH_LONG ).show()
                                fetchProfileInfos()
                            }
                        }).send()
                    dialog.dismiss()
                    dialog.cancel()
                }
                .create()
                .show()

        }

        // Statics /////////////////////////////////////////////////////////////////////////////////

        // Status //////////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityChangeStatus.setOnClickListener {
            if ( isStatusEditEnable ){
                // Confirm /////////////////////////////////////////////////////////////////////////
                Volley_ChangeStatus(this, myID, binding.MyProfileActivityStatusValue.text.toString(), object : Volley_ChangeStatus.ServerCallBack{
                    override fun getSuccess(result: JSONObject) {
                        if ( result.getBoolean("isSuccess") ){
                            binding.MyProfileActivityStatusValue.isEnabled = false
                            binding.MyProfileActivityChangeStatus.text = "change"
                            binding.MyProfileActivityChangeStatus.backgroundTintList = naturalColor
                            binding.MyProfileActivityRemoveStatus.text = "remove"
                            binding.MyProfileActivityRemoveStatus.backgroundTintList = naturalColor
                            isStatusEditEnable = false
                            if ( result.getBoolean("isDelete") ){
                                Toast.makeText(this@MyProfileActivity, "Status successfully removed", Toast.LENGTH_LONG).show()
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity).delete(MySharedPreference.Profile_Status)
                                makeView()
                            }else{
                                Toast.makeText(this@MyProfileActivity, "Status successfully changed", Toast.LENGTH_LONG).show()
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                    .save(MySharedPreference.Profile_Status,binding.MyProfileActivityStatusValue.text.toString())
                                makeView()
                            }
                        }
                    }
                }).send()
            }else{
                // Change //////////////////////////////////////////////////////////////////////////
                binding.MyProfileActivityStatusValue.isEnabled = true
                binding.MyProfileActivityChangeStatus.text = "confirm"
                binding.MyProfileActivityChangeStatus.backgroundTintList = positiveColor
                binding.MyProfileActivityRemoveStatus.text = "cancel"
                binding.MyProfileActivityRemoveStatus.backgroundTintList = negativeColor
                isStatusEditEnable = true
                binding.MyProfileActivityStatusValue.showSoftInputOnFocus = true
                binding.MyProfileActivityStatusValue.requestFocus(View.FOCUS_UP)
                if ( MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Status,"String").toString().toUpperCase(Locale.ROOT) == "NULL" ){
                    binding.MyProfileActivityStatusValue.text = null
                    binding.MyProfileActivityStatusValue.hint = "Type your status"
                }
            }

        }

        binding.MyProfileActivityRemoveStatus.setOnClickListener {
            if ( isStatusEditEnable ){
                // Cancel //////////////////////////////////////////////////////////////////////////
                binding.MyProfileActivityStatusValue.isEnabled = false
                binding.MyProfileActivityChangeStatus.text = "change"
                binding.MyProfileActivityChangeStatus.backgroundTintList = naturalColor
                binding.MyProfileActivityRemoveStatus.text = "remove"
                binding.MyProfileActivityRemoveStatus.backgroundTintList = naturalColor
                isStatusEditEnable = false

                binding.MyProfileActivityStatusValue.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Status,"String","No status").toString())
            }else{
                // Remove //////////////////////////////////////////////////////////////////////////
                Volley_ChangeStatus(this, myID, "", object : Volley_ChangeStatus.ServerCallBack{
                    override fun getSuccess(result: JSONObject) {
                        if ( result.getBoolean("isSuccess") ){
                            if ( result.getBoolean("isDelete") ){
                                Toast.makeText(this@MyProfileActivity, "Status successfully removed", Toast.LENGTH_LONG).show()
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity).delete(MySharedPreference.Profile_Status)
                                makeView()
                            }else{
                                Toast.makeText(this@MyProfileActivity, "Status successfully changed", Toast.LENGTH_LONG).show()
                                MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                    .save(MySharedPreference.Profile_Status,binding.MyProfileActivityStatusValue.text.toString())
                                makeView()
                            }
                        }
                    }
                }).send()
            }
        }

        // Info ////////////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityEmailVerify.setOnClickListener {
            startActivity(Intent(this,VerifyEmailActivity::class.java))
        }

        binding.MyProfileActivityEditProfile.setOnClickListener {
            if ( !isProfileEditEnable ){
                // Change Profile //////////////////////////////////////////////////////////////////
                binding.MyProfileActivityCancelEditProfile.visibility = View.VISIBLE
                binding.MyProfileActivityEditProfile.text = "Confirm"
                binding.MyProfileActivityEditProfile.backgroundTintList = positiveColor
                binding.MyProfileActivityCancelEditProfile.backgroundTintList = negativeColor
                binding.MyProfileActivityFullnameValue.isEnabled = true
                binding.MyProfileActivityUsernameValue.isEnabled = true
                binding.MyProfileActivityEmailValue.isEnabled = true
                binding.MyProfileActivityBirthYearValue.isEnabled = true
                isProfileEditEnable = true
                binding.MyProfileActivityFullnameValue.showSoftInputOnFocus = true
                binding.MyProfileActivityFullnameValue.requestFocus(View.FOCUS_UP)
            }else{
                // Confirm /////////////////////////////////////////////////////////////////////////
                var fullNameValid = false
                var usernameValid = false
                var emailValid = false
                var birthYearValid = false

                // FullName validator //////////////////////////////////////////////////////////////
                if ( binding.MyProfileActivityFullnameValue.text.isNullOrEmpty() ){
                    binding.MyProfileActivityFullnameError.visibility = View.VISIBLE
                    binding.MyProfileActivityFullnameError.text = "Too short"
                }else{
                    if ( binding.MyProfileActivityFullnameValue.text.length < MyRules.minFullNameLength ){
                        binding.MyProfileActivityFullnameError.visibility = View.VISIBLE
                        binding.MyProfileActivityFullnameError.text = "Too short"
                    }else{
                        if ( binding.MyProfileActivityFullnameValue.text.length > MyRules.maxFullNameLength ){
                            binding.MyProfileActivityFullnameError.visibility = View.VISIBLE
                            binding.MyProfileActivityFullnameError.text = "Too long"
                        }else{
                            binding.MyProfileActivityFullnameError.visibility = View.GONE
                            fullNameValid = true
                        }
                    }
                }

                // Username validator //////////////////////////////////////////////////////////////
                if ( binding.MyProfileActivityUsernameValue.text.isNullOrEmpty() ){
                    binding.MyProfileActivityUsernameError.visibility = View.VISIBLE
                    binding.MyProfileActivityUsernameError.text = "Too short"
                }else{
                    if ( binding.MyProfileActivityUsernameValue.text.length < MyRules.minUsernameLength ){
                        binding.MyProfileActivityUsernameError.visibility = View.VISIBLE
                        binding.MyProfileActivityUsernameError.text = "Too short"
                    }else{
                        if ( binding.MyProfileActivityUsernameValue.text.length > MyRules.maxUsernameLength ){
                            binding.MyProfileActivityUsernameError.visibility = View.VISIBLE
                            binding.MyProfileActivityUsernameError.text = "Too long"
                        }else{
                            binding.MyProfileActivityUsernameError.visibility = View.GONE
                            usernameValid = true
                        }
                    }
                }

                // Email validator /////////////////////////////////////////////////////////////////
                if ( binding.MyProfileActivityEmailValue.text.isNullOrEmpty() ){
                    binding.MyProfileActivityEmailError.visibility = View.VISIBLE
                    binding.MyProfileActivityEmailVerify.visibility = View.GONE
                    binding.MyProfileActivityEmailError.text = "Enter your email"
                }else{
                    if ( android.util.Patterns.EMAIL_ADDRESS.matcher(binding.MyProfileActivityEmailValue.text).matches() ){
                        binding.MyProfileActivityEmailError.visibility = View.GONE
                        emailValid = true
                    }else{
                        binding.MyProfileActivityEmailError.visibility = View.VISIBLE
                        binding.MyProfileActivityEmailVerify.visibility = View.GONE
                        binding.MyProfileActivityEmailError.text = "Wrong email address"
                    }
                }

                // Birth Year validator ////////////////////////////////////////////////////////////
                if ( binding.MyProfileActivityBirthYearValue.text.isNullOrEmpty() ){
                    binding.MyProfileActivityBirthYearError.visibility = View.VISIBLE
                    binding.MyProfileActivityBirthYearError.text = "Enter your birth year"
                }else{
                    if ( binding.MyProfileActivityBirthYearValue.text.toString().toInt() < 1900 ){
                        binding.MyProfileActivityBirthYearError.visibility = View.VISIBLE
                        binding.MyProfileActivityBirthYearError.text = "Enter valid birth year"
                    }else{
                        if (Calendar.getInstance().get(Calendar.YEAR) - binding.MyProfileActivityBirthYearValue.text.toString().toInt() < MyRules.minAgeForSignUp ) {
                            binding.MyProfileActivityBirthYearError.visibility = View.VISIBLE
                            binding.MyProfileActivityBirthYearError.text = "You must have at least 18 years old"
                        }else{
                            binding.MyProfileActivityBirthYearError.visibility = View.GONE
                            birthYearValid = true
                        }
                    }
                }

                if ( fullNameValid && usernameValid && emailValid && birthYearValid ){
                    Volley_ChangeProfile(this,myID.toString() ,binding.MyProfileActivityFullnameValue.text.toString(), binding.MyProfileActivityUsernameValue.text.toString(),
                        binding.MyProfileActivityEmailValue.text.toString(), binding.MyProfileActivityBirthYearValue.text.toString(), object : Volley_ChangeProfile.ServerCallBack{
                            override fun getSuccess(result: JSONObject) {
                                if (  result.getBoolean("isSuccess")){
                                    binding.MyProfileActivityCancelEditProfile.visibility = View.GONE
                                    binding.MyProfileActivityEditProfile.text = "Change Profile"
                                    binding.MyProfileActivityEditProfile.backgroundTintList = naturalColor
                                    binding.MyProfileActivityCancelEditProfile.backgroundTintList = naturalColor
                                    binding.MyProfileActivityFullnameValue.isEnabled = false
                                    binding.MyProfileActivityUsernameValue.isEnabled = false
                                    binding.MyProfileActivityEmailValue.isEnabled = false
                                    binding.MyProfileActivityBirthYearValue.isEnabled = false
                                    isProfileEditEnable = false
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                        .save(MySharedPreference.Profile_FullName,binding.MyProfileActivityFullnameValue.text.toString())
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                        .save(MySharedPreference.Profile_Username,binding.MyProfileActivityUsernameValue.text.toString())
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                        .save(MySharedPreference.Profile_Email,binding.MyProfileActivityEmailValue.text.toString())
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                        .save(MySharedPreference.Profile_IsEmailVerify,0)
                                    MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity)
                                        .save(MySharedPreference.Profile_BirthYear,binding.MyProfileActivityBirthYearValue.text.toString().toInt())
                                    Toast.makeText(this@MyProfileActivity, "Your profile successfully changed", Toast.LENGTH_LONG).show()
                                    makeView()
                                }else{
                                    if ( result.getBoolean("isUsernameExist") ){
                                        binding.MyProfileActivityUsernameError.visibility = View.VISIBLE
                                        binding.MyProfileActivityUsernameError.text = "This username is exist, select another"
                                    }
                                    if ( result.getBoolean("isEmailExist") ){
                                        binding.MyProfileActivityUsernameError.visibility = View.VISIBLE
                                        binding.MyProfileActivityUsernameError.text = "This email is selected by another"
                                    }
                                }
                            }
                        }).send()
                }

            }
        }

        binding.MyProfileActivityCancelEditProfile.setOnClickListener {
            // Cancel //////////////////////////////////////////////////////////////////////////////
            binding.MyProfileActivityFullnameError.visibility = View.GONE
            binding.MyProfileActivityUsernameError.visibility = View.GONE
            binding.MyProfileActivityEmailError.visibility = View.GONE
            binding.MyProfileActivityBirthYearError.visibility = View.GONE

            binding.MyProfileActivityCancelEditProfile.visibility = View.GONE
            binding.MyProfileActivityEditProfile.text = "Change Profile"
            binding.MyProfileActivityEditProfile.backgroundTintList = naturalColor
            binding.MyProfileActivityCancelEditProfile.backgroundTintList = naturalColor
            binding.MyProfileActivityFullnameValue.isEnabled = false
            binding.MyProfileActivityUsernameValue.isEnabled = false
            binding.MyProfileActivityEmailValue.isEnabled = false
            binding.MyProfileActivityBirthYearValue.isEnabled = false
            isProfileEditEnable = false
            makeView()
        }

        // Password and security ///////////////////////////////////////////////////////////////////
        binding.MyProfileActivityChangePasswordButton.setOnClickListener {
            startActivity(Intent(this,ChangePasswordActivity::class.java))
        }

        // Blocked list ////////////////////////////////////////////////////////////////////////////
        binding.MyProfileActivityBlockedListValue.setOnClickListener {
            startActivity(Intent(this,BlockedListActivity::class.java))
        }
        binding.MyProfileActivityRemoveAllBlockedListButton.setOnClickListener {
            if ( !isRemoveBlockedEnable ){
                // Reedem all blocked users ////////////////////////////////////////////////////////
                binding.MyProfileActivityCancelBlockedListButton.visibility = View.VISIBLE
                binding.MyProfileActivityRemoveAllBlockedListButton.text = "Confirm"
                binding.MyProfileActivityRemoveAllBlockedListButton.backgroundTintList = positiveColor
                binding.MyProfileActivityCancelBlockedListButton.backgroundTintList = negativeColor
                isRemoveBlockedEnable = true
            }else{
                // Confirm /////////////////////////////////////////////////////////////////////////
                Volley_ChangeBlockedUsers(this,"", object : Volley_ChangeBlockedUsers.ServerCallBack{
                    override fun getSuccess(result: JSONObject) {
                        if ( result.getBoolean("isSuccess") ){
                            MySharedPreference(MySharedPreference.PreferenceProfile,this@MyProfileActivity).save(MySharedPreference.Profile_BlockedUsers,"")
                            DB_UserInfos(this@MyProfileActivity).syncBlocksFromSharedAndDB()
                            binding.MyProfileActivityCancelBlockedListButton.visibility = View.GONE
                            binding.MyProfileActivityRemoveAllBlockedListButton.text = "Reedem all blocked users"
                            binding.MyProfileActivityRemoveAllBlockedListButton.backgroundTintList = naturalColor
                            binding.MyProfileActivityCancelBlockedListButton.backgroundTintList = naturalColor
                            isRemoveBlockedEnable = false
                            makeView()
                        }else{
                            Toast.makeText(this@MyProfileActivity, "Something wrong ... try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }).send()

            }
        }

        binding.MyProfileActivityCancelBlockedListButton.setOnClickListener {
            // Cancel //////////////////////////////////////////////////////////////////////////////
            binding.MyProfileActivityCancelBlockedListButton.visibility = View.GONE
            binding.MyProfileActivityRemoveAllBlockedListButton.text = "Reedem all blocked users"
            binding.MyProfileActivityRemoveAllBlockedListButton.backgroundTintList = naturalColor
            binding.MyProfileActivityCancelBlockedListButton.backgroundTintList = naturalColor
            isRemoveBlockedEnable = false
        }
    }

    private fun fetchProfileInfos() {
        val phoneNumber = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_PhoneNumberWithCode,"string").toString()
        // Load infos from server and update view //////////////////////////////////////////////////
        Volley_LoadProfileInfo(this, username, phoneNumber, object : Volley_LoadProfileInfo.ServerCallBack {
            override fun loadSuccess(result: JSONObject) {
                if ( result.getBoolean("isSuccess") ){
                    makeView()
                }
            }
        }).load()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == galleryRequestCode){
                val image : Uri? = data?.data
                val newImage = MyInternalStorage(this).saveProfilePic(image!!,myID)
                uploadProfilePic(newImage[0],newImage[1])
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_ProfilePic, newImage[2])
                makeView()
            }
        }
    }



    private fun uploadProfilePic(path : String , thumbPath : String ){

        MultipartUploadRequest(this,MyServerSide.saveThumbProfilePic_address)
            .setMethod("POST")
            .addFileToUpload(thumbPath,"thumbProfilePic")
            .addParameter("type","THUMB")
            .addParameter("username",username)
            .setNotificationConfig { context, uploadId ->
                thumbNotificationConfig
            }
            .subscribe(this,this, object : RequestObserverDelegate {
                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {  }
                override fun onCompletedWhileNotObserving() {   }
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable ) { println(exception.message) }
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    //println(serverResponse.bodyString)
                }
            })

        MultipartUploadRequest(this,MyServerSide.saveProfilePic_address)
            .setMethod("POST")
            .addFileToUpload(path,"profile")
            .addParameter("username",username)
            .addParameter("type","PIC")
            .setNotificationConfig { context, uploadId ->
                profileNotificationConfig
            }
            .subscribe(this, this, object : RequestObserverDelegate {
                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {    }
                override fun onCompletedWhileNotObserving() {    }
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable ) {  println(exception.message)   }
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {   }
                override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    //println(serverResponse.bodyString)
                    fetchProfileInfos()
                }
            })
    }

    private fun createNotificationConfig() {
        thumbNotificationConfig = UploadNotificationConfig(notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            )
        )

        profileNotificationConfig = UploadNotificationConfig(notificationChannelID,
            false,
            // Progress ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture is uploading ...",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Success /////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Upload complete" ,
                "Your new profile picture uploaded successfully",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            ),
            // Error ///////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading profile picture" ,
                "Your new profile picture isn't uploading ... try again",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                false,
                null
            ),
            // Canceled ////////////////////////////////////////////////////////////////////////////
            UploadNotificationStatusConfig("Uploading canceled" ,
                "Your new profile picture uploading is canceled by you ",
                R.drawable.nearoom_icon2,
                resources.getColor(R.color.colorPrimary),
                null,
                null,
                ArrayList(3),
                true,
                true,
                null
            )
        )
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel( notificationChannelID,"Upload Profile", NotificationManager.IMPORTANCE_NONE )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val targetIntent = Intent(this  ,MainActivity::class.java)
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP )
        startActivity(targetIntent)
        finish()
    }

    companion object {
        const val notificationChannelID = "UploadProfile"
    }
}
