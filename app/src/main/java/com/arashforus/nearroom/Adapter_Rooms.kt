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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class Adapter_Rooms (val rooms: ArrayList<Object_Nearoom>) : RecyclerView.Adapter<Adapter_Rooms.ViewHolder>() {

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
            val description  = itemView.findViewById(R.id.Nearroomlistitem_Description) as TextView
            val distance  = itemView.findViewById(R.id.Nearroomlistitem_Distance) as TextView
            val capacity  = itemView.findViewById(R.id.Nearroomlistitem_Numberjoin) as TextView

            if (room.pic?.toUpperCase(Locale.ROOT)  == "NULL" || room.pic.isNullOrEmpty() ){
                Glide.with(itemView.context)
                    .load(R.drawable.default_nearoom)
                    .circleCrop()
                    .into(pic)
            }else{
                if ( MyInternalStorage(itemView.context).isNearoomThumbAvailable(room.pic.toString()) ){
                    Glide.with(itemView.context)
                        .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(room.pic.toString()))
                        .error(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                }else{
                    AndroidNetworking.download( MyServerSide().getThumbNearoomUri(room.pic.toString()), MyInternalStorage(itemView.context).getThumbNearoomFolder(), room.pic)
                        .setTag(room.pic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(room.pic.toString()))
                                    .error(R.drawable.default_nearoom)
                                    .circleCrop()
                                    .into(pic)
                            }
                            override fun onError(anError: ANError?) {
                                println("download ${room.pic}  has error")
                            }
                        })
                }

            }
            name.text = room.roomname
            category.text = room.category
            if ( room.description.isNullOrEmpty() ){
                description.text = ""
            }else{
                if ( room.description!!.toUpperCase(Locale.ROOT).trim() == "NULL" ){
                    description.text = ""
                }else{
                    description.text = room.description
                }

            }

            if (room.distance!! < 10000 ){
                distance.text = room.distance!!.roundToInt().toString() + " m"
            }else{
                distance.text = (room.distance!!/1000).roundToLong().toString() + " Km"
            }
            capacity.text = room.joined.toString() + "/" + room.capacity.toString()

        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(rooms[num])
        vh.itemView.setOnClickListener {
            //val intent = Intent(vh.itemView.context,RoomActivity::class.java)
            //intent.putExtra("roomName",rooms[num].roomname)
            //intent.putExtra("roomPic",rooms[num].pic)
            //vh.itemView.context.startActivity(intent)
            MyDialogs(vh.itemView.context).showNearoomFragmentDialogue(rooms[num])
        }
    }

}