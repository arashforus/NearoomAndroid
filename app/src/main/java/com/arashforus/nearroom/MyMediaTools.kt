package com.arashforus.nearroom

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyMediaTools(context: Context) {
/*
    val myBitrate = 1000
    val myFrameRate = 1000
    val myFrameInterval = 1000

    fun compressVideo(){
        val outputFormat = MediaFormat.createVideoFormat("video/mp4",1080,720)
        outputFormat.setInteger( MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface );
        outputFormat.setInteger( MediaFormat.KEY_BIT_RATE, myBitrate );
        outputFormat.setInteger( MediaFormat.KEY_FRAME_RATE, myFrameRate );
        outputFormat.setInteger( MediaFormat.KEY_I_FRAME_INTERVAL, myFrameInterval );

        val encoder = MediaCodec.createEncoderByType("video/mp4")
        encoder.configure(outputFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)


        val mInputBuffers = MediaCodec.createPersistentInputSurface()
        val mOutputBuffers = mediaCodec.getOutputBuffers();
    }


    // With ffmpeg /////////////////////////////////////////////////////////////////////////////////
    fun compress(dir: File, bitRate: String, path: String?) {
        val isFolderCreated = createFolder(dir)

        if (isFolderCreated) {
            val outputPath =
                "$dir/CompressedVideos/${SimpleDateFormat("yyyyMMddHHmmss").format(Date())}.mp4"

            val cmd = arrayOf("-i", path, "-b", bitRate, "-y", outputPath)

            val ffmpeg = FFmpeg.getInstance(getApplication())
            try {
                ffmpeg?.execute(cmd, object : ExecuteBinaryResponseHandler() {
                    override fun onStart() {}
                    override fun onProgress(message: String) {
                        Log.d(PreviewActivity.TAG, "progress: $message")
                    }
                    override fun onFailure(message: String) {
                        Log.d(PreviewActivity.TAG, "failed: $message")
                        _mIsCompressing.postValue(false)
                    }

                    override fun onSuccess(message: String) {
                        Log.d(PreviewActivity.TAG, "success: $message")
                        _mOutputPath.postValue(outputPath)
                        _mIsCompressing.postValue(false)
                    }

                    override fun onFinish() {
                        _mIsCompressing.postValue(false)
                    }
                })
            } catch (e: FFmpegCommandAlreadyRunningException) {
            }
        } else {
            _mIsCompressing.postValue(false)
        }
    }

    private suspend fun createFolder(dir: File): Boolean {
        return withContext(Dispatchers.IO) {
            val folder = "/CompressedVideos"
            val documentsFolder = File(dir, folder)
            if (!documentsFolder.exists())
                return@withContext documentsFolder.mkdirs()
            else
                return@withContext true
        }
    }

*/
}