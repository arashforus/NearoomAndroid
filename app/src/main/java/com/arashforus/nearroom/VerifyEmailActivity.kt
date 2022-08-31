package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.arashforus.nearroom.databinding.ActivityVerifyEmailBinding
import org.json.JSONObject

class VerifyEmailActivity : AppCompatActivity() {

    lateinit var binding : ActivityVerifyEmailBinding

    var email : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Email,"String","") as String

        initToolbar()
        makeView()
        makeListeners()
    }

    private fun initToolbar(){
        setSupportActionBar(binding.VerifyEmailActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Verify email"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        binding.VerifyEmailActivityEmailText.text = email
        binding.VerifyEmailActivityStatus.visibility = View.GONE
    }

    private fun makeListeners() {
        binding.VerifyEmailActivityCode.setOtpCompletionListener {
            Volley_CheckEmailVerification(this,it, object : Volley_CheckEmailVerification.ServerCallBack{
                override fun getSuccess(result: JSONObject) {
                    if ( result.getBoolean("isSuccess") ){
                        binding.VerifyEmailActivitySendButton.isEnabled = false
                        binding.VerifyEmailActivityCode.setAnimationEnable(true)
                        binding.VerifyEmailActivityCode.isEnabled = false
                        binding.VerifyEmailActivityStatus.visibility = View.VISIBLE
                        binding.VerifyEmailActivityStatus.text = "Your email is verified successfully"
                        binding.VerifyEmailActivityStatus.setTextColor(resources.getColor(R.color.buttonPositive))
                        MySharedPreference(MySharedPreference.PreferenceProfile,this@VerifyEmailActivity).save(MySharedPreference.Profile_IsEmailVerify,1)

                    }else{
                        binding.VerifyEmailActivityStatus.visibility = View.VISIBLE
                        binding.VerifyEmailActivityStatus.text = "Wrong verification code"
                        binding.VerifyEmailActivityStatus.setTextColor(resources.getColor(R.color.buttonNegetive))
                    }
                }

            }).send()
        }

        binding.VerifyEmailActivitySendButton.setOnClickListener {
            Volley_SendEmailVerification(this,object : Volley_SendEmailVerification.ServerCallBack{
                override fun getSuccess(result: JSONObject) {
                    if ( result.getBoolean("isSuccess") ){
                        binding.VerifyEmailActivityStatus.visibility = View.VISIBLE
                        binding.VerifyEmailActivityStatus.text = "Verification email sending successfully"
                        binding.VerifyEmailActivityStatus.setTextColor(resources.getColor(R.color.buttonPositive))
                    }else{
                        binding.VerifyEmailActivityStatus.visibility = View.VISIBLE
                        binding.VerifyEmailActivityStatus.text = "Verification email sending unsuccessfully"
                        binding.VerifyEmailActivityStatus.setTextColor(resources.getColor(R.color.buttonNegetive))
                    }
                }

            }).send()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}