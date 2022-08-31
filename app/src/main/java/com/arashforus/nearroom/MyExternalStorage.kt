package com.arashforus.nearroom

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MyExternalStorage(val context: Context) {

    private val appFolder = "${context.externalMediaDirs[0]}/Nearoom"
    private val folderImages =      "/Medias/Nearoom Images"
    private val folderVideos =      "/Medias/Nearoom Videos"
    private val folderAudios =      "/Medias/Nearoom Audios"
    private val folderFiles =       "/Medias/Nearoom Files"

    fun checkAndMakeFolders(){
        val fileImage = File(appFolder + folderImages)
        val fileVideo = File(appFolder + folderVideos)
        val fileAudio = File(appFolder + folderAudios)
        val fileFile = File(appFolder + folderFiles)
        if ( !fileImage.exists() ){
            fileImage.mkdirs()
        }
        if ( !fileVideo.exists() ){
            fileVideo.mkdirs()
        }
        if ( !fileAudio.exists() ){
            fileAudio.mkdirs()
        }
        if ( !fileFile.exists() ){
            fileFile.mkdirs()
        }
    }

    fun saveSendPic(bitmapRaw : Bitmap, percentForCompress : Int ,imageName : String ): String {
        val root = Environment.getRootDirectory()
        val file : File = File("$root/Nearoom/Medias/$folderImages")
        if ( !file.exists() ){
            file.mkdir()
        }
        val newFile = File(file, imageName)
        try {
            val stream: OutputStream = FileOutputStream(newFile)
            bitmapRaw.compress(Bitmap.CompressFormat.JPEG, percentForCompress, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath
    }

    // Is Available files functions ////////////////////////////////////////////////////////////////

    fun isImageAvailable(name : String) : Boolean {
        return File(appFolder + folderImages, name).exists()
    }

    fun isVideoAvailable(name : String) : Boolean {
        return File(appFolder + folderVideos, name).exists()
    }

    fun isAudioAvailable(name : String) : Boolean {
        return File(appFolder + folderAudios, name).exists()
    }

    fun isFileAvailable(name : String) : Boolean {
        return File(appFolder + folderFiles, name).exists()
    }


    // Get Full address as string for files ////////////////////////////////////////////////////////

    fun getImageString(name : String) : String {
        return  File(appFolder + folderImages, name).toString()
    }

    fun getImageFile(name : String) : File {
        return  File(appFolder + folderImages, name)
    }

    fun getVideoString(name : String) : String {
        return  File(appFolder + folderVideos, name).toString()
    }

    fun getAudioString(name : String) : String {
        return  File(appFolder + folderAudios, name).toString()
    }

    fun getFileString(name : String) : String {
        return  File(appFolder + folderFiles, name).toString()
    }

    // Get Folder address //////////////////////////////////////////////////////////////////////////

    fun getImageFolder() : String {
        return  appFolder + folderImages
    }

    fun getVideoFolder() : String {
        return  appFolder + folderVideos
    }

    fun getAudioFolder() : String {
        return  appFolder + folderAudios
    }

    fun getFileFolder() : String {
        return  appFolder + folderFiles
    }


    fun getFileUri(name : String) : Uri {
        val file = File(appFolder + folderFiles, name)
        return Uri.parse(file.absolutePath)
    }

    fun getVideoUri(name : String) : Uri {
        val file = File(appFolder + folderVideos, name)
        return Uri.parse(file.absolutePath)
    }

}