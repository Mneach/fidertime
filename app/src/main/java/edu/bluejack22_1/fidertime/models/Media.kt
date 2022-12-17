package edu.bluejack22_1.fidertime.models

import com.google.firebase.Timestamp

data class Media (
    var id: String = "",
    var name : String? = "",
    var messageId: String = "",
    var timestamp: Timestamp? = null,
    var senderUserId: String = "",
    var type: String = "",
    var size : Long? = 0,
    var url: String = "",
)