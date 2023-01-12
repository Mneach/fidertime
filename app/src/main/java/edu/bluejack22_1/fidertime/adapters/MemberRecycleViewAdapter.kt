package edu.bluejack22_1.fidertime.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentMemberItemBinding
import edu.bluejack22_1.fidertime.fragments.MemberListFragment
import edu.bluejack22_1.fidertime.models.MessageMember
import edu.bluejack22_1.fidertime.models.User

class MemberRecycleViewAdapter (
    private var members : ArrayList<User>,
    private val currentUser: MessageMember,
    private val fragment: MemberListFragment,
    private val messageId : String,
    private val memberGroupIds : ArrayList<String>
    ) : RecyclerView.Adapter<MemberRecycleViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMemberItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding , currentUser , fragment , messageId , memberGroupIds)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val fileItem = members[position]
        viewHolder.bind(fileItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(fileItem.id)
        }
    }

    fun getData(): ArrayList<User> {
        return this.members
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(
        private val itemBinding: FragmentMemberItemBinding,
        private val currentUser : MessageMember,
        private val fragment: MemberListFragment,
        private val messageId: String,
        private val memberGroupIds : ArrayList<String>
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(user : User) {
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
                itemView.context.getString(R.string.last_seen).plus(" ") + user.lastSeenTimestamp?.toDate()
                    ?.let { RelativeDateAdapter(it).getRelativeString() }
            } else {
                user.status
            }

            // hide remove

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

    override fun getItemCount(): Int {
        return members.size
    }
}