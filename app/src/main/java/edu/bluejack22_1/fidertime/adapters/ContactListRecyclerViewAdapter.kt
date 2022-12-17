package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.databinding.FragmentContactItemBinding
import edu.bluejack22_1.fidertime.models.User

class ContactListRecyclerViewAdapter (private var contactItems : ArrayList<User>) : RecyclerView.Adapter<ContactListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentContactItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactItem = contactItems[position]
        holder.bind(contactItem)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(contactItem.id)
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    fun setFilteredList(filteredContactItems : ArrayList<User>){
        this.contactItems = filteredContactItems
        notifyDataSetChanged()
    }


    class ViewHolder(private val itemBinding: FragmentContactItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(contactItem: User) {

            itemBinding.textViewName.text = contactItem.name
            itemBinding.phoneNumber.text = contactItem.phoneNumber
            if(contactItem.profileImageUrl.isNotEmpty()){
                itemBinding.imageViewProfile.load(contactItem.profileImageUrl)
            }else{
                itemBinding.imageViewProfile.setImageResource(R.drawable.default_avatar)
            }
        }
    }

    override fun getItemCount(): Int {
        return contactItems.size
    }

}