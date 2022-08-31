package com.arashforus.nearroom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashforus.nearroom.databinding.FragmentChatsBinding
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ChatsFragment : Fragment() {

    private var _binding :FragmentChatsBinding? = null
    private val binding get() = _binding!!

    lateinit var fragmentContext : Context
    private lateinit var adaptor : Adapter_Chats
    lateinit var username : String
    var userId : Int = 0

    private var privateChats = ArrayList<Object_Chatlist>()
    private var nearoomChats = ArrayList<Object_Chatlist>()
    private var allChats = ArrayList<Object_Chatlist>()

    var ids : ArrayList<Int> = ArrayList()
    var nearooms : ArrayList<Int> = ArrayList()
    private var updateMessageTimer : Timer = Timer()
    private var updateNearoomsMessageTimer : Timer = Timer()
    private var updateUserInfosTimer : Timer = Timer()
    private var updateNearoomInfosTimer : Timer = Timer()
    private var updateNearoomParticipantsTimer : Timer = Timer()


    override fun onCreateView( inflater: LayoutInflater,  container: ViewGroup?,  savedInstanceState: Bundle? ): View {
        _binding = FragmentChatsBinding.inflate(inflater,container,false)

        username = MySharedPreference(MySharedPreference.PreferenceProfile,fragmentContext).load(MySharedPreference.Profile_Username,"string") as String
        userId = MySharedPreference(MySharedPreference.PreferenceProfile,fragmentContext).load(MySharedPreference.Profile_ID,"Int") as Int

        makeAdapter()
        registerBroadcast()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        unregisterBroadcast()
    }

    override fun onAttach(context: Context) {
        fragmentContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        // Update Messages DB and Nearooms Message DB interval /////////////////////////////////////
        updateMessageTimer = Volley_GetLatestMessage(fragmentContext, object : Volley_GetLatestMessage.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                updateChatList()
            }
        }).get()

        updateNearoomsMessageTimer = Volley_GetLatestNearoomMessage(fragmentContext, object : Volley_GetLatestNearoomMessage.ServerCallBack {
            override fun getsuccess(result: JSONObject) {
                updateChatList()
            }
        }).get()

        // Update user info and nearoom info in DB interval ////////////////////////////////////////
        ids = DB_UserInfos(fragmentContext).getAllIds()
        updateUserInfosTimer = Volley_GetUserInfos(fragmentContext, ids, object : Volley_GetUserInfos.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                // Fetch new users from messages and contacts //////////////////////////////////////
                DB_UserInfos(fragmentContext).init(userId)
                // Get all usernames from userinfo DB and check for unavailable pics to download ///
                //ids = DB_UserInfos(fragmentContext).getAllIds()
                Volley_GetUserInfos.dynamicId = DB_UserInfos(fragmentContext).getAllIds()
                MyMediaDownloader(fragmentContext).updateUserInfosPicThumb()
            }
        }).get()

        nearooms = DB_NearoomInfos(fragmentContext).getAllNearooms()
        updateNearoomInfosTimer = Volley_GetNearoomInfos(fragmentContext,nearooms , object : Volley_GetNearoomInfos.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                DB_NearoomInfos(fragmentContext).init()
                Volley_GetNearoomInfos.dynamicNearooms = DB_NearoomInfos(fragmentContext).getAllNearooms()
                MyMediaDownloader(fragmentContext).updateNearoomInfosPicThumb()
            }
        }).get()

        updateNearoomParticipantsTimer = Volley_GetNearoomParticipants(fragmentContext, userId,null, object : Volley_GetNearoomParticipants.ServerCallBack{
            override fun getSuccess(result: JSONObject) {
                // Nothing
            }
        }).get()
    }

    override fun onPause() {
        super.onPause()
        updateMessageTimer.cancel()
        updateMessageTimer.purge()
        updateNearoomsMessageTimer.cancel()
        updateNearoomsMessageTimer.purge()
        updateUserInfosTimer.cancel()
        updateUserInfosTimer.purge()
        updateNearoomInfosTimer.cancel()
        updateNearoomInfosTimer.purge()
        updateNearoomParticipantsTimer.cancel()
        updateNearoomParticipantsTimer.purge()
    }


    fun updateChatList(){
        privateChats = DB_Messages(fragmentContext).loadChatListMessages(userId)
        nearoomChats = DB_NearroomMessages(fragmentContext).loadChatListMessages(userId)
        allChats.clear()
        allChats.addAll(privateChats)
        allChats.addAll(nearoomChats)
        allChats.sortByDescending {
            it.lastChatTime
        }
        adaptor.notifyDataSetChanged()
        if ( allChats.size == 0 ){
            _binding!!.ChatsFragmentNoChatTextLayout.visibility = View.VISIBLE
            _binding!!.ChatsFragmentStartChatText.visibility = View.VISIBLE
        }else{
            _binding!!.ChatsFragmentNoChatTextLayout.visibility = View.GONE
            _binding!!.ChatsFragmentStartChatText.visibility = View.GONE
        }
    }

    private fun makeAdapter() {
        privateChats = DB_Messages(fragmentContext).loadChatListMessages(userId)
        nearoomChats = DB_NearroomMessages(fragmentContext).loadChatListMessages(userId)
        allChats.clear()
        allChats.addAll(privateChats)
        allChats.addAll(nearoomChats)
        allChats.sortByDescending {
            it.lastChatTime
        }
        adaptor = Adapter_Chats(allChats)
        _binding!!.ChatsFragmentRecyclerView.layoutManager = LinearLayoutManager( fragmentContext, LinearLayoutManager.VERTICAL,false )
        _binding!!.ChatsFragmentRecyclerView.adapter = adaptor
        if ( allChats.size == 0 ){
            _binding!!.ChatsFragmentNoChatTextLayout.visibility = View.VISIBLE
            _binding!!.ChatsFragmentStartChatText.visibility = View.VISIBLE
        }else{
            _binding!!.ChatsFragmentNoChatTextLayout.visibility = View.GONE
            _binding!!.ChatsFragmentStartChatText.visibility = View.GONE
        }
    }

    private fun registerBroadcast() {
        LocalBroadcastManager.getInstance(fragmentContext).registerReceiver(myBroadcastReceiver, IntentFilter("MyBroadcast"))
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(fragmentContext).unregisterReceiver(myBroadcastReceiver)
    }

    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if ( intent != null && intent.hasExtra("type") ) {
                when ( intent.getStringExtra("type")!!.toUpperCase(Locale.ROOT).trim() ){
                    "MESSAGEDBUPDATE" -> {
                        updateChatList()
                    }
                }
            }
        }
    }
}