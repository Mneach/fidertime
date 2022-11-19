package edu.bluejack22_1.fidertime.models

import com.google.firebase.Timestamp

data class UserMessage(
    var id: String = "",
    var lastVisitTimestamp: Timestamp? = null,
    var notified: Boolean = false,
    var pinned: Boolean = false,
    var messageType: String = ""
)