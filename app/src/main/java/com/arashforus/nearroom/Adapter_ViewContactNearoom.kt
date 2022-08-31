package com.arashforus.nearroom

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

class Adapter_ViewContactNearoom(val rooms: ArrayList<Object_Nearoom>) : RecyclerView.Adapter<Adapter_ViewContactNearoom.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_nearroom_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(room: Object_Nearoom) {
            val pic = itemView.findViewById(R.id.Nearroomlistitem_pic) as ImageView
            val name  = itemView.findViewById(R.id.Nearroomlistitem_name) as TextView
            val category  = itemView.findViewById(R.id.Nearroomlistitem_Category) as TextView
            val capacity  = itemView.findViewById(R.id.Nearroomlistitem_Numberjoin) as TextView
            val distance  = itemView.findViewById(R.id.Nearroomlistitem_Distance) as TextView
            val description  = itemView.findViewById(R.id.Nearroomlistitem_Description) as TextView

            distance.visibility =View.GONE
            capacity.visibility =View.GONE

            name.setTextColor(Color.WHITE)
            category.setTextColor(Color.WHITE)
            description.setTextColor(Color.WHITE)

            if (room.pic?.toUpperCase(Locale.ROOT)  == "NULL" || room.pic.isNullOrEmpty() ){
                Glide.with(itemView.context)
                    .load(R.drawable.default_nearoom)
                    .placeholder(R.drawable.default_nearoom)
                    .circleCrop()
                    .into(pic)
            }else{
                if ( MyInternalStorage(itemView.context).isNearoomThumbAvailable(room.pic!!) ){
                    Glide.with(itemView.context)
                        .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(room.pic!!))
                        .placeholder(R.drawable.default_nearoom)
                        .error(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                }else{
                    Glide.with(itemView.context)
                        .load(R.drawable.default_nearoom)
                        .placeholder(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                    AndroidNetworking.download( MyServerSide().getThumbNearoomUri(room.pic!!), MyInternalStorage(itemView.context).getThumbNearoomFolder(), room.pic)
                        .setTag(room.pic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(room.pic!!))
                                    .placeholder(R.drawable.default_nearoom)
                                    .error(R.drawable.default_nearoom)
                                    .circleCrop()
                                    .into(pic)
                            }
                            override fun onError(anError: ANError?) {
                                //println("download $logo  has error")
                            }
                        })
                }

            }
            name.text = room.roomname
            category.text = room.category
            description.text = room.description
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(rooms[num])
        vh.itemView.setOnClickListener {
            val intent = Intent(vh.itemView.context,RoomActivity::class.java)
            intent.putExtra("roomname",rooms[num].roomname)
            vh.itemView.context.startActivity(intent)
        }
    }

}