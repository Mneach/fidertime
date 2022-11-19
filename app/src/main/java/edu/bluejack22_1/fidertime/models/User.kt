package edu.bluejack22_1.fidertime.models

data class User(
    var id: String = "",
    var bio: String = "",
    var email: String = "",
    var media: ArrayList<String> = arrayListOf(),
    var name: String = "",
    var phoneNumber: String = "",
    var profileImageUrl: String = "",
    var status: String = "",
    var stories: ArrayList<String> = arrayListOf()
)