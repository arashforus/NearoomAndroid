package com.arashforus.nearroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlin.collections.ArrayList

class Adapter_IntroSlider(private val sliders : ArrayList<Object_Slider>) : RecyclerView.Adapter<Adapter_IntroSlider.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.intro_slide, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return sliders.size
    }

    override fun onBindViewHolder(vh: ViewHolder, num: Int) {
        vh.bindItems(sliders[num])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(sliders: Object_Slider) {
            val pic = itemView.findViewById(R.id.IntroSlide_Pic) as ImageView
            val pic2 = itemView.findViewById(R.id.IntroSlide_Pic2) as ImageView
            val pic3 = itemView.findViewById(R.id.IntroSlide_Pic3) as ImageView
            val title  = itemView.findViewById(R.id.IntroSlide_Title) as TextView
            val description  = itemView.findViewById(R.id.IntroSlide_Description) as TextView
            val layout  = itemView.findViewById(R.id.IntroSlide_Layout) as ConstraintLayout
            //layout.setBackgroundColor(Color.parseColor(sliders.color))
            layout.setBackgroundResource(sliders.color!!)
            Glide.with(itemView.context)
                .load(sliders.photo)
                .into(pic)
            Glide.with(itemView.context)
                .load(sliders.photo2)
                .into(pic2)
            Glide.with(itemView.context)
                .load(sliders.photo3)
                .into(pic3)
            pic.alpha = 0.0f
            pic2.alpha = 0.0f
            pic3.alpha = 0.0f
            //pic.setImageResource(sliders.photo!!)
            title.text = sliders.title
            description.text = sliders.description
            //println("in adapter ${title.text}")
        }
    }
}