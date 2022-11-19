package edu.bluejack22_1.fidertime.common

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import edu.bluejack22_1.fidertime.models.UserMessage

class FirebaseQueries {
    companion object {
        fun subscribeToUser(userId: String, callback: (user: User) -> Unit) {
            val userRef = Firebase.firestore.collection("users").document(userId)
            userRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject<User>()
                    user!!.id = snapshot.id
                    callback.invoke(user)
                }
            }
        }

        fun subscribeToMessage(messageId: String, callback: (message: Message) -> Unit) {
            val messageRef = Firebase.firestore.collection("messages").document(messageId)
            messageRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val message = snapshot.toObject<Message>()
                    message!!.id = snapshot.id
                    callback.invoke(message)
                }
            }
        }

        fun subscribeToChats(messageId: String, lastVisible: DocumentSnapshot?, callback: (chats: ArrayList<Chat>) -> Unit) {
            val chatsRef = Firebase.firestore.collection("chats").whereEqualTo("messageId", messageId)
                .orderBy("timestamp", Direction.DESCENDING)
            chatsRef.addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val chats = ArrayList<Chat>()
                for (doc in value!!) {
                    val chat = doc.toObject<Chat>()
                    chat.id = doc.id
                    chats.add(chat)
                }
                callback.invoke(chats)
            }
        }

        fun subscribeToUserPinnedMessages(userId: String, callback: (userMessages: ArrayList<UserMessage>) -> Unit): ListenerRegistration {
            val userMessagesRef = Firebase.firestore.collection("users").document(userId).collection("messages")
                .whereEqualTo("pinned", true)
            return userMessagesRef.addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    val userMessages = arrayListOf<UserMessage>()
                    for (doc in value) {
                        val userMessage = doc.toObject<UserMessage>()
                        userMessage.id = doc.id
                        userMessages.add(userMessage)
                    }
                    callback.invoke(userMessages)
                }
            }
        }
    }
}