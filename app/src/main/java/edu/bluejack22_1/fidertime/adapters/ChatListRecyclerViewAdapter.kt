package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemOutBinding
import edu.bluejack22_1.fidertime.models.Chat

class ChatListRecyclerViewAdapter(query: Query) : FirestoreAdapter<ChatListRecyclerViewAdapter.ViewHolder>(query) {

    private val userId = Firebase.auth.currentUser!!.uid
    private val CHAT_IN = 1
    private val CHAT_OUT = 2

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == CHAT_OUT) {
            val binding = FragmentChatItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatOutViewHolder(binding, binding.root)
        }
        val binding = FragmentChatItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ChatInViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == CHAT_OUT) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatOutViewHolder).bind(snapshot) }
        }
        else {
            getSnapshot(position)?.let { snapshot -> (holder as ChatInViewHolder).bind(snapshot) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getSnapshot(position)?.get("senderUserId") == userId) {
            return CHAT_OUT
        }
        return CHAT_IN
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(chat: DocumentSnapshot) {}
    }

    class ChatInViewHolder(private val binding: FragmentChatItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            binding.textViewChat.text = chat.chatText
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.textViewName.text = it.name
                binding.imageViewProfile.load(it.profileImageUrl)
            }
        }
    }

    class ChatOutViewHolder(private val binding: FragmentChatItemOutBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            binding.textViewChat.text = chat.chatText
            binding.textViewReadBy.text = "Read ${chat.readBy.size.toString()}"
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.imageViewProfile.load(it.profileImageUrl)
            }
        }
    }
}