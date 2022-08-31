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
import kotlin.collections.ArrayList

class Adapter_Contacts(private val contactsList: ArrayList<Object_User>) : RecyclerView.Adapter<Adapter_Contacts.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_contact_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(user: Object_User) {
            val profilePic = itemView.findViewById(R.id.ContactsItem_ProfilePic) as ImageView
            val fullName  = itemView.findViewById(R.id.ContactsItem_Fullname) as TextView
            val username  = itemView.findViewById(R.id.ContactsItem_Username) as TextView
            val status  = itemView.findViewById(R.id.ContactsItem_Status) as TextView

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

        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(contactsList[num])

        vh.itemView.setOnClickListener {
            val intent = Intent(vh.itemView.context,PrivateActivity::class.java)
            intent.putExtra("toId",contactsList[num].id)
            vh.itemView.context.startActivity(intent)
        }

        vh.itemView.setOnLongClickListener { 
            MyDialogs(it.context).showContactsFragmentDialogue(contactsList[num])
            true
        }
    }

}