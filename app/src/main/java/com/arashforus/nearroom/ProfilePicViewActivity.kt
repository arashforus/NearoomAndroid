package com.arashforus.nearroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.arashforus.nearroom.databinding.ActivityProfilePicViewBinding
import com.bumptech.glide.Glide

class ProfilePicViewActivity : AppCompatActivity() {

    lateinit var binding : ActivityProfilePicViewBinding
    //val username = null
    private var userId = 0
    private var roomId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePicViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if ( intent.hasExtra("userId") ){
            userId = intent.getIntExtra("userId",0)
        }

        if ( intent.hasExtra("roomId") ){
            roomId = intent.getIntExtra("roomId",0)
        }
        if ( userId > 0 ){
            makeUserView()
        }else{
            if ( roomId > 0 ){
                makeRoomView()
            }
        }

    }

    private fun makeUserView() {
        val user = DB_UserInfos(this).getUserInfo(userId)
        if ( !user.phoneFullname.isNullOrEmpty() ){
            binding.ProfilePicViewActivityTitle.text = user.phoneFullname
        }else{
            binding.ProfilePicViewActivityTitle.text = user.fullName
        }

        if ( user.profilePic.isNullOrEmpty()){
            if ( user.phonePhotoUri.isNullOrEmpty() ){
                Glide.with(this)
                    .load(R.drawable.defaultprofile)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
            }else{
                Glide.with(this)
                    .load(user.phonePhotoUri)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
            }
        }else{
            if (MyInternalStorage(this).isProfileAvailable(user.profilePic!!)){
                Glide.with(this)
                    .load(MyInternalStorage(this).getProfilePicString(user.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
            }else{
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbProfilePicString(user.profilePic!!))
                    .error(R.drawable.defaultprofile)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
                AndroidNetworking.download( MyServerSide().getProfileUri(user.profilePic!!), MyInternalStorage(this).getProfileFolder(), user.profilePic)
                    .setTag(user.profilePic)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@ProfilePicViewActivity)
                                .load( MyInternalStorage(this@ProfilePicViewActivity).getProfilePicString( user.profilePic!! ) )
                                .error(R.drawable.defaultprofile)
                                .centerInside()
                                .into(binding.ProfilePicViewActivityPic)
                        }

                        override fun onError(anError: ANError?) {
                            Toast.makeText(this@ProfilePicViewActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun makeRoomView() {
        val room = DB_NearoomInfos(this).getRoomDetail(roomId)

        binding.ProfilePicViewActivityTitle.text = room?.roomname

        if ( room?.pic.isNullOrEmpty()){
            Glide.with(this)
                .load(R.drawable.default_nearoom)
                .centerInside()
                .into(binding.ProfilePicViewActivityPic)
        }else{
            if (MyInternalStorage(this).isNearoomAvailable(room?.pic!!)){
                Glide.with(this)
                    .load(MyInternalStorage(this).getNearoomPicString(room.pic!!))
                    .error(R.drawable.default_nearoom)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
            }else{
                Glide.with(this)
                    .load(MyInternalStorage(this).getThumbNearoomPicString(room.pic!!))
                    .error(R.drawable.default_nearoom)
                    .centerInside()
                    .into(binding.ProfilePicViewActivityPic)
                AndroidNetworking.download( MyServerSide().getNearoomPicUri(room.pic!!), MyInternalStorage(this).getNearoomFolder(), room.pic!!)
                    .setTag(room.pic!!)
                    .setPriority(Priority.HIGH)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Glide.with(this@ProfilePicViewActivity)
                                .load( MyInternalStorage(this@ProfilePicViewActivity).getNearoomPicString( room.pic!! ) )
                                .error(R.drawable.default_nearoom)
                                .centerInside()
                                .into(binding.ProfilePicViewActivityPic)
                        }

                        override fun onError(anError: ANError?) {
                            Toast.makeText(this@ProfilePicViewActivity, "Something wrong ...", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }
}