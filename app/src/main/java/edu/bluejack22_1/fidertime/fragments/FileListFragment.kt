package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.FileListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.LinkListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.MediaListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentFileListBinding
import edu.bluejack22_1.fidertime.models.Media

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FileListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FileListFragment (
    private val imageRef : Query
        ) : Fragment() {

    private var _binding: FragmentFileListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var userMediaListener : ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFileListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewImage
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))

        subscribeToFiles()
        return binding.root
    }

    private fun subscribeToFiles() {
        userMediaListener = imageRef.addSnapshotListener { value, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (value != null && !value.isEmpty) {
                val userFiles = arrayListOf<Media>()
                for (doc in value) {
                    val media = doc.toObject<Media>()
                    media.id = doc.id
                    userFiles.add(media)
                }
                attachRecyclerViewAdapter(userFiles)
            }
        }
    }

    private fun attachRecyclerViewAdapter(userMedia: ArrayList<Media>) {
        val adapter = FileListRecyclerViewAdapter(userMedia)
        recyclerViewMessages.adapter = adapter
    }
}