package com.arashforus.nearroom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.setBlendMode
import java.util.*
import kotlin.random.Random

class Background_Bubbles(context: Context, attrs: AttributeSet) : View(context, attrs)
{

    private val minCircleRadious = 500
    private val maxCircleRadious = 1000
    private var circleNums = 5
    private var maxWidth = 1000
    private var maxHeight = 2000
    private var minAnimateTime = 30000L
    private var maxAnimateTime = 50000L

    var timer =Timer()

    private var backgroundMyColor = R.color.sparkleWallpaperBackgrundColor
    private var circleColor = R.color.sparkleWallpaperDotColor

    private val circles = arrayListOf<Bubble_circle>()

    private val circlePaint = Paint().apply {
        color = resources.getColor(circleColor)
        style = Paint.Style.FILL
        alpha = 140
        isDither = true
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        maskFilter = BlurMaskFilter(30f,BlurMaskFilter.Blur.NORMAL)
        setBlendMode(BlendModeCompat.PLUS)
    }

    private val backgroundPaint = Paint().apply {
        color = resources.getColor(backgroundMyColor)

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPaint(backgroundPaint)
        //canvas?.drawColor(resources.getColor(backgroundColor))
        circles.forEach {
            //it.paint.alpha = Random.nextInt(50,200)
            canvas?.drawCircle(it.xc,it.yc,it.radious,it.paint)
        }
        //canvas?.drawPaint(overlayPaint)
    }

    private fun makePath(pointsNumber:Int) : Path{
        val path = Path()
        path.reset()
        path.moveTo(Random.nextFloat()*maxWidth,Random.nextFloat()*maxHeight)
        for (i in 1..pointsNumber){
            path.lineTo(Random.nextFloat()*maxWidth , Random.nextFloat()*maxHeight)
        }
        path.close()
        return path
    }

    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        maxWidth = MyTools(context).screenWidth()
        maxHeight = MyTools(context).screenHeight()
        circles.clear()
        for (i in 1..circleNums){
            //circlePaint.alpha = Random.nextInt(50,200)
            circles.add(Bubble_circle(Random.nextFloat()*maxWidth,Random.nextFloat()*maxHeight,
                Random.nextInt(minCircleRadious,maxCircleRadious)*1f, circlePaint
            ))
        }
        circles.forEach {
            val path = makePath(2)
            ObjectAnimator.ofFloat(it,"xc","yc",path).apply {
                duration = Random.nextLong(minAnimateTime,maxAnimateTime)
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
            }.start()
        }

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                postInvalidate()
            }
        },100,100)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer.cancel()
        timer.purge()
    }

    fun setNumbers ( num: Int ) {
        when ( num ){
            0 -> circleNums = 3
            1 -> circleNums = 5
            2 -> circleNums = 7
            3 -> circleNums = 9
            4 -> circleNums = 11
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setSpeed ( num: Int ) {
        when ( num ){
            0 -> {
                minAnimateTime = 40000L
                maxAnimateTime = 65000L
            }
            1 -> {
                minAnimateTime = 30000L
                maxAnimateTime = 50000L
            }
            2 -> {
                minAnimateTime = 20000L
                maxAnimateTime = 35000L
            }
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setBackground ( color : Int ) {
        backgroundMyColor = color
        backgroundPaint.color = color
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setColor( color : Int ) {
        circleColor = color
        circlePaint.color = color
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }


}
// Object Circle /////////////////////////////////////////////////////////////////////////////////
private class Bubble_circle( ){
    var xc :Float = 0f
    var yc :Float = 0f
    var radious :Float = 0f
    var paint :Paint = Paint()

    constructor(xc:Float ,yc:Float , radious:Float , paint:Paint) : this() {
        this.xc = xc
        this.yc = yc
        this.radious = radious
        this.paint = paint
    }

    fun setxc(xc:Float){
        this.xc = xc
    }

    fun setyc(yc:Float){
        this.yc = yc
    }

}