package com.arashforus.nearroom

class Object_Nearoom {

    var roomId = 0
    var roomname : String? = null
    var pic : String? = null
    var description : String? = null
    var category : String? = null
    var capacity : Int? = 0
    var joined : Int? = 0
    var distance : Double? = 0.0
    var mute : Int? = 0

    constructor(roomId : Int ,roomName : String ,roomPic : String? ,description : String? ,category : String ,capacity : Int ,joined : Int? , distance : Double ) {
        this.roomId = roomId
        this.roomname = roomName
        this.pic = roomPic
        this.description = description
        this.category = category
        this.capacity = capacity
        this.joined = joined
        this.distance = distance
    }

    constructor(roomId : Int ,roomName : String ,roomPic : String? ,description : String? ,category : String ,capacity : Int ,joined : Int? , distance : Double ,mute : Int? ) {
        this.roomId = roomId
        this.roomname = roomName
        this.pic = roomPic
        this.description = description
        this.category = category
        this.capacity = capacity
        this.joined = joined
        this.distance = distance
        this.mute = mute
    }

}