package edu.bluejack22_1.fidertime.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentMessageItemBinding
import edu.bluejack22_1.fidertime.models.*

class MessageListRecyclerViewAdapter(private val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val messageItem = messages[position]
        viewHolder.bind(messageItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(messageItem.id)
        }
    }

    override fun getItemCount() = messages.size

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        private val db = Firebase.firestore
        private val userId = Firebase.auth.currentUser!!.uid

        fun bind(messageItem: Message) {
            itemBinding.textViewUnreadChatCount.visibility = View.GONE
            itemBinding.textViewLastChat.text = messageItem.lastChatText
            itemBinding.textViewTime.text = messageItem.lastChatTimestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getRelativeString() }

            setNameAndProfile(messageItem)
            subscribeToUnreadChatCount(messageItem)
        }

        private fun subscribeToUnreadChatCount(messageItem: Message) {
            val messageId = messageItem.id
            val messageMemberRef = db.collection("messages").document(messageId).collection("members").document(userId)
            messageMemberRef.addSnapshotListener {snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject<MessageMember>()
                    user!!.id = snapshot.id
                    setUnreadChatCount(user, messageId)
                }
            }
        }

        private fun setUnreadChatCount(user: MessageMember, messageId: String) {
            val userLastVisitTimestamp = user.lastVisitTimestamp
            if (userLastVisitTimestamp != null) {
                val unreadChatCountQuery = db.collection("chats").whereEqualTo("messageId", messageId)
                    .whereGreaterThanOrEqualTo("timestamp", userLastVisitTimestamp).count()
                unreadChatCountQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot.count > 0) {
                            itemBinding.textViewUnreadChatCount.visibility = View.VISIBLE
                            itemBinding.textViewUnreadChatCount.text = snapshot.count.toString()
                        }
                    }
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
                FirebaseQueries.subscribeToUser(withUserId!!) {
                    setNameAndProfile(it)
                }
            }
        }

        private fun setNameAndProfile(user: User) {
            itemBinding.textViewName.text = user.name
            itemBinding.imageViewProfile.load(user.profileImageUrl)
        }
    }

}