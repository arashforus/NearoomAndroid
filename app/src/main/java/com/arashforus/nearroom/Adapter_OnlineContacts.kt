package com.arashforus.nearroom

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide

class Adapter_OnlineContacts(private val contactsList: ArrayList<Object_User>) : RecyclerView.Adapter<Adapter_OnlineContacts.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_online_contact_item, parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int {
        newList.clear()
        contactsList.forEach {
            if ( !it.lastSeen.isNullOrEmpty() ){
                if ( MyDateTime().secondDiff(it.lastSeen.toString()) < 3600 ){
                    newList.add(it)
                }
            }
        }
        return newList.size
    }

    companion object{
        var newList = ArrayList<Object_User>()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(user: Object_User) {
            val profilePic = itemView.findViewById(R.id.ContactsItemOnline_ProfilePic) as ImageView
            val fullName = itemView.findViewById(R.id.ContactsItemOnline_Fullname) as TextView
            val username = itemView.findViewById(R.id.ContactsItemOnline_Username) as TextView
            val status = itemView.findViewById(R.id.ContactsItemOnline_Status) as TextView
            val online = itemView.findViewById(R.id.ContactsItemOnline_Online) as TextView
            val onlineSubtext = itemView.findViewById(R.id.ContactsItemOnline_OnlineSubtext) as TextView

            if ( !user.profilePic.isNullOrEmpty() ) {
                if ( MyInternalStorage(itemView.context).isProfileThumbAvailable(user.profilePic!!) ){
                    Glide.with(itemView.context)
                        .load(MyInternalStorage(itemView.context).getThumbProfilePicString(user.profilePic!!))
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }else{
                    Glide.with(itemView.context)
                        .load(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                    AndroidNetworking.download( MyServerSide().getThumbProfileUri(user.profilePic!!), MyInternalStorage(itemView.context).getThumbProfileFolder(), user.profilePic)
                        .setTag(user.profilePic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbProfilePicString(user.profilePic!!))
                                    .error(R.drawable.defaultprofile)
                                    .circleCrop()
                                    .into(profilePic)
                            }
                            override fun onError(anError: ANError?) {
                                println("download ${user.profilePic}  has error")
                            }
                        })
                }
            }else{
                if ( !user.phonePhotoUri.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(user.phonePhotoUri)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }else{
                    Glide.with(itemView.context)
                        .load(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }
            }

            fullName.text = user.phoneFullname
            username.text = user.username
            if ( user.status.isNullOrEmpty() ) {
                status.visibility = View.GONE
            }else{
                status.text = user.status
            }

            val secDiff = MyDateTime().secondDiff(user.lastSeen.toString())
            if ( secDiff <= 3600 ) {
                if ( secDiff < 15 ){
                    online.text = "Online"
                    onlineSubtext.visibility = View.GONE
                }else{
                    online.text = "Last seen"
                    onlineSubtext.text = MyDateTime().gmtToLocal(user.lastSeen.toString(),"hh:mm")
                }
            }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        //println("------------------------$num ${contactsList[num].username} ---------------")
        //println(MyDateTime().secondDiff(contactsList[num].lastSeen.toString()))
        if ( MyDateTime().secondDiff(newList[num].lastSeen.toString()) < 3600 ){
            vh.bindItems(newList[num])

            vh.itemView.setOnClickListener {
                val intent = Intent(vh.itemView.context,PrivateActivity::class.java)
                intent.putExtra("toId",newList[num].id)
                vh.itemView.context.startActivity(intent)
            }

            vh.itemView.setOnLongClickListener {
                MyDialogs(it.context).showContactsFragmentDialogue(newList[num])
                true
            }
        }
    }

}