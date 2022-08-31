package com.arashforus.nearroom

class Object_User {

    var id: Int? = null
    var serverId: Int? = null
    var username: String  = ""
    var phoneNumber: String? = null
    var fullName: String? = null
    var phoneFullname: String? = null
    var profilePic: String? = null
    var phonePhotoUri: String? = null
    var status: String? = null
    var lastSeen: String? = null
    var isTypingTo: String? = null
    var isTypingTime: String? = null
    var mute: Int = 0
    var blocked: Int = 0
    var favourite: Int = 0


    constructor( id: Int?, serverId : Int?, username : String , phoneNumber: String? ,fullName: String?, profilePic : String?, status : String? ,
                 lastSeen : String? , isTypingTo : String? , isTypingTime : String? , mute : Int , blocked : Int ){
        this.id = id
        this.serverId = serverId
        this.username = username
        this.phoneNumber = phoneNumber
        this.fullName = fullName
        this.profilePic = profilePic
        this.status = status
        this.lastSeen = lastSeen
        this.isTypingTo = isTypingTo
        this.isTypingTime = isTypingTime
        this.mute = mute
        this.blocked = blocked
    }

    constructor( id: Int?, serverId : Int?, username : String , phoneNumber: String? ,fullName: String?, phoneFullname :String? , profilePic : String?,
                 phonePhotoUri : String?,status : String? ,lastSeen : String? ,isTypingTo : String? ,isTypingTime : String? ,mute : Int ,blocked : Int ){
        this.id = id
        this.serverId = serverId
        this.username = username
        this.phoneNumber = phoneNumber
        this.fullName = fullName
        this.phoneFullname = phoneFullname
        this.profilePic = profilePic
        this.phonePhotoUri = phonePhotoUri
        this.status = status
        this.lastSeen = lastSeen
        this.isTypingTo = isTypingTo
        this.isTypingTime = isTypingTime
        this.mute = mute
        this.blocked = blocked
    }

    constructor( id: Int?, serverId : Int?, username : String , phoneNumber: String? ,fullName: String?, phoneFullname :String? , profilePic : String?,
                 phonePhotoUri: String?,status: String? ,lastSeen: String? ,isTypingTo: String? ,isTypingTime: String? ,mute: Int ,blocked: Int, favourite:Int){
        this.id = id
        this.serverId = serverId
        this.username = username
        this.phoneNumber = phoneNumber
        this.fullName = fullName
        this.phoneFullname = phoneFullname
        this.profilePic = profilePic
        this.phonePhotoUri = phonePhotoUri
        this.status = status
        this.lastSeen = lastSeen
        this.isTypingTo = isTypingTo
        this.isTypingTime = isTypingTime
        this.mute = mute
        this.blocked = blocked
        this.favourite = favourite
    }


}