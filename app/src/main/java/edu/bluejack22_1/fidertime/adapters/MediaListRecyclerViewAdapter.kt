package edu.bluejack22_1.fidertime.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.databinding.FragmentMediaItemBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMediaVideoItemBinding
import edu.bluejack22_1.fidertime.models.Media

class MediaListRecyclerViewAdapter (private var medias : ArrayList<Media>) : RecyclerView.Adapter<MediaListRecyclerViewAdapter.ViewHolder>(){
    private val IMAGETYPE = 1
    private val VIDEOTYPE = 2

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MediaListRecyclerViewAdapter.ViewHolder {
        if(viewType == IMAGETYPE){
            val binding = FragmentMediaItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ImageViewHolder(binding, binding.root)
        }else{
            val binding = FragmentMediaVideoItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return VideoViewHolder(binding, binding.root)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = medias[position]
        holder.bind(media)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(media.id)
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    override fun getItemCount(): Int {
        return medias.size
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(media : Media)
    }

    class ImageViewHolder(private val itemBinding: FragmentMediaItemBinding, itemView: View) : MediaListRecyclerViewAdapter.ViewHolder(
        itemView
    ) {
        override fun bind(media: Media) {
            itemBinding.imageViewProfile.load(media.url) {
                crossfade(true)
                crossfade(300)
                placeholder(R.drawable.image_placeholder)
            }
        }
    }

    class VideoViewHolder(private val itemBinding: FragmentMediaVideoItemBinding, itemView: View) : MediaListRecyclerViewAdapter.ViewHolder(
        itemView
    ) {
        override fun bind(media : Media) {
            val uri = Uri.parse(media.url)
            Glide.with(itemView.context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        itemBinding.imageViewMedia.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

        }
    }

}