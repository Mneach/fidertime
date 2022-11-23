package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import edu.bluejack22_1.fidertime.databinding.FragmentMediaItemBinding
import edu.bluejack22_1.fidertime.models.Media

class MediaListRecyclerViewAdapter(private val media: ArrayList<Media>) : RecyclerView.Adapter<MediaListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMediaItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
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

    class ViewHolder(private val itemBinding: FragmentMediaItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(messageItem: Media) {
            itemBinding.imageViewProfile.load(messageItem.url)
        }
    }
}