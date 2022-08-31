package com.arashforus.nearroom

import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import java.util.*
import kotlin.collections.ArrayList

class Adapter_RoomInfoMembers(private val members: ArrayList<Object_User>) : RecyclerView.Adapter<Adapter_RoomInfoMembers.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_members_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val roomId = ( holder.itemView.context as Activity ).intent.getIntExtra("roomId",0)
        adminsId = DB_NearoomsParticipant(holder.itemView.context).roomAdminIds(roomId)
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(members[num])
        vh.itemView.setOnClickListener {
            val intent = Intent(vh.itemView.context,PrivateActivity::class.java)
            intent.putExtra("toUsername",members[num].username)
            vh.itemView.context.startActivity(intent)
        }
    }

    companion object{
        var adminsId = arrayListOf<Int>()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(member: Object_User) {
            val pic = itemView.findViewById(R.id.MembersItem_ProfilePic) as ImageView
            val name  = itemView.findViewById(R.id.MembersItem_Fullname) as TextView
            val username  = itemView.findViewById(R.id.MembersItem_Username) as TextView
            val status  = itemView.findViewById(R.id.MembersItem_Status) as TextView
            val admin  = itemView.findViewById(R.id.MembersItem_Admin) as ImageView
            val adminText  = itemView.findViewById(R.id.MembersItem_AdminText) as TextView

            admin.visibility =View.GONE
            adminText.visibility =View.GONE

            name.setTextColor(Color.WHITE)
            username.setTextColor(Color.WHITE)
            status.setTextColor(Color.WHITE)

            if ( member.profilePic?.toUpperCase(Locale.ROOT)  == "NULL" || member.profilePic.isNullOrEmpty() ){
                if ( member.phonePhotoUri?.toUpperCase(Locale.ROOT)  == "NULL" || member.phonePhotoUri.isNullOrEmpty() ){
                    Glide.with(itemView.context)
                        .load(R.drawable.defaultprofile)
                        .placeholder(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(pic)
                }else{
                    Glide.with(itemView.context)
                        .load(member.phonePhotoUri)
                        .placeholder(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(pic)
                }
            }else{
                if ( MyInternalStorage(itemView.context).isProfileThumbAvailable(member.profilePic!!) ){
                    Glide.with(itemView.context)
                        .load(MyInternalStorage(itemView.context).getThumbProfilePicString(member.profilePic!!))
                        .placeholder(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(pic)
                }else{
                    Glide.with(itemView.context)
                        .load(R.drawable.defaultprofile)
                        .placeholder(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(pic)
                    AndroidNetworking.download( MyServerSide().getThumbProfileUri(member.profilePic!!), MyInternalStorage(itemView.context).getThumbProfileFolder(), member.profilePic)
                        .setTag(member.profilePic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbProfilePicString(member.profilePic!!))
                                    .placeholder(R.drawable.defaultprofile)
                                    .error(R.drawable.defaultprofile)
                                    .circleCrop()
                                    .into(pic)
                            }
                            override fun onError(anError: ANError?) {
                                //println("download $logo  has error")
                            }
                        })
                }

            }

            if ( member.phoneFullname.isNullOrEmpty() ){
                name.text = member.fullName
            }else{
                name.text = member.phoneFullname
            }

            username.text = member.username

            if ( member.status.isNullOrEmpty() || member.status?.toUpperCase(Locale.ROOT) == "NULL" ){
                status.visibility = View.GONE
            }else{
                status.text = member.status
            }

            if ( adminsId.contains( member.id ) ){
                //admin.visibility = View.VISIBLE
                adminText.visibility = View.VISIBLE
            }

        }
    }



}