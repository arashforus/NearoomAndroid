package com.arashforus.nearroom

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arashforus.nearroom.databinding.ActivitySignupBinding
import org.json.JSONObject
import java.util.*


class SignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignupBinding

    var usernamevalid = false
    var fullnamevalid = false
    var emailvalid = false
    var birthyearvalid = false
    var passwordvalid = false
    var retypepasswordvalid = false
    private var termsvalid = false

    var phoneNumber = ""
    private var phoneNumberWithCode = ""
    private var phoneNumberWithCodeFormatted = ""
    private var country = ""
    private var countryFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        phoneNumber = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_PhoneNumber,"string") as String
        phoneNumberWithCode = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_PhoneNumberWithCode,"string") as String
        phoneNumberWithCodeFormatted = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_PhoneNumberWithCodeFormatted,"string") as String
        country = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_Country,"string") as String
        countryFlag = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_CountryFlag,"int") as Int

        binding.SignupPhoneNumberEditText.setText(phoneNumberWithCode)
        binding.SignupCountry.setText(country)
        binding.SignupCountryFlag.setImageResource(countryFlag)

        makeValidators()
        makeListeners()

    }

    private fun makeListeners() {
        // SignUp Button Listener  /////////////////////////////////////////////////////////////////
        binding.SignupActivitySignupButton.setOnClickListener {
            if (usernamevalid && fullnamevalid && emailvalid && birthyearvalid && passwordvalid && retypepasswordvalid && termsvalid ){

                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_Username,binding.SignupActivityUsername.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_FullName,binding.SignupActivityFullName.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_Email,binding.SignupActivityEmail.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_PhoneNumber,phoneNumber)
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_PhoneNumberWithCode,binding.SignupPhoneNumberEditText.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_PhoneNumberWithCodeFormatted,phoneNumberWithCodeFormatted)
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_BirthYear,binding.SignupActivityBirthyear.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_Country,binding.SignupCountry.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_CountryFlag,countryFlag.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_Password,binding.SignupActivityPassword.text.toString())
                MySharedPreference(MySharedPreference.PreferenceProfile,this).save(MySharedPreference.Profile_TermsAccepted,termsvalid)

                Volley_SignUpToDB(this, object : Volley_SignUpToDB.ServerCallBack {
                    override fun saveSuccess(result: JSONObject) {
                        if (result.getBoolean("isSuccess") ) {
                            Toast.makeText(this@SignupActivity,"Welcome to NEAROOM", Toast.LENGTH_SHORT).show()
                            MySharedPreference(MySharedPreference.PreferenceApp,this@SignupActivity).save(MySharedPreference.App_Login,true)
                            MySharedPreference(MySharedPreference.PreferenceApp,this@SignupActivity).save(MySharedPreference.App_FirstTimeInit,false)
                            MySharedPreference(MySharedPreference.PreferenceApp,this@SignupActivity).save(MySharedPreference.App_LastMessageIdGet,0)
                            MySharedPreference(MySharedPreference.PreferenceApp,this@SignupActivity).save(MySharedPreference.App_LastNearoomMessageIdGet,0)
                            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                            overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                        } else {
                            Toast.makeText(this@SignupActivity,"save unsuccessfully , Try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }).save()
            }else{
                Toast.makeText(this, "Please enter valid information", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeValidators() {
        // Username Validator  /////////////////////////////////////////////////////////////////////
        binding.SignupActivityUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null) {
                    if (s.length < MyRules.minUsernameLength ) {
                        binding.SignupActivityUsernameLayout.error = "Username length must be more than ${MyRules.minUsernameLength} characters"
                        usernamevalid = false
                    } else {
                        if ( s.length > MyRules.maxUsernameLength ){
                            binding.SignupActivityUsernameLayout.error = "Username length must be less than ${MyRules.maxUsernameLength} characters"
                            usernamevalid = false
                        }else{
                            binding.SignupActivityUsernameLayout.error = null
                            usernamevalid = true
                            Volley_CheckUsernameAvailable(this@SignupActivity, s.toString(), object : Volley_CheckUsernameAvailable.ServerCallBack {
                                override fun isUsernameAvailable(result: JSONObject) {
                                    if ( result.getBoolean("isSuccess")) {
                                        binding.SignupActivityUsernameLayout.error = "This username is exist"
                                        usernamevalid = false
                                    }else{
                                        binding.SignupActivityUsernameLayout.error = null
                                        usernamevalid = true
                                    }
                                }

                            }).check()
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // FullName Validator  /////////////////////////////////////////////////////////////////////
        binding.SignupActivityFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ) {
                    if (s.length < 6) {
                        binding.SignupActivityFullnameLayout.error = "FullName must be more than ${MyRules.minFullNameLength} characters"
                        fullnamevalid = false
                    }else{
                        if ( s.length > MyRules.maxFullNameLength ){
                            binding.SignupActivityFullnameLayout.error = "FullName must be less than ${MyRules.maxFullNameLength} characters"
                            fullnamevalid = false
                        }else{
                            binding.SignupActivityFullnameLayout.error = null
                            fullnamevalid = true
                        }

                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // Email Validator /////////////////////////////////////////////////////////////////////////
        binding.SignupActivityEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ) {
                    if ( android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches() ) {
                        binding.SignupActivityEmailLayout.error = null
                        emailvalid = true
                    }else{
                        binding.SignupActivityEmailLayout.error = "Email address format is wrong"
                        emailvalid = false
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // BirthYear Validator  ////////////////////////////////////////////////////////////////////
        binding.SignupActivityBirthyear.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ) {
                    if ( s.toString().toInt() < 1900 ) {
                        binding.SignupActivityBirthyearLayout.error = "Enter valid birth year"
                        birthyearvalid = false
                    }else{
                        if (Calendar.getInstance().get(Calendar.YEAR) - s.toString().toInt() < MyRules.minAgeForSignUp ) {
                            binding.SignupActivityBirthyearLayout.error = "You must have at least 18 years old to use this app"
                            birthyearvalid = false
                        }else{
                            binding.SignupActivityBirthyearLayout.error = null
                            birthyearvalid = true
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // Password Validator  /////////////////////////////////////////////////////////////////////
        binding.SignupActivityPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ) {
                    if ( s.length < MyRules.minPasswordLength ) {
                        binding.SignupActivityPasswordLayout.error = "Password is too short"
                        passwordvalid = false
                    }else{
                        if ( s.length > MyRules.maxPasswordLength )  {
                            binding.SignupActivityPasswordLayout.error = "Password is too long"
                            passwordvalid = false
                        }else{
                            binding.SignupActivityPasswordLayout.error = null
                            passwordvalid = true
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // Retype Password Validator  //////////////////////////////////////////////////////////////
        binding.SignupActivityRetypePassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ) {
                    if ( s.toString() == binding.SignupActivityPassword.text.toString() ) {
                        binding.SignupActivityRetypePasswordLayout.error = null
                        retypepasswordvalid = true
                    }else{
                        binding.SignupActivityRetypePasswordLayout.error = "Two passwords doesn't match"
                        retypepasswordvalid = false
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

        // Check terms Listener  ///////////////////////////////////////////////////////////////////
        binding.SignupActivityCheckTerms.setOnCheckedChangeListener{ _, isChecked ->
            termsvalid = isChecked
        }

        binding.SignupActivityCheckTerms.setOnClickListener {
            if (termsvalid){
                startActivity(Intent(this, TermsOfUseActivity::class.java))
                overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.zoom_out_enter,R.anim.zoom_out_exit)
    }

}