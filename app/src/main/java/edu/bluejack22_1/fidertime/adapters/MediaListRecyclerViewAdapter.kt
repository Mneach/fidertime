package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemOutBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMediaItemBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMediaVideoItemBinding
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Media

class MediaListRecyclerViewAdapter(query : Query) : FirestoreAdapter<MediaListRecyclerViewAdapter.ViewHolder>(query) {

    private val IMAGETYPE = 1
    private val VIDEOTYPE = 2

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        if(viewType == IMAGETYPE){
            val binding = FragmentMediaItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ImageViewHolder(binding , binding.root)
        }else{
            val binding = FragmentMediaVideoItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return VideoViewHolder(binding , binding.root)
        }

    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (getItemViewType(position) == IMAGETYPE) {
            getSnapshot(position)?.let { snapshot -> (viewHolder as ImageViewHolder).bind(snapshot) }
        }else{
            getSnapshot(position)?.let { snapshot -> (viewHolder as VideoViewHolder).bind(snapshot) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(getSnapshot(position)?.getString("type") == "image"){
            return IMAGETYPE
        }else{
            return VIDEOTYPE
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(snapshot: DocumentSnapshot)
    }

    class ImageViewHolder(private val itemBinding: FragmentMediaItemBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val media = snapshot.toObject<Media>()!!
            itemBinding.imageViewProfile.load(media.url) {
                crossfade(true)
                crossfade(300)
                placeholder(R.drawable.image_placeholder)
            }
        }
    }

    class VideoViewHolder(private val itemBinding: FragmentMediaVideoItemBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val media = snapshot.toObject<Media>()!!
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