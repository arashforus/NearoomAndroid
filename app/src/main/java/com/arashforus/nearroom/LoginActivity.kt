package com.arashforus.nearroom

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.arashforus.nearroom.databinding.ActivityLoginBinding
import com.skydoves.transformationlayout.onTransformationStartContainer
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    var usernamevalid = false
    var passwordvalid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        binding.LoginActivitySigninButton.isEnabled = false

        makeListeners()

    }

    private fun makeListeners() {
        // Join us listener  ///////////////////////////////////////////////////////////////////////
        binding.LoginActivityJoinus.setOnClickListener {
            startActivity(Intent(this, RegisterNumberActivity::class.java))
            overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
        }

        // Forget password listener  ///////////////////////////////////////////////////////////////
        binding.LoginActivityForgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
            overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
        }

        // Sign in button listener  ////////////////////////////////////////////////////////////////
        binding.LoginActivitySigninButton.setOnClickListener {
            var username  = ""
            var phoneNumber  = ""
            val password = binding.LoginActivityPassword.text.toString()
            val usernameOrPhoneNumber = binding.LoginActivityUsername.text.toString().trim()
            //usernameOrPhoneNumber[0].isDigit()
            if ( usernameOrPhoneNumber.isDigitsOnly() || usernameOrPhoneNumber[0] == '+' ) {
                phoneNumber = MyTools(this).addPlusToNumber(usernameOrPhoneNumber)
            }else{
                username = usernameOrPhoneNumber
            }
            Volley_Signin(this, username,phoneNumber,password, object : Volley_Signin.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    if ( result.getBoolean("isSuccess") ){
                        if ( result.getBoolean("isCorrect") ){
                            Toast.makeText(this@LoginActivity,"Password correct", Toast.LENGTH_SHORT).show()
                            Volley_LoadProfileInfo(this@LoginActivity, username, phoneNumber, object : Volley_LoadProfileInfo.ServerCallBack {
                                override fun loadSuccess(result: JSONObject) {
                                    if (result.get("isSuccess") as Boolean){
                                        MySharedPreference(MySharedPreference.PreferenceApp, this@LoginActivity).save(MySharedPreference.App_Login, true)
                                        MySharedPreference(MySharedPreference.PreferenceApp, this@LoginActivity).save(MySharedPreference.App_FirstTimeInit, false)
                                        MySharedPreference(MySharedPreference.PreferenceApp, this@LoginActivity).save(MySharedPreference.App_LastMessageIdGet, 0)
                                        MySharedPreference(MySharedPreference.PreferenceApp, this@LoginActivity).save(MySharedPreference.App_LastNearoomMessageIdGet, 0)
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                                        finish()
                                    }else{
                                        Toast.makeText(this@LoginActivity,"Loading Failed ... try again", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }).load()
                        }else{
                            Toast.makeText(this@LoginActivity,"Password incorrect", Toast.LENGTH_SHORT ).show()
                        }
                    }
                }
            }).check()

        }

        // username or phone number change text listener  //////////////////////////////////////////
        binding.LoginActivityUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ){
                    if ( s.length >= 6 ){
                        // input has more than 6 characters ////////////////////////////////////////////
                        binding.LoginActivityUsernameLayout.error = null
                        usernamevalid = true
                    }else{
                        // input has less than 6 characters ////////////////////////////////////////////
                        if ( s.isDigitsOnly() || s[0] == '+') {
                            binding.LoginActivityUsernameLayout.error = "Enter your number with country code"
                            usernamevalid = false
                        }else {
                            binding.LoginActivityUsernameLayout.error = "Enter your correct username"
                            usernamevalid = false
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
                binding.LoginActivitySigninButton.isEnabled = usernamevalid && passwordvalid
            }

        })

        // Password change text listener  //////////////////////////////////////////////////////////
        binding.LoginActivityPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s != null ){
                    if ( s.length >= 6 ){
                        binding.LoginActivityPasswordLayout.error = null
                        passwordvalid = true
                    }else{
                        binding.LoginActivityPasswordLayout.error = "Enter your password complete"
                        passwordvalid = false
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
                binding.LoginActivitySigninButton.isEnabled = usernamevalid && passwordvalid
            }

        })
    }


}