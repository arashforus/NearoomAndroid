package com.arashforus.nearroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class Adapter_Stickers(val stickers: ArrayList<String> , val callback : onItemClicked) : RecyclerView.Adapter<Adapter_Stickers.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_sticker, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(sticker: String) {
            val text = itemView.findViewById(R.id.Sticker_Text) as TextView
            text.text = sticker
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(stickers[num])
        vh.itemView.findViewById<ConstraintLayout>(R.id.Sticker_Layout).setOnClickListener {
            callback.onStickerClick(stickers[num])
        }
    }

    interface onItemClicked{
        fun onStickerClick(emoji : String)
    }

}