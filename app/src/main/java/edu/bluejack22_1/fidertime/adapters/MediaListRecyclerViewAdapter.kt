package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.databinding.FragmentMediaItemBinding
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Media

class MediaListRecyclerViewAdapter(query : Query) : FirestoreAdapter<MediaListRecyclerViewAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentMediaItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mediaItem = getSnapshot(position)
        if (mediaItem != null) {
            viewHolder.bind(mediaItem)
        }
        viewHolder.itemView.setOnClickListener {
            if (mediaItem != null) {
                onItemClick?.invoke(mediaItem.id)
            }
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentMediaItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(snapshot: DocumentSnapshot) {
            val media = snapshot.toObject<Media>()!!
            itemBinding.imageViewProfile.load(media.url)
        }
    }
}