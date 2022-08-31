package com.arashforus.nearroom

class Object_Slider {
    var title: String? = null
    var description: String?  = null
    var color: Int? = null
    var photo: Int? = null
    var photo2: Int? = null
    var photo3: Int? = null

    constructor( title: String?, description: String?, color: Int? , photo: Int? ,photo2: Int? ,photo3: Int? ){
        this.title = title
        this.description = description
        this.color = color
        this.photo = photo
        this.photo2 = photo2
        this.photo3 = photo3
    }
}