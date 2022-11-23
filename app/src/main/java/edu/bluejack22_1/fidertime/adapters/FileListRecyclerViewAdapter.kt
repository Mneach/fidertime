package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentFileItemBinding
import edu.bluejack22_1.fidertime.models.Media
import java.text.DecimalFormat

class FileListRecyclerViewAdapter(private val media: ArrayList<Media>) : RecyclerView.Adapter<FileListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FragmentFileItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
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

    class ViewHolder(private val itemBinding: FragmentFileItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(fileItem: Media) {
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