package com.arashforus.nearroom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.*
import java.util.*
import kotlin.math.max

class Background_Circle(context: Context, attrs: AttributeSet) : View(context, attrs){

    private var maxWidth = 1000
    private var maxHeight = 2000
    private var maxRadius = 1000

    private val circleNumbers = 6
    private var centerX = 0f
    private var centerY = 0f
    private var lastCenterX = 0f
    private var lastCenterY = 0f
    private var alphaStart = 220
    //val alphaEnd = 0
    private val periodTime = 5000L
    private var isDown = false

    private lateinit var radiusAnimator : ObjectAnimator

    //private val backgroundColor = R.color.sparkleWallpaperBackgrundColor
    private val circleColor = R.color.colorPrimary
    private val circles = arrayListOf<Circle_circle>()
    private val circlePaint = Paint().apply {
        color = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
             resources.getColor(circleColor,resources.newTheme())
        }else{
            resources.getColor(circleColor)
        }
        style = Paint.Style.FILL
        isDither = true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        circles.forEach {
            it.paint.alpha = (( ( maxRadius - it.radius * 1 ) / maxRadius )* alphaStart ).toInt()
            canvas?.drawCircle(lastCenterX,lastCenterY,it.radius,it.paint)
        }
    }

    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        maxWidth = MyTools(context).screenWidth()
        maxHeight = MyTools(context).screenHeight()
        maxRadius = max( maxWidth, maxHeight ) / 2
        if ( lastCenterX == 0f && lastCenterY == 0f ){
            centerX = ( maxWidth / 2 ).toFloat()
            centerY = ( maxHeight / 2 ).toFloat()
        }else{
            centerX = lastCenterX
            centerY = lastCenterY
        }

        val periodStep = periodTime / ( circleNumbers-1 )
        circles.clear()
        for (i in 1 until circleNumbers){
            circles.add(Circle_circle(i,centerX,centerY,0f, circlePaint))
        }
        circles.forEach {
            radiusAnimator = ObjectAnimator.ofFloat(it,"radius",maxRadius*1f).apply {
                duration = periodTime
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                startDelay = ( it.num - 1 ) * periodStep
            }
            radiusAnimator.start()
        }

        val timer =Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                postInvalidate()
            }
        },50,50)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        lastCenterX = (right/2).toFloat()
        lastCenterY = (bottom/2).toFloat()
    }

    fun animateDown(time : Long){
        if ( !isDown ){
            ValueAnimator.ofFloat(lastCenterY,lastCenterY*2).apply {
                duration = time
                addUpdateListener {
                    lastCenterY = it.animatedValue as Float
                }
            }.start()

            ValueAnimator.ofInt(alphaStart,80).apply {
                duration = time
                addUpdateListener {
                    alphaStart = it.animatedValue as Int
                }
            }.start()
            isDown = true
        }
    }
}
// Object Circle /////////////////////////////////////////////////////////////////////////////////
class Circle_circle( ){
    var num : Int = 0
    var xc :Float = 0f
    var yc :Float = 0f
    var radius :Float = 0f
    var paint :Paint = Paint()

    constructor(num:Int ,xc:Float ,yc:Float , radius:Float , paint:Paint) : this() {
        this.num = num
        this.xc = xc
        this.yc = yc
        this.radius = radius
        this.paint = paint
    }
    /*
    fun setnum(num:Int){
        this.num = num
    }

    fun setxc(xc:Float){
        this.xc = xc
    }

    fun setyc(yc:Float){
        this.yc = yc
    }

    fun setradius(radious: Float){
        this.radius = radious
    }

    fun setpaint(paint: Paint){
        this.paint = paint
    }

    */

}