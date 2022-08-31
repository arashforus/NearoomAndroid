package com.arashforus.nearroom

class MyServerSide {

    companion object{
        private const val wholeAddress = "http://nearoom.galleriha.com/sms/"

        // Pic and files folders ///////////////////////////////////////////////////////////////////
        const val profile_address = wholeAddress + "profilepic/"
        const val thumbProfile_address = wholeAddress + "thumbprofilepic/"
        const val nearoomPic_address = wholeAddress + "nearoompic/"
        const val thumbNearoomPic_address = wholeAddress + "thumbnearoompic/"
        const val photo_address = wholeAddress + "pic/"
        const val thumbPhoto_address = wholeAddress + "thumbpic/"
        const val video_address = wholeAddress + "vid/"
        const val thumbVideo_address = wholeAddress + "thumbvid/"
        const val audio_address = wholeAddress + "aud/"
        const val file_address = wholeAddress + "file/"

        const val sponsorshiplogo_address = wholeAddress + "sponsorshiplogo/"
        const val applogo_address = wholeAddress + "applogo/"

        // Pages ///////////////////////////////////////////////////////////////////////////////////
        const val FAQ_address = wholeAddress + "FAQ.php"
        const val privacyPolicy_address = wholeAddress + "privacypolicy.php"
        const val donate_address = wholeAddress + "donate.php"
        const val downloadApp = wholeAddress + "download.php"

        // Save pics and files /////////////////////////////////////////////////////////////////////
        const val saveThumbProfilePic_address = wholeAddress + "savethumbprofilepic.php"
        const val saveProfilePic_address = wholeAddress + "saveprofilepic.php"
        const val saveThumbNearoomPic_address = wholeAddress + "savethumbnearoompic.php"
        const val saveNearoomPic_address = wholeAddress + "savenearoompic.php"
        const val saveThumbPicture_address = wholeAddress + "savethumb.php"
        const val savePicture_address = wholeAddress + "savesendpic.php"
        const val saveThumbVideo_address = wholeAddress + "savethumbvid.php"
        const val saveVideo_address = wholeAddress + "savesendvid.php"
        const val saveAudio_address = wholeAddress + "savesendaud.php"
        const val saveFile_address = wholeAddress + "savesendfile.php"
        //val saveThumbPicture_address = "http://nearoom.galleriha.com/sms/savethumb.php"

        // Volley Addresses ////////////////////////////////////////////////////////////////////////
        const val signIn = wholeAddress + "signin.php"
        const val loadProfile = wholeAddress + "loadprofile.php"
        const val checkIsExist = wholeAddress + "checkisexist.php"
        const val addResetPasswordLink = wholeAddress + "addresetpasswordlink.php"
        const val addVerificationCode = wholeAddress + "addverificationcode.php"
        const val signUp = wholeAddress + "signup.php"
        const val checkUsername = wholeAddress + "checkusername.php"
        const val getLastSponsorhship = wholeAddress + "getlastsponsorship.php"
        const val checkVersion = wholeAddress + "checkversion.php"
        const val updateTokenId = wholeAddress + "updatetokenid.php"
        const val deleteTokenId = wholeAddress + "deletetokenid.php"
        const val getLatestMessage = wholeAddress + "getlatestmessages.php"
        const val getLatestNearoomMessage = wholeAddress + "getlatestnearoommessages.php"
        const val getAllNearoomMessage = wholeAddress + "getallnearoommessages.php"
        const val getRegisteredContacts = wholeAddress + "getregisteredcontacts.php"
        const val getUserInfos = wholeAddress + "getuserinfos.php"
        const val getNearoomInfos = wholeAddress + "getnearoominfos.php"
        const val getMessages = wholeAddress + "getmessages.php"
        const val findNearooms = wholeAddress + "findnearrooms.php"
        const val createNewRoom = wholeAddress + "createnewroom.php"
        const val getDateJoined = wholeAddress + "getdatejoined.php"
        const val deleteProfilePic = wholeAddress + "deleteprofilepic.php"
        const val deleteNearoomPic = wholeAddress + "deletenearoompic.php"
        const val changeStatus = wholeAddress + "changestatus.php"
        const val changeProfile = wholeAddress + "changeprofile.php"
        const val changeNearoomInfo = wholeAddress + "changenearoominfo.php"
        const val changeBlockList = wholeAddress + "changeblocklist.php"
        const val changePassword = wholeAddress + "changepassword.php"
        const val sendEmailVerification = wholeAddress + "sendemailverification.php"
        const val checkEmailVerification = wholeAddress + "checkemailverification.php"
        const val sendContactUs = wholeAddress + "sendcontactus.php"
        const val leaveNearoom = wholeAddress + "leavenearoom.php"
        const val removeNearoom = wholeAddress + "removenearoom.php"
        const val setLastSeen = wholeAddress + "setlastseen.php"
        const val isTyping = wholeAddress + "istyping.php"
        const val deleteChats = wholeAddress + "deletechats.php"
        const val deleteMessage = wholeAddress + "deletemessage.php"
        const val deleteNearoomChats = wholeAddress + "deletenearoomchats.php"
        const val sendMessageStatus = wholeAddress + "setmessagestatus.php"
        const val sendMessage = wholeAddress + "sendmessage.php"
        const val checkVerificationCode = wholeAddress + "checkverificationcode.php"
        const val getLastSeens = wholeAddress + "getlastseens.php"
        const val getPasswordDateChange = wholeAddress + "getpassworddatechange.php"
        const val setNearoomMessageStatus = wholeAddress + "setnearoommessagestatus.php"
        const val joinNearoom = wholeAddress + "joinnearoom.php"
        const val getNearoomParticipant = wholeAddress + "getnearoomparticipant.php"

    }


    // Fetch Uri from addresses and name ///////////////////////////////////////////////////////////
    fun getProfileUri(uri : String) : String {
        return profile_address + uri
    }

    fun getNearoomPicUri(uri : String) : String {
        return nearoomPic_address + uri
    }

    fun getPhotoUri(uri : String) : String {
        return photo_address + uri
    }

    fun getVideoUri(uri : String) : String {
        return video_address + uri
    }

    fun getThumbProfileUri(uri : String) : String {
        return thumbProfile_address + uri
    }

    fun getThumbNearoomUri(uri : String) : String {
        return thumbNearoomPic_address + uri
    }

    fun getThumbPhotoUri(uri : String) : String {
        return thumbPhoto_address + uri
    }

    fun getThumbVideoUri(uri : String) : String {
        return thumbVideo_address + uri
    }

    fun getAudioUri(uri : String) : String {
        return audio_address + uri
    }

    fun getFileUri(uri : String) : String {
        return file_address + uri
    }

    fun getSponsorshipLogoUri(uri : String) : String {
        return sponsorshiplogo_address + uri
    }

    fun getAppLogoUri(uri : String) : String {
        return applogo_address + uri
    }

}