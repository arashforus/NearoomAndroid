package com.arashforus.nearroom

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class MyDialogs(val context: Context) {

    fun showContactsFragmentDialogue( user: Object_User ){
        Dialog(context).apply {
            this.setContentView(R.layout.dialogue_contacts_fragment_menu)
            this.findViewById<ConstraintLayout>(R.id.DialogueContactsFragmentMenu_Layout).minWidth = (MyTools(context).screenWidth() * 0.89).roundToInt()
            // Make views //////////////////////////////////////////////////////////////////////////
            val profilePic = this.findViewById(R.id.DialogueContactsFragmentMenu_Pic) as ImageView
            if ( !user.profilePic.isNullOrEmpty() ) {
                if ( MyInternalStorage(context).isProfileThumbAvailable(user.profilePic!!) ){
                    Glide.with(context)
                        .load(MyInternalStorage(context).getThumbProfilePicString(user.profilePic!!))
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }else{
                    Glide.with(context)
                        .load(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                    AndroidNetworking.download( MyServerSide().getThumbProfileUri(user.profilePic!!), MyInternalStorage(context).getThumbProfileFolder(), user.profilePic)
                        .setTag(user.profilePic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(context)
                                    .load(MyInternalStorage(context).getThumbProfilePicString(user.profilePic!!))
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
                    Glide.with(context)
                        .load(user.phonePhotoUri)
                        .error(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }else{
                    Glide.with(context)
                        .load(R.drawable.defaultprofile)
                        .circleCrop()
                        .into(profilePic)
                }
            }

            this.findViewById<TextView>(R.id.DialogueContactsFragmentMenu_Fullname).text = user.phoneFullname
            this.findViewById<TextView>(R.id.DialogueContactsFragmentMenu_Username).text = user.username
            this.findViewById<TextView>(R.id.DialogueContactsFragmentMenu_Status).text = user.status
            if ( user.favourite == 0 ){
                this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).text = "Add to favourite"
                this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).textSize = 14f
                this.findViewById<ImageView>(R.id.DialogueContactsFragmentMenu_FavouriteStar).visibility = View.GONE
            }else{
                this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).text = "Remove from favourite"
                this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).textSize = 12f
                this.findViewById<ImageView>(R.id.DialogueContactsFragmentMenu_FavouriteStar).visibility = View.VISIBLE
            }

            // Make Listeners //////////////////////////////////////////////////////////////////////
            this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_SendMessageButton).setOnClickListener {
                val intent = Intent(context,PrivateActivity::class.java)
                intent.putExtra("toId",user.id)
                context.startActivity(intent)
            }
            this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).setOnClickListener {
                if (user.favourite == 0){
                    DB_UserInfos(context).setFavourite(user.id!!,1)
                    this.findViewById<ImageView>(R.id.DialogueContactsFragmentMenu_FavouriteStar).visibility = View.VISIBLE
                    this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).text = "Remove from favourite"
                    this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).textSize = 12f
                    user.favourite = 1
                }else{
                    DB_UserInfos(context).setFavourite(user.id!!,0)
                    this.findViewById<ImageView>(R.id.DialogueContactsFragmentMenu_FavouriteStar).visibility = View.GONE
                    this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).text = "Add to favourite"
                    this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_AddFavouriteButton).textSize = 14f
                    user.favourite = 0
                }
            }
            this.findViewById<Button>(R.id.DialogueContactsFragmentMenu_SeeProfileButton).setOnClickListener {
                val intent = Intent(context,ViewContactActivity::class.java)
                intent.putExtra("userId",user.id)
                context.startActivity(intent)
            }
            this.findViewById<ImageView>(R.id.DialogueContactsFragmentMenu_Pic).setOnClickListener {
                val intent = Intent(context,ProfilePicViewActivity::class.java)
                intent.putExtra("userId",user.id)
                context.startActivity(intent)
            }

            this.show()
        }
    }


    fun showChatsFragmentDialogue( chatlist: Object_Chatlist ){
        Dialog(context).apply {
            this.setContentView(R.layout.dialogue_chats_fragment_menu)
            this.findViewById<ConstraintLayout>(R.id.DialogueChatsFragmentMenu_Layout).minWidth = (MyTools(context).screenWidth() * 0.89).roundToInt()
            // Make views //////////////////////////////////////////////////////////////////////////
            val pic = this.findViewById(R.id.DialogueChatsFragmentMenu_Pic) as ImageView
            val fullName = this.findViewById(R.id.DialogueChatsFragmentMenu_Fullname) as TextView
            val time = this.findViewById(R.id.DialogueChatsFragmentMenu_Time) as TextView
            val message = this.findViewById(R.id.DialogueChatsFragmentMenu_Message) as TextView
            val unread = this.findViewById(R.id.DialogueChatsFragmentMenu_Unread) as TextView
            val seeProfileButton = this.findViewById(R.id.DialogueChatsFragmentMenu_SeeProfileButton) as Button
            val deleteButton = this.findViewById(R.id.DialogueChatsFragmentMenu_DeleteButton) as Button
            val sendMessageButton = this.findViewById(R.id.DialogueChatsFragmentMenu_SendMessageButton) as Button

            if (chatlist.roomName.isNullOrEmpty() ){
                // Private Message /////////////////////////////////////////////////////////////////
                if (chatlist.pic.isNullOrEmpty()){
                    if ( chatlist.savedPic.isNullOrEmpty() ){
                        Glide.with(context)
                            .load(R.drawable.defaultprofile)
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }else{
                        Glide.with(context)
                            .load(chatlist.savedPic)
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }
                }else{
                    if ( MyInternalStorage(context).isProfileThumbAvailable(chatlist.pic.toString()) ){
                        Glide.with(context)
                            .load(MyInternalStorage(context).getThumbProfilePicString(chatlist.pic.toString()))
                            .error(R.drawable.defaultprofile)
                            .circleCrop()
                            .into(pic)
                    }else{
                        AndroidNetworking.download( MyServerSide().getThumbProfileUri(chatlist.pic.toString()), MyInternalStorage(context).getThumbProfileFolder(), chatlist.pic)
                            .setTag(chatlist.pic)
                            .setPriority(Priority.HIGH)
                            .build()
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    Glide.with(context)
                                        .load(MyInternalStorage(context).getThumbProfilePicString(chatlist.pic.toString()))
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
                    fullName.text = chatlist.username
                }else{
                    fullName.text = chatlist.savedName
                }

                when (chatlist.type?.toUpperCase(Locale.ROOT)?.trim()){
                    "TEXT" -> message.text = chatlist.lastChat
                    "PIC" -> message.text = "\ud83d\uddbc IMAGE"
                    "VID" -> message.text = "\ud83d\udcfd VIDEO"
                    "AUD" -> message.text = "\ud83c\udfa7 AUDIO"
                    "FILE" -> message.text = "\ud83d\udcc1 FILE"
                    else -> message.text = chatlist.lastChat
                }
                //lastChat.text = chatlist.lastchat
                val unreadMessages = DB_Messages(context).countUnreadMessages(chatlist.userId!!)
                if ( unreadMessages > 0 ){
                    unread.visibility = View.VISIBLE
                    unread.text = unreadMessages.toString()
                }else{
                    unread.visibility = View.GONE
                }

                seeProfileButton.text = "See profile"
                deleteButton.text = "Delete chat"
                sendMessageButton.text = "Send message"

                // Make listeners //////////////////////////////////////////////////////////////////
                sendMessageButton.setOnClickListener {
                    val intent = Intent(context,PrivateActivity::class.java)
                    intent.putExtra("toId",chatlist.userId)
                    context.startActivity(intent)
                }

                seeProfileButton.setOnClickListener {
                    val intent = Intent(context,ViewContactActivity::class.java)
                    intent.putExtra("userId",chatlist.userId)
                    context.startActivity(intent)
                }

                pic.setOnClickListener {
                    val intent = Intent(context,ProfilePicViewActivity::class.java)
                    intent.putExtra("userId",chatlist.userId)
                    context.startActivity(intent)
                }

                deleteButton.setOnClickListener {

                }

            }else{
                // Nearoom Message /////////////////////////////////////////////////////////////////
                if (chatlist.pic == null){
                    Glide.with(context)
                        .load(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                }else{
                    if ( MyInternalStorage(context).isNearoomThumbAvailable(chatlist.pic.toString()) ){
                        Glide.with(context)
                            .load(MyInternalStorage(context).getThumbNearoomPicString(chatlist.pic.toString()))
                            .error(R.drawable.default_nearoom)
                            .circleCrop()
                            .into(pic)
                    }else{
                        AndroidNetworking.download( MyServerSide().getThumbNearoomUri(chatlist.pic.toString()), MyInternalStorage(context).getThumbNearoomFolder(), chatlist.pic)
                            .setTag(chatlist.pic)
                            .setPriority(Priority.HIGH)
                            .build()
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    Glide.with(context)
                                        .load(MyInternalStorage(context).getThumbNearoomPicString(chatlist.pic.toString()))
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

                fullName.text = chatlist.roomName

                when (chatlist.type?.toUpperCase(Locale.ROOT)?.trim()){
                    "TEXT" -> message.text = "${chatlist.lastSender} : ${chatlist.lastChat}"
                    "PIC" -> message.text = "${chatlist.lastSender} : \ud83d\uddbc IMAGE"
                    "VID" -> message.text = "${chatlist.lastSender} : \ud83d\udcfd VIDEO"
                    "AUD" -> message.text = "${chatlist.lastSender} : \ud83c\udfa7 AUDIO"
                    "FILE" -> message.text = "${chatlist.lastSender} : \ud83d\udcc1 FILE"
                    "CREATE" -> message.text = "${chatlist.lastSender} create the nearoom"
                    "JOIN" -> message.text = "${chatlist.lastSender} join the nearoom"
                    "LEFT" -> message.text = "${chatlist.lastSender} left the nearoom"
                    "REMOVE" -> message.text = "${chatlist.lastSender} remove the nearoom"
                    "ADMIN" -> message.text = "${chatlist.lastSender} is now admin"
                    "NOTADMIN" -> message.text = "${chatlist.lastSender} is no longer admin"
                    else -> message.text = "${chatlist.lastSender} : ${chatlist.lastChat}"
                }
                val unreadMessages = DB_NearroomMessages(context).countUnreadMessages(chatlist.roomId!!)
                if ( unreadMessages > 0 ){
                    unread.visibility = View.VISIBLE
                    unread.text = unreadMessages.toString()
                }else{
                    unread.visibility = View.GONE
                }

                seeProfileButton.text = "Room info"
                deleteButton.text = "Delete chat"
                sendMessageButton.text = "Send message"

                // Make listeners //////////////////////////////////////////////////////////////////
                sendMessageButton.setOnClickListener {
                    val intent = Intent(context,RoomActivity::class.java)
                    intent.putExtra("roomId",chatlist.roomId)
                    context.startActivity(intent)
                }

                seeProfileButton.setOnClickListener {
                    val intent = Intent(context,RoomInfoActivity::class.java)
                    intent.putExtra("roomId",chatlist.roomId)
                    intent.putExtra("fromActivity","mainActivity")
                    context.startActivity(intent)
                }

                pic.setOnClickListener {
                    val intent = Intent(context,ProfilePicViewActivity::class.java)
                    intent.putExtra("roomId",chatlist.roomId)
                    context.startActivity(intent)
                }

                deleteButton.setOnClickListener {

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

            this.show()
        }
    }

    fun showNearoomFragmentDialogue( nearoom: Object_Nearoom ){
        Dialog(context).apply {
            this.setContentView(R.layout.dialogue_nearoom_fragment_menu)
            this.findViewById<ConstraintLayout>(R.id.DialogueNearoomFragmentMenu_Layout).minWidth = (MyTools(context).screenWidth() * 0.89).roundToInt()
            // Make views //////////////////////////////////////////////////////////////////////////
            val pic = this.findViewById(R.id.DialogueNearoomFragmentMenu_Pic) as ImageView
            val name = this.findViewById(R.id.DialogueNearoomFragmentMenu_Name) as TextView
            val category = this.findViewById(R.id.DialogueNearoomFragmentMenu_Category) as TextView
            val description = this.findViewById(R.id.DialogueNearoomFragmentMenu_Description) as TextView
            val distance = this.findViewById(R.id.DialogueNearoomFragmentMenu_Distance) as TextView
            val joined = this.findViewById(R.id.DialogueNearoomFragmentMenu_Joined) as TextView
            val joinButton = this.findViewById(R.id.DialogueNearoomFragmentMenu_JoinButton) as Button

            val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int
            val username = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_Username,"String") as String

            val isJoined = DB_NearoomsParticipant(context).isMember(nearoom.roomId,myId)

            if ( !nearoom.pic.isNullOrEmpty() ) {
                if ( MyInternalStorage(context).isNearoomThumbAvailable(nearoom.pic!!) ){
                    Glide.with(context)
                        .load(MyInternalStorage(context).getThumbNearoomPicString(nearoom.pic!!))
                        .error(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                }else{
                    Glide.with(context)
                        .load(R.drawable.default_nearoom)
                        .error(R.drawable.default_nearoom)
                        .circleCrop()
                        .into(pic)
                    AndroidNetworking.download( MyServerSide().getThumbNearoomUri(nearoom.pic!!), MyInternalStorage(context).getThumbNearoomFolder(), nearoom.pic)
                        .setTag(nearoom.pic)
                        .setPriority(Priority.HIGH)
                        .build()
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(context)
                                    .load(MyInternalStorage(context).getThumbNearoomPicString(nearoom.pic!!))
                                    .error(R.drawable.default_nearoom)
                                    .circleCrop()
                                    .into(pic)
                            }
                            override fun onError(anError: ANError?) {
                                println("download ${nearoom.pic}  has error")
                            }
                        })
                }
            }else{
                Glide.with(context)
                    .load(R.drawable.default_nearoom)
                    .error(R.drawable.default_nearoom)
                    .circleCrop()
                    .into(pic)
            }

            name.text = nearoom.roomname
            category.text = nearoom.category
            if ( nearoom.description.isNullOrEmpty() ){
                description.visibility = View.GONE
            }else{
                description.text = nearoom.description
            }

            distance.text =  "${nearoom.distance!!.roundToInt()} m"
            joined.text = nearoom.joined.toString() + "/" + nearoom.capacity.toString()

            // Make Listeners //////////////////////////////////////////////////////////////////////
            if ( isJoined ){
                joinButton.text = "Send message"
                joinButton.setOnClickListener {
                    val intent = Intent(context, RoomActivity::class.java)
                    intent.putExtra("roomId", nearoom.roomId)
                    context.startActivity(intent)
                }
            }else{
                joinButton.setOnClickListener {
                    Volley_JoinNearoom( context, myId, username, nearoom.roomId, object : Volley_JoinNearoom.ServerCallBack {
                        override fun getResponse(result: JSONObject) {
                            if (result.getBoolean("isSuccess")) {
                                val intent = Intent(context, RoomActivity::class.java)
                                intent.putExtra("roomId", nearoom.roomId)
                                context.startActivity(intent)
                            } else {
                                if (result.getBoolean("isFull")) {
                                    Toast.makeText(context, "This nearoom is full", Toast.LENGTH_SHORT).show()
                                }
                                if (result.getBoolean("isMember")) {
                                    val intent = Intent(context, RoomActivity::class.java)
                                    intent.putExtra("roomId", nearoom.roomId)
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }).send()
                }
            }

            pic.setOnClickListener {
                val intent = Intent(context,ProfilePicViewActivity::class.java)
                intent.putExtra("roomId",nearoom.roomId)
                context.startActivity(intent)
            }

            this.show()
        }
    }

    fun showTextMessageFragmentDialogue( message : Object_Message ){
        Dialog(context).apply {
            this.setContentView(R.layout.dialogue_text_message_fragment_menu)
            this.findViewById<ConstraintLayout>(R.id.DialogueTextMessageFragmentMenu_Layout).minWidth = (MyTools(context).screenWidth() * 0.89).roundToInt()
            // Make views //////////////////////////////////////////////////////////////////////////
            val sender = this.findViewById(R.id.DialogueTextMessageFragmentMenu_Sender) as TextView
            val time = this.findViewById(R.id.DialogueTextMessageFragmentMenu_Time) as TextView
            val text = this.findViewById(R.id.DialogueTextMessageFragmentMenu_Message) as TextView
            val deleteButton = this.findViewById(R.id.DialogueTextMessageFragmentMenu_DeleteButton) as Button
            val shareButton = this.findViewById(R.id.DialogueTextMessageFragmentMenu_ShareButton) as Button
            val copyButton = this.findViewById(R.id.DialogueTextMessageFragmentMenu_CopyButton) as Button

            val myId = MySharedPreference(MySharedPreference.PreferenceProfile,context).load(MySharedPreference.Profile_ID,"Int") as Int

            if ( message.senderId == myId ){
                sender.text = "Me"
                deleteButton.visibility = View.VISIBLE
            }else{
                sender.text = DB_UserInfos(context).getUsername(message.senderId)
                deleteButton.visibility = View.GONE
            }

            text.text = message.message
            time.text = MyDateTime().gmtToLocal(message.timestamp)

            // Make Listeners //////////////////////////////////////////////////////////////////////
            deleteButton.setOnClickListener {
                Volley_DeleteMessage(context ,myId ,message.receiverId ,message.id , object : Volley_DeleteMessage.ServerCallBack{
                    override fun getResponse(result: JSONObject) {
                        if ( result.getBoolean("isSuccess") ){
                            Toast.makeText(context, "Message is deleted successfully", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, "Something wrong ...", Toast.LENGTH_SHORT).show()
                        }
                    }

                }).send()
            }

            shareButton.setOnClickListener {
                val intent = Intent()
                intent.setAction(Intent.ACTION_SEND)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT, message.message );
                context.startActivity(Intent.createChooser(intent, "Share via"));
            }

            copyButton.setOnClickListener {
                val sdk = android.os.Build.VERSION.SDK_INT
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as android.text.ClipboardManager
                    clipboard.setText(message.message)
                }else{
                    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clipDate = ClipData.newPlainText("",message.message)
                    clipboard.setPrimaryClip(clipDate)
                }
                Toast.makeText(context, "Copied to clipboard ...", Toast.LENGTH_SHORT).show()
            }

            this.show()
        }
    }

}