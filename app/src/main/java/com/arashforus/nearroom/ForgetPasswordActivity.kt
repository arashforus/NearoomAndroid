package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.text.trimmedLength
import com.arashforus.nearroom.databinding.ActivityForgetPasswordBinding

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    var redColor = 0
    var greenColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        binding.ForgetPasswordActivityResult.visibility = View.INVISIBLE
        redColor = resources.getColor(R.color.buttonNegetive)
        greenColor = resources.getColor(R.color.buttonPositive)

        makeListeners()

    }

    private fun makeListeners() {
        // Listener for phoneNumber input for disable username input ///////////////////////////////
        binding.ForgetPasswordActivityPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ForgetPasswordActivityUsername.isEnabled =
                    binding.ForgetPasswordActivityPhoneNumber.length() == 0
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // Listener for username input for disable phoneNumber input ///////////////////////////////
        binding.ForgetPasswordActivityUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ForgetPasswordActivityPhoneNumber.isEnabled =
                    binding.ForgetPasswordActivityUsername.length() == 0
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })


        // Listener for Reset button ///////////////////////////////////////////////////////////////
        binding.ForgetPasswordActivityResetButton.setOnClickListener {
            if ( binding.ForgetPasswordActivityUsername.isEnabled ){
                if ( binding.ForgetPasswordActivityUsername.text.trimmedLength() > 5  ){
                    Volley_CheckAndSendResetPasswordLink(this, "", binding.ForgetPasswordActivityUsername.text.toString(), object : Volley_CheckAndSendResetPasswordLink.ServerCallBack {
                            override fun resultFailed(result: String) {
                                binding.ForgetPasswordActivityResult.text = result
                                binding.ForgetPasswordActivityResult.setTextColor(redColor)
                                binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                            }

                            override fun resultSuccess(result: String) {
                                binding.ForgetPasswordActivityResult.text = result
                                binding.ForgetPasswordActivityResult.setTextColor(greenColor)
                                binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                            }
                        }).check()
                }else{
                    binding.ForgetPasswordActivityResult.text = "Enter complete username"
                    binding.ForgetPasswordActivityResult.setTextColor(redColor)
                    binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                }
            }
            if ( binding.ForgetPasswordActivityPhoneNumber.isEnabled ){
                if ( binding.ForgetPasswordActivityPhoneNumber.text.trimmedLength() > 5  ){
                    Volley_CheckAndSendResetPasswordLink(this, binding.ForgetPasswordActivityPhoneNumber.text.toString(), "", object : Volley_CheckAndSendResetPasswordLink.ServerCallBack {
                        override fun resultFailed(result: String) {
                            binding.ForgetPasswordActivityResult.text = result
                            binding.ForgetPasswordActivityResult.setTextColor(redColor)
                            binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                        }

                        override fun resultSuccess(result: String) {
                            binding.ForgetPasswordActivityResult.text = result
                            binding.ForgetPasswordActivityResult.setTextColor(greenColor)
                            binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                        }
                    }).check()
                }else{
                    binding.ForgetPasswordActivityResult.text = "Enter complete phone number"
                    binding.ForgetPasswordActivityResult.setTextColor(redColor)
                    binding.ForgetPasswordActivityResult.visibility = View.VISIBLE
                }
            }


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.zoom_out_enter,R.anim.zoom_out_exit)
    }
}

