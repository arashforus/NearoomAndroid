package com.arashforus.nearroom

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener

class MyMediaDownloader(val context: Context) {

    interface MyCallback{
        fun downloadSuccess()
    }

    /*
    fun updateContactsProfileThumb(){
        val profilePics = DB_Contacts(context).getAllProfilePics()
        profilePics.forEach {
            if (!MyInternalStorage(context).isProfileThumbAvailable(it)){
                AndroidNetworking.download( MyServerSide().getThumbProfileUri(it), MyInternalStorage(context).getThumbProfileFolder(), it)
                    .setTag(it)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            println("download $it  is complete")
                        }
                        override fun onError(anError: ANError?) {
                            println("download $it  has error")
                        }
                    })
            }
        }
    }
     */

    fun updateUserInfosPicThumb(){
        val profilePics = DB_UserInfos(context).getAllProfilePhotos()
        profilePics.forEach {
            if (!MyInternalStorage(context).isProfileThumbAvailable(it)){
                AndroidNetworking.download( MyServerSide().getThumbProfileUri(it), MyInternalStorage(context).getThumbProfileFolder(), it)
                    .setTag(it)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            println("download $it  is complete")
                        }
                        override fun onError(anError: ANError?) {
                            println("download $it  has error")
                        }
                    })
            }
        }
    }

    fun updateNearoomInfosPicThumb(){
        val nearoomPics = DB_NearoomInfos(context).getAllNearoomPhotos()
        nearoomPics.forEach {
            if ( !MyInternalStorage(context).isNearoomThumbAvailable(it) ){
                AndroidNetworking.download( MyServerSide().getThumbNearoomUri(it), MyInternalStorage(context).getThumbNearoomFolder(), it)
                    .setTag(it)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            println("download $it  is complete")
                        }
                        override fun onError(anError: ANError?) {
                            println("download $it  has error")
                        }
                    })
            }
        }
    }

    fun updateImagesThumb(){
        val images = DB_Messages(context).getAllImages()
        images.forEach {
            if (!MyInternalStorage(context).isPicThumbAvailable(it)){
                AndroidNetworking.download( MyServerSide().getThumbPhotoUri(it), MyInternalStorage(context).getThumbImageFolder(), it)
                    .setTag(it)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            println("download $it  is complete")
                        }
                        override fun onError(anError: ANError?) {
                            println("download $it  has error")
                        }
                    })
            }
        }
    }

    fun updateVideosThumb(){
        val videos = DB_Messages(context).getAllVideos()
        videos.forEach {
            if (!MyInternalStorage(context).isVideoThumbAvailable(it)){
                AndroidNetworking.download( MyServerSide().getThumbVideoUri(it), MyInternalStorage(context).getThumbVideoFolder(), it)
                    .setTag(it)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            println("download $it  is complete")
                        }
                        override fun onError(anError: ANError?) {
                            println("download $it  has error")
                        }
                    })
            }
        }
    }



    fun updateSponsosorshipLogo( callback: MyCallback) {
        val logo = MySharedPreference(MySharedPreference.PreferenceSponsorship,context).load(MySharedPreference.Sponsorship_Logo,"String") as String
        if ( logo !== "Null" ){
            if (!MyInternalStorage(context).isSponsorshipLogoAvailable(logo)){
                AndroidNetworking.download( MyServerSide().getSponsorshipLogoUri(logo), MyInternalStorage(context).getSponsorshipLogoFolder(), logo)
                    .setTag(logo)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            //println("download $logo  is complete")
                            callback.downloadSuccess()
                        }
                        override fun onError(anError: ANError?) {
                            //println("download $logo  has error")
                        }
                    })
            }
        }
    }


}