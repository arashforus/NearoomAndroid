package com.arashforus.nearroom

class Object_Contact {

    var id: Int = 0
    var contactId: Long  = 0
    var photoUri: String? = null
    var firstName: String? = null
    var surname: String? = null
    var fullName: String? = null
    var phoneNumbers: String? = null
    var username: String? = null

    constructor( id: Int, contactId: Long, photoUri: String?, firstName: String?, surname: String?,fullName: String?, phoneNumbers: String? , username: String? ){
        this.id = id
        this.contactId = contactId
        this.photoUri = photoUri
        this.firstName = firstName
        this.surname = surname
        this.fullName = fullName
        this.phoneNumbers = phoneNumbers
        this.username = username
    }

    constructor( photoUri: String?,fullName: String?, id: Int ){
        this.photoUri = photoUri
        this.fullName = fullName
        this.id = id
    }

}