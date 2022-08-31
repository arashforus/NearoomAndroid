package com.arashforus.nearroom

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.util.*
import kotlin.collections.ArrayList

class Adapter_Albums(val albums : ArrayList<Object_Album>) : RecyclerView.Adapter<Adapter_Albums.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_album_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(albums[num])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(album: Object_Album) {
            val pic = itemView.findViewById(R.id.AlbumRow_Image) as ImageView
            val name  = itemView.findViewById(R.id.AlbumRow_Name) as TextView
            val count  = itemView.findViewById(R.id.AlbumRow_Number) as TextView
            val layout  = itemView.findViewById(R.id.AlbumRow_Layout) as ConstraintLayout
            val height = MyTools(itemView.context).screenWidth() / 3
            //val options = RequestOptions()
            //    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            //    .override(160, 160)
            //    .skipMemoryCache(true)
            //.error(R.drawable.ic_image_unavailable)
            if ( album.type?.toUpperCase(Locale.ROOT) == "AUDIO" ){
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                val path = ContentUris.withAppendedId(artworkUri, album.imagePath!!.toLong())
                Glide.with(itemView.context)
                    .load(path)
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    .override(height)
                    .centerCrop()
                    .into(pic)

            }else{
                Glide.with(itemView.context)
                    .load(album.imagePath)
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    //.thumbnail(0.3f)
                    .override(height)
                    .centerCrop()
                    .into(pic)
            }

            name.text = album.folderName
            count.text = album.imageCount.toString()

            layout.maxHeight = height

            layout.setOnClickListener {
                val intent = Intent(it.context,GalleryMediaActivity::class.java)
                intent.putExtra("bucketId",album.folderId)
                intent.putExtra("bucketName",album.folderName)
                when ( album.type?.toUpperCase(Locale.ROOT)?.trim() ){
                    "VIDEO" -> {
                        intent.putExtra("type","VIDEO")
                        intent.putExtra("subtitle","${album.imageCount} videos")
                    }
                    "PICTURE" -> {
                        intent.putExtra("type","PICTURE")
                        intent.putExtra("subtitle","${album.imageCount} images")
                    }
                    "PIC" -> {
                        intent.putExtra("type","PICTURE")
                        intent.putExtra("subtitle","${album.imageCount} images")
                    }
                    "IMAGE" -> {
                        intent.putExtra("type","PICTURE")
                        intent.putExtra("subtitle","${album.imageCount} images")
                    }
                    "AUDIO" -> {
                        intent.putExtra("type","AUDIO")
                        intent.putExtra("subtitle","${album.imageCount} audios")
                    }
                }

                val parentActivity = itemView.context as Activity
                intent.putExtra("fromActivity",parentActivity.intent.getStringExtra("fromActivity"))
                intent.putExtra("toId",parentActivity.intent.getIntExtra("toId",0))
                //intent.putExtra("picProfile",parentActivity.intent.getStringExtra("picProfile"))
                intent.putExtra("roomId",parentActivity.intent.getIntExtra("roomId",0))
                itemView.context.startActivity(intent)
            }
        }
    }
}