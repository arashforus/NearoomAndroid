package com.arashforus.nearroom

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class Adapter_ViewContactMedias(private val medias : ArrayList<Object_Media>) : RecyclerView.Adapter<Adapter_ViewContactMedias.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = when ( medias[0].type?.toUpperCase(Locale.ROOT)?.trim() ){
            "FILE" -> LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_last_files, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_last_medias, parent, false)
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(medias[num])
        if ( num == 9 ){
            when ( medias[num].type?.toUpperCase(Locale.ROOT) ){
                "FILE" -> {
                    vh.itemView.findViewById<Button>(R.id.LastFileItem_SeeAllButton).visibility = View.VISIBLE
                }
                else -> {
                    vh.itemView.findViewById<Button>(R.id.LastMediaItem_SeeAllButton).visibility = View.VISIBLE
                }
            }

        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(media: Object_Media) {

            when ( media.type?.toUpperCase(Locale.ROOT) ){
                "PIC" -> {
                    val pic = itemView.findViewById(R.id.LastMediaItem_Pic) as ImageView
                    val type  = itemView.findViewById(R.id.LastMediaItem_IconType) as ImageView
                    val button  = itemView.findViewById(R.id.LastMediaItem_SeeAllButton) as Button
                    val layout  = itemView.findViewById(R.id.LastMediaItem_Layout) as ConstraintLayout

                    type.setImageResource(R.drawable.ic_menu_gallery)

                    if ( MyExternalStorage(itemView.context).isImageAvailable(media.path.toString())){
                        Glide.with(itemView.context)
                            .load(MyExternalStorage(itemView.context).getImageString(media.path.toString()))
                            .error(R.drawable.noimage)
                            .into(pic)
                    }else{
                        if ( MyInternalStorage(itemView.context).isPicThumbAvailable(media.path.toString()) ){
                            Glide.with(itemView.context)
                                .load(MyInternalStorage(itemView.context).getThumbPicString(media.path.toString()))
                                .error(R.drawable.noimage)
                                .into(pic)
                        }else{
                            Glide.with(itemView.context)
                                .load(R.drawable.noimage)
                                .into(pic)

                            AndroidNetworking.download( MyServerSide().getThumbPhotoUri(media.path!!), MyInternalStorage(itemView.context).getThumbImageFolder(), media.path)
                                .setTag(media.path)
                                .setPriority(Priority.HIGH)
                                .build()
                                .startDownload(object : DownloadListener {
                                    override fun onDownloadComplete() {
                                        Glide.with(itemView.context)
                                            .load(MyInternalStorage(itemView.context).getThumbPicString(media.path.toString()))
                                            .error(R.drawable.noimage)
                                            .into(pic)
                                    }
                                    override fun onError(anError: ANError?) {
                                        //println("download $logo  has error")
                                    }
                                })

                        }

                    }

                    layout.setOnClickListener {
                        val targetIntent = Intent(itemView.context,ImageViewActivity::class.java)
                        targetIntent.putExtra("title",media.path)
                        targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
                        targetIntent.putExtra("uri",media.path)
                        itemView.context.startActivity(targetIntent)
                    }

                    button.setOnClickListener {
                        val targetIntent = Intent(itemView.context,GalleryMediaActivity::class.java)
                        targetIntent.putExtra("title","All Images")
                        //targetIntent.putExtra("subtitle","ViewContact")
                        targetIntent.putExtra("type","ViewContactPicture")
                        targetIntent.putExtra("fromActivity","ViewContact")
                        val parentActivity = itemView.context as Activity
                        val username = parentActivity.intent.getStringExtra("username")
                        val userId = DB_UserInfos(itemView.context).getUserId(username!!)
                        targetIntent.putExtra("contactId",userId)
                        itemView.context.startActivity(targetIntent)

                    }

                }
                "VID" -> {
                    val pic = itemView.findViewById(R.id.LastMediaItem_Pic) as ImageView
                    val type  = itemView.findViewById(R.id.LastMediaItem_IconType) as ImageView
                    val button  = itemView.findViewById(R.id.LastMediaItem_SeeAllButton) as Button
                    val layout  = itemView.findViewById(R.id.LastMediaItem_Layout) as ConstraintLayout


                    if ( MyInternalStorage(itemView.context).isVideoThumbAvailable(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")) ){
                        Glide.with(itemView.context)
                            .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")))
                            .error(R.drawable.novideo)
                            .into(pic)
                    }else{
                        Glide.with(itemView.context)
                            .load(R.drawable.novideo)
                            .into(pic)

                        AndroidNetworking.download( MyServerSide().getThumbVideoUri(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")), MyInternalStorage(itemView.context).getThumbVideoFolder(), MyTools(itemView.context).changeFileType(media.path.toString(),"jpg"))
                            .setTag(media.path)
                            .setPriority(Priority.HIGH)
                            .build()
                            .startDownload(object : DownloadListener {
                                override fun onDownloadComplete() {
                                    Glide.with(itemView.context)
                                        .load(MyInternalStorage(itemView.context).getThumbVideoString(MyTools(itemView.context).changeFileType(media.path.toString(),"jpg")))
                                        .error(R.drawable.novideo)
                                        .into(pic)
                                }
                                override fun onError(anError: ANError?) {
                                    //println("download $logo  has error")
                                }
                            })

                    }

                    type.setImageResource(R.drawable.ic_menu_slideshow)

                    layout.setOnClickListener {
                        val targetIntent = Intent(itemView.context,VideoViewActivity::class.java)
                        targetIntent.putExtra("title",media.path)
                        targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
                        targetIntent.putExtra("uri",media.path)
                        itemView.context.startActivity(targetIntent)

                    }

                    button.setOnClickListener {
                        val targetIntent = Intent(itemView.context,GalleryMediaActivity::class.java)
                        targetIntent.putExtra("title","All Videos")
                        //targetIntent.putExtra("subtitle","ViewContact")
                        targetIntent.putExtra("type","ViewContactVideo")
                        targetIntent.putExtra("fromActivity","ViewContact")
                        val parentActivity = itemView.context as Activity
                        val username = parentActivity.intent.getStringExtra("username")
                        val userId = DB_UserInfos(itemView.context).getUserId(username!!)
                        targetIntent.putExtra("contactId",userId)
                        itemView.context.startActivity(targetIntent)

                    }

                }
                "FILE" -> {
                    val name = itemView.findViewById(R.id.LastFileItem_Name) as TextView
                    val size  = itemView.findViewById(R.id.LastFileItem_Size) as TextView
                    val extension  = itemView.findViewById(R.id.LastFileItem_Extension) as TextView
                    val button  = itemView.findViewById(R.id.LastFileItem_SeeAllButton) as Button
                    val layout  = itemView.findViewById(R.id.LastFileItem_Layout) as ConstraintLayout

                    name.text = media.path
                    size.text = MyTools(itemView.context).getReadableSizeFromByte(media.size!!)
                    extension.text = MyTools(itemView.context).findFileType(media.path!!)

                    button.setOnClickListener {
                        val targetIntent = Intent(itemView.context,GalleryFileActivity::class.java)
                        //targetIntent.putExtra("title",media.path)
                        //targetIntent.putExtra("subtitle",MyTools(itemView.context).getReadableSizeFromByte(media.size!!))
                        //targetIntent.putExtra("uri",media.path)
                        val parentActivity = itemView.context as Activity
                        val username = parentActivity.intent.getStringExtra("username")
                        val userId = DB_UserInfos(itemView.context).getUserId(username!!)
                        targetIntent.putExtra("contactId",userId)
                        itemView.context.startActivity(targetIntent)
                    }

                    layout.setOnClickListener {

                    }

                }
            }


        }
    }
}