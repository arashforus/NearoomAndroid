package com.arashforus.nearroom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.trimmedLength
import com.arashforus.nearroom.databinding.ActivityRegisterNumberBinding
import org.json.JSONObject

class RegisterNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterNumberBinding

    private val SEND_SMS_PERMISSION_CODE = 100
    private val RECEIVE_SMS_PERMISSION_CODE = 101
    var enableCountryListener : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterNumberBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        // Permissions /////////////////////////////////////////////////////////////////////////////
        checkPermission(Manifest.permission.SEND_SMS,SEND_SMS_PERMISSION_CODE)
        checkPermission(Manifest.permission.RECEIVE_SMS,RECEIVE_SMS_PERMISSION_CODE)

        binding.RegisterNumberActivityCountryCode.setText(binding.RegisterNumberActivityCountrySelect.selectedCountryCodeWithPlus)

        makeListeners()
        setupCountrySelect()

    }

    private fun setupCountrySelect() {
        binding.RegisterNumberActivityCountrySelect.registerCarrierNumberEditText(binding.RegisterNumberActivityPhonenumber)
        binding.RegisterNumberActivityCountrySelect.setDetectCountryWithAreaCode(true)
        binding.RegisterNumberActivityCountrySelect.setOnCountryChangeListener {
            if (enableCountryListener) {
                binding.RegisterNumberActivityCountryCode.setText(binding.RegisterNumberActivityCountrySelect.selectedCountryCodeWithPlus)
            }
        }
    }

    private fun makeListeners() {
        // Send Code button listener ///////////////////////////////////////////////////////////////
        binding.RegisterNumberActivitySendCodeBtn.setOnClickListener{
            var phoneNumber = binding.RegisterNumberActivityPhonenumber.text.toString().filter { it.isDigit() }
            if ( phoneNumber.trimmedLength() > 0 ){
                if ( phoneNumber[0].equals('0',false) ) {
                    phoneNumber = phoneNumber.drop(1)
                    binding.RegisterNumberActivityPhonenumber.setText(phoneNumber)
                }
            }

            if (phoneNumber.length in 4..13 && binding.RegisterNumberActivityCountrySelect.isValidFullNumber ){

                val phoneNumberWithCodeFormatted = binding.RegisterNumberActivityCountryCode.text.toString() +" "+ binding.RegisterNumberActivityPhonenumber.text.toString()
                val phoneNumberWithCode = "+"+phoneNumberWithCodeFormatted.filter { it.isDigit() }

                Volley_SendVerificationCode(this, phoneNumberWithCode, object : Volley_SendVerificationCode.ServerCallBack {
                    override fun sendSuccess(result: JSONObject) {
                        if (result.getBoolean("isSuccess") ){
                            if ( !result.isNull("isExist") ){
                                MyTools(this@RegisterNumberActivity).closeKeyboard()
                                AlertDialog.Builder(this@RegisterNumberActivity)
                                    .setTitle("You have an account")
                                    .setMessage("This phone number registered before , You can login to your account or if you forget your password you can reset your password")
                                    .setPositiveButton("Login" ) { dialog, _ ->
                                        onBackPressed()
                                        dialog.cancel()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Reset password" ) { dialog, _ ->
                                        dialog.cancel()
                                        dialog.dismiss()
                                        startActivity(Intent(this@RegisterNumberActivity,ForgetPasswordActivity::class.java))
                                        overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                                    }
                                    .setCancelable(true)
                                    .create()
                                    .show()
                            }else{
                                if ( !result.isNull("isExceedAttempt") ){
                                    MyTools(this@RegisterNumberActivity).closeKeyboard()
                                    AlertDialog.Builder(this@RegisterNumberActivity)
                                        .setTitle("Reach exceed attempt")
                                        .setMessage("5 attempt send verification code to your number is reached for today , you can try again tomorrow")
                                        .setPositiveButton("OK" ) { dialog, _ ->
                                            dialog.cancel()
                                            dialog.dismiss()
                                        }
                                        .setCancelable(true)
                                        .create()
                                        .show()
                                }else{
                                    MySharedPreference(MySharedPreference.PreferenceTempProfile,this@RegisterNumberActivity).save(MySharedPreference.TempProfile_PhoneNumberWithCodeFormatted,phoneNumberWithCodeFormatted)
                                    MySharedPreference(MySharedPreference.PreferenceTempProfile,this@RegisterNumberActivity).save(MySharedPreference.TempProfile_PhoneNumberWithCode,phoneNumberWithCode)
                                    MySharedPreference(MySharedPreference.PreferenceTempProfile,this@RegisterNumberActivity).save(MySharedPreference.TempProfile_PhoneNumber,phoneNumber)
                                    MySharedPreference(MySharedPreference.PreferenceTempProfile,this@RegisterNumberActivity).save(MySharedPreference.TempProfile_Country,binding.RegisterNumberActivityCountrySelect.selectedCountryEnglishName)
                                    MySharedPreference(MySharedPreference.PreferenceTempProfile,this@RegisterNumberActivity).save(MySharedPreference.TempProfile_CountryFlag,binding.RegisterNumberActivityCountrySelect.selectedCountryFlagResourceId)
                                    startActivity(Intent(this@RegisterNumberActivity,ReceiveCodeActivity::class.java))
                                    overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                                }

                            }
                        }else{
                            Toast.makeText(this@RegisterNumberActivity, "Something wrong", Toast.LENGTH_LONG).show()
                        }
                    }
                }).check()

            }else{
                Toast.makeText(this, "Enter correct number", Toast.LENGTH_LONG).show()
            }

        }

        // Country code listener and update country ////////////////////////////////////////////////
        binding.RegisterNumberActivityCountryCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  return  }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( before != count ) {
                    enableCountryListener = false
                    val enteredCode = s.toString().filter { it.isDigit() }
                    if ( enteredCode != "" ) {
                        binding.RegisterNumberActivityCountrySelect.setCountryForPhoneCode(enteredCode.toInt())
                    }
                    enableCountryListener = true
                }
            }
            override fun afterTextChanged(s: Editable?) {  return  }
        })

    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray ) {
        super .onRequestPermissionsResult(requestCode,permissions,grantResults)
        if (requestCode == SEND_SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(this,"Send SMS Permission Granted",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Send SMS Permission Denied",Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == RECEIVE_SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(this,"Receive SMS Permission Granted",Toast.LENGTH_SHORT ).show()
            } else {
                Toast.makeText(this,"Receive SMS Permission Denied",Toast.LENGTH_SHORT ).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.zoom_out_enter,R.anim.zoom_out_exit)
    }


}