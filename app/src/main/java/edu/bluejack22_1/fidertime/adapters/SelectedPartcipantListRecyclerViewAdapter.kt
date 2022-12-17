package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import edu.bluejack22_1.fidertime.databinding.FragmentAddParticipantItemBinding
import edu.bluejack22_1.fidertime.databinding.FragmentContactItemBinding
import edu.bluejack22_1.fidertime.models.User

class SelectedPartcipantListRecyclerViewAdapter (private var contactItems : ArrayList<User>) : RecyclerView.Adapter<SelectedPartcipantListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentAddParticipantItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactItem = contactItems[position]
        holder.bind(contactItem)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(contactItem)
        }
    }

    var onItemClick : ((User) -> Unit)? = null

    fun setNewSelectedParticipant(filteredContactItems : ArrayList<User>){
        this.contactItems = filteredContactItems
        notifyDataSetChanged()
    }


    class ViewHolder(private val itemBinding: FragmentAddParticipantItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(contactItem: User) {

            itemBinding.textViewName.text = contactItem.name
            if(contactItem.profileImageUrl.isNotEmpty()){
                itemBinding.imageViewProfile.load(contactItem.profileImageUrl)
            }
        }
    }

    override fun getItemCount(): Int {
        return contactItems.size
    }

}