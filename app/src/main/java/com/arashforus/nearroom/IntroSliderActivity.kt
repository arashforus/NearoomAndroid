package com.arashforus.nearroom

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.arashforus.nearroom.databinding.ActivityIntroSliderBinding

class IntroSliderActivity : AppCompatActivity() {

    lateinit var binding : ActivityIntroSliderBinding

    var slideNumber = 0
    val animationDuration = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transparentStatusBar()
        val sliders = getSliders()
        val adapter = Adapter_IntroSlider(sliders)
        binding.IntroSliderActivityViewPager.adapter = adapter
        binding.IntroSliderActivityViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapter.notifyDataSetChanged()

                selectDot(position)
                slideNumber = position
                if ( position == 4 ){
                    binding.IntroSliderActivityStartButton.text = "START"
                }else{
                    binding.IntroSliderActivityStartButton.text = "NEXT"

                }

                Handler().postDelayed({

                    val pic = findViewById<ImageView>(R.id.IntroSlide_Pic)
                    val pic2 = findViewById<ImageView>(R.id.IntroSlide_Pic2)
                    val pic3 = findViewById<ImageView>(R.id.IntroSlide_Pic3)

                    when (position) {
                        0 -> {
                            // Background Animation ////////////////////////////////////////////////
                            pic.translationX = -100f
                            pic.alpha = 0.0f
                            pic.animate()
                                .translationXBy(100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                            // Phone Animation /////////////////////////////////////////////////////
                            pic2.scaleX = 0f
                            pic2.scaleY = 0f
                            pic2.alpha = 0.0f
                            pic2.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .start()
                            // Emotions Animation //////////////////////////////////////////////////
                            pic3.translationX = 100f
                            pic3.alpha = 0.0f
                            pic3.animate()
                                .translationXBy(-100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                        }
                        1 -> {
                            // Map Animation ///////////////////////////////////////////////////////
                            pic.scaleX = 0f
                            pic.scaleY = 0f
                            //pic.alpha = 0.0f
                            pic.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                            // Peoples Animation ///////////////////////////////////////////////////
                            pic2.scaleX = 0f
                            pic2.scaleY = 0f
                            //pic2.alpha = 0.0f
                            pic2.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .start()
                            // Dots Animation //////////////////////////////////////////////////////
                            pic3.translationY = 100f
                            pic3.alpha = 0.0f
                            pic3.animate()
                                .translationYBy(-100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                        }

                        2 -> {
                            // Background Animation ////////////////////////////////////////////////
                            pic.scaleX = 0f
                            pic.scaleY = 0f
                            pic.alpha = 0.0f
                            pic.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                            // Device Animation ////////////////////////////////////////////////////
                            pic2.scaleX = 0f
                            pic2.scaleY = 0f
                            pic2.alpha = 0.0f
                            pic2.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .start()
                            // Dots Animation //////////////////////////////////////////////////////
                            pic3.translationY = 100f
                            pic3.alpha = 0.0f
                            pic3.animate()
                                .translationYBy(-100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .withEndAction {
                                    pic3.animate()
                                        .scaleXBy(0.2f)
                                        .scaleYBy(0.2f)
                                        .setInterpolator(BounceInterpolator())
                                        .setDuration(1000)
                                        .setUpdateListener {
                                            it.repeatMode = ValueAnimator.REVERSE
                                            it.repeatCount = ValueAnimator.INFINITE
                                        }
                                        .start()
                                }
                                .start()
                        }

                        3 -> {
                            // Background Animation ////////////////////////////////////////////////
                            pic.scaleX = 0f
                            pic.scaleY = 0f
                            pic.alpha = 0.0f
                            pic.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                            // Device Animation ////////////////////////////////////////////////////
                            pic2.scaleX = 0f
                            pic2.scaleY = 0f
                            pic2.alpha = 0.0f
                            pic2.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .start()
                            // Emotions Animation //////////////////////////////////////////////////
                            pic3.translationY = 100f
                            pic3.alpha = 0.0f
                            pic3.animate()
                                .translationYBy(-100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                        }

                        4 -> {
                            // Background Animation ////////////////////////////////////////////////
                            pic.scaleX = 0f
                            pic.scaleY = 0f
                            pic.alpha = 0.0f
                            pic.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                            // Device Animation ////////////////////////////////////////////////////
                            pic2.scaleX = 0f
                            pic2.scaleY = 0f
                            pic2.alpha = 0.0f
                            pic2.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .start()
                            // Emotions Animation //////////////////////////////////////////////////
                            pic3.translationY = 100f
                            pic3.alpha = 0.0f
                            pic3.animate()
                                .translationYBy(-100f)
                                .alpha(1.0f)
                                .setInterpolator(AccelerateInterpolator())
                                .setDuration(animationDuration)
                                .setStartDelay(100)
                                .start()
                        }

                    }

                },300)



            }

        })

        binding.IntroSliderActivityStartButton.setOnClickListener {
            if ( slideNumber == 4 ){
                MySharedPreference(MySharedPreference.PreferenceApp,this).save(MySharedPreference.App_IntroShowed,true)
                val nextIntent = Intent(this,LoginActivity::class.java)
                startActivity(nextIntent)
            }else{
                binding.IntroSliderActivityViewPager.setCurrentItem(slideNumber+1,true)
            }

        }

    }

    private fun getSliders(): ArrayList<Object_Slider> {
        val sliders = ArrayList<Object_Slider>()
        val titles = resources.getStringArray(R.array.intro_slider_titles)
        val desc = resources.getStringArray(R.array.intro_slider_Desc)
        val images = arrayOf(R.drawable.intro_simple_background,R.drawable.intro_nearby_map,R.drawable.intro_live_background,R.drawable.intro_free_background,R.drawable.intro_touch_background)
        val images2 = arrayOf(R.drawable.intro_simple_phone,R.drawable.intro_nearby_locators,R.drawable.intro_live_phone,R.drawable.intro_free_phone,R.drawable.intro_touch_character)
        val images3 = arrayOf(R.drawable.intro_simple_emotion,R.drawable.intro_nearby_people,R.drawable.intro_live_dots,R.drawable.intro_free_coins,R.drawable.intro_touch_notification)
        val colors = arrayOf(R.color.Slider1Color,R.color.Slider2Color,R.color.Slider3Color,R.color.Slider1Color,R.color.Slider2Color)
        for ( i in titles.indices){
            sliders.add(Object_Slider(titles[i],desc[i],colors[i],images[i],images2[i],images3[i]))
        }
        return sliders
    }

    private fun transparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun selectDot(number: Int){
        binding.IntroSliderActivityDot1.setImageResource(R.drawable.ic_baseline_dot_empty)
        binding.IntroSliderActivityDot2.setImageResource(R.drawable.ic_baseline_dot_empty)
        binding.IntroSliderActivityDot3.setImageResource(R.drawable.ic_baseline_dot_empty)
        binding.IntroSliderActivityDot4.setImageResource(R.drawable.ic_baseline_dot_empty)
        binding.IntroSliderActivityDot5.setImageResource(R.drawable.ic_baseline_dot_empty)
        binding.IntroSliderActivityDot1.scaleX = 1.0f
        binding.IntroSliderActivityDot1.scaleY = 1.0f
        binding.IntroSliderActivityDot2.scaleX = 1.0f
        binding.IntroSliderActivityDot2.scaleY = 1.0f
        binding.IntroSliderActivityDot3.scaleX = 1.0f
        binding.IntroSliderActivityDot3.scaleY = 1.0f
        binding.IntroSliderActivityDot4.scaleX = 1.0f
        binding.IntroSliderActivityDot4.scaleY = 1.0f
        binding.IntroSliderActivityDot5.scaleX = 1.0f
        binding.IntroSliderActivityDot5.scaleY = 1.0f

        when ( number ){
            0 -> {
                binding.IntroSliderActivityDot1.setImageResource(R.drawable.ic_baseline_circle_full)
                binding.IntroSliderActivityDot1.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(200)
                    .start()
            }
            1 -> {
                binding.IntroSliderActivityDot2.setImageResource(R.drawable.ic_baseline_circle_full)
                binding.IntroSliderActivityDot2.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(200)
                    .start()
            }
            2 -> {
                binding.IntroSliderActivityDot3.setImageResource(R.drawable.ic_baseline_circle_full)

                binding.IntroSliderActivityDot3.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(200)
                    .start()
            }
            3 -> {
                binding.IntroSliderActivityDot4.setImageResource(R.drawable.ic_baseline_circle_full)
                binding.IntroSliderActivityDot4.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(200)
                    .start()
            }
            4 -> {
                binding.IntroSliderActivityDot5.setImageResource(R.drawable.ic_baseline_circle_full)
                binding.IntroSliderActivityDot5.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(200)
                    .start()
            }
        }
    }


}

