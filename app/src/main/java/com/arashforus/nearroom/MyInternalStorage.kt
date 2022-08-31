package com.arashforus.nearroom

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Size
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import java.io.*
import kotlin.random.Random

class MyInternalStorage(val context : Context) {

    private val folderProfiles = "profiles"
    private val folderNearooms = "nearooms"
    //private val folderImages = "images"
    //private val folderVideos = "videos"
    private val folderThumbProfiles = "profileThumbs"
    private val folderThumbNearooms = "nearoomThumbs"
    private val folderThumbImages = "imageThumbs"
    private val folderThumbVideos = "videoThumbs"
    //private val folderAudios = "audios"
    //private val folderFiles = "files"
    private val folderWallpaper = "Wallpaper"

    private val folderThumbVideosCrop = "videoThumbsCrop"
    private val folderThumbVideosTrim = "videoThumbsTrim"
    private val folderTempCamera = "cameraTemp"

    private val folderSponsorshipLogo = "sponsorshipLogo"
    private val folderAppLogo = "appLogo"

    private val maxSizeByte = 1300*1024*8    // in bytes
    private val maxSizeThumbByte = 110*1024*8    // in bytes
    //val maxSizeThumbStringByte = 20*1024*8    // in bytes

    fun saveProfilePic(uri : Uri , myId :Int): Array<String> {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmap.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val smallPercentForCompress =  minOf((( maxSizeThumbByte.toDouble() / bitmap.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderProfiles, Context.MODE_APPEND)
        var thumbFile = wrapper.getDir(folderThumbProfiles, Context.MODE_APPEND)
        val imageName = "PROFILE_"+ MyDateTime().UTC("yyyyMMddhhmmss") + myId +Random.nextInt(10000,99999)
        file = File(file, "$imageName.jpg")
        thumbFile = File(thumbFile, "$imageName.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream: OutputStream = FileOutputStream(thumbFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            thumbStream.flush()
            thumbStream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
        return arrayOf(file.absolutePath,thumbFile.absolutePath, "$imageName.jpg")
    }


    fun saveProfilePic(bitmapRaw : Bitmap , myId : Int): Array<String> {
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val smallPercentForCompress = minOf((( maxSizeThumbByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderProfiles, Context.MODE_APPEND)
        var thumbFile = wrapper.getDir(folderThumbProfiles, Context.MODE_APPEND)
        val imageName = "PROFILE_"+ MyDateTime().UTC("yyyyMMddhhmmss") + myId +Random.nextInt(10000,99999)
        file = File(file, "$imageName.jpg")
        thumbFile = File(thumbFile, "$imageName.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream: OutputStream = FileOutputStream(thumbFile)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            thumbStream.flush()
            thumbStream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }

        return arrayOf(file.absolutePath,thumbFile.absolutePath, "$imageName.jpg")
    }

    fun saveSendPic(uri : Uri , myId : Int ): Array<String> {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmap.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val smallPercentForCompress =  minOf((( maxSizeThumbByte.toDouble() / bitmap.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        //var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
        var thumbFile = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        val imageName = "IMG_"+ MyDateTime().UTC("yyyyMMddhhmmss") + myId +Random.nextInt(10000,99999)
        val file = File(MyExternalStorage(context).getImageFolder(), "$imageName.jpg")
        thumbFile = File(thumbFile, "$imageName.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream: OutputStream = FileOutputStream(thumbFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            thumbStream.flush()
            thumbStream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
        return arrayOf(file.absolutePath,thumbFile.absolutePath, "$imageName.jpg")
    }

    fun saveSendPic(bitmapRaw : Bitmap , myId :Int  ): Array<String> {
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val smallPercentForCompress =  minOf((( maxSizeThumbByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        //var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
        var thumbFile = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        val imageName = "IMG_"+ MyDateTime().UTC("yyyyMMddhhmmss")+ myId + Random.nextInt(10000,99999)
        val file = File(MyExternalStorage(context).getImageFolder(), "$imageName.jpg")
        thumbFile = File(thumbFile, "$imageName.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream: OutputStream = FileOutputStream(thumbFile)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            thumbStream.flush()
            thumbStream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
        //val fullImagePath = MyExternalStorage(context).saveSendPic(bitmapRaw, imageName)

        return arrayOf(file.absolutePath,thumbFile.absolutePath, "$imageName.jpg")
    }

    /*fun saveSendPicWithEncode(bitmapRaw : Bitmap , username :String , toUsername : String ): Array<String> {
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val smallPercentForCompress =  minOf((( maxSizeThumbByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
        var encoded : String = ""
        //var thumbFile = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        val imagename = username +"_"+toUsername +"_"+ MyDateTime().UTC("yyyyMMddhhmmss")+Random.nextInt(10000,99999)
        file = File(file, "$imagename.jpg")
        //thumbFile = File(thumbFile, imagename+".jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream = ByteArrayOutputStream()
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            encoded = Base64.encodeToString(thumbStream.toByteArray(),Base64.DEFAULT)
        } catch (e: IOException){
            e.printStackTrace()
        }
        return arrayOf(file.absolutePath,encoded, "$imagename.jpg")
    }

    fun saveSendPicWithEncode(uri : Uri , username :String , toUsername : String ): Array<String> {
        val bitmapRaw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val bigPercentForCompress =  minOf((( maxSizeByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        //val smallPercentForCompress =  minOf((( maxSizeThumbStringByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
        var encoded : String = ""
        val imagename = username +"_"+toUsername +"_"+ MyDateTime().UTC("yyyyMMddhhmmss")+Random.nextInt(10000,99999)
        file = File(file, "$imagename.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, bigPercentForCompress, stream)
            stream.flush()
            stream.close()
            val thumbStream = ByteArrayOutputStream()
            //bitmapRaw.compress(Bitmap.CompressFormat.JPEG, smallPercentForCompress, thumbStream)
            encoded = Base64.encodeToString(thumbStream.toByteArray(),Base64.DEFAULT)
        } catch (e: IOException){
            e.printStackTrace()
        }
        return arrayOf(file.absolutePath, encoded, "$imagename.jpg")
    }

     */

    fun saveSendVideo(uri : Uri , myId :Int): Array<String> {
        val wrapper = ContextWrapper(context)
        //var newfile = wrapper.getDir(folderVideos, Context.MODE_APPEND)
        var thumbfile = wrapper.getDir(folderThumbVideos, Context.MODE_APPEND)
        val imageName = "VID_" + MyDateTime().UTC("yyyyMMddhhmmss") + myId + Random.nextInt(10000,99999)
        val newFile = File(MyExternalStorage(context).getVideoFolder(), "$imageName.mp4")
        thumbfile = File(thumbfile, "$imageName.jpg")
            try {
                VideoCompressor.start(uri.path.toString(), newFile.absolutePath, object : CompressionListener {
                    override fun onCancelled() {
                        println("Compress video cancelled")
                    }
                    override fun onFailure(failureMessage: String) {
                        println("Compress video fail : $failureMessage")
                    }
                    override fun onProgress(percent: Float) {
                        //TODO("Not yet implemented")
                    }
                    override fun onStart() {
                        println("Compress video stared")
                    }
                    @SuppressLint("WrongThread")
                    override fun onSuccess() {
                        println("Compress video success")
                        //val instream : FileInputStream? = context.contentResolver.openAssetFileDescriptor(uri,"r")!!.createInputStream()
                        //val outstream: OutputStream = FileOutputStream(newfile)
                        //instream?.copyTo(outstream)
                        //instream?.close()
                        //outstream.close()
                        val size = Size(100,100)
                        val ca = CancellationSignal()
                        val vidThumb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ThumbnailUtils.createVideoThumbnail(newFile,size,ca)
                        } else {
                            ThumbnailUtils.createVideoThumbnail(newFile.absolutePath,0)
                        }
                        val thumbStream: OutputStream = FileOutputStream(thumbfile)
                        vidThumb!!.compress(Bitmap.CompressFormat.JPEG, 100, thumbStream)
                        thumbStream.flush()
                        thumbStream.close()
                    }
                },VideoQuality.MEDIUM,false,false)

            } catch (e: IOException){
                e.printStackTrace()
            }
        return arrayOf( newFile.absolutePath, thumbfile.absolutePath, "$imageName.jpg", "$imageName.mp4" )
    }

    fun saveSendAudio(uri : Uri , myId :Int ): Array<String> {
        val wrapper = ContextWrapper(context)
        //var newfile = wrapper.getDir(folderAudios, Context.MODE_APPEND)
        val audioName = "AUD_" + MyDateTime().UTC("yyyyMMddhhmmss") + myId + Random.nextInt(10000,99999)
        val newFile = File(MyExternalStorage(context).getAudioFolder(), "$audioName.mp3")
        val inStream : FileInputStream? = context.contentResolver.openAssetFileDescriptor(uri,"r")!!.createInputStream()
        val outStream: OutputStream = FileOutputStream(newFile)
        inStream?.copyTo(outStream)
        inStream?.close()
        outStream.close()

        return arrayOf(newFile.absolutePath, "$audioName.mp3" )
    }

    fun saveSendFile(uri : Uri , myId :Int ): Array<String> {
        val wrapper = ContextWrapper(context)
        //var newfile = wrapper.getDir(folderFiles, Context.MODE_APPEND)
        val fileName = "FILE_" + MyDateTime().UTC("yyyyMMddhhmmss") + myId + Random.nextInt(10000,99999)
        val extension = MyTools(context).findFileType(getNameOfUri(uri)!!)
        val newFile = File(MyExternalStorage(context).getFileFolder(), "$fileName.$extension")
        val inStream : FileInputStream? = context.contentResolver.openAssetFileDescriptor(uri,"r")!!.createInputStream()
        val outStream: OutputStream = FileOutputStream(newFile)
        inStream?.copyTo(outStream)
        inStream?.close()
        outStream.close()

        return arrayOf(newFile.absolutePath, "$fileName.$extension" )
    }

    fun saveWallpaperPic(bitmapRaw : Bitmap ): String {
        val percentForCompress = minOf((( maxSizeByte.toDouble() / bitmapRaw.allocationByteCount.toDouble() ) * 100).toInt() , 100 )
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderWallpaper, Context.MODE_APPEND)
        val imageName = "Wallpaper" +"_"+ MyDateTime().UTC("yyyyMMddhhmmss")
        file = File(file, "$imageName.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, percentForCompress, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath
    }

    fun getNameOfUri(uri: Uri): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex!!)
        returnCursor?.close()
        return fileName
    }

    // Is Available files functions ////////////////////////////////////////////////////////////////

    fun isProfileAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderProfiles, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    fun isProfileThumbAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbProfiles, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    fun isNearoomAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderNearooms, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    fun isNearoomThumbAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbNearooms, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    //fun isPicAvailable(name : String) : Boolean {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return file.exists()
    //}

    fun isPicThumbAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    //fun isVideoAvailable(name : String) : Boolean {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderVideos, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return file.exists()
    //}

    fun isVideoThumbAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbVideos, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    //fun isAudioAvailable(name : String) : Boolean {
   //     val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderAudios, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return file.exists()
    //}

    //fun isFileAvailable(name : String) : Boolean {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderFiles, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return file.exists()
    //}

    fun isSponsorshipLogoAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderSponsorshipLogo, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    fun isAppLogoAvailable(name : String) : Boolean {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderAppLogo, Context.MODE_APPEND)
        file = File(file, name)
        return file.exists()
    }

    // Get Uri with name of file ///////////////////////////////////////////////////////////////////

    //fun getPicUri(name : String) : Uri{
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
    //    file = File(file, name)
   //     return Uri.parse(file.absolutePath)
    //}

    fun getThumbUri(name : String) : Uri{
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        file = File(file, name)
        return Uri.parse(file.absolutePath)
    }

    fun getCameraTempUri(name : String) : Uri{
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderTempCamera, Context.MODE_APPEND)
        file = File(file, name)
        return Uri.parse(file.absolutePath)
    }

    //fun getVideoUri(name : String) : Uri{
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderVideos, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return Uri.parse(file.absolutePath)
    //}

    //fun getFileUri(name : String) : Uri{
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderFiles, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return Uri.parse(file.absolutePath)
    //}

    // Get Full address as string for files ////////////////////////////////////////////////////////

    fun getProfilePicString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderProfiles, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    fun getThumbProfilePicString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbProfiles, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    fun getNearoomPicString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderNearooms, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    fun getThumbNearoomPicString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbNearooms, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    //fun getPicString(name : String) : String {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderImages, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return  file.toString()
    //}

    fun getThumbPicString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    //fun getVideoString(name : String) : String {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderVideos, Context.MODE_APPEND)
     //   file = File(file, name)
    //    return  file.toString()
    //}

    fun getThumbVideoString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderThumbVideos, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    //fun getAudioString(name : String) : String {
     //   val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderAudios, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return  file.toString()
    //}

    //fun getFileString(name : String) : String {
    //    val wrapper = ContextWrapper(context)
    //    var file = wrapper.getDir(folderFiles, Context.MODE_APPEND)
    //    file = File(file, name)
    //    return  file.toString()
    //}

    fun getSponsorshipLogoString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderSponsorshipLogo, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    fun getAppLogoString(name : String) : String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(folderAppLogo, Context.MODE_APPEND)
        file = File(file, name)
        return  file.toString()
    }

    // Get Folder address //////////////////////////////////////////////////////////////////////////

    fun getProfileFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderProfiles, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getThumbProfileFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbProfiles, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getNearoomFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderNearooms, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getThumbNearoomFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbNearooms, Context.MODE_APPEND)
        return  file.toString()
    }

    //fun getImageFolder() : String {
    //   val wrapper = ContextWrapper(context)
    //    val file = wrapper.getDir(folderImages, Context.MODE_APPEND)
    //    return  file.toString()
    //}

    fun getThumbImageFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbImages, Context.MODE_APPEND)
        return  file.toString()
    }

    //fun getVideoFolder() : String {
    //    val wrapper = ContextWrapper(context)
    //    val file = wrapper.getDir(folderVideos, Context.MODE_APPEND)
    //    return  file.toString()
    //}

    fun getThumbVideoFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbVideos, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getThumbVideoCropFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbVideosCrop, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getThumbVideoTrimFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderThumbVideosTrim, Context.MODE_APPEND)
        return  file.toString()
    }

    //fun getAudioFolder() : String {
    //    val wrapper = ContextWrapper(context)
    //    val file = wrapper.getDir(folderAudios, Context.MODE_APPEND)
    //    return  file.toString()
    //}

   // fun getFileFolder() : String {
    //    val wrapper = ContextWrapper(context)
    //    val file = wrapper.getDir(folderFiles, Context.MODE_APPEND)
    //    return  file.toString()
    //}

    fun getSponsorshipLogoFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderSponsorshipLogo, Context.MODE_APPEND)
        return  file.toString()
    }

    fun getAppLogoFolder() : String {
        val wrapper = ContextWrapper(context)
        val file = wrapper.getDir(folderAppLogo, Context.MODE_APPEND)
        return  file.toString()
    }


}