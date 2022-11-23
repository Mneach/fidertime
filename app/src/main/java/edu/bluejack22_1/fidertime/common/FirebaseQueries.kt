package edu.bluejack22_1.fidertime.common

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import edu.bluejack22_1.fidertime.models.UserMessage

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
    }
}