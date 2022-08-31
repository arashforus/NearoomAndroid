package com.arashforus.nearroom

class Object_NearoomMessage {

    var id : Int = 0
    var type : String
    var senderId : Int = 0
    var roomId : Int = 0
    var message : String? = null
    var uri : String? = null
    var timestamp : String
    var size : Int? = null
    var send : Int = 0
    var received : Int = 0
    var read : Int = 0
    private var receivedtime : String? = null
    private var readtime : String? = null
    var selected : Boolean = false


    constructor(type : String , senderId : Int ,roomId : Int ,message : String? ,uri : String? , size : Int? ,timestamp : String , send : Int , received : Int , read : Int , receivedTime: String? , readTime:String?) {
        this.type = type
        this.senderId = senderId
        this.roomId = roomId
        this.message = message
        this.uri = uri
        this.size = size
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read
        this.receivedtime = receivedTime
        this.readtime = readTime
    }

    constructor(id : Int ,type : String , senderId : Int ,roomId : Int ,message : String? ,uri : String? , size : Int? ,timestamp : String , send : Int , received : Int , read : Int , receivedTime: String? , readTime:String?) {
        this.id = id
        this.type = type
        this.senderId = senderId
        this.roomId = roomId
        this.message = message
        this.uri = uri
        this.size = size
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read
        this.receivedtime = receivedTime
        this.readtime = readTime
    }

    constructor(type : String , senderId : Int ,roomId : Int ,message : String? ,uri : String? , size : Int? ,timestamp : String , send : Int , received : Int , read : Int , receivedTime: String? , readTime:String? ,selected : Boolean) {
        this.type = type
        this.senderId = senderId
        this.roomId = roomId
        this.message = message
        this.uri = uri
        this.size = size
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read
        this.receivedtime = receivedTime
        this.readtime = readTime
        this.selected = selected
    }

}