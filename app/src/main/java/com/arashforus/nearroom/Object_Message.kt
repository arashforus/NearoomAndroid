package com.arashforus.nearroom

class Object_Message {

    var id : Int = 0
    var type : String
    var senderId : Int = 0
    var receiverId : Int = 0
    var message : String? = null
    //var thumb : String? = null
    var uri : String? = null
    var timestamp : String
    var size : Int? = null
    var send : Int = 0
    var received : Int = 0
    var read : Int = 0
    lateinit var receivedtime : String
    lateinit var readtime : String
    var selected : Boolean = false

    constructor(type : String , senderId : Int ,receiverId : Int ,message : String ,timestamp : String , send : Int , received : Int , read : Int) {
        this.type = type
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read

    }

    constructor(type : String , senderId : Int ,receiverId : Int ,message : String? ,uri : String? , size : Int? ,timestamp : String , send : Int , received : Int , read : Int) {
        this.type = type
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.uri = uri
        this.size = size
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read

    }

    constructor(id : Int ,type : String , senderId : Int ,receiverId : Int ,message : String? ,uri : String? , size : Int? ,timestamp : String , send : Int , received : Int , read : Int , receivedtime: String? , readtime:String?) {
        this.id = id
        this.type = type
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.uri = uri
        this.size = size
        this.timestamp = timestamp
        this.send = send
        this.received = received
        this.read = read
        if (receivedtime != null) {
            this.receivedtime = receivedtime
        }
        if (readtime != null) {
            this.readtime = readtime
        }
    }

}