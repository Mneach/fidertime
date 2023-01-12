package edu.bluejack22_1.fidertime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentFileItemBinding
import edu.bluejack22_1.fidertime.models.Media
import java.text.DecimalFormat

class FileListRecyclerViewAdapter (private var medias : ArrayList<Media>) : RecyclerView.Adapter<FileListRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemBinding = FragmentFileItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactItem = medias[position]
        holder.bind(contactItem)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(contactItem.id)
        }
    }

    var onItemClick : ((String) -> Unit)? = null

    class ViewHolder(private val itemBinding: FragmentFileItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {


        fun bind(media: Media) {

            val size: Double? = media.size?.toDouble()?.div(1024.0)
            val covertSizeToDecimal = DecimalFormat("#.#").format(size)
            val convertToDate = media.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getDateFormat() }
            val convertToTime = media.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getTimeFormat() }

            val resultDate = "$convertToDate at $convertToTime"
            itemBinding.name.text = media.name
            itemBinding.fileDescription.text = covertSizeToDecimal.toString() + " KB, " + resultDate
        }
    }


}