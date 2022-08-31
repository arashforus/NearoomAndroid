package com.arashforus.nearroom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class Background_Rainbow(context: Context, attrs: AttributeSet) : View(context, attrs){

    private var colorAnimation = ValueAnimator()
    private var colorAlpha = 255
    private var updateTime = 5000L

    private var backgroundMyColor = R.color.sparkleWallpaperBackgrundColor

    private val backgroundPaint = Paint().apply {
        color = resources.getColor(backgroundMyColor)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPaint(backgroundPaint)
        //canvas?.drawColor(resources.getColor(backgroundColor))

    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val color1 = Color.argb(colorAlpha,255,0,0)
        val color2 = Color.argb(colorAlpha,255,255,0)
        val color3 = Color.argb(colorAlpha,0,255,0)
        val color4 = Color.argb(colorAlpha,0,255,255)
        val color5 = Color.argb(colorAlpha,0,0,255)
        val color6 = Color.argb(colorAlpha,255,0,255)
        val color7 = Color.argb(colorAlpha,255,0,0)

        colorAnimation = ValueAnimator.ofArgb(color1,color2,color3,color4,color5,color6,color7).also {
            it.duration = updateTime
            it.addUpdateListener {
                backgroundPaint.color = it.animatedValue as Int
                postInvalidate()
            }
            it.repeatMode = ValueAnimator.RESTART
            it.repeatCount = ValueAnimator.INFINITE
            it.start()
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        colorAnimation.pause()
        colorAnimation.cancel()
        colorAnimation.removeAllUpdateListeners()
    }

    fun setSpeed ( num: Int ) {
        when ( num ){
            0 -> updateTime = 11000L
            1 -> updateTime = 9000L
            2 -> updateTime = 7000L
            3 -> updateTime = 5000L
            4 -> updateTime = 3000L
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setAlpha ( num: Int ) {
        when ( num ){
            0 -> colorAlpha = 30
            1 -> colorAlpha = 60
            2 -> colorAlpha = 90
            3 -> colorAlpha = 120
            4 -> colorAlpha = 150
            5 -> colorAlpha = 180
            6 -> colorAlpha = 210
            7 -> colorAlpha = 255
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }


}
