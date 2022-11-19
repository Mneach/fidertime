package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.databinding.FragmentPinnedMessageItemBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import edu.bluejack22_1.fidertime.models.UserMessage

class PinnedMessageListRecyclerViewAdapter(private val messages: ArrayList<UserMessage>) : RecyclerView.Adapter<PinnedMessageListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemBinding = FragmentPinnedMessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val messageItem = messages[position]
        viewHolder.bind(messageItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(messageItem.id)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentPinnedMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        private val db = Firebase.firestore
        private val userId = "Km69GgIsRZhgKUsb0aIq0YSZWVX2"

        fun bind(messageItem: UserMessage) {
            subscribeToMessage(messageItem)
        }

        private fun subscribeToMessage(messageItem: UserMessage) {
            val messageId = messageItem.id
            val messageRef = db.collection("messages").document(messageId)
            messageRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val message = snapshot.toObject<Message>()
                    message!!.id = snapshot.id
                    setNameAndProfile(message)
                }
            }
        }

        private fun setNameAndProfile(message: Message) {
            if (message.messageType == "group") {
                itemBinding.textViewName.text = message.groupName
                itemBinding.imageViewProfile.load(message.groupImageUrl)
            }
            else {
                val withUserId = message.members.find { memberId -> memberId != userId }
                subscribeToUser(withUserId!!)
            }
        }

        private fun setNameAndProfile(user: User) {
            itemBinding.textViewName.text = user.name
            itemBinding.imageViewProfile.load(user.profileImageUrl)
        }

        private fun subscribeToUser(withUserId: String) {
            val userRef = db.collection("users").document(withUserId)
            userRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject<User>()
                    user!!.id = snapshot.id
                    setNameAndProfile(user)
                }
            }
        }
    }


}