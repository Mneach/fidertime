package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.activities.BOT_ID
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemOutBinding
import edu.bluejack22_1.fidertime.models.Chat

const val IN = 1
const val OUT = 2

class BotChatListRecyclerViewAdapter(private val chats: ArrayList<Chat>) :
    RecyclerView.Adapter<BotChatListRecyclerViewAdapter.ViewHolder>() {

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(chat: Chat)
    }

    class ChatInViewHolder(private val binding: FragmentChatItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(chat: Chat) {
            binding.textViewChat.text = chat.chatText
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
            binding.textViewName.text = "Bot"
        }

    }

    class ChatOutViewHolder(private val binding: FragmentChatItemOutBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(chat: Chat) {
            binding.textViewChat.text = chat.chatText
            binding.textViewReadBy.text = itemView.context.getString(R.string.read)
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == IN) {
            val binding = FragmentChatItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            ChatInViewHolder(binding, binding.root)
        } else {
            val binding = FragmentChatItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            ChatOutViewHolder(binding, binding.root)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chats[position].senderUserId == BOT_ID) {
            IN
        } else {
            OUT
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (getItemViewType(position) == IN) {
            (viewHolder as ChatInViewHolder).bind(chats[position])
        }
        if (getItemViewType(position) == OUT) {
            (viewHolder as ChatOutViewHolder).bind(chats[position])
        }
    }

    override fun getItemCount() = chats.size

}