package com.arashforus.nearroom

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import com.arashforus.nearroom.databinding.ActivityBackgroundLiveWallpaperBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.util.*

class BackgroundLiveWallpaperActivity : AppCompatActivity() {

    lateinit var binding : ActivityBackgroundLiveWallpaperBinding

    private var selectedWallpaper = "Particle"

    private var savedParticleNums = 2
    private var savedParticleSpeed = 1
    private var savedParticleSize = 1
    private var savedParticleBackgroundColor = R.color.sparkleWallpaperBackgrundColor
    private var savedParticleColor = R.color.sparkleWallpaperDotColor

    private var savedBubblesNums = 2
    private var savedBubblesSpeed = 1
    private var savedBubblesBackgroundColor = R.color.sparkleWallpaperBackgrundColor
    private var savedBubblesColor = R.color.sparkleWallpaperDotColor

    private var savedRainbowAlpha = 3
    private var savedRainbowSpeed = 1

    private var isParticleExtra = false
    private var isBubblesExtra = false
    private var isRainbowExtra = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackgroundLiveWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        readParametersFromSharedPref(true,true,true,true)
        setSavedValues()
        makeListeners()

        when ( selectedWallpaper.toUpperCase(Locale.ROOT)){
            "PARTICLE" -> makePreview("PARTICLE")
            "BUBBLES" -> makePreview("BUBBLES")
            "RAINBOW" -> makePreview("RAINBOW")
            else -> makePreview("PARTICLE")
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.BackgroundLiveWallpaperActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Live wallpaper"
        //supportActionBar?.subtitle = "Select and modify chat background"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun readParametersFromSharedPref(readCurrent:Boolean , readParticle : Boolean , readBubbles : Boolean , readRainbow : Boolean) {
        if ( readCurrent ){
            selectedWallpaper = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_Name,"String","Particle") as String
        }
        if ( readParticle ){
            // Read Particle Parameters ////////////////////////////////////////////////////////////////
            savedParticleNums = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_ParticleNums,"Int",2) as Int
            savedParticleSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_ParticleSpeed,"Int",1) as Int
            savedParticleSize = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_ParticleSize,"Int",1) as Int
            savedParticleBackgroundColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_ParticleBackgroundColor,"Int",0) as Int
            savedParticleColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_ParticleColor,"Int",resources.getColor(R.color.sparkleWallpaperDotColor)) as Int
        }

        if ( readBubbles ){
            // Read Bubble Parameters //////////////////////////////////////////////////////////////////
            savedBubblesNums = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_BubblesNums,"Int" , 2) as Int
            savedBubblesSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_BubblesSpeed,"Int" , 1) as Int
            savedBubblesBackgroundColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_BubblesBackgroundColor,"Int",resources.getColor(R.color.sparkleWallpaperBackgrundColor)) as Int
            savedBubblesColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_BubblesColor,"Int",resources.getColor(R.color.sparkleWallpaperDotColor)) as Int
        }

        if ( readRainbow ){
            savedRainbowAlpha = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_RainbowAlpha,"Int" , 3) as Int
            savedRainbowSpeed = MySharedPreference(MySharedPreference.PreferenceWallpaper,this)
                .load(MySharedPreference.Wallpaper_RainbowSpeed,"Int" , 1) as Int
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun setSavedValues() {
        // Set particle default ////////////////////////////////////////////////////////////////////
        binding.BackgroundLiveWallpaperActivityExtraParticleNumbersSpinner.setSelection(savedParticleNums)
        binding.BackgroundLiveWallpaperActivityExtraParticleSpeedSpinner.setSelection(savedParticleSpeed)
        binding.BackgroundLiveWallpaperActivityExtraParticleSizeSpinner.setSelection(savedParticleSize)
        binding.BackgroundLiveWallpaperActivityExtraParticleBackgroundColorView.setBackgroundColor(savedParticleBackgroundColor)
        binding.BackgroundLiveWallpaperActivityExtraParticleColorView.setBackgroundColor(savedParticleColor)

        // Set bubbles default ////////////////////////////////////////////////////////////////////
        binding.BackgroundLiveWallpaperActivityExtraBubblesNumbersSpinner.setSelection(savedBubblesNums)
        binding.BackgroundLiveWallpaperActivityExtraBubblesSpeedSpinner.setSelection(savedBubblesSpeed)
        binding.BackgroundLiveWallpaperActivityExtraBubblesBackgroundColorView.setBackgroundColor(savedBubblesBackgroundColor)
        binding.BackgroundLiveWallpaperActivityExtraBubblesColorView.setBackgroundColor(savedBubblesColor)

        // Set rainbow default ////////////////////////////////////////////////////////////////////
        binding.BackgroundLiveWallpaperActivityExtraRainbowAlphaSpinner.setSelection(savedRainbowAlpha)
        binding.BackgroundLiveWallpaperActivityExtraRainbowSpeedSpinner.setSelection(savedRainbowSpeed)
    }

    private fun makePreview(wallpaper : String){
        binding.BackgroundLiveWallpaperActivityPreviewParticle.visibility = View.GONE
        binding.BackgroundLiveWallpaperActivityPreviewBubbles.visibility = View.GONE
        binding.BackgroundLiveWallpaperActivityPreviewRainbow.visibility = View.GONE
        when (wallpaper.toUpperCase(Locale.ROOT) ){
            "PARTICLE" -> {
                binding.BackgroundLiveWallpaperActivityPreviewParticle.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setNumbers(savedParticleNums)
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setSize(savedParticleSize)
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setSpeed(savedParticleSpeed)
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setBackground(savedParticleBackgroundColor)
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setColor(savedParticleColor)
            }
            "BUBBLES" -> {
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setNumbers(savedBubblesNums)
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setSpeed(savedBubblesSpeed)
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setBackground(savedBubblesBackgroundColor)
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setColor(savedBubblesColor)
            }
            "RAINBOW" -> {
                binding.BackgroundLiveWallpaperActivityPreviewRainbow.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityPreviewRainbow.setAlpha(savedRainbowAlpha)
                binding.BackgroundLiveWallpaperActivityPreviewRainbow.setSpeed(savedRainbowSpeed)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun makeListeners() {
        binding.BackgroundLiveWallpaperActivityCancelButton.setOnClickListener {
            binding.BackgroundLiveWallpaperActivityApplyButton.text = "Apply"
            binding.BackgroundLiveWallpaperActivityCancelButton.text = "Cancel"

            if ( isParticleExtra ){
                binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityExtraParticleLayout.visibility = View.GONE
                isParticleExtra = false
                return@setOnClickListener
            }

            if ( isBubblesExtra ){
                binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityExtraBubblesLayout.visibility = View.GONE
                isBubblesExtra = false
                return@setOnClickListener
            }

            if ( isRainbowExtra ){
                binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.VISIBLE
                binding.BackgroundLiveWallpaperActivityExtraRainbowLayout.visibility = View.GONE
                isRainbowExtra = false
                return@setOnClickListener
            }

            onBackPressed()
        }

        binding.BackgroundLiveWallpaperActivityApplyButton.setOnClickListener {
            if ( isParticleExtra ){
                readParametersFromSharedPref(false,true,false,false)
                makePreview("Particle")
                setSavedValues()
                return@setOnClickListener
            }

            if ( isBubblesExtra ){
                readParametersFromSharedPref(false,false,true,false)
                makePreview("Bubbles")
                setSavedValues()
                return@setOnClickListener
            }

            if ( isRainbowExtra ){
                readParametersFromSharedPref(false,false,false,true)
                makePreview("Rainbow")
                setSavedValues()
                return@setOnClickListener
            }

            MySharedPreference(MySharedPreference.PreferenceWallpaper,this).also {
                it.save(MySharedPreference.Wallpaper_Name,selectedWallpaper)
                it.save(MySharedPreference.Wallpaper_ParticleNums,savedParticleNums)
                it.save(MySharedPreference.Wallpaper_ParticleSpeed,savedParticleSpeed)
                it.save(MySharedPreference.Wallpaper_ParticleSize,savedParticleSize)
                it.save(MySharedPreference.Wallpaper_ParticleBackgroundColor,savedParticleBackgroundColor)
                it.save(MySharedPreference.Wallpaper_ParticleColor,savedParticleColor)
                it.save(MySharedPreference.Wallpaper_BubblesNums,savedBubblesNums)
                it.save(MySharedPreference.Wallpaper_BubblesSpeed,savedBubblesSpeed)
                it.save(MySharedPreference.Wallpaper_BubblesBackgroundColor,savedBubblesBackgroundColor)
                it.save(MySharedPreference.Wallpaper_BubblesColor,savedBubblesColor)
                it.save(MySharedPreference.Wallpaper_RainbowAlpha,savedRainbowAlpha)
                it.save(MySharedPreference.Wallpaper_RainbowSpeed,savedRainbowSpeed)

            }
            onBackPressed()
        }

        binding.BackgroundLiveWallpaperActivityParticleButton.setOnClickListener {
            binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.GONE
            binding.BackgroundLiveWallpaperActivityExtraParticleLayout.visibility = View.VISIBLE
            binding.BackgroundLiveWallpaperActivityApplyButton.text = "Reset"
            binding.BackgroundLiveWallpaperActivityCancelButton.text = "Back"
            isParticleExtra = true
            makePreview("PARTICLE")
            makeParticleExtraListeners()
            selectedWallpaper = "Particle"


        }

        binding.BackgroundLiveWallpaperActivityBubblesButton.setOnClickListener {
            binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.GONE
            binding.BackgroundLiveWallpaperActivityExtraBubblesLayout.visibility = View.VISIBLE
            binding.BackgroundLiveWallpaperActivityApplyButton.text = "Reset"
            binding.BackgroundLiveWallpaperActivityCancelButton.text = "Back"
            isBubblesExtra = true
            makePreview("BUBBLES")
            makeBubblesExtraListeners()
            selectedWallpaper = "Bubbles"
        }

        binding.BackgroundLiveWallpaperActivityRainbowButton.setOnClickListener {
            binding.BackgroundLiveWallpaperActivityWallpapersLayout.visibility = View.GONE
            binding.BackgroundLiveWallpaperActivityExtraRainbowLayout.visibility = View.VISIBLE
            binding.BackgroundLiveWallpaperActivityApplyButton.text = "Reset"
            binding.BackgroundLiveWallpaperActivityCancelButton.text = "Back"
            isRainbowExtra = true
            makePreview("RAINBOW")
            makeRainbowExtraListeners()
            selectedWallpaper = "Rainbow"
        }

        binding.BackgroundLiveWallpaperActivityResetFactoryLayout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset factory")
                .setMessage("All the live wallpapers settings will be back to default parameters . Do you want to continue ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, _ ->
                    selectedWallpaper = "Particle"

                    savedParticleNums = 2
                    savedParticleSpeed = 1
                    savedParticleSize = 1
                    savedParticleBackgroundColor = resources.getColor(R.color.sparkleWallpaperBackgrundColor)
                    savedParticleColor = resources.getColor(R.color.sparkleWallpaperDotColor)

                    savedBubblesNums = 2
                    savedBubblesSpeed = 1
                    savedBubblesBackgroundColor = resources.getColor(R.color.sparkleWallpaperBackgrundColor)
                    savedBubblesColor = resources.getColor(R.color.sparkleWallpaperDotColor)

                    savedRainbowAlpha = 3
                    savedRainbowSpeed = 1

                    setSavedValues()
                    makePreview("Particle")
                    dialog.dismiss()
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    dialog.cancel()
                }
                .show()

        }

    }

    private fun makeRainbowExtraListeners() {
        binding.BackgroundLiveWallpaperActivityExtraRainbowAlphaSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedRainbowAlpha = position
                binding.BackgroundLiveWallpaperActivityPreviewRainbow.setAlpha(savedRainbowAlpha)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.BackgroundLiveWallpaperActivityExtraRainbowSpeedSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedRainbowSpeed = position
                binding.BackgroundLiveWallpaperActivityPreviewRainbow.setSpeed(savedRainbowSpeed)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    @SuppressLint("ResourceAsColor")
    private fun makeBubblesExtraListeners() {
        binding.BackgroundLiveWallpaperActivityExtraBubblesNumbersSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedBubblesNums = position
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setNumbers(savedBubblesNums)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.BackgroundLiveWallpaperActivityExtraBubblesSpeedSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedBubblesSpeed = position
                binding.BackgroundLiveWallpaperActivityPreviewBubbles.setSpeed(savedBubblesSpeed)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.BackgroundLiveWallpaperActivityExtraBubblesBackgroundColorView.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back" ) { dialog, _ ->
                    dialog?.dismiss()
                    dialog?.cancel()
                }
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    savedBubblesBackgroundColor = lastSelectedColor
                    binding.BackgroundLiveWallpaperActivityExtraBubblesBackgroundColorView.setBackgroundColor(savedBubblesBackgroundColor)
                    binding.BackgroundLiveWallpaperActivityPreviewBubbles.setBackground(savedBubblesBackgroundColor)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

        binding.BackgroundLiveWallpaperActivityExtraBubblesColorView.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back" ) { dialog, _ ->
                    dialog?.dismiss()
                    dialog?.cancel()
                }
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    savedBubblesColor = lastSelectedColor
                    binding.BackgroundLiveWallpaperActivityExtraBubblesColorView.setBackgroundColor(savedBubblesColor)
                    binding.BackgroundLiveWallpaperActivityPreviewBubbles.setColor(savedBubblesColor)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun makeParticleExtraListeners() {
        binding.BackgroundLiveWallpaperActivityExtraParticleNumbersSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                    //binding.BackgroundLiveWallpaperActivityPreviewParticle.setNumbers(parent?.getItemAtPosition(position) as Int)
                    savedParticleNums = position
                    binding.BackgroundLiveWallpaperActivityPreviewParticle.setNumbers(savedParticleNums)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }

        binding.BackgroundLiveWallpaperActivityExtraParticleSpeedSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedParticleSpeed = position
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setSpeed(savedParticleSpeed)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.BackgroundLiveWallpaperActivityExtraParticleSizeSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long  ) {
                savedParticleSize = position
                binding.BackgroundLiveWallpaperActivityPreviewParticle.setSize(savedParticleSize)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.BackgroundLiveWallpaperActivityExtraParticleBackgroundColorView.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back"  ) { dialog, _ ->
                    dialog?.dismiss()
                    dialog?.cancel()
                }
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    savedParticleBackgroundColor = lastSelectedColor
                    binding.BackgroundLiveWallpaperActivityExtraParticleBackgroundColorView.setBackgroundColor(savedParticleBackgroundColor)
                    binding.BackgroundLiveWallpaperActivityPreviewParticle.setBackground(savedParticleBackgroundColor)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

        binding.BackgroundLiveWallpaperActivityExtraParticleColorView.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back" ) { dialog, _ ->
                    dialog?.dismiss()
                    dialog?.cancel()
                }
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    savedParticleColor = lastSelectedColor
                    binding.BackgroundLiveWallpaperActivityExtraParticleColorView.setBackgroundColor(savedParticleColor)
                    binding.BackgroundLiveWallpaperActivityPreviewParticle.setColor(savedParticleColor)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

    }




}