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
import edu.bluejack22_1.fidertime.databinding.FragmentMemberItemBinding
import edu.bluejack22_1.fidertime.fragments.MemberListFragment
import edu.bluejack22_1.fidertime.models.MessageMember
import edu.bluejack22_1.fidertime.models.User

class MemberRecycleViewAdapter(
    query: Query,
    private val currentUser: MessageMember,
    private val fragment: MemberListFragment ,
    private val messageId : String,
    private val memberGroupIds : ArrayList<String>
) : FirestoreAdapter<MemberRecycleViewAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMemberItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding , currentUser , fragment , messageId , memberGroupIds)
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

    class ViewHolder(
            private val itemBinding: FragmentMemberItemBinding,
            private val currentUser : MessageMember,
            private val fragment: MemberListFragment,
            private val messageId: String,
            private val memberGroupIds : ArrayList<String>
        ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(snapshot: DocumentSnapshot?) {
            val user = snapshot?.toObject<User>()!!
            user.id = snapshot.id
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

            // hide remove member
            if(!currentUser.admin || user.id == currentUser.id){
                itemBinding.removeMember.visibility = View.GONE
            }else{
                itemBinding.removeMember.setOnClickListener {
                    FirebaseQueries.deleteMember(messageId , user.id , memberGroupIds){
                        Toast.makeText(itemBinding.root.context , R.string.success_remove_member , Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // get user role
            FirebaseQueries.getUserRole(messageId , user.id){
                if(it.admin){
                    itemBinding.role.text = itemBinding.root.context.getString(R.string.admin)
                }else{
                    itemBinding.role.text = itemBinding.root.context.getString(R.string.member)
                }
            }
        }
    }
}