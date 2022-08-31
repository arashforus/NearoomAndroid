package com.arashforus.nearroom

class Object_Album {

    var type : String? = null
    var folderId: String? = null
    var folderName: String? = null
    var imagePath: String?  = null
    var imageCount: Int = 0
    var date: String? = null
    var audioPath: String? = null


    constructor( folderName: String?, imagePath: String?, imageCount: Int, date: String? ){
        this.folderName = folderName
        this.imagePath = imagePath
        this.imageCount = imageCount
        this.date = date
    }

    constructor( folderId : String? , folderName: String?, imagePath: String?, imageCount: Int, date: String? ){
        this.folderId = folderId
        this.folderName = folderName
        this.imagePath = imagePath
        this.imageCount = imageCount
        this.date = date
    }

    constructor( type : String ,folderId : String? , folderName: String?, imagePath: String?, imageCount: Int, date: String? ){
        this.type = type
        this.folderId = folderId
        this.folderName = folderName
        this.imagePath = imagePath
        this.imageCount = imageCount
        this.date = date
    }

    // For Audio Albums
    constructor( type : String ,folderId : String? , folderName: String?, imagePath: String?, audioPath:String? , imageCount: Int, date: String? ){
        this.type = type
        this.folderId = folderId
        this.folderName = folderName
        this.imagePath = imagePath
        this.audioPath = audioPath
        this.imageCount = imageCount
        this.date = date
    }

}