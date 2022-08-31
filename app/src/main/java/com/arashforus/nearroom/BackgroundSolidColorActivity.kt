package com.arashforus.nearroom

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arashforus.nearroom.databinding.ActivityBackgroundSolidColorBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class BackgroundSolidColorActivity : AppCompatActivity() {

    lateinit var binding : ActivityBackgroundSolidColorBinding

    private var selectColor : Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackgroundSolidColorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val selectedColor = MySharedPreference(MySharedPreference.PreferenceWallpaper,this).load(MySharedPreference.Wallpaper_SolidColor,"Int") as Int
        selectColor = if ( selectedColor == 0 ){
            resources.getColor(R.color.solidPalletColor1)
        }else{
            selectedColor
        }
        binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)

        initToolbar()
        makeListeners()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.BackgroundSolidActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Solid Color"
        supportActionBar?.subtitle = "Select color for chat background"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_30)
    }

    private fun makeListeners() {
        binding.BackgroundSolidActivityColor1.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor1)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(1)
        }

        binding.BackgroundSolidActivityColor2.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor2)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(2)
        }

        binding.BackgroundSolidActivityColor3.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor3)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(3)
        }

        binding.BackgroundSolidActivityColor4.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor4)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(4)
        }

        binding.BackgroundSolidActivityColor5.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor5)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(5)
        }

        binding.BackgroundSolidActivityColor6.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor6)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(6)
        }

        binding.BackgroundSolidActivityColor7.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor7)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(7)
        }

        binding.BackgroundSolidActivityColor8.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor8)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(8)
        }

        binding.BackgroundSolidActivityColor9.setOnClickListener {
            selectColor = resources.getColor(R.color.solidPalletColor9)
            binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
            showSelectColor(9)
        }

        binding.BackgroundSolidActivityMoreColor.setOnClickListener {
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
                    selectColor = lastSelectedColor
                    binding.BackgroundSolidActivityPreviewLayout.setBackgroundColor(selectColor)
                    showSelectColor(10)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

        binding.BackgroundSolidActivityCancelButton.setOnClickListener {
            onBackPressed()
        }

        binding.BackgroundSolidActivityApplyButton.setOnClickListener {
            MySharedPreference(MySharedPreference.PreferenceWallpaper,this).save(MySharedPreference.Wallpaper_Name,"Solid")
            MySharedPreference(MySharedPreference.PreferenceWallpaper,this).save(MySharedPreference.Wallpaper_SolidColor,selectColor)
            onBackPressed()
        }
    }

    private fun showSelectColor(num: Int) {
        val blackColor = Color.BLACK
        binding.BackgroundSolidActivityColor1.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor2.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor3.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor4.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor5.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor6.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor7.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor8.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityColor9.setBackgroundColor(Color.TRANSPARENT)
        binding.BackgroundSolidActivityMoreColor.setBackgroundColor(Color.TRANSPARENT)
        when ( num ) {
            1 -> binding.BackgroundSolidActivityColor1.setBackgroundColor(blackColor)
            2 -> binding.BackgroundSolidActivityColor2.setBackgroundColor(blackColor)
            3 -> binding.BackgroundSolidActivityColor3.setBackgroundColor(blackColor)
            4 -> binding.BackgroundSolidActivityColor4.setBackgroundColor(blackColor)
            5 -> binding.BackgroundSolidActivityColor5.setBackgroundColor(blackColor)
            6 -> binding.BackgroundSolidActivityColor6.setBackgroundColor(blackColor)
            7 -> binding.BackgroundSolidActivityColor7.setBackgroundColor(blackColor)
            8 -> binding.BackgroundSolidActivityColor8.setBackgroundColor(blackColor)
            9 -> binding.BackgroundSolidActivityColor9.setBackgroundColor(blackColor)
            10 -> binding.BackgroundSolidActivityMoreColor.setBackgroundColor(selectColor)
        }
    }

}