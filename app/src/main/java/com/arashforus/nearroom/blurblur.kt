package com.arashforus.nearroom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.appcompat.widget.DrawableUtils

class blurblur(val context: Context , val view: View) {

    val screenwidth = MyTools(context).screenWidth()
    val screenheight = MyTools(context).screenHeight()
    val paint = Paint().apply {
        //setLayerType(View.LAYER_TYPE_SOFTWARE,null)
        color = Color.YELLOW
        setMaskFilter(BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL))
    }

    private fun createScreenShot(view: View, scaledViewWidth: Int, scaledViewHeight: Int): Bitmap? {
        val localBitmap = Bitmap.createBitmap(scaledViewWidth, scaledViewHeight, Bitmap.Config.ARGB_8888 )
        val localCanvas = Canvas(localBitmap)
        view.draw(localCanvas)
        return localBitmap
    }

    fun show(){
        val newbitmap = createScreenShot(view,screenwidth,screenheight)
        view.visibility = View.VISIBLE
        view.background = BitmapDrawable(null,newbitmap)
        //view.postInvalidate()
        //view.setBackgroundColor(R.color.myChatMessageColor)
        //view.
    }

}