package com.arashforus.nearroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import com.google.android.gms.common.data.DataBufferUtils
import org.json.JSONObject

class Adapter_BlockedList(private val blockedList: ArrayList<Object_Blocked>) : RecyclerView.Adapter<Adapter_BlockedList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_blocked_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return blockedList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(blocked: Object_Blocked) {
            val profilePic = itemView.findViewById(R.id.BlockedItem_Pic) as ImageView
            val username  = itemView.findViewById(R.id.BlockedItem_Username) as TextView
            //val removeButton  = itemView.findViewById(R.id.BlockedItem_RemoveButton) as Button
            if ( !blocked.photoUri.isNullOrEmpty() ) {
                if ( MyInternalStorage(itemView.context).isProfileThumbAvailable(blocked.photoUri!!) ){
                    Glide.with(itemView.context)
                        .load(MyInternalStorage(itemView.context).getThumbProfilePicString(blocked.photoUri!!))
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }else{
                    AndroidNetworking.download( MyServerSide().getThumbProfileUri(blocked.photoUri.toString()), MyInternalStorage(itemView.context).getThumbProfileFolder(), blocked.photoUri)
                        .setTag(blocked.photoUri)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbProfilePicString(blocked.photoUri.toString()))
                                    .error(R.drawable.defaultprofile)
                                    .circleCrop()
                                    .into(profilePic)
                            }
                            override fun onError(anError: ANError?) {
                                println("download ${blocked.photoUri}  has error")
                            }
                        })
                }
            }else{
                Glide.with(itemView.context)
                    .load(R.drawable.defaultprofile)
                    .circleCrop()
                    .into(profilePic)
            }


            username.text = blocked.username
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(blockedList[num])
        val progress = MyProgressDialog().Constructor(vh.itemView.context,"Please wait ...")
        progress.setCancelable(false)
        vh.itemView.findViewById<Button>(R.id.BlockedItem_RemoveButton).setOnClickListener {
            progress.show()
            val userId = DB_UserInfos(vh.itemView.context).getUserId(blockedList[num].username.toString())
            val newBlockedList = MyTools(vh.itemView.context).changeBlockedList(userId,false)
            //println(newBlockedList)
            Volley_ChangeBlockedUsers( vh.itemView.context, newBlockedList, object : Volley_ChangeBlockedUsers.ServerCallBack {
                    override fun getSuccess(result: JSONObject) {
                        if (result.getBoolean("isSuccess")) {
                            progress.dismiss()
                            MySharedPreference(MySharedPreference.PreferenceProfile,vh.itemView.context).save(MySharedPreference.Profile_BlockedUsers,newBlockedList)
                            DB_UserInfos(vh.itemView.context).syncBlocksFromSharedAndDB()
                            this@Adapter_BlockedList.notifyItemRemoved(num)
                            blockedList.remove(blockedList[num])
                            this@Adapter_BlockedList.notifyDataSetChanged()
                        } else {
                            progress.dismiss()
                            Toast.makeText(vh.itemView.context, "something wrong , try again ...", Toast.LENGTH_SHORT).show()
                        }
                    }

                }).send()

        }
    }

}