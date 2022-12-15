package edu.bluejack22_1.fidertime.models

data class UserMessage(
    var id: String = "",
    var notified: Boolean = false,
    var pinned: Boolean = false,
)