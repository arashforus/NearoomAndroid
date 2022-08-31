package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashforus.nearroom.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    lateinit var binding : ActivitySearchBinding

    private var contactList = ArrayList<Object_User>()
    private var nearoomList = ArrayList<Object_Nearoom>()
    private var privateMessageList = ArrayList<Object_Chatlist>()
    private var nearoomMessageList = ArrayList<Object_Chatlist>()

    private lateinit var contactAdapter :Adapter_Contacts
    private lateinit var nearoomAdapter :Adapter_ViewContactNearoom
    private lateinit var privateMessageAdapter :Adapter_Chats
    private lateinit var nearoomMessageAdapter :Adapter_Chats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        makeView()
        makeListeners()

    }

    private fun initToolbar() {
        setSupportActionBar(binding.SearchActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Search"
        supportActionBar?.subtitle = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeView() {
        binding.SearchActivityContactsLayout.visibility = View.GONE
        binding.SearchActivityNearoomLayout.visibility = View.GONE
        binding.SearchActivityPrivateMessagesLayout.visibility = View.GONE
        binding.SearchActivityNearoomMessagesLayout.visibility = View.GONE
        binding.SearchActivityContactsRecyclerView.visibility = View.GONE
        binding.SearchActivityNearoomRecyclerView.visibility = View.GONE
        binding.SearchActivityPrivateMessagesRecyclerView.visibility = View.GONE
        binding.SearchActivityNearoomMessagesRecyclerView.visibility = View.GONE

        binding.SearchActivityInputText.showSoftInputOnFocus = true
        binding.SearchActivityInputText.requestFocus(View.FOCUS_UP)
    }

    private fun makeListeners() {
        binding.SearchActivityInputText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {      }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ( s.isNullOrEmpty() ) {
                    makeView()
                }else{
                    search(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {       }

        })

    }

    private fun search( word : String) {
        // Make Contacts Recycler View List ////////////////////////////////////////////////////////
        contactList = DB_UserInfos(this).search(word , 10)
        if (contactList.size == 0 ){
            binding.SearchActivityContactsRecyclerView.visibility =View.GONE
            binding.SearchActivityContactsLayout.visibility = View.GONE
        }else{
            binding.SearchActivityContactsLayout.visibility = View.VISIBLE
            binding.SearchActivityContactsRecyclerView.visibility =View.VISIBLE
            contactAdapter = Adapter_Contacts(contactList)
            binding.SearchActivityContactsRecyclerView.layoutManager = LinearLayoutManager( this, LinearLayoutManager.VERTICAL,false )
            binding.SearchActivityContactsRecyclerView.adapter = contactAdapter

            binding.SearchActivityContactsItem.text = "${contactList.size} items"

            if ( contactList.size == 10 ){
                binding.SearchActivityContactsShowAll.visibility = View.VISIBLE
                binding.SearchActivityContactsItem.text = "${DB_UserInfos(this).searchCount(word)} items"
                binding.SearchActivityContactsShowAll.setOnClickListener {
                    contactList.clear()
                    contactList.addAll(DB_UserInfos(this).search(word , 999))
                    contactAdapter.notifyDataSetChanged()
                    contactAdapter.notifyItemChanged(11)
                }
            }else{
                binding.SearchActivityContactsShowAll.visibility = View.INVISIBLE
                binding.SearchActivityContactsShowAll.setOnClickListener {  }
            }
        }
        // Make Nearooms Recycler View List ////////////////////////////////////////////////////////
        nearoomList = DB_NearoomInfos(this).search(word , 10)
        if (nearoomList.size == 0 ){
            binding.SearchActivityNearoomRecyclerView.visibility =View.GONE
            binding.SearchActivityNearoomLayout.visibility = View.GONE
        }else{
            binding.SearchActivityNearoomLayout.visibility = View.VISIBLE
            binding.SearchActivityNearoomRecyclerView.visibility =View.VISIBLE
            nearoomAdapter = Adapter_ViewContactNearoom(nearoomList)
            binding.SearchActivityNearoomRecyclerView.layoutManager = LinearLayoutManager( this, LinearLayoutManager.VERTICAL,false )
            binding.SearchActivityNearoomRecyclerView.adapter = nearoomAdapter

            binding.SearchActivityNearoomItem.text = "${nearoomList.size} items"

            if ( nearoomList.size == 10 ){
                binding.SearchActivityNearoomShowAll.visibility = View.VISIBLE
                binding.SearchActivityNearoomItem.text = "${DB_NearoomInfos(this).searchCount(word)} items"
                binding.SearchActivityNearoomShowAll.setOnClickListener {
                    nearoomList.clear()
                    nearoomList.addAll(DB_NearoomInfos(this).search(word , 999))
                    nearoomAdapter.notifyDataSetChanged()
                    nearoomAdapter.notifyItemChanged(11)
                }
            }else{
                binding.SearchActivityNearoomShowAll.visibility = View.INVISIBLE
                binding.SearchActivityNearoomShowAll.setOnClickListener {  }
            }
        }

        // Make Private messages Recycler View List ////////////////////////////////////////////////
        privateMessageList = DB_Messages(this).search(word , 10)
        if (privateMessageList.size == 0 ){
            binding.SearchActivityPrivateMessagesLayout.visibility = View.GONE
            binding.SearchActivityPrivateMessagesRecyclerView.visibility =View.GONE
        }else{
            binding.SearchActivityPrivateMessagesLayout.visibility = View.VISIBLE
            binding.SearchActivityPrivateMessagesRecyclerView.visibility =View.VISIBLE
            privateMessageAdapter = Adapter_Chats(privateMessageList)
            binding.SearchActivityPrivateMessagesRecyclerView.layoutManager = LinearLayoutManager( this, LinearLayoutManager.VERTICAL,false )
            binding.SearchActivityPrivateMessagesRecyclerView.adapter = privateMessageAdapter

            binding.SearchActivityPrivateMessagesItem.text = "${privateMessageList.size} items"

            if ( privateMessageList.size == 10 ){
                binding.SearchActivityPrivateMessagesShowAll.visibility = View.VISIBLE
                binding.SearchActivityPrivateMessagesItem.text = "${DB_Messages(this).searchCount(word)} items"
                binding.SearchActivityPrivateMessagesShowAll.setOnClickListener {
                    privateMessageList.clear()
                    privateMessageList.addAll(DB_Messages(this).search(word , 999))
                    privateMessageAdapter.notifyDataSetChanged()
                    privateMessageAdapter.notifyItemChanged(11)
                }
            }else{
                binding.SearchActivityPrivateMessagesShowAll.visibility = View.INVISIBLE
                binding.SearchActivityPrivateMessagesShowAll.setOnClickListener {  }
            }
        }

        // Make Nearoom messages Recycler View List ////////////////////////////////////////////////
        nearoomMessageList = DB_NearroomMessages(this).search(word , 10)
        if (nearoomMessageList.size == 0 ){
            binding.SearchActivityNearoomMessagesLayout.visibility = View.GONE
            binding.SearchActivityNearoomMessagesRecyclerView.visibility =View.GONE
        }else{
            binding.SearchActivityNearoomMessagesLayout.visibility = View.VISIBLE
            binding.SearchActivityNearoomMessagesRecyclerView.visibility =View.VISIBLE
            nearoomMessageAdapter = Adapter_Chats(nearoomMessageList)
            binding.SearchActivityNearoomMessagesRecyclerView.layoutManager = LinearLayoutManager( this, LinearLayoutManager.VERTICAL,false )
            binding.SearchActivityNearoomMessagesRecyclerView.adapter = nearoomMessageAdapter

            binding.SearchActivityNearoomMessagesItem.text = "${nearoomMessageList.size} items"

            if ( nearoomMessageList.size > 10 ){
                binding.SearchActivityNearoomMessagesShowAll.visibility = View.VISIBLE
                binding.SearchActivityNearoomMessagesItem.text = "${DB_NearroomMessages(this).searchCount(word)} items"
                binding.SearchActivityNearoomMessagesShowAll.setOnClickListener {
                    nearoomMessageList.clear()
                    nearoomMessageList.addAll(DB_NearroomMessages(this).search(word , 999))
                    nearoomMessageAdapter.notifyDataSetChanged()
                    nearoomMessageAdapter.notifyItemChanged(11)
                }
            }else{
                binding.SearchActivityNearoomMessagesShowAll.visibility = View.INVISIBLE
                binding.SearchActivityNearoomMessagesShowAll.setOnClickListener {  }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}