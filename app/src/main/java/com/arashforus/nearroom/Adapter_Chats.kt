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

class Adapter_Chats(private val chatLists: ArrayList<Object_Chatlist>) : RecyclerView.Adapter<Adapter_Chats.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_chatlist_item, parent, false)
        return ViewHolder(v)
    }

    //override fun onViewRecycled(holder: ViewHolder) {
    //val myactivity : Activity = holder.itemView.context as Activity
    //onTransformationStartContainer(myactivity)
    //    super.onViewRecycled(holder)
    //}

    override fun getItemCount(): Int {
        return chatLists.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(chatlist: Object_Chatlist) {
            val pic = itemView.findViewById(R.id.ChatlistItem_Pic) as ImageView
            val username  = itemView.findViewById(R.id.Chatlistitem_Username) as TextView
            val lastChat  = itemView.findViewById(R.id.Chatlistitem_Lastchat) as TextView
            val time  = itemView.findViewById(R.id.Chatlistitem_Time) as TextView
            val unread  = itemView.findViewById(R.id.Chatlistitem_Unread) as TextView

            if (chatlist.roomName.isNullOrEmpty() ){
                // Private Message /////////////////////////////////////////////////////////////////
                if (chatlist.pic.isNullOrEmpty()){
                    if ( chatlist.savedPic.isNullOrEmpty() ){
                        Glide.with(itemView.context)
                            .load(R.drawable.defaultprofile)
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }else{
                        Glide.with(itemView.context)
                            .load(chatlist.savedPic)
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }
                }else{
                    if ( MyInternalStorage(itemView.context).isProfileThumbAvailable(chatlist.pic.toString()) ){
                        Glide.with(itemView.context)
                            .load(MyInternalStorage(itemView.context).getThumbProfilePicString(chatlist.pic.toString()))
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }else{
                        AndroidNetworking.download( MyServerSide().getThumbProfileUri(chatlist.pic.toString()), MyInternalStorage(itemView.context).getThumbProfileFolder(), chatlist.pic)
                            .setTag(chatlist.pic)
                            .setPriority(Priority.HIGH)
                            .build()
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    Glide.with(itemView.context)
                                        .load(MyInternalStorage(itemView.context).getThumbProfilePicString(chatlist.pic.toString()))
                                        .error(R.drawable.defaultprofile)
                                        .circleCrop()
                                        .into(pic)
                                }
                                override fun onError(anError: ANError?) {
                                    println("download ${chatlist.pic}  has error")
                                }
                            })
                    }

                }

                if ( chatlist.savedName.isNullOrEmpty() ){
                    username.text = chatlist.username
                }else{
                    username.text = chatlist.savedName
                }

                when (chatlist.type?.toUpperCase(Locale.ROOT)?.trim()){
                    "TEXT" -> lastChat.text = chatlist.lastChat
                    "PIC" -> lastChat.text = "\ud83d\uddbc IMAGE"
                    "VID" -> lastChat.text = "\ud83d\udcfd VIDEO"
                    "AUD" -> lastChat.text = "\ud83c\udfa7 AUDIO"
                    "FILE" -> lastChat.text = "\ud83d\udcc1 FILE"
                    else -> lastChat.text = chatlist.lastChat
                }
                //lastChat.text = chatlist.lastchat
                val unreadMessages = DB_Messages(itemView.context).countUnreadMessages(chatlist.userId!!)
                if ( unreadMessages > 0 ){
                    unread.visibility = View.VISIBLE
                    unread.text = unreadMessages.toString()
                }else{
                    unread.visibility = View.GONE
                }
            }else{
                // Nearoom Message /////////////////////////////////////////////////////////////////
                if (chatlist.pic == null){
                    Glide.with(itemView.context)
                        .load(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                }else{
                    if ( MyInternalStorage(itemView.context).isNearoomThumbAvailable(chatlist.pic.toString()) ){
                        Glide.with(itemView.context)
                            .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(chatlist.pic.toString()))
                            .error(R.drawable.default_nearoom)
                            .circleCrop()
                            .into(pic)
                    }else{
                        AndroidNetworking.download( MyServerSide().getThumbNearoomUri(chatlist.pic.toString()), MyInternalStorage(itemView.context).getThumbNearoomFolder(), chatlist.pic)
                            .setTag(chatlist.pic)
                            .setPriority(Priority.HIGH)
                            .build()
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    Glide.with(itemView.context)
                                        .load(MyInternalStorage(itemView.context).getThumbNearoomPicString(chatlist.pic.toString()))
                                        .error(R.drawable.default_nearoom)
                                        .circleCrop()
                                        .into(pic)
                                }
                                override fun onError(anError: ANError?) {
                                    println("download ${chatlist.pic}  has error")
                                }
                            })
                    }

                }
                username.text = chatlist.roomName
                when (chatlist.type?.toUpperCase(Locale.ROOT)?.trim()){
                    "TEXT" -> lastChat.text = "${chatlist.lastSender} : ${chatlist.lastChat}"
                    "PIC" -> lastChat.text = "${chatlist.lastSender} : \ud83d\uddbc IMAGE"
                    "VID" -> lastChat.text = "${chatlist.lastSender} : \ud83d\udcfd VIDEO"
                    "AUD" -> lastChat.text = "${chatlist.lastSender} : \ud83c\udfa7 AUDIO"
                    "FILE" -> lastChat.text = "${chatlist.lastSender} : \ud83d\udcc1 FILE"
                    "CREATE" -> lastChat.text = "${chatlist.lastSender} create the nearoom"
                    "JOIN" -> lastChat.text = "${chatlist.lastSender} join the nearoom"
                    "LEFT" -> lastChat.text = "${chatlist.lastSender} left the nearoom"
                    "REMOVE" -> lastChat.text = "${chatlist.lastSender} remove the nearoom"
                    "ADMIN" -> lastChat.text = "${chatlist.lastSender} is now admin"
                    "NOTADMIN" -> lastChat.text = "${chatlist.lastSender} is no longer admin"
                    else -> lastChat.text = "${chatlist.lastSender} : ${chatlist.lastChat}"
                }
                val unreadMessages = DB_NearroomMessages(itemView.context).countUnreadMessages(chatlist.roomId!!)
                if ( unreadMessages > 0 ){
                    unread.visibility = View.VISIBLE
                    unread.text = unreadMessages.toString()
                }else{
                    unread.visibility = View.GONE
                }
            }
            when (MyDateTime().DayDiff(chatlist.lastChatTime!!)) {
                -1 -> time.text = MyDateTime().gmtToLocal(chatlist.lastChatTime!!)
                0 -> time.text = MyDateTime().gmtToLocal(chatlist.lastChatTime!!)
                1 -> time.text = "Yesterday"
                2 -> time.text = MyDateTime().getDayOfWeek(2)
                3 -> time.text = MyDateTime().getDayOfWeek(3)
                4 -> time.text = MyDateTime().getDayOfWeek(4)
                5 -> time.text = MyDateTime().getDayOfWeek(5)
                6 -> time.text = MyDateTime().getDayOfWeek(6)
                else -> time.text = MyDateTime().gmtToLocal(chatlist.lastChatTime!!,"yy/MM/dd")
            }

        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(chatLists.get(num))
        vh.itemView.setOnClickListener {
            if ( chatLists[num].roomName == null || chatLists[num].roomName == "" ){
                val intent = Intent(vh.itemView.context,PrivateActivity::class.java)
                intent.putExtra("toId",chatLists[num].userId)
                vh.itemView.context.startActivity(intent)
            }else{
                val intent = Intent(vh.itemView.context,RoomActivity::class.java)
                intent.putExtra("roomId",chatLists[num].roomId)
                vh.itemView.context.startActivity(intent)
            }
            //val myactivity : Activity = vh.itemView.context as Activity
            //val transLayout : TransformationLayout = myactivity.findViewById<TransformationLayout>(R.id.Chatlistitem_TransformationLayout)
            //val bundle = transLayout.withActivity(myactivity, "myTransitionName")
            //val intent = Intent(vh.itemView.context,PrivateActivity::class.java)
            //intent.putExtra("TransformationParams", transLayout.getParcelableParams())
            //intent.putExtra("toUsername",chatlists[num].username)
            //intent.putExtra("picProfile",chatlists[num].pic)
            //vh.itemView.context.startActivity(intent,bundle)

            //myactivity.overridePendingTransition(R.anim.zoom_out_fade,R.anim.zoom_in_fade)

        }

        vh.itemView.setOnLongClickListener {
            MyDialogs(it.context).showChatsFragmentDialogue(chatLists[num])
            return@setOnLongClickListener true
        }
    }

}