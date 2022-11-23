package edu.bluejack22_1.fidertime.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_1.fidertime.databinding.FragmentLinkItemBinding
import edu.bluejack22_1.fidertime.models.Media

class LinkListRecyclerViewAdapter (private val media: ArrayList<Media>) : RecyclerView.Adapter<LinkListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentLinkItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mediaItem = media[position]
        viewHolder.bind(mediaItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(mediaItem.id)
        }
    }

    override fun getItemCount() = media.size

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentLinkItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(fileItem: Media) {
            itemBinding.name.text = fileItem.url
        }
    }
}