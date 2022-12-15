package edu.bluejack22_1.fidertime.common

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.fidertime.models.*

class FirebaseQueries {

    companion object {


        fun getUsers(callback: (users : ArrayList<User>) -> Unit){
            val usersRef = Firebase.firestore.collection("users")
            usersRef.get().addOnSuccessListener{ value ->
                val users = ArrayList<User>()
                for (doc in value) {
                    val user = doc.toObject<User>()
                    user.id = doc.id
                    users.add(user)
                }
                callback.invoke(users)
            }
        }

        fun getUserById(userId: String , callback : (user : User) -> Unit){

            val usersRef = Firebase.firestore.collection("users").document(userId)
            usersRef.get().addOnSuccessListener{value ->
                if (value != null && value.exists()) {
                    val user = value.toObject<User>()
                    user!!.id = value.id
                    callback.invoke(user)
                }else{
                    // return object with empty value
                    val user = User()
                    callback.invoke(user)
                }
            }
        }

        fun getMessageByUserAndMessageType(userId : String , messageType : String , callback: (messages : ArrayList<Message>) -> Unit){
            val messageRef = Firebase.firestore.collection("messages")
            messageRef.whereArrayContains("members" , userId).whereEqualTo("messageType" , messageType)
                .get().addOnSuccessListener { value ->
                    var messages = ArrayList<Message>()
                    if (value != null && !value.isEmpty) {
                        for(doc in value){
                            var message = doc.toObject<Message>()
                            message.id = doc.id
                            messages.add(message)
                        }
                        callback.invoke(messages)
                    }else{
                        // return object with empty value
                        callback.invoke(messages)
                    }
                }
        }

        fun getUserByPhoneNumber(phoneNumber: String , callback : (user : ArrayList<User>) -> Unit){
            val usersRef = Firebase.firestore.collection("users")
            usersRef.whereEqualTo("phoneNumber", phoneNumber)
                .get().addOnSuccessListener{value ->
                    val users = ArrayList<User>()
                    for (doc in value!!) {
                        val user = doc.toObject<User>()
                        user.id = doc.id
                        users.add(user)
                    }
                    callback.invoke(users)
            }
        }

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

        fun subscribeToContacts(callback: (user: ArrayList<User>) -> Unit) {
            val userRef = Firebase.firestore.collection("users").orderBy("name" , Direction.ASCENDING)
            userRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val users = ArrayList<User>()
                    for(doc in snapshot){
                        if(doc.id != Utilities.getAuthFirebase().uid){
                            val user = doc.toObject<User>()
                            user.id = doc.id
                            users.add(user)
                        }
                    }
                    callback.invoke(users)
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

        fun updateUserData(docId : String , user : User , callback: () -> Unit){
            Firebase.firestore.collection("users")
                .document(docId)
                .set(user)
                .addOnSuccessListener {
                    callback.invoke()
                }
        }

        fun uploadImage(user : User , filePath : Uri , context : Context , callback: (imageUrl : String) -> Unit){
            val imageName = Utilities.getImageName(filePath.path.toString())
            val storageReference = FirebaseStorage.getInstance().getReference(user.email + "/images/" + imageName)

            var progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait...")
            progressDialog.show()

            storageReference.putFile(filePath)
                .addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { imageUrl ->
                        progressDialog.dismiss()
                        Log.d("Image Url = " , imageUrl.toString())
                        callback.invoke(imageUrl.toString())
                    }
                }
                .addOnFailureListener{
                    return@addOnFailureListener
                }
                .addOnProgressListener {
                    var currentProgress = (100.0 * it.bytesTransferred) / it.totalByteCount
                    progressDialog.setMessage("Progress ${currentProgress.toInt()}%")
                }
        }



        fun uploadMedia(filePath : Uri, type: String, context : Context, callback: (imageUrl : String) -> Unit){
            val userId = Firebase.auth.currentUser!!.uid
            val fileName = Utilities.getImageName(filePath.path.toString())
            val storageReference = FirebaseStorage.getInstance().getReference(userId + "/" + type + "s/" + fileName)

            var progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait...")
            progressDialog.show()

            storageReference.putFile(filePath)
                .addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { imageUrl ->
                        progressDialog.dismiss()
                        Log.d("Image Url = " , imageUrl.toString())
                        callback.invoke(imageUrl.toString())
                    }
                }
                .addOnFailureListener{
                    return@addOnFailureListener
                }
                .addOnProgressListener {
                    var currentProgress = (100.0 * it.bytesTransferred) / it.totalByteCount
                    progressDialog.setMessage("Progress ${currentProgress.toInt()}%")
                }
        }

        fun updateMemberLastVisit(messageId: String, userId: String) {
            Firebase.firestore.collection("messages").document(messageId)
                .collection("members").document(userId)
                .update("lastVisitTimestamp", Timestamp.now())
        }

        fun sendChatMedia(chat: Chat, callback: () -> Unit) {
            val messageId = chat.messageId
            val media = Media("", null, chat.messageId, Timestamp.now(), chat.senderUserId, chat.chatType, null, chat.mediaUrl)
            val chatsRef = Firebase.firestore.collection("chats").document()
            val messagesRef = Firebase.firestore.collection("messages").document(messageId)
            val mediaRef = Firebase.firestore.collection("media").document()

            Firebase.firestore.runBatch { batch ->
                batch.set(chatsRef, chat)
                batch.update(messagesRef, mapOf(
                    "lastChatText" to chat.chatText,
                    "lastChatTimestamp" to chat.timestamp,
                    "lastChatType" to chat.chatType
                ))
                batch.set(mediaRef, media)
            }.addOnSuccessListener {
                callback.invoke()
            }
        }

        fun sendChatText(chat: Chat, callback: () -> Unit) {
            val messageId = chat.messageId
            val chatsRef = Firebase.firestore.collection("chats").document()
            val messagesRef = Firebase.firestore.collection("messages").document(messageId)

            Firebase.firestore.runBatch { batch ->
                batch.set(chatsRef, chat)
                batch.update(messagesRef, mapOf(
                    "lastChatText" to chat.chatText,
                    "lastChatTimestamp" to chat.timestamp,
                    "lastChatType" to chat.chatType
                ))
            }.addOnSuccessListener {
                callback.invoke()
            }
        }

        fun addMessage(message : Message, callback: (messageId : String) -> Unit){
            val usersRef = Firebase.firestore.collection("users")
            val messageRef = Firebase.firestore.collection("messages").document()
            val messageMembersRef = Firebase.firestore.collection("messages").document(messageRef.id).collection("members")
            val messageMembers = message.members

            Firebase.firestore.runBatch { batch ->
                batch.set(messageRef, message)
                messageMembers.forEach {messageMember ->
                    batch.set(messageMembersRef.document(messageMember), MessageMember(messageMember, false, null))
                    batch.set(usersRef.document(messageMember).collection("messages").document(messageRef.id),
                        UserMessage(id = messageRef.id, notified = true, pinned = false))
                }
            }.addOnSuccessListener {
                callback.invoke(messageRef.id)
            }
        }

        fun getMessageIsPinned(userId: String, messageId: String, callback: (Boolean) -> Unit) {
            val userMessagesRef = Firebase.firestore.collection("users").document(userId).collection("messages").document(messageId)
            userMessagesRef.get().addOnSuccessListener { snapshot ->
                val pinned = snapshot.getBoolean("pinned")!!
                callback.invoke(pinned)
            }
        }

        fun togglePinnedMessage(userId: String, messageId: String, pinnedBefore: Boolean, callback: () -> Unit) {
            val userMessagesRef = Firebase.firestore.collection("users").document(userId).collection("messages").document(messageId)
            userMessagesRef.update("pinned", !pinnedBefore)
            callback.invoke()
        }

        fun subscribeToMemberLastVisit(messageId: String, timestamp: Timestamp, callback: (Int) -> Unit) {
            val messageMembersRef = Firebase.firestore.collection("messages").document(messageId).collection("members")
                .whereGreaterThanOrEqualTo("lastVisitTimestamp", timestamp)
            messageMembersRef.addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    callback.invoke(value.size() - 1)
                }
            }
        }
    }
}