package com.arashforus.nearroom

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class Adapter_GalleryFiles(private val files : ArrayList<Object_Media>) : RecyclerView.Adapter<Adapter_GalleryFiles.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_last_files, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(files[num])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(file: Object_Media) {
            val name = itemView.findViewById(R.id.LastFileItem_Name) as TextView
            val size  = itemView.findViewById(R.id.LastFileItem_Size) as TextView
            val extension  = itemView.findViewById(R.id.LastFileItem_Extension) as TextView
            //val button  = itemView.findViewById(R.id.LastFileItem_SeeAllButton) as Button
            val layout  = itemView.findViewById(R.id.LastFileItem_Layout) as ConstraintLayout
            val icon = itemView.findViewById(R.id.LastFileItem_Icon) as ImageView

            if ( ( itemView.context as Activity).localClassName == "GalleryFileActivity" ) {
                name.setTextColor(Color.BLACK)
                extension.setTextColor(Color.WHITE)
                icon.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }

            name.text = file.path
            size.text = MyTools(itemView.context).getReadableSizeFromByte(file.size!!)
            extension.text = MyTools(itemView.context).findFileType(file.path!!)

            layout.setOnClickListener {
                val targetIntent = Intent(Intent.ACTION_VIEW)
                when ( MyTools(itemView.context).findFileType(file.path!!).toUpperCase(Locale.ROOT) ){
                    "JPG" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"image/jpeg")
                    "JPEG" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"image/jpeg")
                    "PNG" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"image/jpeg")
                    "GIF" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"image/gif")
                    "WAV" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"audio/x-wav")
                    "MP3" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"audio/x-wav")
                    "3GP" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "MPG" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "MPEG" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "MPE" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "MP4" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "AVI" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"video/*")
                    "PDF" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/pdf")
                    "DOC" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/msword")
                    "DOCX" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/msword")
                    "PPT" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/vnd.ms-powerpoint")
                    "PPTX" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/vnd.ms-powerpoint")
                    "XLS" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/vnd.ms-excel")
                    "XLSX" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/vnd.ms-excel")
                    "TXT" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"text/plain")
                    "ZIP" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/zip")
                    "RAR" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/x-rar-compressed")
                    "RTF" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/rtf")
                    //"ZIP" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/zip")
                    //"ZIP" -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"application/zip")
                    else -> targetIntent.setDataAndType(MyExternalStorage(itemView.context).getFileUri(file.path!!),"*/*")
                }
                //targetIntent.setDataAndType(MyInternalStorage(itemView.context).getFileUri(file.path!!),"application/pdf")
                targetIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                itemView.context.startActivity(targetIntent)
            }

        }
    }
}