package com.arashforus.nearroom

class Object_Media{

    var type : String? = "PIC"
    var folderId: String? = null
    var folderName: String? = null
    var id: Int? = 0
    var name: String?  = null
    var path: String?  = null
    var albumId:String? = null
    var size: Int? = 0
    var date: String? = null
    var mimeType: String? = null

    constructor( folderId:String? ,folderName: String?, id:Int? , name:String? , path: String?, size: Int? , date: String?, mimeType: String? ){
        this.folderId = folderId
        this.folderName = folderName
        this.id = id
        this.name = name
        this.path = path
        this.size = size
        this.date = date
        this.mimeType = mimeType
    }

    constructor( type : String? , folderId:String? ,folderName: String?, id:Int? , name:String? , path: String?, size: Int? , date: String?, mimeType: String? ){
        this.type = type
        this.folderId = folderId
        this.folderName = folderName
        this.id = id
        this.name = name
        this.path = path
        this.size = size
        this.date = date
        this.mimeType = mimeType
    }

    // For Audio Files
    constructor( type : String? , folderId:String? ,folderName: String?, id:Int? ,albumId:String? , name:String? , path: String?, size: Int? , date: String?, mimeType: String? ){
        this.type = type
        this.folderId = folderId
        this.folderName = folderName
        this.id = id
        this.albumId = albumId
        this.name = name
        this.path = path
        this.size = size
        this.date = date
        this.mimeType = mimeType
    }


}