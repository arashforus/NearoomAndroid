package com.arashforus.nearroom

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.arashforus.nearroom.databinding.ActivityPhotoEditBinding
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.isseiaoki.simplecropview.CropImageView
import ja.burhanrashid52.photoeditor.*
//import ja.burhanrashid52.photoeditor.*
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class PhotoEditActivity : AppCompatActivity() {

    lateinit var binding : ActivityPhotoEditBinding
    private var isCropExtraVisible = false
    private var isFilterExtraVisible = false
    private var isDrawExtraVisible = false
    private var isStickerExtraVisible = false
    private var isTextExtraVisible = false
    private var isMoreStickersVisible = false
    private var isEraserMode = false

    var path : String? = null
    var size : Int? = null
    var type : String? = null

    lateinit var mPhotoEditor : PhotoEditor
    private val maxBrushSize = 70f
    private val minBrushSize = 10f
    private val defaultBrushSize = 40f

    private var defaultTextColor : Int = 0
    private var defaultBrushColor : Int = 0
    private var numberOfViews = 0
    private var emojis = ArrayList<String>()

    private val WRITE_EXTERNAL_STORAGE_CODE = 1701

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        path = intent.getStringExtra("path")
        size = intent.getIntExtra("size",0)
        type = intent.getStringExtra("type")
        defaultTextColor = resources.getColor(R.color.textPalletColor1)
        defaultBrushColor = resources.getColor(R.color.textPalletColor1)

        initToolbar()
        initEmojiAdapter()
        makeDefaultExtra()
        setupPhotoEditor()
        makeListeners()
        animateLayoutChange()
    }

    private fun setupPhotoEditor() {
        binding.PhotoEditActivityPhotoEditor.source.setImageDrawable(Drawable.createFromPath(path))
        mPhotoEditor = PhotoEditor.Builder(this,binding.PhotoEditActivityPhotoEditor)
            .setPinchTextScalable(true)
            .build()
    }

    private fun initEmojiAdapter() {
        emojis = PhotoEditor.getEmojis(this)
        val adapter = Adapter_Stickers(emojis, object : Adapter_Stickers.onItemClicked {
            override fun onStickerClick(emoji: String) {
                mPhotoEditor.addEmoji(emoji)
                binding.PhotoEditActivityStickers.visibility = View.GONE
                isMoreStickersVisible = false
                binding.PhotoEditActivityExtraStickerText7.text = "More"
            }
        })
        binding.PhotoEditActivityStickersRecyclerView.layoutManager = GridLayoutManager(this, 6)
        binding.PhotoEditActivityStickersRecyclerView.setHasFixedSize(true)
        binding.PhotoEditActivityStickersRecyclerView.adapter = adapter
    }

    private fun initToolbar() {
        setSupportActionBar(binding.PhotoEditActivityToolbar)
        supportActionBar?.title = "Edit Photo"
        if ( size !== null  ){
            supportActionBar?.subtitle = "Size : ${MyTools(this).getReadableSizeFromByte(size!!)}"
        }
    }

    private fun makeListeners() {
        binding.PhotoEditActivityCropLayout.setOnClickListener {
            hideAllExtras()
            if ( isCropExtraVisible ){
                binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
                setAllToFalse()
            }else{
                binding.PhotoEditActivityExtraCropLayout.visibility = View.VISIBLE
                binding.PhotoEditActivityCropImageView.visibility = View.VISIBLE
                selectTools("CROP")
                mPhotoEditor.saveAsBitmap(object : OnSaveBitmap {
                    override fun onBitmapReady(saveBitmap: Bitmap?) {
                        Glide.with(this@PhotoEditActivity)
                            .load(saveBitmap)
                            .into(binding.PhotoEditActivityCropImageView)
                        binding.PhotoEditActivityCropImageView.setCropMode(CropImageView.CropMode.FREE)
                        binding.PhotoEditActivityCropImageView.setInitialFrameScale(1f)
                        makeExtraCropListeners()
                    }

                    override fun onFailure(e: Exception?) {
                        Toast.makeText(this@PhotoEditActivity,"Something wrong",Toast.LENGTH_SHORT).show()
                    }

                })
                setAllToFalse()
                isCropExtraVisible = true
            }
        }

        binding.PhotoEditActivityFilterLayout.setOnClickListener {
            hideAllExtras()
            if ( isFilterExtraVisible ){
                binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
                setAllToFalse()
            }else{
                binding.PhotoEditActivityExtraFilterLayout.visibility = View.VISIBLE
                makeFilterViews()
                makeExtraFilterListeners()
                setAllToFalse()
                selectTools("FILTER")
                selectFilter(0)
                isFilterExtraVisible = true
            }
        }

        binding.PhotoEditActivityDrawLayout.setOnClickListener {
            hideAllExtras()
            if ( isDrawExtraVisible ){
                binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
                mPhotoEditor.setBrushDrawingMode(false)
                setAllToFalse()
            }else{
                binding.PhotoEditActivityExtraDrawLayout.visibility = View.VISIBLE
                setAllToFalse()
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.brushColor = defaultBrushColor
                mPhotoEditor.brushSize = defaultBrushSize
                binding.PhotoEditActivityExtraDrawSeekBar.progress = 50
                selectDrawColor(1)
                makeExtraDrawListeners()
                selectTools("DRAW")
                isDrawExtraVisible = true
                isEraserMode = false
            }
        }

        binding.PhotoEditActivityStickerLayout.setOnClickListener {
            hideAllExtras()
            if ( isStickerExtraVisible ){
                binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
                setAllToFalse()
            }else{
                binding.PhotoEditActivityExtraStickerLayout.visibility = View.VISIBLE
                binding.PhotoEditActivityExtraStickerText1.text = emojis[0]
                binding.PhotoEditActivityExtraStickerText2.text = emojis[1]
                binding.PhotoEditActivityExtraStickerText3.text = emojis[2]
                binding.PhotoEditActivityExtraStickerText4.text = emojis[3]
                binding.PhotoEditActivityExtraStickerText5.text = emojis[4]
                binding.PhotoEditActivityExtraStickerText6.text = emojis[5]
                makeExtraStickerListeners()
                selectTools("STICKER")
                setAllToFalse()
                isStickerExtraVisible = true
            }
        }

        binding.PhotoEditActivityTextLayout.setOnClickListener {
            hideAllExtras()
            if ( isTextExtraVisible ){
                binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
                setAllToFalse()
            }else{
                binding.PhotoEditActivityExtraTextLayout.visibility = View.VISIBLE
                makeExtraTextListeners()
                setAllToFalse()
                selectTools("TEXT")
                isTextExtraVisible = true
            }
        }
        // On long Press on PhotoEditor Views //////////////////////////////////////////////////////
        mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
            override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
                hideAllExtras()
                binding.PhotoEditActivityExtraTextLayout.visibility = View.VISIBLE
                binding.PhotoEditActivityTypeTextLayout.visibility = View.VISIBLE
                binding.PhotoEditActivityTypeTextInput.setText(text)
                binding.PhotoEditActivityTypeTextAddText.text = "Change"
                binding.PhotoEditActivityTypeTextAddLayout.setOnClickListener {
                    mPhotoEditor.editText(rootView!!,binding.PhotoEditActivityTypeTextInput.text.toString(),colorCode)
                    binding.PhotoEditActivityTypeTextLayout.visibility = View.GONE
                    binding.PhotoEditActivityTypeTextInput.setText("")
                    makeExtraTextListeners()
                    binding.PhotoEditActivityTypeTextAddText.text = "Add"
                }

                binding.PhotoEditActivityTypeTextCloseLayout.setOnClickListener {
                    binding.PhotoEditActivityTypeTextLayout.visibility = View.GONE
                    makeExtraTextListeners()
                    binding.PhotoEditActivityTypeTextAddText.text = "Add"
                }
            }

            override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                numberOfViews = numberOfAddedViews
            }

            override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                numberOfViews = numberOfAddedViews
            }

            override fun onStartViewChangeListener(viewType: ViewType?) {        }
            override fun onStopViewChangeListener(viewType: ViewType?) {      }
        })

        // Undo and Redo Buttons ///////////////////////////////////////////////////////////////////
        binding.PhotoEditActivityUndoLayout.setOnClickListener {
            mPhotoEditor.undo()
        }

        binding.PhotoEditActivityRedoLayout.setOnClickListener {
            mPhotoEditor.redo()
        }

        // Cancel and Send Buttons /////////////////////////////////////////////////////////////////
        binding.PhotoEditActivityCancelButton.setOnClickListener {
            onBackPressed()
        }

        binding.PhotoEditActivitySendButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_CODE)
                }
            } else {
                saveImageAndReturn()
            }

        }

    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                saveImageAndReturn()
            }else{
                Toast.makeText(this, "Saving image denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun saveImageAndReturn() {
        var file = ContextWrapper(this).getDir("tempEdit", Context.MODE_APPEND)
        val imageName =  MyDateTime().UTC("yyyyMMddhhmmss")+ Random.nextInt(10000,99999)
        file = File(file, "$imageName.jpg")
        mPhotoEditor.saveAsFile(file.absolutePath, object : PhotoEditor.OnSaveListener {
            override fun onSuccess(imagePath: String) {
                val targetIntent = if (intent.getStringExtra("fromActivity")!!.toUpperCase(Locale.ROOT) == "ROOM"){
                    Intent(this@PhotoEditActivity,RoomActivity::class.java)
                }else{
                    Intent(this@PhotoEditActivity,PrivateActivity::class.java)
                }
                targetIntent.putExtra("type","Image")
                targetIntent.putExtra("fromActivity",intent.getStringExtra("fromActivity"))
                targetIntent.putExtra("toId",intent.getIntExtra("toId",0))
                targetIntent.putExtra("roomId",intent.getIntExtra("roomId",0))
                //targetIntent.putExtra("picProfile",intent.getStringExtra("picProfile"))
                targetIntent.putExtra("path",imagePath)
                targetIntent.putExtra("purpose","SendImage")
                startActivity(targetIntent)
                finish()
            }
            override fun onFailure(exception: Exception) {        }
        })
    }

    private fun makeExtraTextListeners() {
        binding.PhotoEditActivityExtraTextAddLayout.setOnClickListener {
            binding.PhotoEditActivityTypeTextLayout.visibility = View.VISIBLE

            binding.PhotoEditActivityTypeTextAddLayout.setOnClickListener {
                if ( binding.PhotoEditActivityTypeTextInput.text.isNotEmpty() ) {
                    mPhotoEditor.addText(binding.PhotoEditActivityTypeTextInput.text.toString(),defaultTextColor)
                    binding.PhotoEditActivityTypeTextInput.setText("")
                }
            }

            binding.PhotoEditActivityTypeTextCloseLayout.setOnClickListener {
                binding.PhotoEditActivityTypeTextLayout.visibility = View.GONE
            }
        }

        binding.PhotoEditActivityExtraTextMoreColorsLayout.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Text Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        dialog?.cancel()
                    }

                })
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    defaultTextColor = lastSelectedColor
                    binding.PhotoEditActivityExtraTextMoreColorsLayout.setBackgroundColor(lastSelectedColor)
                    selectTextColor(10)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

        binding.PhotoEditActivityExtraTextColor1.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor1)
            selectTextColor(1)
        }

        binding.PhotoEditActivityExtraTextColor2.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor2)
            selectTextColor(2)
        }

        binding.PhotoEditActivityExtraTextColor3.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor3)
            selectTextColor(3)
        }

        binding.PhotoEditActivityExtraTextColor4.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor4)
            selectTextColor(4)
        }

        binding.PhotoEditActivityExtraTextColor5.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor5)
            selectTextColor(5)
        }

        binding.PhotoEditActivityExtraTextColor6.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor6)
            selectTextColor(6)
        }

        binding.PhotoEditActivityExtraTextColor7.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor7)
            selectTextColor(7)
        }

        binding.PhotoEditActivityExtraTextColor8.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor8)
            selectTextColor(8)
        }

        binding.PhotoEditActivityExtraTextColor9.setOnClickListener {
            defaultTextColor = resources.getColor(R.color.textPalletColor9)
            selectTextColor(9)
        }
    }

    private fun makeFilterViews() {
        binding.PhotoEditActivityExtraFilter1.setImageResource(R.drawable.filter_auto_fix)
        binding.PhotoEditActivityExtraFilter2.setImageResource(R.drawable.filter_temprature)
        binding.PhotoEditActivityExtraFilter3.setImageResource(R.drawable.filter_lomish)
        binding.PhotoEditActivityExtraFilter4.setImageResource(R.drawable.filter_contrast)
        binding.PhotoEditActivityExtraFilter5.setImageResource(R.drawable.filter_brightness)
        binding.PhotoEditActivityExtraFilter6.setImageResource(R.drawable.filter_cross_process)
        binding.PhotoEditActivityExtraFilter7.setImageResource(R.drawable.filter_posterize)
    }

    private fun makeExtraFilterListeners(){
        binding.PhotoEditActivityExtraFilterNoneLayout.setOnClickListener {
            selectFilter(0)
            mPhotoEditor.setFilterEffect(PhotoFilter.NONE)
        }

        binding.PhotoEditActivityExtraFilter1.setOnClickListener {
            selectFilter(1)
            mPhotoEditor.setFilterEffect(PhotoFilter.AUTO_FIX)
        }

        binding.PhotoEditActivityExtraFilter2.setOnClickListener {
            selectFilter(2)
            mPhotoEditor.setFilterEffect(PhotoFilter.TEMPERATURE)
        }

        binding.PhotoEditActivityExtraFilter3.setOnClickListener {
            selectFilter(3)
            mPhotoEditor.setFilterEffect(PhotoFilter.LOMISH)
        }

        binding.PhotoEditActivityExtraFilter4.setOnClickListener {
            selectFilter(4)
            mPhotoEditor.setFilterEffect(PhotoFilter.CONTRAST)
        }

        binding.PhotoEditActivityExtraFilter5.setOnClickListener {
            selectFilter(5)
            mPhotoEditor.setFilterEffect(PhotoFilter.BRIGHTNESS)
        }

        binding.PhotoEditActivityExtraFilter6.setOnClickListener {
            selectFilter(6)
            mPhotoEditor.setFilterEffect(PhotoFilter.CROSS_PROCESS)
        }

        binding.PhotoEditActivityExtraFilter7.setOnClickListener {
            selectFilter(7)
            mPhotoEditor.setFilterEffect(PhotoFilter.POSTERIZE)
        }


    }


    private fun makeExtraDrawListeners() {
        binding.PhotoEditActivityExtraDrawColor1.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor1)
            selectDrawColor(1)
        }

        binding.PhotoEditActivityExtraDrawColor2.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor2)
            selectDrawColor(2)
        }

        binding.PhotoEditActivityExtraDrawColor3.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor3)
            selectDrawColor(3)
        }

        binding.PhotoEditActivityExtraDrawColor4.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor4)
            selectDrawColor(4)
        }

        binding.PhotoEditActivityExtraDrawColor5.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor5)
            selectDrawColor(5)
        }

        binding.PhotoEditActivityExtraDrawColor6.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor6)
            selectDrawColor(6)
        }

        binding.PhotoEditActivityExtraDrawColor7.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor7)
            selectDrawColor(7)
        }

        binding.PhotoEditActivityExtraDrawColor8.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor8)
            selectDrawColor(8)
        }

        binding.PhotoEditActivityExtraDrawColor9.setOnClickListener {
            isEraserMode = false
            mPhotoEditor.brushColor = resources.getColor(R.color.textPalletColor9)
            selectDrawColor(9)
        }

        binding.PhotoEditActivityExtraDrawMoreColors.setOnClickListener {
            ColorPickerDialogBuilder.with(this,R.style.ColorPickerTheme)
                .setTitle("Select Color")
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .showColorPreview(false)
                .showBorder(true)
                .showColorEdit(false)
                .setNegativeButton("Back", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        dialog?.cancel()
                    }

                })
                .setPositiveButton("Select") { d, lastSelectedColor, _ ->
                    isEraserMode = false
                    mPhotoEditor.setBrushDrawingMode(true)
                    mPhotoEditor.brushColor = lastSelectedColor
                    binding.PhotoEditActivityExtraDrawMoreColors.setBackgroundColor(lastSelectedColor)
                    selectDrawColor(10)
                    d.dismiss()
                    d.cancel()
                }
                .build()
                .show()
        }

        binding.PhotoEditActivityExtraDrawEraserLayout.setOnClickListener {
            selectDrawColor(0)
            //mPhotoEditor.setBrushDrawingMode(false)
            isEraserMode = true
            val seekProgress = binding.PhotoEditActivityExtraDrawSeekBar.progress
            mPhotoEditor.setBrushEraserSize(( ( seekProgress.toFloat() / 100 ) * ( maxBrushSize - minBrushSize ) ) + minBrushSize)
            mPhotoEditor.brushEraser()
        }

        binding.PhotoEditActivityExtraDrawSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mPhotoEditor.brushSize = ( ( progress.toFloat() / 100 ) * ( maxBrushSize - minBrushSize ) ) + minBrushSize
                mPhotoEditor.setBrushEraserSize(( ( progress.toFloat() / 100 ) * ( maxBrushSize - minBrushSize ) ) + minBrushSize)
                if ( isEraserMode ) {
                    mPhotoEditor.brushEraser()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {     }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {      }
        })


    }

    private fun makeExtraCropListeners() {
        val crop = binding.PhotoEditActivityCropImageView
        val animTime = 300
        binding.PhotoEditActivityExtraCropRotateUnclockLayout.setOnClickListener {
            crop.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D,animTime)
        }

        binding.PhotoEditActivityExtraCropRotateClockLayout.setOnClickListener {
            crop.rotateImage(CropImageView.RotateDegrees.ROTATE_90D,animTime)
        }

        binding.PhotoEditActivityExtraCrop169Layout.setOnClickListener {
            crop.setCropMode(CropImageView.CropMode.RATIO_16_9,animTime)
        }

        binding.PhotoEditActivityExtraCrop916Layout.setOnClickListener {
            crop.setCropMode(CropImageView.CropMode.RATIO_9_16,animTime)
        }

        binding.PhotoEditActivityExtraCrop11Layout.setOnClickListener {
            crop.setCropMode(CropImageView.CropMode.SQUARE,animTime)
        }

        binding.PhotoEditActivityExtraCropFreeLayout.setOnClickListener {
            crop.setCropMode(CropImageView.CropMode.FREE,animTime)
        }

        binding.PhotoEditActivityExtraCropDoneLayout.setOnClickListener {
            makeDefaultExtra()
            binding.PhotoEditActivityPhotoEditor.source.setImageBitmap(crop.croppedBitmap)
            isCropExtraVisible = false
        }

    }

    private fun makeExtraStickerListeners(){
        binding.PhotoEditActivityExtraStickerText1.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[0])
        }

        binding.PhotoEditActivityExtraStickerText2.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[1])
        }

        binding.PhotoEditActivityExtraStickerText3.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[2])
        }

        binding.PhotoEditActivityExtraStickerText4.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[3])
        }

        binding.PhotoEditActivityExtraStickerText5.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[4])
        }

        binding.PhotoEditActivityExtraStickerText6.setOnClickListener {
            mPhotoEditor.addEmoji(emojis[5])
        }

        binding.PhotoEditActivityExtraStickerMoreLayout.setOnClickListener {
            if ( isMoreStickersVisible ){
                binding.PhotoEditActivityStickers.visibility = View.GONE
                binding.PhotoEditActivityExtraStickerText7.text = "More"
            }else{
                binding.PhotoEditActivityStickers.visibility = View.VISIBLE
                binding.PhotoEditActivityExtraStickerText7.text = "Close"
            }
            isMoreStickersVisible = !isMoreStickersVisible
        }
    }

    private fun selectTools(tool: String) {
        val whiteColor = resources.getColor(R.color.photoEditorToolsTextColor)
        val darkColor = resources.getColor(R.color.photoEditorToolsBackground)
        unselectTools()
        when ( tool.toUpperCase(Locale.ROOT).trim() ){
            "CROP" -> {
                binding.PhotoEditActivityCropIcon.imageTintList = ColorStateList.valueOf(darkColor)
                binding.PhotoEditActivityCropText.setTextColor(darkColor)
                binding.PhotoEditActivityCropLayout.setBackgroundColor(whiteColor)
            }
            "FILTER" -> {
                binding.PhotoEditActivityFilterIcon.imageTintList = ColorStateList.valueOf(darkColor)
                binding.PhotoEditActivityFilterText.setTextColor(darkColor)
                binding.PhotoEditActivityFilterLayout.setBackgroundColor(whiteColor)
            }
            "DRAW" -> {
                binding.PhotoEditActivityDrawIcon.imageTintList = ColorStateList.valueOf(darkColor)
                binding.PhotoEditActivityDrawText.setTextColor(darkColor)
                binding.PhotoEditActivityDrawLayout.setBackgroundColor(whiteColor)
            }
            "STICKER" -> {
                binding.PhotoEditActivityStickerIcon.imageTintList = ColorStateList.valueOf(darkColor)
                binding.PhotoEditActivityStickerText.setTextColor(darkColor)
                binding.PhotoEditActivityStickerLayout.setBackgroundColor(whiteColor)
            }
            "TEXT" -> {
                binding.PhotoEditActivityTextIcon.imageTintList = ColorStateList.valueOf(darkColor)
                binding.PhotoEditActivityTextText.setTextColor(darkColor)
                binding.PhotoEditActivityTextLayout.setBackgroundColor(whiteColor)
            }
        }
    }

    private fun unselectTools(){
        val defaultTextColor = resources.getColor(R.color.photoEditorToolsTextColor)

        binding.PhotoEditActivityCropIcon.imageTintList = null
        binding.PhotoEditActivityCropText.setTextColor(defaultTextColor)
        binding.PhotoEditActivityCropLayout.setBackgroundColor(Color.TRANSPARENT)

        binding.PhotoEditActivityFilterIcon.imageTintList = null
        binding.PhotoEditActivityFilterText.setTextColor(defaultTextColor)
        binding.PhotoEditActivityFilterLayout.setBackgroundColor(Color.TRANSPARENT)

        binding.PhotoEditActivityDrawIcon.imageTintList = null
        binding.PhotoEditActivityDrawText.setTextColor(defaultTextColor)
        binding.PhotoEditActivityDrawLayout.setBackgroundColor(Color.TRANSPARENT)

        binding.PhotoEditActivityStickerIcon.imageTintList = null
        binding.PhotoEditActivityStickerText.setTextColor(defaultTextColor)
        binding.PhotoEditActivityStickerLayout.setBackgroundColor(Color.TRANSPARENT)

        binding.PhotoEditActivityTextIcon.imageTintList = null
        binding.PhotoEditActivityTextText.setTextColor(defaultTextColor)
        binding.PhotoEditActivityTextLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun makeDefaultExtra() {
        hideAllExtras()
        binding.PhotoEditActivityExtraText.visibility = View.VISIBLE
    }

    private fun hideAllExtras(){
        binding.PhotoEditActivityExtraCropLayout.visibility = View.GONE
        binding.PhotoEditActivityExtraTextLayout.visibility = View.GONE
        binding.PhotoEditActivityExtraStickerLayout.visibility = View.GONE
        binding.PhotoEditActivityExtraDrawLayout.visibility = View.GONE
        binding.PhotoEditActivityExtraFilterLayout.visibility = View.GONE
        binding.PhotoEditActivityExtraText.visibility = View.GONE

        binding.PhotoEditActivityTypeTextLayout.visibility = View.GONE
        binding.PhotoEditActivityCropImageView.visibility = View.GONE
        binding.PhotoEditActivityStickers.visibility = View.GONE

    }

    private fun setAllToFalse(){
        isTextExtraVisible = false
        isStickerExtraVisible = false
        isFilterExtraVisible = false
        isDrawExtraVisible = false
        isCropExtraVisible = false
        isMoreStickersVisible = false

        mPhotoEditor.setBrushDrawingMode(false)
    }

    private fun animateLayoutChange(){
        val lt = LayoutTransition()
        lt.setDuration(100)
        binding.PhotoEditActivityExtraLayout.layoutTransition = lt
        binding.PhotoEditActivityStickers.layoutTransition = lt
        binding.PhotoEditActivityLayout.layoutTransition = lt

    }

    private fun selectFilter(num : Int){
        // 0 : None
        val whiteColor = Color.WHITE
        val grayColor = resources.getColor(R.color.photoEditorToolsBackground)
        binding.PhotoEditActivityExtraFilterNoneLayout.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilterNoneText.setTextColor(whiteColor)
        binding.PhotoEditActivityExtraFilter1.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter2.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter3.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter4.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter5.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter6.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraFilter7.setBackgroundColor(Color.TRANSPARENT)
        when ( num ){
            0 -> {
                binding.PhotoEditActivityExtraFilterNoneLayout.setBackgroundColor(whiteColor)
                binding.PhotoEditActivityExtraFilterNoneText.setTextColor(grayColor)
            }
            1 -> binding.PhotoEditActivityExtraFilter1.setBackgroundColor(whiteColor)
            2 -> binding.PhotoEditActivityExtraFilter2.setBackgroundColor(whiteColor)
            3 -> binding.PhotoEditActivityExtraFilter3.setBackgroundColor(whiteColor)
            4 -> binding.PhotoEditActivityExtraFilter4.setBackgroundColor(whiteColor)
            5 -> binding.PhotoEditActivityExtraFilter5.setBackgroundColor(whiteColor)
            6 -> binding.PhotoEditActivityExtraFilter6.setBackgroundColor(whiteColor)
            7 -> binding.PhotoEditActivityExtraFilter7.setBackgroundColor(whiteColor)
        }
    }

    private fun selectTextColor(num : Int){
        val whiteColor = Color.WHITE
        binding.PhotoEditActivityExtraTextColor1.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor2.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor3.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor4.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor5.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor6.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor7.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor8.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraTextColor9.setBackgroundColor(Color.TRANSPARENT)
        when ( num ){
            1 -> binding.PhotoEditActivityExtraTextColor1.setBackgroundColor(whiteColor)
            2 -> binding.PhotoEditActivityExtraTextColor2.setBackgroundColor(whiteColor)
            3 -> binding.PhotoEditActivityExtraTextColor3.setBackgroundColor(whiteColor)
            4 -> binding.PhotoEditActivityExtraTextColor4.setBackgroundColor(whiteColor)
            5 -> binding.PhotoEditActivityExtraTextColor5.setBackgroundColor(whiteColor)
            6 -> binding.PhotoEditActivityExtraTextColor6.setBackgroundColor(whiteColor)
            7 -> binding.PhotoEditActivityExtraTextColor7.setBackgroundColor(whiteColor)
            8 -> binding.PhotoEditActivityExtraTextColor8.setBackgroundColor(whiteColor)
            9 -> binding.PhotoEditActivityExtraTextColor9.setBackgroundColor(whiteColor)
        }
    }

    private fun selectDrawColor( num : Int ){
        // 0 : Eraser           10 : More Colors
        val whiteColor = Color.WHITE
        val grayColor = resources.getColor(R.color.photoEditorToolsBackground)
        binding.PhotoEditActivityExtraDrawEraserIcon.imageTintList = ColorStateList.valueOf(whiteColor)
        binding.PhotoEditActivityExtraDrawEraserText.setTextColor(whiteColor)
        binding.PhotoEditActivityExtraDrawEraserLayout.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor1.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor2.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor3.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor4.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor5.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor6.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor7.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor8.setBackgroundColor(Color.TRANSPARENT)
        binding.PhotoEditActivityExtraDrawColor9.setBackgroundColor(Color.TRANSPARENT)
        when ( num ) {
            0 -> {
                binding.PhotoEditActivityExtraDrawEraserIcon.imageTintList = ColorStateList.valueOf(grayColor)
                binding.PhotoEditActivityExtraDrawEraserText.setTextColor(grayColor)
                binding.PhotoEditActivityExtraDrawEraserLayout.setBackgroundColor(whiteColor)
            }
            1 -> binding.PhotoEditActivityExtraDrawColor1.setBackgroundColor(whiteColor)
            2 -> binding.PhotoEditActivityExtraDrawColor2.setBackgroundColor(whiteColor)
            3 -> binding.PhotoEditActivityExtraDrawColor3.setBackgroundColor(whiteColor)
            4 -> binding.PhotoEditActivityExtraDrawColor4.setBackgroundColor(whiteColor)
            5 -> binding.PhotoEditActivityExtraDrawColor5.setBackgroundColor(whiteColor)
            6 -> binding.PhotoEditActivityExtraDrawColor6.setBackgroundColor(whiteColor)
            7 -> binding.PhotoEditActivityExtraDrawColor7.setBackgroundColor(whiteColor)
            8 -> binding.PhotoEditActivityExtraDrawColor8.setBackgroundColor(whiteColor)
            9 -> binding.PhotoEditActivityExtraDrawColor9.setBackgroundColor(whiteColor)
            //10 -> binding.PhotoEditActivityExtraDrawMoreColors.setBackgroundColor()
        }
    }
}