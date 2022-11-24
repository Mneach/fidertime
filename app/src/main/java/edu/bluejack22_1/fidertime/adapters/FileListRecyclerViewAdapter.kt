package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentFileItemBinding
import edu.bluejack22_1.fidertime.models.Media
import java.text.DecimalFormat

class FileListRecyclerViewAdapter(query : Query) : FirestoreAdapter<FileListRecyclerViewAdapter.ViewHolder>(query) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentFileItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val fileItem = getSnapshot(position)
        viewHolder.bind(fileItem)
        viewHolder.itemView.setOnClickListener {
            if (fileItem != null) {
                onItemClick?.invoke(fileItem.id)
            }
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentFileItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(snapshot: DocumentSnapshot?) {
            val fileItem = snapshot?.toObject<Media>()!!
            val size : Double? = fileItem.size?.toDouble()?.div(1024.0)
            val covertSizeToDecimal = DecimalFormat("#.#").format(size)
            val convertToDate = fileItem.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getDateFormat() }
            val convertToTime = fileItem.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getTimeFormat() }

            val resultDate = "$convertToDate at $convertToTime"
            itemBinding.name.text = fileItem.name
            itemBinding.fileDescription.text = covertSizeToDecimal.toString() + " KB, " + resultDate
        }
    }
}