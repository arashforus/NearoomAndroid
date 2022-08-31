package com.arashforus.nearroom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.view.View
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ImageView
import java.util.*



class BlurView(context: Context, attrs: AttributeSet) : View(context,attrs) {

    var view : View? = null
    val screenwidth = MyTools(context).screenWidth()
    val screenheight = MyTools(context).screenHeight()
    val paint = Paint().apply {
        style = Paint.Style.FILL
        isDither = true
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        setMaskFilter(BlurMaskFilter(100f, BlurMaskFilter.Blur.NORMAL))

    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if ( view != null){
            val newbitmap = createScreenShot(view!!,screenwidth,screenheight)
            canvas?.drawBitmap(newbitmap!!,0f,0f,paint)
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if ( visibility == View.VISIBLE) {
            view = parent as View
            postInvalidate()
        }else{
            //view = null
        }

    }

    private fun createScreenShot(view: View, scaledViewWidth: Int, scaledViewHeight: Int): Bitmap? {
        val localBitmap = Bitmap.createBitmap(scaledViewWidth, scaledViewHeight, Bitmap.Config.ARGB_8888 )
        val localCanvas: Canvas = Canvas(localBitmap)
        view.draw(localCanvas)
        return localBitmap
    }


    fun show(){
        view?.visibility = View.VISIBLE
        postInvalidate()
    }

    fun update(){
        postInvalidate()
    }

}