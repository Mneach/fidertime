package edu.bluejack22_1.fidertime.adapters

import android.util.Log
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
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentMessageItemBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.MessageMember
import edu.bluejack22_1.fidertime.models.User

class MessageListRecyclerViewAdapter (private var messages : ArrayList<Message>) : RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mesage = messages[position]
        if(position == itemCount - 1){
            if (mesage != null) {
                setLastItem(mesage.id)
            }
        }
        viewHolder.bind(mesage)
        viewHolder.itemView.setOnClickListener {
            mesage.id.let { it1 -> onItemClick?.invoke(it1) }
        }
    }

    var onItemClick : ((String) -> Unit)? = null
    private var lastItem : String? = null

    private fun setLastItem(idItem: String) {
        this.lastItem = idItem
    }

    fun getLastItem() : String? {
        return this.lastItem
    }

    fun getData(): ArrayList<Message> {
        return this.messages
    }

    class ViewHolder(private val itemBinding: FragmentMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        private val db = Firebase.firestore
        private val userId = Firebase.auth.currentUser!!.uid

        fun bind(messageItem: Message) {
            itemBinding.textViewUnreadChatCount.visibility = View.GONE
            if (messageItem.lastChatType == "text") {
                itemBinding.textViewLastChat.text = messageItem.lastChatText
            } else {
                if (messageItem.lastChatType == "image") {
                    itemBinding.textViewLastChat.text = itemView.context.getString(R.string.sent_an_image)
                }
                else {
                    itemBinding.textViewLastChat.text = itemView.context.getString(R.string.sent_a) + messageItem.lastChatType
                }
            }
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
            else {
                val unreadChatCountQuery = db.collection("chats").whereEqualTo("messageId", messageId).count()
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
            if(user.profileImageUrl != ""){
                itemBinding.imageViewProfile.load(user.profileImageUrl)
            }else{
                itemBinding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
            }

        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}