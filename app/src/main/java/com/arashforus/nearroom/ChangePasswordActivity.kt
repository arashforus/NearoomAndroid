package com.arashforus.nearroom

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.arashforus.nearroom.databinding.ActivityChangePasswordBinding
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityChangePasswordBinding
    var myId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyTools(this).resizableWindow(this)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myId = MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_ID,"Int") as Int

        initToolbar()
        invisibleHints()
        makeListener()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.ChangePasswordActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Change Password"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun invisibleHints() {
        binding.ChangePasswordActivityResult.visibility = View.GONE
        binding.ChangePasswordActivityCurrentPasswordError.visibility = View.GONE
        binding.ChangePasswordActivityRetypePasswordError.visibility = View.GONE
    }

    private fun makeListener() {
        binding.ChangePasswordActivityNewPasswordValue.addTextChangedListener(object :  TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {      }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length < MyRules.minPasswordLength){
                        binding.ChangePasswordActivityPasswordStrengthValue.setText("Password should be more than ${MyRules.minPasswordLength} characters")
                        binding.ChangePasswordActivityPasswordStrengthValue.setTextColor(Color.WHITE)
                    }else{
                        if ( s.length > MyRules.maxPasswordLength ){
                            binding.ChangePasswordActivityPasswordStrengthValue.setText("Password should be less than ${MyRules.maxPasswordLength} characters")
                            binding.ChangePasswordActivityPasswordStrengthValue.setTextColor(Color.WHITE)
                        }else{
                            val strength = MyTools(this@ChangePasswordActivity).measurePasswordStrength(s.toString())
                            binding.ChangePasswordActivityPasswordStrengthValue.setText(strength[0].toString())
                            binding.ChangePasswordActivityPasswordStrengthValue.setTextColor(resources.getColor(Integer.parseInt(strength[1].toString())))
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {     }
        })

        binding.ChangePasswordActivityChangePasswordButton.setOnClickListener {
            invisibleHints()
            val currentPass = binding.ChangePasswordActivityCurrentPasswordValue.text.toString()
            val newPass = binding.ChangePasswordActivityNewPasswordValue.text.toString()
            val retypePass = binding.ChangePasswordActivityRetypePasswordValue.text.toString()
            if ( newPass.length in MyRules.minPasswordLength until MyRules.maxPasswordLength ){
                if ( newPass == retypePass){
                    if (currentPass == MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Password,"String")){
                        Volley_ChangePassword(this,myId , currentPass, newPass, object : Volley_ChangePassword.ServerCallBack {
                            override fun getSuccess(result: JSONObject) {
                                binding.ChangePasswordActivityResult.visibility = View.VISIBLE
                                binding.ChangePasswordActivityResult.text = "Your new password successfully changed"
                                binding.ChangePasswordActivityResult.setTextColor(resources.getColor(R.color.buttonPositive))
                            }
                            override fun getError() {
                                binding.ChangePasswordActivityResult.visibility = View.VISIBLE
                                binding.ChangePasswordActivityResult.text = "Something wrong , Try again"
                                binding.ChangePasswordActivityResult.setTextColor(resources.getColor(R.color.buttonNegetive))
                            }
                        }).check()
                    }else{
                        binding.ChangePasswordActivityCurrentPasswordError.visibility = View.VISIBLE
                    }
                }else{
                    binding.ChangePasswordActivityRetypePasswordError.visibility = View.VISIBLE
                }
            }else{
                Toast.makeText(this, "Enter new password completely", Toast.LENGTH_SHORT).show()
            }

        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}