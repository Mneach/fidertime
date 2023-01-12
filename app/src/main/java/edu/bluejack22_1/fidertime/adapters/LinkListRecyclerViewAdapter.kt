package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_1.fidertime.databinding.FragmentLinkItemBinding
import edu.bluejack22_1.fidertime.models.Media

class LinkListRecyclerViewAdapter  (private var medias : ArrayList<Media>) : RecyclerView.Adapter<LinkListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentLinkItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val linkItem = medias[position]
        viewHolder.bind(linkItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(linkItem.url)
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentLinkItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(linkItem: Media?) {
            itemBinding.name.text = linkItem?.url
        }
    }

    override fun getItemCount(): Int {
       return medias.size
    }
}