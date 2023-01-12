package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentChooseAdminBinding
import edu.bluejack22_1.fidertime.databinding.FragmentContactItemBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMemberItemBinding
import edu.bluejack22_1.fidertime.fragments.MemberListFragment
import edu.bluejack22_1.fidertime.models.MessageMember
import edu.bluejack22_1.fidertime.models.User

class ChooseAdminRecyclerViewAdapter (query: Query) : FirestoreAdapter<ChooseAdminRecyclerViewAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentChooseAdminBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
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

    var onItemClick: ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentChooseAdminBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(snapshot: DocumentSnapshot?) {
            val user = snapshot?.toObject<User>()!!
            user.id = snapshot.id
            if (user.profileImageUrl.isNotEmpty()) {
                itemBinding.imageViewProfile.load(user.profileImageUrl) {
                    crossfade(true)
                    crossfade(300)
                    placeholder(R.drawable.image_placeholder)
                }
            } else {
                itemBinding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
            }

            val last_seen = itemView.context.getString(R.string.last_seen)
            itemBinding.name.text = user.name
            itemBinding.status.text = if (user.status == "offline") {
                "$last_seen " + user.lastSeenTimestamp?.toDate()
                    ?.let { RelativeDateAdapter(it).getRelativeString() }
            } else {
                user.status
            }
        }
    }
}