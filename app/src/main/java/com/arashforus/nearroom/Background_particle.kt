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

class Background_particle(context: Context, attrs: AttributeSet) : View(context, attrs){

    private var minCircleRadious = 5
    private var maxCircleRadious = 15
    private var circleNums = 150
    //private var isCustomDimension = false
    private var maxWidth = 500
    private var maxHeight = 1000
    private var minAnimateTime = 30000L
    private var maxAnimateTime = 60000L

    var timer =Timer()

    private var backgroundMyColor = R.color.sparkleWallpaperBackgrundColor
    private var circleColor = R.color.sparkleWallpaperDotColor

    private val gradient = LinearGradient(0f,0f,1000f,2000f,Color.BLACK,Color.WHITE,Shader.TileMode.MIRROR)

    private val circles = arrayListOf<particle_circle>()

    private val circlePaint = Paint().apply {
        color = resources.getColor(circleColor)
        style = Paint.Style.FILL
        alpha = 120
        isDither = true
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        setMaskFilter(BlurMaskFilter(5f,BlurMaskFilter.Blur.NORMAL))
        setBlendMode(BlendModeCompat.MODULATE)
    }

    private val backgroundPaint = Paint().apply {
        color = resources.getColor(backgroundMyColor)

    }

    //val overlayPaint = Paint().apply {
    //    shader = gradient
    //    setBlendMode(BlendModeCompat.OVERLAY)
    //    setLayerType(LAYER_TYPE_SOFTWARE,null)
    //    maskFilter = BlurMaskFilter(15f,BlurMaskFilter.Blur.NORMAL)
    //}


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPaint(backgroundPaint)
        //canvas?.drawColor(resources.getColor(backgroundColor))
        circles.forEach {
            it.paint.alpha = Random.nextInt(50,200)
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
            circles.add(particle_circle(Random.nextFloat()*maxWidth,Random.nextFloat()*maxHeight,
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
        },100,50)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer.cancel()
        timer.purge()
    }

    fun setNumbers ( num: Int ) {
        when ( num ){
            0 -> circleNums = 50
            1 -> circleNums = 100
            2 -> circleNums = 150
            3 -> circleNums = 200
            4 -> circleNums = 250
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setSize ( num: Int ) {
        when ( num ){
            0 -> {
                minCircleRadious = 2
                maxCircleRadious = 10
            }
            1 -> {
                minCircleRadious = 5
                maxCircleRadious = 15
            }
            2 -> {
                minCircleRadious = 10
                maxCircleRadious = 25
            }
            3 -> {
                minCircleRadious = 15
                maxCircleRadious = 30
            }
            4 -> {
                minCircleRadious = 20
                maxCircleRadious = 40
            }
        }
        this.invalidate()
        onDetachedFromWindow()
        onAttachedToWindow()
    }

    fun setSpeed ( num: Int ) {
        when ( num ){
            0 -> {
                minAnimateTime = 40000L
                maxAnimateTime = 75000L
            }
            1 -> {
                minAnimateTime = 30000L
                maxAnimateTime = 60000L
            }
            2 -> {
                minAnimateTime = 20000L
                maxAnimateTime = 45000L
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
private class particle_circle( ){
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