package com.arashforus.nearroom

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.arashforus.nearroom.databinding.ActivityReceiveCodeBinding
import com.mukesh.OnOtpCompletionListener
import org.json.JSONObject

open class ReceiveCodeActivity : AppCompatActivity() , SMSReceiver.ICallback {

    private lateinit var binding : ActivityReceiveCodeBinding

    var phoneNumberWithCodeFormatted = ""
    private var phoneNumberWithCode = ""

    private val receiver : SMSReceiver = SMSReceiver(object : SMSReceiver.ICallback {
        override fun updateUI(value: String) {
            binding.ReceiveCodeActivityCode.setText(value.filter { it.isDigit() })
        }
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveCodeBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        phoneNumberWithCodeFormatted = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_PhoneNumberWithCodeFormatted,"string").toString()
        phoneNumberWithCode = MySharedPreference(MySharedPreference.PreferenceTempProfile,this).load(MySharedPreference.TempProfile_PhoneNumberWithCode,"string").toString()

        makeView()
        makeListeners()

    }

    private fun makeView() {
        binding.ReceiveCodeActivityResult.visibility = View.INVISIBLE
        binding.ReceiveCodeActivityNextButton.isEnabled = false
        binding.ReceiveCodeActivityDescriptionText.text =  "Your unique verification code send to \n$phoneNumberWithCodeFormatted"
        //binding.ReceiveCodeActivityTimer.visibility = View.INVISIBLE
        //binding.ReceiveCodeActivitySendAgain.visibility = View.INVISIBLE

        binding.ReceiveCodeActivityTimer.visibility = View.VISIBLE
        binding.ReceiveCodeActivitySendAgain.visibility = View.VISIBLE
        binding.ReceiveCodeActivitySendAgain.isEnabled = false
        object : CountDownTimer(90000,1000) {
            override fun onTick(secondsRemaining: Long) {
                if ( secondsRemaining >= 60000 ){
                    val sec = ( secondsRemaining - 60000 ) / 1000
                    if ( sec >= 10 ){
                        binding.ReceiveCodeActivityTimer.text = "01:$sec"
                    }else{
                        binding.ReceiveCodeActivityTimer.text = "01:0$sec"
                    }
                }else{
                    val sec = secondsRemaining  / 1000
                    if ( sec >= 10 ){
                        binding.ReceiveCodeActivityTimer.text = "00:$sec"
                    }else{
                        binding.ReceiveCodeActivityTimer.text = "00:0$sec"
                    }
                }
            }
            override fun onFinish() {
                binding.ReceiveCodeActivityTimer.visibility = View.INVISIBLE
                binding.ReceiveCodeActivitySendAgain.isEnabled = true
            }
        }.start()

    }

    private fun makeListeners() {
        binding.ReceiveCodeActivityChangeNumberTextView.setOnClickListener{
            onBackPressed()
        }

        binding.ReceiveCodeActivitySendAgain.setOnClickListener{
            Volley_SendVerificationCode(this, phoneNumberWithCode, object : Volley_SendVerificationCode.ServerCallBack {
                override fun sendSuccess(result: JSONObject) {
                    if (result.getBoolean("isSuccess") ){
                        if ( !result.isNull("isExist") ){
                            MyTools(this@ReceiveCodeActivity).closeKeyboard()
                            AlertDialog.Builder(this@ReceiveCodeActivity)
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
                                    startActivity(Intent(this@ReceiveCodeActivity,ForgetPasswordActivity::class.java))
                                    overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                                }
                                .setCancelable(true)
                                .create()
                                .show()
                        }else{
                            if ( !result.isNull("isExceedAttempt") ){
                                MyTools(this@ReceiveCodeActivity).closeKeyboard()
                                AlertDialog.Builder(this@ReceiveCodeActivity)
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
                                Toast.makeText(this@ReceiveCodeActivity, "Sending code is successFull", Toast.LENGTH_SHORT).show()
                                binding.ReceiveCodeActivityTimer.visibility = View.VISIBLE
                                binding.ReceiveCodeActivitySendAgain.visibility = View.VISIBLE
                                binding.ReceiveCodeActivitySendAgain.isEnabled = false
                                object : CountDownTimer(90000,1000) {
                                    override fun onTick(secondsRemaining: Long) {
                                        if ( secondsRemaining >= 60000 ){
                                            val sec = ( secondsRemaining - 60000 ) / 1000
                                            if ( sec >= 10 ){
                                                binding.ReceiveCodeActivityTimer.text = "01:$sec"
                                            }else{
                                                binding.ReceiveCodeActivityTimer.text = "01:0$sec"
                                            }
                                        }else{
                                            val sec = secondsRemaining  / 1000
                                            if ( sec >= 10 ){
                                                binding.ReceiveCodeActivityTimer.text = "00:$sec"
                                            }else{
                                                binding.ReceiveCodeActivityTimer.text = "00:0$sec"
                                            }
                                        }
                                    }
                                    override fun onFinish() {
                                        binding.ReceiveCodeActivityTimer.visibility = View.INVISIBLE
                                        binding.ReceiveCodeActivitySendAgain.isEnabled = true
                                    }
                                }.start()
                            }

                        }
                    }else{
                        Toast.makeText(this@ReceiveCodeActivity, "Something wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }).check()

        }

        binding.ReceiveCodeActivityNextButton.setOnClickListener{
            Volley_CheckVerificationCode(this, phoneNumberWithCode, binding.ReceiveCodeActivityCode.text.toString(), object : Volley_CheckVerificationCode.ServerCallBack {
                    override fun isCorrect(result: JSONObject) {
                        if (result.getBoolean("isSuccess") ) {
                            binding.ReceiveCodeActivityResult.text = "Wait for log in ..."
                            binding.ReceiveCodeActivityResult.setTextColor(Color.GREEN)
                            binding.ReceiveCodeActivityResult.visibility = View.VISIBLE
                            val intent = Intent(this@ReceiveCodeActivity, SignupActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.zoom_in_enter,R.anim.zoom_in_exit)
                        } else {
                            binding.ReceiveCodeActivityResult.text = "Incorrect verification code , try again ..."
                            binding.ReceiveCodeActivityResult.visibility = View.VISIBLE
                        }
                    }
                }).check()
        }


        binding.ReceiveCodeActivityCode.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String?) {  return  }
        })

        binding.ReceiveCodeActivityCode.doAfterTextChanged { text: Editable? ->
            binding.ReceiveCodeActivityNextButton.isEnabled = text?.length == 6
        }

    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    override fun updateUI(value: String) {
        binding.ReceiveCodeActivityDescriptionText.text = value
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.zoom_out_enter,R.anim.zoom_out_exit)
    }


}
