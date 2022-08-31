package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashforus.nearroom.databinding.ActivityBlockedListBinding
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject

class BlockedListActivity : AppCompatActivity() {

    lateinit var binding : ActivityBlockedListBinding
    lateinit var adapter : Adapter_BlockedList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        DB_UserInfos(this).syncBlocksFromSharedAndDB()
        val ids = DB_UserInfos(this).getAllIds()
        Volley_GetUserInfos(this,ids,object : Volley_GetUserInfos.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                DB_UserInfos(this@BlockedListActivity).syncBlocksFromSharedAndDB()
                initAdapter()
            }
        }).getOnce()
        initAdapter()
    }

    private fun initToolbar(){
        setSupportActionBar(binding.BlockedListActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "Blocked Users"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun initAdapter() {
        //val blockedList = MySharedPreference(MySharedPreference.PreferenceProfile,this).loadJSONArray(MySharedPreference.Profile_BlockedUsers)
        //val blockedListArray = makeBlockedList(blockedList)
        val blockList = DB_UserInfos(this).getBlocksObject()
        if ( blockList.size == 0 ){
            binding.BlockedListActivityEmptyText.visibility = View.VISIBLE
        }else{
            binding.BlockedListActivityEmptyText.visibility = View.GONE
        }
        adapter = Adapter_BlockedList(blockList)
        val layout = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        binding.BlockedListActivityRecyclerView.layoutManager = layout
        binding.BlockedListActivityRecyclerView.adapter = adapter

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}