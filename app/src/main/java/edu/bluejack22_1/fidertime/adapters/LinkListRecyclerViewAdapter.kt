package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.databinding.FragmentLinkItemBinding
import edu.bluejack22_1.fidertime.models.Media

class LinkListRecyclerViewAdapter (query : Query) : FirestoreAdapter<LinkListRecyclerViewAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentLinkItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val linkItem = getSnapshot(position)
        viewHolder.bind(linkItem)
        viewHolder.itemView.setOnClickListener {
            if (linkItem != null) {
                val linkData = linkItem.toObject<Media>()!!
                onItemClick?.invoke(linkData.url)
            }
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentLinkItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(snapshot: DocumentSnapshot?) {
            val linkItem = snapshot?.toObject<Media>()!!
            itemBinding.name.text = linkItem.url
        }
    }
}