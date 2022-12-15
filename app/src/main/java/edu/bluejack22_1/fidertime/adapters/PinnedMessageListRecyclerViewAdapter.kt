package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.databinding.FragmentPinnedMessageItemBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import edu.bluejack22_1.fidertime.models.UserMessage

class PinnedMessageListRecyclerViewAdapter(query: Query) : FirestoreAdapter<PinnedMessageListRecyclerViewAdapter.ViewHolder>(query) {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemBinding = FragmentPinnedMessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val snapshot = getSnapshot(position)
        viewHolder.bind(snapshot)
        viewHolder.itemView.setOnClickListener {
            snapshot?.id?.let { it1 -> onItemClick?.invoke(it1) }
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentPinnedMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        private val userId = Firebase.auth.currentUser!!.uid

        fun bind(snapshot: DocumentSnapshot?) {
            val messageItem = snapshot!!.toObject<Message>()!!
            messageItem.id = snapshot.id
            FirebaseQueries.subscribeToMessage(messageItem.id) { message ->
                setNameAndProfile(message)
            }
        }

        private fun setNameAndProfile(message: Message) {
            if (message.messageType == "group") {
                itemBinding.textViewName.text = message.groupName
                itemBinding.imageViewProfile.load(message.groupImageUrl)
            }
            else {
                val withUserId = message.members.find { memberId -> memberId != userId }
                FirebaseQueries.subscribeToUser(withUserId!!) { user ->
                    setNameAndProfile(user)
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


}