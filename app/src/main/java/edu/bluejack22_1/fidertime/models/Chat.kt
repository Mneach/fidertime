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
    var imageUrl: String = "",
) {
    fun getType(): TypeEnum {
        return when(chatType) {
            "text" -> TypeEnum.TEXT
            "image" -> TypeEnum.IMAGE
            "video" -> TypeEnum.VIDEO
            "file" -> TypeEnum.FILE
            else -> TypeEnum.TEXT
        }
    }
}