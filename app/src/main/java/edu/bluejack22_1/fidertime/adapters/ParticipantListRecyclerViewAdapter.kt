package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.databinding.FragmentContactItemBinding
import edu.bluejack22_1.fidertime.models.User

class ParticipantListRecyclerViewAdapter (private var contactItems : ArrayList<User>) : RecyclerView.Adapter<ParticipantListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentContactItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
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

    fun setFilteredList(filteredParticipantData : ArrayList<User>){
        this.contactItems = filteredParticipantData
        notifyDataSetChanged()
    }


    class ViewHolder(private val itemBinding: FragmentContactItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(contactItem: User) {

            itemBinding.textViewName.text = contactItem.name
            itemBinding.phoneNumber.text = contactItem.phoneNumber
            if(contactItem.profileImageUrl.isNotEmpty()){
                itemBinding.imageViewProfile.load(contactItem.profileImageUrl)
            }
        }
    }

    override fun getItemCount(): Int {
        return contactItems.size
    }

}