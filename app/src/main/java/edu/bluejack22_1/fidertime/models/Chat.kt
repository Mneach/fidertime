package edu.bluejack22_1.fidertime.models

import com.google.firebase.Timestamp
import edu.bluejack22_1.fidertime.common.TypeEnum

data class Chat(
    var id: String = "",
    var chatText: String = "",
    var chatType: String = "",
    var messageId: String = "",
    var readBy: ArrayList<String> = arrayListOf(),
    var senderUserId: String = "",
    var timestamp: Timestamp? = null,
    var mediaUrl: String = "",
    var mediaName: String = "",
    var mediaSize: Long = 0
) {
    fun getType(): TypeEnum {
        return when(chatType) {
            "text" -> TypeEnum.TEXT
            "image" -> TypeEnum.IMAGE
            "video" -> TypeEnum.VIDEO
            "file" -> TypeEnum.FILE
            "voice" -> TypeEnum.VOICE
            else -> TypeEnum.TEXT
        }
    }
}