package edu.bluejack22_1.fidertime.models

import com.google.firebase.Timestamp

data class MessageMember(
    var id: String = "",
    var admin: Boolean = false,
    var lastVisitTimestamp: Timestamp? = null
)