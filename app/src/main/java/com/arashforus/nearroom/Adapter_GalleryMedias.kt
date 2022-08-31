package com.arashforus.nearroom

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList

class Adapter_GalleryMedias(private val medias : ArrayList<Object_Media>) : RecyclerView.Adapter<Adapter_GalleryMedias.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_media_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(medias[num])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(media: Object_Media) {
            val pic = itemView.findViewById(R.id.MediaItem_Pic) as ImageView
            val size  = itemView.findViewById(R.id.MediaItem_Size) as TextView
            val text  = itemView.findViewById(R.id.MediaItem_Text) as TextView
            val checkBox  = itemView.findViewById(R.id.MediaItem_CheckBox) as CheckBox
            val layout  = itemView.findViewById(R.id.MediaItem_Layout) as ConstraintLayout
            val height = MyTools(itemView.context).screenWidth() / 3

            when ( media.type?.toUpperCase(Locale.ROOT) ){
                "AUDIO" -> {
                    val artworkUri = Uri.parse("content://media/external/audio/albumart")
                    //println("Album Id : ${media.albumId}")
                    val path = if ( media.albumId !== null ){
                        ContentUris.withAppendedId(artworkUri, media.albumId!!.toLong())
                    }else{
                        null
                    }
                    Glide.with(itemView.context)
                        .load(path)
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage)
                        .override(height)
                        .centerCrop()
                        .into(pic)
                    text.visibility = View.VISIBLE
                    text.text = media.name
                }
                else -> {
                    when ( (itemView.context as Activity).intent.getStringExtra("type")?.toUpperCase(Locale.ROOT)  ){
                        "VIEWCONTACTPICTURE" , "ROOMINFOPICTURE" -> {
                            if ( MyExternalStorage(itemView.context).isImageAvailable(media.path.toString())){
                                Glide.with(itemView.context)
                                    .load(MyExternalStorage(itemView.context).getImageString(media.path.toString()))
                                    .placeholder(R.drawable.noimage)
                                    .error(R.drawable.noimage)
                                    .override(height)
                                    .centerCrop()
                                    .into(pic)
                            }else{
                                if ( MyInternalStorage(itemView.context).isPicThumbAvailable(media.path.toString()) ){
                                    Glide.with(itemView.context)
                                        .load(MyInternalStorage(itemView.context).getThumbPicString(media.path.toString()))
                                        .placeholder(R.drawable.noimage)
                                        .error(R.drawable.noimage)
                                        .override(height)
                                        .centerCrop()
                                        .into(pic)
                                }else{
                                    Glide.with(itemView.context)
                                        .load(R.drawable.noimage)
                                        .placeholder(R.drawable.noimage)
                                        .override(height)
                                        .centerCrop()
                                        .into(pic)

                                    AndroidNetworking.download( MyServerSide().getThumbPhotoUri(media.path!!), MyInternalStorage(itemView.context).getThumbImageFolder(), media.path)
                                        .setTag(media.path)
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .startDownload(object : DownloadListener {
                                            override fun onDownloadComplete() {
                                                Glide.with(itemView.context)
                                                    .load(MyInternalStorage(itemView.context).getThumbPicString(media.path.toString()))
                                                    .placeholder(R.drawable.noimage)
                                                    .error(R.drawable.noimage)
                                                    .override(height)
                                                    .centerCrop()
                                                    .into(pic)
                                            }
                                            override fun onError(anError: ANError?) {
                                                //println("download $logo  has error")
                                            }
                                        })
                                }
                            }
                        }
                        "VIEWCONTACTVIDEO" , "ROOMINFOVIDEO" -> {
                            if ( MyInternalStorage(itemView.context).isVideoThumbAvailable(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")) ){
                                println("-------------- here in available ------------------")
                                Glide.with(itemView.context)
                                    .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")))
                                    .placeholder(R.drawable.novideo)
                                    .error(R.drawable.novideo)
                                    .override(height)
                                    .centerCrop()
                                    .into(pic)
                            }else{
                                println("-------------- here in unavailable ------------------")
                                Glide.with(itemView.context)
                                    .load(R.drawable.novideo)
                                    .placeholder(R.drawable.novideo)
                                    .override(height)
                                    .centerCrop()
                                    .into(pic)

                                AndroidNetworking.download( MyServerSide().getThumbVideoUri(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")), MyInternalStorage(itemView.context).getThumbVideoFolder(), MyTools(itemView.context).changeFileType(media.path.toString(),"jpg"))
                                    .setTag(media.path)
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .startDownload(object : DownloadListener {
                                        override fun onDownloadComplete() {
                                            Glide.with(itemView.context)
                                                .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")))
                                                .placeholder(R.drawable.novideo)
                                                .error(R.drawable.novideo)
                                                .override(height)
                                                .centerCrop()
                                                .into(pic)
                                        }
                                        override fun onError(anError: ANError?) {
                                            //println("download $logo  has error")
                                        }
                                    })
                            }
                        }
                        else -> {
                            Glide.with(itemView.context)
                                .load(media.path)
                                .placeholder(R.drawable.noimage)
                                .error(R.drawable.noimage)
                                //.thumbnail(0.3f)
                                .override(height)
                                .centerCrop()
                                .into(pic)
                        }
                    }

                }
            }

            size.text = media.size?.let { MyTools(itemView.context).getReadableSizeFromByte(it) }
            checkBox.visibility = View.GONE
            layout.maxHeight = height
            layout.setOnClickListener {
                val parentActivity = itemView.context as Activity
                // Check if from select profile ////////////////////////////////////////////////////
                if ( parentActivity.intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT).trim() == "PROFILE" ){
                    val targetIntent = Intent(itemView.context,ProfileEditActivity::class.java)
                    targetIntent.putExtra("fromActivity","Profile")
                    targetIntent.putExtra("path",media.path)
                    targetIntent.putExtra("size",media.size)
                    targetIntent.putExtra("type",media.type)
                    itemView.context.startActivity(targetIntent)
                    return@setOnClickListener
                }
                // Check if from select nearoom picture ////////////////////////////////////////////
                if ( parentActivity.intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT).trim() == "ROOMINFO" ){
                    val targetIntent = Intent(itemView.context,ProfileEditActivity::class.java)
                    targetIntent.putExtra("fromActivity","roomInfo")
                    targetIntent.putExtra("roomId",parentActivity.intent.getIntExtra("roomId",0))
                    targetIntent.putExtra("path",media.path)
                    targetIntent.putExtra("size",media.size)
                    targetIntent.putExtra("type",media.type)
                    itemView.context.startActivity(targetIntent)
                    return@setOnClickListener
                }
                // Check if from select wallpaper //////////////////////////////////////////////////
                if ( parentActivity.intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT).trim() == "WALLPAPER" ){
                    val targetIntent = Intent(itemView.context,WallpaperEditActivity::class.java)
                    //MySharedPreference(MySharedPreference.PreferenceWallpaper,itemView.context).save(MySharedPreference.Wallpaper_ImagePath,media.path)
                    targetIntent.putExtra("path",media.path)
                    targetIntent.putExtra("size",media.size)
                    targetIntent.putExtra("type",media.type)
                    itemView.context.startActivity(targetIntent)
                    return@setOnClickListener
                }
                // Check if from view contact //////////////////////////////////////////////////////
                if ( parentActivity.intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT).trim() == "VIEWCONTACT" ){
                    val targetIntent = Intent(itemView.context,ImageViewActivity::class.java)
                    targetIntent.putExtra("title",media.path)
                    targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
                    targetIntent.putExtra("uri",media.path)
                    itemView.context.startActivity(targetIntent)
                    return@setOnClickListener
                }
                when ( media.type?.toUpperCase(Locale.ROOT) ){
                    "VIDEO" -> {
                        val targetIntent = Intent(itemView.context,VideoEditActivity::class.java)
                        targetIntent.putExtra("path",media.path)
                        targetIntent.putExtra("size",media.size)
                        targetIntent.putExtra("type",media.type)
                        targetIntent.putExtra("fromActivity",parentActivity.intent.getStringExtra("fromActivity"))
                        targetIntent.putExtra("toId",parentActivity.intent.getIntExtra("toId",0))
                        targetIntent.putExtra("roomId",parentActivity.intent.getIntExtra("roomId",0))
                        //targetIntent.putExtra("picProfile",parentActivity.intent.getStringExtra("picProfile"))
                        itemView.context.startActivity(targetIntent)
                    }
                    "AUDIO" -> {
                        val targetIntent = Intent(itemView.context,AudioEditActivity::class.java)
                        targetIntent.putExtra("path",media.path)
                        targetIntent.putExtra("size",media.size)
                        targetIntent.putExtra("type",media.type)
                        targetIntent.putExtra("albumId",media.albumId)
                        targetIntent.putExtra("songName",media.name)
                        targetIntent.putExtra("fromActivity",parentActivity.intent.getStringExtra("fromActivity"))
                        targetIntent.putExtra("toId",parentActivity.intent.getIntExtra("toId",0))
                        targetIntent.putExtra("roomId",parentActivity.intent.getIntExtra("roomId",0))
                        //targetIntent.putExtra("picProfile",parentActivity.intent.getStringExtra("picProfile"))
                        itemView.context.startActivity(targetIntent)
                    }
                    else -> {
                        val targetIntent = Intent(itemView.context,PhotoEditActivity::class.java)
                        targetIntent.putExtra("path",media.path)
                        targetIntent.putExtra("size",media.size)
                        targetIntent.putExtra("type",media.type)
                        targetIntent.putExtra("fromActivity",parentActivity.intent.getStringExtra("fromActivity"))
                        targetIntent.putExtra("toId",parentActivity.intent.getIntExtra("toId",0))
                        targetIntent.putExtra("roomId",parentActivity.intent.getIntExtra("roomId",0))
                        //targetIntent.putExtra("picProfile",parentActivity.intent.getStringExtra("picProfile"))
                        itemView.context.startActivity(targetIntent)
                    }
                }

            }
            layout.setOnLongClickListener {
                checkBox.visibility = View.VISIBLE
                return@setOnLongClickListener true
            }
        }
    }
}