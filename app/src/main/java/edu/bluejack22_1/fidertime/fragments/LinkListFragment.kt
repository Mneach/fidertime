package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.FileListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.LinkListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.MediaListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentFileListBinding
import edu.bluejack22_1.fidertime.databinding.FragmentLinkListBinding
import edu.bluejack22_1.fidertime.models.Media

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LinkListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LinkListFragment (private var linkRef : Query) : Fragment() {

    private var _binding: FragmentLinkListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var adapter : LinkListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLinkListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewImage
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))

        attachRecyclerViewAdapter(linkRef)
        return binding.root
    }

    private fun attachRecyclerViewAdapter(query : Query) {
        adapter = LinkListRecyclerViewAdapter(query)
        recyclerViewMessages.adapter = adapter
        adapter.onItemClick = {
            /*val uri = Uri.parse(it)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)*/
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}