package com.arashforus.nearroom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class Adapter_NearoomMessages(val messages: ArrayList<Object_NearoomMessage>) : RecyclerView.Adapter<Adapter_NearoomMessages.ViewHolder>() {

    private val sendFromText = 0
    private val sendFromMedia = 1
    private val sendFromVideo = 2
    private val sendFromAudio = 3
    private val sendFromFile = 4

    private val sendToText = 10
    private val sendToMedia = 11
    private val sendToVideo = 12
    private val sendToAudio = 13
    private val sendToFile = 14

    private val general = 20


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        myId = MySharedPreference(MySharedPreference.PreferenceProfile,recyclerView.context ).load(MySharedPreference.Profile_ID,"Int") as Int
        AndroidNetworking.initialize(recyclerView.context)
        lastDate = null
        audioPlayingName = ""
        mediaPlayer = MediaPlayer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v : View = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_text_bubble, parent, false)
        when(viewType){
            sendFromText -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.mychat_text_bubble, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    v.foregroundGravity = Gravity.END
                }
            }
            sendFromMedia -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.mychat_image_bubble, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    v.foregroundGravity = Gravity.END
                }
            }
            sendFromVideo -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.mychat_video_bubble, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    v.foregroundGravity = Gravity.END
                }
            }
            sendFromAudio -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.mychat_audio_bubble, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    v.foregroundGravity = Gravity.END
                }
            }
            sendFromFile -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.mychat_file_bubble, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    v.foregroundGravity = Gravity.END
                }
            }
            sendToText -> v = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_text_bubble, parent, false)
            sendToMedia -> v = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_image_bubble, parent, false)
            sendToVideo -> v = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_video_bubble, parent, false)
            sendToAudio -> v = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_audio_bubble, parent, false)
            sendToFile -> v = LayoutInflater.from(parent.context).inflate(R.layout.yourchat_file_bubble, parent, false)
            general -> v = LayoutInflater.from(parent.context).inflate(R.layout.general_bubble, parent, false)
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        var viewtype = 0
        if (messages[position].senderId == myId){
            when (messages[position].type.toUpperCase(Locale.ROOT).trim()) {
                "TEXT" -> viewtype = sendFromText
                "PIC" -> viewtype = sendFromMedia
                "VID" -> viewtype = sendFromVideo
                "AUD" -> viewtype = sendFromAudio
                "FILE" -> viewtype = sendFromFile
                "CREATE" -> viewtype = general
                "JOIN" -> viewtype = general
                "LEFT" -> viewtype = general
                "REMOVE" -> viewtype = general
                "ADMIN" -> viewtype = general
                "NOTADMIN" -> viewtype = general
            }
        }else{
            when (messages[position].type.toUpperCase(Locale.ROOT).trim()) {
                "TEXT" -> viewtype = sendToText
                "PIC" -> viewtype = sendToMedia
                "VID" -> viewtype = sendToVideo
                "AUD" -> viewtype = sendToAudio
                "FILE" -> viewtype = sendToFile
                "CREATE" -> viewtype = general
                "JOIN" -> viewtype = general
                "LEFT" -> viewtype = general
                "REMOVE" -> viewtype = general
                "ADMIN" -> viewtype = general
                "NOTADMIN" -> viewtype = general
            }
        }
        return viewtype
    }

    companion object{
        var lastDate : String? = null
        var myId : Int = 0
        const val firstTimeTimestamp = "2000/01/01 12:00:00"
        const val imageCorner = 30
        const val blurCorner = 10

        var mediaPlayer = MediaPlayer()
        var audioPlayingName = ""

        var seekTimer = Timer("seekAudio")
        private const val seekDelay : Long = 10
        private const val seekPeriod : Long = 1000
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val width = MyTools(itemView.context).screenWidth()

        fun bindItems(message: Object_NearoomMessage) {
            when (message.type.toUpperCase(Locale.ROOT).trim()) {
                "CREATE" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You create the nearoom"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} create the nearoom"
                    }
                }
                "JOIN" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You join this nearoom"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} join the nearoom"
                    }
                }
                "LEFT" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You left this nearoom"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} left the nearoom"
                    }
                }
                "REMOVE" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You remove this nearoom"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} remove the nearoom"
                    }
                }
                "ADMIN" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You are now admin"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} is now admin"
                    }
                }
                "NOTADMIN" -> {
                    val text = itemView.findViewById(R.id.GeneralBubble_Text) as TextView
                    text.text = if ( message.senderId == myId ){
                        "You are no longer admin"
                    }else{
                        "${DB_UserInfos(itemView.context).getUsername(message.senderId)} is no longer admin"
                    }
                }
                "TEXT" -> {
                    val msg : TextView
                    val time : TextView
                    val status : ImageView
                    val layout : LinearLayout

                    if (message.senderId == myId){
                        msg = itemView.findViewById(R.id.mychat_message)
                        time = itemView.findViewById(R.id.mychat_time) as TextView
                        status = itemView.findViewById(R.id.mychat_tik) as ImageView
                        layout = itemView.findViewById(R.id.MyChat_LayoutAll) as LinearLayout

                        setTick(message.send,message.received,message.read,status)
                    }else{
                        msg = itemView.findViewById(R.id.yourchat_message) as TextView
                        time = itemView.findViewById(R.id.yourchat_time) as TextView
                        //status = itemView.findViewById(R.id.mychat_tik) as ImageView
                        layout = itemView.findViewById(R.id.Yourchat_LayoutAll) as LinearLayout

                        val sender = itemView.findViewById<TextView>(R.id.yourchat_sender)
                        sender.visibility = View.VISIBLE
                        sender.text = DB_UserInfos(itemView.context).getUsername(message.senderId)
                    }

                    msg.text = message.message
                    time.text = MyDateTime().gmtToLocal(message.timestamp)

                    if ( message.selected ) {
                        layout.setBackgroundColor(Color.GREEN)
                    }else{
                        layout.setBackgroundColor(Color.TRANSPARENT)
                    }
                }
                "PIC" -> {
                    val pic : ImageView
                    val downloadLayout : ConstraintLayout
                    val downloadSize : TextView
                    val progressBar: ProgressBar
                    val time : TextView
                    val status : ImageView

                    if (message.senderId == myId){
                        pic = itemView.findViewById(R.id.MyChat_Media_Image)
                        downloadLayout = itemView.findViewById(R.id.MyChat_Media_Download_layout)
                        downloadSize = itemView.findViewById(R.id.MyChat_Media_Size)
                        progressBar = itemView.findViewById(R.id.MyChat_Media_ProgressBar)
                        time = itemView.findViewById(R.id.MyChat_Media_Time)
                        status = itemView.findViewById(R.id.MyChat_Media_Tik)

                        setTick(message.send,message.received,message.read,status)
                    }else{
                        pic = itemView.findViewById(R.id.YourChat_Media_Image)
                        downloadLayout = itemView.findViewById(R.id.YourChat_Media_Download_Layout)
                        downloadSize = itemView.findViewById(R.id.YourChat_Media_Download_Size)
                        progressBar = itemView.findViewById(R.id.YourChat_Media_ProgressBar)
                        time = itemView.findViewById(R.id.YourChat_Media_Time)
                        //status = itemView.findViewById(R.id.MyChat_Media_Tik)
                        val sender = itemView.findViewById<TextView>(R.id.YourChat_Media_Sender)
                        sender.visibility = View.VISIBLE
                        sender.text = DB_UserInfos(itemView.context).getUsername(message.senderId)
                    }

                    time.text = MyDateTime().gmtToLocal(message.timestamp)

                    if (!message.uri.isNullOrEmpty()){
                        if ( MyExternalStorage(itemView.context).isImageAvailable(message.uri!!) ){
                            Glide.with(itemView.context)
                                .load(MyExternalStorage(itemView.context).getImageString(message.uri!!))
                                .dontAnimate()
                                .transform(CenterCrop(),RoundedCorners(imageCorner))
                                .placeholder(R.drawable.noimage)
                                .override((width * 0.7).roundToInt())
                                .into(pic)
                            downloadLayout.visibility = View.GONE
                            progressBar.visibility = View.GONE
                        }else{
                            val reqOpt = RequestOptions()
                                .override((width * 0.7).roundToInt())
                                .transform(BlurTransformation(25,3),RoundedCorners(imageCorner))
                                .centerCrop()

                            pic.minimumWidth = (width * 0.7).roundToInt()
                            pic.maxWidth = (width * 0.7).roundToInt()
                            pic.minimumHeight = (width * 0.7).roundToInt()
                            pic.maxHeight = (width * 0.7).roundToInt()

                            if ( MyInternalStorage(itemView.context).isPicThumbAvailable(message.uri!!) ){
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbPicString(message.uri!!))
                                    .error(R.drawable.noimage)
                                    .placeholder(R.drawable.noimage)
                                    .apply(reqOpt)
                                    .transform(CenterCrop(),BlurTransformation(25,3),RoundedCorners(
                                        blurCorner
                                    ))
                                    .dontAnimate()
                                    .override((width * 0.7).roundToInt())
                                    .dontAnimate()
                                    .into(pic)
                                //.submit()
                            }else{
                                Glide.with(itemView.context)
                                    .load(R.drawable.noimage)
                                    .dontAnimate()
                                    .placeholder(R.drawable.noimage)
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .override((width * 0.7).roundToInt())
                                    //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                    .into(pic)
                                AndroidNetworking.download( MyServerSide().getThumbPhotoUri(message.uri.toString()), MyInternalStorage(itemView.context).getThumbImageFolder(), message.uri)
                                    .setTag(message.uri)
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .startDownload(object : DownloadListener {
                                        override fun onDownloadComplete() {
                                            Glide.with(itemView.context)
                                                .load(MyInternalStorage(itemView.context).getThumbPicString(message.uri!!))
                                                .dontAnimate()
                                                .error(R.drawable.noimage)
                                                .placeholder(R.drawable.noimage)
                                                .transform(CenterCrop(),RoundedCorners(
                                                    imageCorner
                                                ))
                                                .override((width * 0.7).roundToInt())
                                                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                                .into(pic)
                                        }
                                        override fun onError(anError: ANError?) {
                                            println("download ${message.uri}  has error")
                                        }
                                    })
                            }
                            downloadLayout.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            downloadSize.text = MyTools(itemView.context).getReadableSizeFromByte(message.size!!)

                        }
                    }

                }
                "VID" -> {
                    val pic : ImageView
                    val downloadLayout : ConstraintLayout
                    val downloadSize : TextView
                    val progressbar : ProgressBar
                    val time : TextView
                    val status : ImageView

                    if (message.senderId == myId){
                        pic = itemView.findViewById(R.id.MyChat_Video_Image)
                        downloadLayout = itemView.findViewById(R.id.MyChat_Video_Download_layout)
                        downloadSize = itemView.findViewById(R.id.MyChat_Video_Size)
                        progressbar = itemView.findViewById(R.id.MyChat_Video_ProgressBar)
                        time = itemView.findViewById(R.id.MyChat_Video_Time)
                        status = itemView.findViewById(R.id.MyChat_Video_Tik)

                        setTick(message.send,message.received,message.read,status)
                    }else{
                        pic = itemView.findViewById(R.id.YourChat_Video_Image)
                        downloadLayout = itemView.findViewById(R.id.YourChat_Video_Download_Layout)
                        downloadSize = itemView.findViewById(R.id.YourChat_Video_Download_Size)
                        progressbar = itemView.findViewById(R.id.YourChat_Video_ProgressBar)
                        time = itemView.findViewById(R.id.YourChat_Video_Time)

                        val sender = itemView.findViewById<TextView>(R.id.YourChat_Video_Sender)
                        sender.visibility = View.VISIBLE
                        sender.text = DB_UserInfos(itemView.context).getUsername(message.senderId)
                    }

                    time.text = MyDateTime().gmtToLocal(message.timestamp)

                    if ( !message.uri.isNullOrEmpty() ){
                        if ( MyExternalStorage(itemView.context).isVideoAvailable(message.uri!!) ){
                            downloadLayout.visibility = View.GONE
                            progressbar.visibility = View.GONE
                            if ( MyInternalStorage(itemView.context).isVideoThumbAvailable(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")) ){
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")))
                                    .dontAnimate()
                                    .placeholder(R.drawable.novideo)
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .override((width * 0.7).roundToInt())
                                    .into(pic)
                            }else{
                                Glide.with(itemView.context)
                                    .load(R.drawable.novideo)
                                    .dontAnimate()
                                    .placeholder(R.drawable.novideo)
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .override((width * 0.7).roundToInt())
                                    .into(pic)
                                AndroidNetworking.download( MyServerSide().getThumbVideoUri(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")), MyInternalStorage(itemView.context).getThumbVideoFolder(), MyTools(itemView.context).changeFileType(message.uri!!,"jpg"))
                                    .setTag(MyTools(itemView.context).changeFileType(message.uri!!,"jpg"))
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .startDownload(object : DownloadListener {
                                        override fun onDownloadComplete() {
                                            Glide.with(itemView.context)
                                                .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")))
                                                .dontAnimate()
                                                .placeholder(R.drawable.novideo)
                                                .transform(CenterCrop(),RoundedCorners(
                                                    imageCorner
                                                ))
                                                //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                                .override((width * 0.7).roundToInt())
                                                .into(pic)
                                        }
                                        override fun onError(anError: ANError?) {
                                            println("download ${MyTools(itemView.context).changeFileType(message.uri!!,"jpg")}  has error")
                                        }
                                    })
                            }
                        }else{
                            downloadLayout.visibility = View.VISIBLE
                            progressbar.visibility = View.GONE
                            downloadSize.text = MyTools(itemView.context).getReadableSize(message.size!!)
                            if ( MyInternalStorage(itemView.context).isVideoThumbAvailable(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")) ){
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")))
                                    .dontAnimate()
                                    .placeholder(R.drawable.novideo)
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                    .override((width * 0.7).roundToInt())
                                    .into(pic)
                            }else{
                                Glide.with(itemView.context)
                                    .load(R.drawable.novideo)
                                    .dontAnimate()
                                    .placeholder(R.drawable.novideo)
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                    .override((width * 0.7).roundToInt())
                                    .into(pic)
                                AndroidNetworking.download( MyServerSide().getThumbVideoUri(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")), MyInternalStorage(itemView.context).getThumbVideoFolder(), MyTools(itemView.context).changeFileType(message.uri!!,"jpg"))
                                    .setTag(MyTools(itemView.context).changeFileType(message.uri!!,"jpg"))
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .startDownload(object : DownloadListener {
                                        override fun onDownloadComplete() {
                                            Glide.with(itemView.context)
                                                .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(message.uri!!,"jpg")))
                                                .dontAnimate()
                                                .placeholder(R.drawable.novideo)
                                                .transform(CenterCrop(),RoundedCorners(
                                                    imageCorner
                                                ))
                                                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                                .override((width * 0.7).roundToInt())
                                                .into(pic)
                                        }
                                        override fun onError(anError: ANError?) {
                                            println("download ${MyTools(itemView.context).changeFileType(message.uri!!,"jpg")}  has error")
                                        }
                                    })
                            }
                        }
                    }
                }
                "AUD" -> {
                    val play : ImageView
                    val duration : TextView
                    val seekBar : SeekBar
                    val progressBar : ProgressBar
                    val time : TextView
                    val status : ImageView
                    val layout : ConstraintLayout

                    if (message.senderId == myId){
                        play = itemView.findViewById(R.id.Mychat_Audio_Play)
                        duration = itemView.findViewById(R.id.Mychat_Audio_Duration)
                        seekBar = itemView.findViewById(R.id.Mychat_Audio_SeekBar)
                        progressBar = itemView.findViewById(R.id.MyChat_Audio_ProgressBar)
                        time = itemView.findViewById(R.id.MyChat_Audio_Time)
                        status = itemView.findViewById(R.id.MyChat_Audio_Tik)
                        layout = itemView.findViewById(R.id.MyChat_Audio_Layout)

                        setTick(message.send,message.received,message.read,status)
                    }else{
                        play = itemView.findViewById(R.id.YourChat_Audio_Play)
                        duration = itemView.findViewById(R.id.YourChat_Audio_Duration)
                        seekBar = itemView.findViewById(R.id.YourChat_Audio_SeekBar)
                        progressBar = itemView.findViewById(R.id.YourChat_Audio_ProgressBar)
                        time = itemView.findViewById(R.id.YourChat_Audio_Time)
                        layout = itemView.findViewById(R.id.YourChat_Audio_Layout)

                        val sender = itemView.findViewById<TextView>(R.id.YourChat_Audio_Sender)
                        sender.visibility = View.VISIBLE
                        sender.text = DB_UserInfos(itemView.context).getUsername(message.senderId)
                    }

                    layout.minWidth = (width * 0.7).roundToInt()
                    layout.maxWidth = (width * 0.7).roundToInt()
                    time.text = MyDateTime().gmtToLocal(message.timestamp)

                    progressBar.visibility = View.GONE

                    val audioDuration = if ( message.message !== null ){
                        MyTools(itemView.context).secondToFullTime(message.message!!.toInt())
                    }else{
                        "Unknown"
                    }
                    if ( !message.uri.isNullOrEmpty() ){
                        if ( MyExternalStorage(itemView.context).isAudioAvailable(message.uri!!) ){
                            play.setImageResource(R.drawable.ic_baseline_play_arrow)
                            seekBar.isEnabled = true
                            duration.text = audioDuration
                            if ( message.message !== null ){
                                seekBar.max = message.message!!.toInt()
                            }

                        }else{
                            play.setImageResource(R.drawable.ic_baseline_download)
                            seekBar.isEnabled = false
                            duration.text = "$audioDuration ( ${MyTools(itemView.context).getReadableSizeFromByte(message.size!!)} )"

                            play.setOnClickListener {
                                AndroidNetworking.download( MyServerSide().getAudioUri(message.uri.toString()), MyExternalStorage(itemView.context).getAudioFolder(), message.uri)
                                    .setTag(message.uri)
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .startDownload(object : DownloadListener {
                                        override fun onDownloadComplete() {
                                            play.setImageResource(R.drawable.ic_baseline_play_arrow)
                                            seekBar.isEnabled = true
                                            duration.text = audioDuration
                                            if ( message.message !== null ){
                                                seekBar.max = message.message!!.toInt()
                                            }
                                        }
                                        override fun onError(anError: ANError?) {
                                            println("download ${message.uri}  has error")
                                        }
                                    })
                            }

                        }
                    }
                }
                "FILE" -> {
                    val name : TextView
                    val time : TextView
                    val status : ImageView
                    val extension : TextView
                    val size : TextView
                    val downloadIcon : ImageView

                    if (message.senderId == myId){
                        name = itemView.findViewById(R.id.MyChat_File_Name)
                        time = itemView.findViewById(R.id.MyChat_File_Time)
                        status = itemView.findViewById(R.id.MyChat_File_Tik)
                        extension = itemView.findViewById(R.id.MyChat_File_Extension)
                        size = itemView.findViewById(R.id.MyChat_File_Size)
                        downloadIcon = itemView.findViewById(R.id.MyChat_File_DownloadIcon)

                        setTick(message.send,message.received,message.read,status)
                    }else{
                        name = itemView.findViewById(R.id.YourChat_File_Name)
                        time = itemView.findViewById(R.id.YourChat_File_Time)
                        extension = itemView.findViewById(R.id.YourChat_File_Extension)
                        size = itemView.findViewById(R.id.YourChat_File_Size)
                        downloadIcon = itemView.findViewById(R.id.YourChat_File_DownloadIcon)

                        val sender = itemView.findViewById<TextView>(R.id.YourChat_File_Sender)
                        sender.visibility = View.VISIBLE
                        sender.text = DB_UserInfos(itemView.context).getUsername(message.senderId)
                    }

                    name.text = message.uri
                    time.text = MyDateTime().gmtToLocal(message.timestamp)

                    size.text = MyTools(itemView.context).getReadableSizeFromByte(message.size!!)
                    extension.text = MyTools(itemView.context).findFileType(message.uri!!)

                    if ( MyExternalStorage(itemView.context).isFileAvailable(message.uri!!) ){
                        downloadIcon.visibility = View.GONE
                    }else{
                        downloadIcon.visibility = View.VISIBLE
                    }
                }
            }


        }

        private fun setTick(sent : Int? , received : Int? , read : Int? , imageview : ImageView){
            if (read == 1){
                imageview.setImageResource(R.drawable.ic_tik_seen_final)
            }else{
                if (received == 1){
                    imageview.setImageResource(R.drawable.ic_tik_deliverd_final)
                }else{
                    if (sent == 1){
                        imageview.setImageResource(R.drawable.ic_tik_sent_final)
                    }else{
                        imageview.setImageResource(R.drawable.ic_tik_time_final)
                    }
                }
            }
        }

    }



    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(messages[num])

        if ( num < messages.size-1 ){
            compareDates(vh , messages[num+1].timestamp , messages[num].timestamp )
        }else{
            if ( num == messages.size-1 ){
                compareDates(vh , firstTimeTimestamp, messages[num].timestamp )
            }
        }

        when (messages[num].type.toUpperCase(Locale.ROOT).trim() ) {
            "TEXT" -> {
                vh.itemView.setOnClickListener {
                    //Toast.makeText(vh.itemView.context, messages[num].message, Toast.LENGTH_SHORT).show()
                }
                vh.itemView.setOnLongClickListener {
                    //MyDialogs(vh.itemView.context).showTextMessageFragmentDialogue(messages[num])
                    true
                }
            }
            "PIC" -> {
                val mediaImage :ImageView
                val downloadLayout :ConstraintLayout
                val progress :ProgressBar

                if (messages[num].senderId == myId){
                    mediaImage = vh.itemView.findViewById(R.id.MyChat_Media_Image)
                    downloadLayout = vh.itemView.findViewById(R.id.MyChat_Media_Download_layout)
                    progress = vh.itemView.findViewById(R.id.MyChat_Media_ProgressBar)
                }else{
                    mediaImage = vh.itemView.findViewById(R.id.YourChat_Media_Image)
                    downloadLayout = vh.itemView.findViewById(R.id.YourChat_Media_Download_Layout)
                    progress = vh.itemView.findViewById(R.id.YourChat_Media_ProgressBar)
                }

                mediaImage.setOnClickListener {
                    val imageView = Intent(it.context, ImageViewActivity::class.java)
                    imageView.putExtra("title", DB_UserInfos(it.context).getUsername(messages[num].senderId))
                    imageView.putExtra("subtitle", messages[num].timestamp)
                    imageView.putExtra("uri", messages[num].uri.toString())
                    it.context.startActivity(imageView)
                }

                downloadLayout.setOnClickListener {
                    downloadLayout.visibility =View.GONE
                    progress.visibility = View.VISIBLE
                    progress.isIndeterminate = false
                    progress.max = 100
                    val width = MyTools(it.context).screenWidth()
                    AndroidNetworking.download( MyServerSide().getPhotoUri(messages[num].uri!!),MyExternalStorage(it.context).getImageFolder(),messages[num].uri )
                        .setTag(messages[num].uri)
                        .setPriority(Priority.HIGH)
                        .build()
                        .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                progress.setProgress(((bytesDownloaded / totalBytes) * 100).toInt(),true)
                            }else{
                                progress.progress = ((bytesDownloaded / totalBytes) * 100).toInt()
                            }
                        }
                        .startDownload(object : DownloadListener {
                            override fun onDownloadComplete() {
                                Glide.with(it.context)
                                    .load(MyExternalStorage(it.context).getImageString(messages[num].uri!!))
                                    .dontAnimate()
                                    .transform(CenterCrop(),RoundedCorners(imageCorner))
                                    .placeholder(R.drawable.noimage)
                                    .override((width * 0.7).roundToInt())
                                    .into(mediaImage)
                                downloadLayout.visibility = View.GONE
                                progress.visibility = View.GONE
                            }
                            override fun onError(anError: ANError?) {
                                println(anError?.message)
                            }
                        })
                }
            }
            "VID" -> {
                val videoImage :ImageView
                val downloadLayout :ConstraintLayout
                val progress :ProgressBar
                if (messages[num].senderId == myId){
                    videoImage = vh.itemView.findViewById(R.id.MyChat_Video_Image)
                    downloadLayout = vh.itemView.findViewById(R.id.MyChat_Video_Download_layout)
                    progress = vh.itemView.findViewById(R.id.MyChat_Video_ProgressBar)
                }else{
                    videoImage = vh.itemView.findViewById(R.id.YourChat_Video_Image)
                    downloadLayout = vh.itemView.findViewById(R.id.YourChat_Video_Download_Layout)
                    progress = vh.itemView.findViewById(R.id.YourChat_Video_ProgressBar)
                }

                videoImage.setOnClickListener {
                    val videoView = Intent(it.context, VideoViewActivity::class.java)
                    videoView.putExtra("title", DB_UserInfos(it.context).getUsername(messages[num].senderId))
                    videoView.putExtra("subtitle", messages[num].timestamp)
                    videoView.putExtra("uri", messages[num].uri.toString())
                    it.context.startActivity(videoView)
                }

                downloadLayout.setOnClickListener {
                    downloadLayout.visibility =View.GONE
                    progress.visibility = View.VISIBLE
                    progress.isIndeterminate = false
                    AndroidNetworking.download( MyServerSide().getVideoUri(messages[num].uri!!), MyExternalStorage(it.context).getVideoFolder(), messages[num].uri )
                        .setTag(messages[num].uri)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                progress.setProgress(((bytesDownloaded / totalBytes) * 100).toInt(),true)
                            }else{
                                progress.progress = ((bytesDownloaded / totalBytes) * 100).toInt()
                            }
                        }
                        .startDownload(object : DownloadListener {
                            val width = MyTools(it.context).screenWidth()
                            override fun onDownloadComplete() {
                                if ( MyInternalStorage(it.context).isVideoThumbAvailable(MyTools(it.context).changeFileType(messages[num].uri!!,"jpg")) ){
                                    Glide.with(it.context)
                                        .load(MyInternalStorage(it.context).getThumbVideoString(MyTools(it.context).changeFileType(messages[num].uri!!,"jpg")))
                                        .dontAnimate()
                                        .placeholder(R.drawable.novideo)
                                        .transform(CenterCrop(),RoundedCorners(imageCorner))
                                        //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                        .override((width * 0.7).roundToInt())
                                        .into(videoImage)
                                }else{
                                    Glide.with(it.context)
                                        .load(R.drawable.novideo)
                                        .dontAnimate()
                                        .placeholder(R.drawable.novideo)
                                        .transform(CenterCrop(),RoundedCorners(imageCorner))
                                        //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                        .override((width * 0.7).roundToInt())
                                        .into(videoImage)
                                    AndroidNetworking.download( MyServerSide().getThumbVideoUri(MyTools(it.context).changeFileType(messages[num].uri!!,"jpg")), MyInternalStorage(it.context).getThumbVideoFolder(), MyTools(it.context).changeFileType(messages[num].uri!!,"jpg"))
                                        .setTag(MyTools(it.context).changeFileType(messages[num].uri!!,"jpg"))
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .startDownload(object : DownloadListener {
                                            override fun onDownloadComplete() {
                                                Glide.with(it.context)
                                                    .load(MyInternalStorage(it.context).getThumbVideoString(MyTools(it.context).changeFileType(messages[num].uri!!,"jpg")))
                                                    .dontAnimate()
                                                    .placeholder(R.drawable.novideo)
                                                    .transform(CenterCrop(),RoundedCorners(
                                                        imageCorner
                                                    ))
                                                    //.apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                                                    .override((width * 0.7).roundToInt())
                                                    .into(videoImage)
                                            }
                                            override fun onError(anError: ANError?) {
                                                println("download ${MyTools(it.context).changeFileType(messages[num].uri!!,"jpg")}  has error")
                                            }
                                        })
                                }
                            }
                            override fun onError(anError: ANError?) {
                                println(anError?.message)
                            }
                        })
                }

            }
            "AUD" -> {
                val playButton: ImageView
                val seekBar: AppCompatSeekBar
                val progressBar : ProgressBar
                val currentDuration : TextView
                //val layout: ConstraintLayout
                if (messages[num].senderId == myId) {
                    playButton = vh.itemView.findViewById(R.id.Mychat_Audio_Play)
                    seekBar = vh.itemView.findViewById(R.id.Mychat_Audio_SeekBar)
                    progressBar = vh.itemView.findViewById(R.id.MyChat_Audio_ProgressBar)
                    currentDuration = vh.itemView.findViewById(R.id.Mychat_Audio_Duration)
                    //layout = vh.itemView.findViewById(R.id.MyChat_Audio_Layout)
                } else {
                    playButton = vh.itemView.findViewById(R.id.YourChat_Audio_Play)
                    seekBar = vh.itemView.findViewById(R.id.YourChat_Audio_SeekBar)
                    progressBar = vh.itemView.findViewById(R.id.YourChat_Audio_ProgressBar)
                    currentDuration = vh.itemView.findViewById(R.id.YourChat_Audio_Duration)
                    //layout = vh.itemView.findViewById(R.id.YourChat_Audio_Layout)
                }
                if ( MyExternalStorage(vh.itemView.context).isAudioAvailable(messages[num].uri!!) ){
                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged( seekBar: SeekBar?, progress: Int, fromUser: Boolean ) {
                            if ( fromUser && audioPlayingName == messages[num].uri ){
                                mediaPlayer.seekTo(progress * 1000)
                            }
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {         }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {        }
                    })
                    playButton.setOnClickListener {
                        if ( audioPlayingName == messages[num].uri ){
                            if ( mediaPlayer !== null && mediaPlayer.isPlaying ){
                                mediaPlayer.pause()
                                playButton.setImageResource(R.drawable.ic_baseline_play_arrow)
                            }else{
                                mediaPlayer.start()
                                playButton.setImageResource(R.drawable.ic_baseline_pause)
                            }
                        }else{
                            mediaPlayer.setDataSource(MyExternalStorage(vh.itemView.context).getAudioString(messages[num].uri!!))
                            mediaPlayer.prepare()
                            mediaPlayer.setOnPreparedListener {
                                audioPlayingName = messages[num].uri!!
                                mediaPlayer.start()
                                playButton.setImageResource(R.drawable.ic_baseline_pause)
                                makeSeekHandler(vh.itemView.context,seekBar,currentDuration)
                            }
                            mediaPlayer.setOnSeekCompleteListener {
                                playButton.setImageResource(R.drawable.ic_baseline_play_arrow)
                            }
                        }
                        if ( mediaPlayer.isPlaying ){
                            makeSeekHandler(vh.itemView.context,seekBar,currentDuration)
                        }else{
                            seekTimer.cancel()
                            seekTimer.purge()
                        }

                    }
                }else{
                    playButton.setOnClickListener {
                        AndroidNetworking.download( MyServerSide().getAudioUri(messages[num].uri!!), MyExternalStorage(it.context).getAudioFolder(), messages[num].uri )
                            .setTag(messages[num].uri)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                                progressBar.visibility = View.VISIBLE
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    progressBar.setProgress(((bytesDownloaded / totalBytes) * 100).toInt(),true)
                                }else{
                                    progressBar.progress = ((bytesDownloaded / totalBytes) * 100).toInt()
                                }
                            }
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    progressBar.visibility = View.GONE
                                    println("Download complete")
                                    notifyDataSetChanged()
                                }
                                override fun onError(anError: ANError?) {
                                    println(anError?.message)
                                    progressBar.visibility = View.GONE
                                }
                            })
                    }
                }


            }
            "FILE" -> {
                val size : TextView
                val layout: ConstraintLayout

                if (messages[num].senderId == myId) {
                    layout = vh.itemView.findViewById(R.id.MyChat_File_Layout)
                    size =  vh.itemView.findViewById(R.id.MyChat_File_Size)
                }else{
                    layout = vh.itemView.findViewById(R.id.YourChat_File_Layout)
                    size =  vh.itemView.findViewById(R.id.YourChat_File_Size)
                }

                if ( MyExternalStorage(vh.itemView.context).isFileAvailable(messages[num].uri!!) ){
                    layout.setOnClickListener {
                        val path = MyExternalStorage(vh.itemView.context).getFileString(messages[num].uri!!)
                        val targetIntent = Intent(Intent.ACTION_VIEW)
                        when ( MyTools(vh.itemView.context).findFileType(messages[num].uri!!).toUpperCase(Locale.ROOT) ){
                            "JPG" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"image/jpeg")
                            "JPEG" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"image/jpeg")
                            "PNG" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"image/jpeg")
                            "GIF" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"image/gif")
                            "WAV" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"audio/x-wav")
                            "MP3" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"audio/x-wav")
                            "3GP" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "MPG" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "MPEG" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "MPE" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "MP4" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "AVI" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"video/*")
                            "PDF" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/pdf")
                            "DOC" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/msword")
                            "DOCX" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/msword")
                            "PPT" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/vnd.ms-powerpoint")
                            "PPTX" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/vnd.ms-powerpoint")
                            "XLS" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/vnd.ms-excel")
                            "XLSX" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/vnd.ms-excel")
                            "TXT" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"text/plain")
                            "ZIP" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/zip")
                            "RAR" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/x-rar-compressed")
                            "RTF" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/rtf")
                            //"ZIP" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/zip")
                            //"ZIP" -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"application/zip")
                            else -> targetIntent.setDataAndType(MyExternalStorage(vh.itemView.context).getFileUri(path),"*/*")
                        }
                        //targetIntent.setDataAndType(MyInternalStorage(itemView.context).getFileUri(file.path!!),"application/pdf")
                        targetIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        vh.itemView.context.startActivity(targetIntent)
                    }
                }else{
                    layout.setOnClickListener {
                        AndroidNetworking.download( MyServerSide().getFileUri(messages[num].uri!!), MyExternalStorage(it.context).getFileFolder(), messages[num].uri )
                            .setTag(messages[num].uri)
                            .setPriority(Priority.HIGH)
                            .build()
                            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                                size.text = "Downloading( ${((bytesDownloaded / totalBytes) * 100).toInt()} )"
                            }
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    println("Download complete")
                                    notifyDataSetChanged()
                                }
                                override fun onError(anError: ANError?) {
                                    println(anError?.message)
                                }
                            })
                    }
                }


            }
        }
    }

    private fun compareDates(vh: ViewHolder, previousDate : String?, currentDate : String ){
        //println("in compare dates -- lastDate : $previousDate ")
        //println("in compare dates -- newDate : $currentDate ")
        if ( previousDate == null || previousDate == ""){
            //oldDate = newDate
            return
        }
        val dif = MyDateTime().DayDiff(previousDate, currentDate)
        //println("Day diff of item number ${vh.adapterPosition} is : $dif ........")
        val dateLayout = vh.itemView.findViewById<LinearLayout>(R.id.ChatDateBubble_Layout)
        val dateText = vh.itemView.findViewById<TextView>(R.id.ChatDateBubble_Text)
        if ( dif > 0 ) {
            dateLayout.visibility = View.VISIBLE
            val dayDiffToToday = MyDateTime().DayDiff(currentDate)
            when (dayDiffToToday) {
                -1 -> dateText.text = "Today"
                0 -> dateText.text = "Today"
                1 -> dateText.text = "Yesterday"
                2 -> dateText.text = MyDateTime().getDayOfWeek(2)
                3 -> dateText.text = MyDateTime().getDayOfWeek(3)
                4 -> dateText.text = MyDateTime().getDayOfWeek(4)
                5 -> dateText.text = MyDateTime().getDayOfWeek(5)
                6 -> dateText.text = MyDateTime().getDayOfWeek(6)
                else -> dateText.text = MyDateTime().gmtToLocal(currentDate, "yyyy / MM / dd")
            }
        }else{
            dateLayout.visibility = View.GONE
        }
    }

    private fun makeSeekHandler(context : Context, seekBar : SeekBar, currentDuration : TextView, ) {
        seekTimer = Timer("seekAudio")
        seekTimer.schedule(object : TimerTask() {
            override fun run() {
                ( context as Activity).runOnUiThread {
                    seekBar.progress = mediaPlayer.currentPosition / 1000
                    currentDuration.text =  "${MyTools(context).secondToFullTime(mediaPlayer.duration/1000)} ( ${MyTools(context).secondToFullTime(
                        mediaPlayer.currentPosition / 1000)} )"
                }
            }

        }, seekDelay, seekPeriod)
    }

}