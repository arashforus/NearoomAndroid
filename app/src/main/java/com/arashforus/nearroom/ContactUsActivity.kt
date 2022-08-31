package com.arashforus.nearroom

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.arashforus.nearroom.databinding.ActivityContactUsBinding
import org.json.JSONObject
import java.util.*

class ContactUsActivity : AppCompatActivity() {

    lateinit var binding : ActivityContactUsBinding
    var purpose = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        MyTools(this).resizableWindow(this)
        setContentView(binding.root)

        if ( intent.hasExtra("purpose") ){
            purpose = intent.getStringExtra("purpose")!!
        }

        initToolbar()
        loadParameters()
        makeButtonListeners()

    }

    private fun initToolbar(){
        setSupportActionBar(binding.ContactUsActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Contact Us"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    //private val generalPos = 0
    private val sponsorshipPos = 1
    private val reportUsernamePos = 2
    private val reportRoomPos = 3
    //private val reportBugPos = 4
    //private val suggestionPos = 5

    private fun loadParameters() {
        binding.ContactUsActivityFullNameText.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_FullName,"String").toString())
        binding.ContactUsActivityPhoneNumberText.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_PhoneNumberWithCode,"String").toString())
        binding.ContactUsActivityEmailText.setText(MySharedPreference(MySharedPreference.PreferenceProfile,this).load(MySharedPreference.Profile_Email,"String").toString())
        when ( purpose.toUpperCase(Locale.ROOT).trim() ){
            "SPONSORSHIP" -> binding.ContactUsActivitySubjectSpinner.setSelection(sponsorshipPos)
            "REPORTUSERNAME" -> binding.ContactUsActivitySubjectSpinner.setSelection(reportUsernamePos)
            "REPORTROOM" -> binding.ContactUsActivitySubjectSpinner.setSelection(reportRoomPos)
        }
    }

    private fun makeButtonListeners() {
        binding.ContactUsActivitySend.setOnClickListener {
            val fullName = binding.ContactUsActivityFullNameText.text.toString()
            val phoneNumber = binding.ContactUsActivityPhoneNumberText.text.toString()
            val email = binding.ContactUsActivityEmailText.text.toString()
            val subject = binding.ContactUsActivitySubjectSpinner.selectedItem.toString()
            val message = binding.ContactUsActivityMessageText.text.toString()
            val reportUserId = intent.getIntExtra("reportUserId",0)
            val reportRoomId = intent.getIntExtra("reportRoomId",0)

            println("reportUserId = $reportUserId And reportRoomId = $reportRoomId")
            if ( fullName.isNotEmpty() && subject.isNotEmpty() && message.isNotEmpty()){
                Volley_SendContactUs(this, fullName, phoneNumber, email, subject, message,reportUserId, reportRoomId, object : Volley_SendContactUs.ServerCallBack {
                    override fun getSuccess(result: JSONObject) {
                        if (result.getBoolean("isSuccess")){
                            AlertDialog.Builder(this@ContactUsActivity)
                                .setTitle("Done")
                                .setMessage("Your message sending successfully , Our team will reply as soon as possible")
                                .setCancelable(true)
                                .setPositiveButton("Ok") { dialog, _ ->
                                    dialog.dismiss()
                                    dialog.cancel()
                                }
                                .show()
                        }else{
                            AlertDialog.Builder(this@ContactUsActivity)
                                .setTitle("Failed")
                                .setMessage("Your message sending with error , Please check your connectivity and try again")
                                .setCancelable(true)
                                .setPositiveButton("Ok") { dialog, _ ->
                                    dialog.dismiss()
                                    dialog.cancel()
                                }
                                .show()
                        }
                    }
                }).send()
            }else{
                Toast.makeText(this, "Fill the blank spaces", Toast.LENGTH_LONG).show()
            }

        }

        binding.ContactUsActivitySubjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                ( parent?.getChildAt(0) as TextView ).setTextColor(Color.WHITE)
                ( parent.getChildAt(0) as TextView ).textSize = 18f
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }
}