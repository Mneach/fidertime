package edu.bluejack22_1.fidertime.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Message(
    var id: String = "",
    var chats: ArrayList<String> = arrayListOf(),
    var groupDescription: String = "",
    var groupImageUrl: String = "",
    var groupName: String = "",
    var lastChatText: String? = "",
    var lastChatTimestamp: Timestamp? = null,
    var lastChatType: String? = "",
    var media: ArrayList<String> = arrayListOf(),
    var members: ArrayList<String> = arrayListOf(),
    var messageType: String? = ""
)