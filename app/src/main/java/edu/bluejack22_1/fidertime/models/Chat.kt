package edu.bluejack22_1.fidertime.models

import com.google.firebase.Timestamp

data class Chat(
    var id: String = "",
    var chatText: String = "",
    var chatType: String = "",
    var messageId: String = "",
    var readBy: ArrayList<String> = arrayListOf(),
    var senderUserId: String = "",
    var timestamp: Timestamp? = null
)