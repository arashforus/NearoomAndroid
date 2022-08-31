package com.arashforus.nearroom

class Object_Chatlist {

    //var id : Int? = 0
    var type : String? = null
    var roomName : String? = null
    var roomId : Int? = 0
    var username : String? = null
    var userId : Int? = 0
    var savedName : String? = null
    var savedPic : String? = null
    var pic : String? = null
    var lastSender : String? = null
    var lastChat : String? = null
    var lastChatTime : String? = null
    var send : Int = 0
    var received : Int = 0
    var read : Int = 0
    var unreadCount : Int = 0

    // Constructor for private chats ///////////////////////////////////////////////////////////////
    constructor(type : String ,username : String , userId : Int? , savedName : String? , savedPic : String? ,pic : String? ,lastChat : String ,lastChatTime : String , send : Int , received : Int , read : Int, unreadCount :Int) {
        this.type = type
        this.username = username
        this.userId = userId
        this.savedName = savedName
        this.savedPic = savedPic
        this.pic = pic
        this.lastChat = lastChat
        this.lastChatTime = lastChatTime
        this.send = send
        this.received = received
        this.read = read
        this.unreadCount = unreadCount
    }

    // Constructor for room chats //////////////////////////////////////////////////////////////////
    constructor(type : String ,roomName : String ,roomId : Int? ,pic : String? ,lastSender : String ,lastChat : String ,lastChatTime : String  , send : Int , received : Int , read : Int) {
        this.type = type
        this.roomName = roomName
        this.roomId = roomId
        this.pic = pic
        this.lastSender = lastSender
        this.lastChat = lastChat
        this.lastChatTime = lastChatTime
        this.send = send
        this.received = received
        this.read = read

    }

}