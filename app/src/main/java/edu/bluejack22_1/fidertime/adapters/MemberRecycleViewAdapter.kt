package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentMediaItemBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMemberItemBinding
import edu.bluejack22_1.fidertime.models.Media
import edu.bluejack22_1.fidertime.models.User
import java.text.DecimalFormat

class MemberRecycleViewAdapter(query : Query) : FirestoreAdapter<MemberRecycleViewAdapter.ViewHolder>(query) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMemberItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val fileItem = getSnapshot(position)
        viewHolder.bind(fileItem)
        viewHolder.itemView.setOnClickListener {
            if (fileItem != null) {
                onItemClick?.invoke(fileItem.id)
            }
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentMemberItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(snapshot: DocumentSnapshot?) {
            val user = snapshot?.toObject<User>()!!
            if(user.profileImageUrl.isNotEmpty()){
                itemBinding.imageViewProfile.load(user.profileImageUrl) {
                    crossfade(true)
                    crossfade(300)
                    placeholder(R.drawable.image_placeholder)
                }
            }else{
                itemBinding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
            }
            itemBinding.name.text = user.name
            itemBinding.status.text = if (user.status == "offline") {
                "Last seen " + user.lastSeenTimestamp?.toDate()
                    ?.let { RelativeDateAdapter(it).getRelativeString() }
            } else {
                user.status
            }
        }
    }
}