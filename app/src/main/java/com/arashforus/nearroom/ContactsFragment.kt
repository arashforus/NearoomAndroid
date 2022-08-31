package com.arashforus.nearroom

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arashforus.nearroom.databinding.FragmentContactsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ContactsFragment : Fragment() {

    private var _binding : FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val readContactsPermissionCode = 200
    private val addContactRequestCode = 215
    private lateinit var fragmentActivity : Activity
    private lateinit var fragmentContext : Context

    private lateinit var contactsList : ArrayList<Object_User>
    private lateinit var adapter :Adapter_Contacts
    private lateinit var favouritesAdapter :Adapter_FavouriteContacts
    private lateinit var onlinesAdapter :Adapter_OnlineContacts

    //private var updateMessageTimer : Timer = Timer()
    private var updateUserInfosTimer : Timer = Timer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactsBinding.inflate(inflater,container,false)

        makeView()
        makeListeners()

        // Check for permission and update contact DB and update list //////////////////////////////
        checkPermission(Manifest.permission.READ_CONTACTS, readContactsPermissionCode)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()

        // Update Messages DB and Nearooms DB interval /////////////////////////////////////////////
        //updateMessageTimer = Volley_GetLatestMessage(fragmentContext, object : Volley_GetLatestMessage.ServerCallBack {
        //    override fun getsuccess(result: JSONObject) {
        //        updateAdapter()
        //    }
        //}).get()

         //Update contact infos for all users in Userinfo DB interval /////////////////////////////////
        //var usernames = DB_Contacts(fragmentContext).getAllUsernames()
        var ids = DB_UserInfos(fragmentContext).getAllIds()
        updateUserInfosTimer = Volley_GetUserInfos(fragmentContext, ids, object : Volley_GetUserInfos.ServerCallBack {
            override fun getSuccess(result: JSONObject) {
                ids = DB_UserInfos(fragmentContext).getAllIds()
                updateAdapter()
            }
        }).get()

    }

    override fun onPause() {
        super.onPause()
        //updateMessageTimer.cancel()
        //updateMessageTimer.purge()
        updateUserInfosTimer.cancel()
        updateUserInfosTimer.purge()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        fragmentActivity = activity
        fragmentContext = activity.applicationContext
    }

    private fun makeView() {
        // Make list of contacts ///////////////////////////////////////////////////////////////////
        contactsList = DB_Contacts(fragmentContext).getRegisteredUsersInfo()

        // Make Favourites Recycler View List //////////////////////////////////////////////////////
        favouritesAdapter = Adapter_FavouriteContacts(contactsList)
        binding.ContactsFragmentFavouritesRecyclerView.layoutManager = LinearLayoutManager( fragmentContext, LinearLayoutManager.VERTICAL,false )
        binding.ContactsFragmentFavouritesRecyclerView.adapter = favouritesAdapter
        if (favouritesAdapter.itemCount == 0 ){
            binding.ContactsFragmentNoFavouriteText.visibility = View.VISIBLE
        }

        // Make Onlines Recycler View List /////////////////////////////////////////////////////////
        onlinesAdapter = Adapter_OnlineContacts(contactsList)
        binding.ContactsFragmentOnlinesRecyclerView.layoutManager = LinearLayoutManager( fragmentContext, LinearLayoutManager.VERTICAL,false )
        binding.ContactsFragmentOnlinesRecyclerView.adapter = onlinesAdapter
        if (onlinesAdapter.itemCount == 0 ){
            binding.ContactsFragmentNoOnlineText.visibility = View.VISIBLE
        }

        // Make All list Recycler View List ////////////////////////////////////////////////////////
        adapter = Adapter_Contacts(contactsList)
        binding.ContactsFragmentRecyclerView.layoutManager = LinearLayoutManager( fragmentContext, LinearLayoutManager.VERTICAL,false )
        binding.ContactsFragmentRecyclerView.adapter = adapter
        if (adapter.itemCount == 0 ){
            binding.ContactsFragmentNoAllText.visibility = View.VISIBLE
        }


    }

    private fun makeListeners() {
        binding.ContactsFragmentPermissonButton.setOnClickListener {
            checkPermission(Manifest.permission.READ_CONTACTS, readContactsPermissionCode)
        }
        binding.ContactsFragmentAddButton.setOnClickListener {
            //val intent = Intent(Intent.ACTION_INSERT).apply {
            //    type = ContactsContract.Contacts.CONTENT_TYPE
            //}
            val intent = Intent(ContactsContract.Intents.Insert.ACTION)
            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
            startActivityForResult(intent , addContactRequestCode)
        }

        binding.ContactsFragmentInviteButton.setOnClickListener {
            //val intent = Intent(ContactsContract.Intents.INVITE_CONTACT)
            val fullName = MySharedPreference(MySharedPreference.PreferenceProfile,fragmentContext).load(MySharedPreference.Profile_FullName,"String")
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi, I'm $fullName on * Nearoom *\nJoin me on new generation of messaging app\nIt's simple and fast with live wallpapers and nearby rooms\n${MyServerSide.downloadApp}")
            sendIntent.putExtra("sms_body", "Hi, I'm $fullName on * Nearoom *\nJoin me on new generation of messaging app\nIt's simple and fast with live wallpapers and nearby rooms\n${MyServerSide.downloadApp}")
            sendIntent.type = "text/plain"
            //sendIntent.setPackage("com.whatsapp")
            startActivity(sendIntent)
        }
    }

    /*fun showMenu(v: View) {
        PopupMenu(fragmentContext, v).apply {
            // MainActivity implements OnMenuItemClickListener
            setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.ContactFragmentMenu_Favourite -> {
                            //archive(item)
                            true
                        }
                        R.id.ContactFragmentMenu_SeeProfile -> {
                            //delete(item)
                            true
                        }
                        R.id.ContactFragmentMenu_SendMessage -> {
                            //delete(item)
                            true
                        }
                        else -> false
                    }
                }
            })
            inflate(R.menu.contact_fragment_bottom_menu)
            show()
        }
    }*/


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(fragmentContext, permission) == PackageManager.PERMISSION_DENIED ) {
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            updateAndShowContacts()
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == readContactsPermissionCode ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                binding.ContactsFragmentPermissonText.visibility = View.INVISIBLE
                binding.ContactsFragmentPermissonButton.visibility = View.INVISIBLE
                updateAndShowContacts()
            }else{
                binding.ContactsFragmentPermissonText.visibility = View.VISIBLE
                binding.ContactsFragmentPermissonButton.visibility = View.VISIBLE
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( requestCode == addContactRequestCode ){
            if ( resultCode == Activity.RESULT_OK){
                Toast.makeText(fragmentContext, "Adding contact done", Toast.LENGTH_SHORT).show()
            }
            if ( resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(fragmentContext, "Adding contact canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAndShowContacts() {
        CoroutineScope(Dispatchers.Default).launch {
            println("Check and update contacts DB ")
            DB_Contacts(fragmentContext).checkUpdates()
            val numbers = DB_Contacts(fragmentContext).getArrayNumbersWithCountryCode()
            Volley_GetRegisteredContacts(fragmentContext, numbers, object : Volley_GetRegisteredContacts.ServerCallBack {
                override fun getSuccess(result: JSONObject) {
                    if (result.get("isSuccess") as Boolean){
                        //MyMediaDownloader(fragmentContext).updateContactsProfileThumb()
                        updateAdapter()
                    }else{
                        println("Getting registerd numberd from server isnt successfull")
                    }
                }
            }).get()
        }
    }



    fun updateAdapter(){
        contactsList.clear()
        contactsList.addAll(DB_Contacts(fragmentContext).getRegisteredUsersInfo())
        favouritesAdapter.notifyDataSetChanged()
        onlinesAdapter.notifyDataSetChanged()
        adapter.notifyDataSetChanged()

        if (favouritesAdapter.itemCount == 0 ){
            binding.ContactsFragmentNoFavouriteText.visibility = View.VISIBLE
        }else{
            binding.ContactsFragmentNoFavouriteText.visibility = View.GONE
        }

        if (onlinesAdapter.itemCount == 0 ){
            binding.ContactsFragmentNoOnlineText.visibility = View.VISIBLE
        }else{
            binding.ContactsFragmentNoOnlineText.visibility = View.GONE
        }

        if (adapter.itemCount == 0 ){
            binding.ContactsFragmentNoAllText.visibility = View.VISIBLE
        }else{
            binding.ContactsFragmentNoAllText.visibility = View.GONE
        }
    }


}