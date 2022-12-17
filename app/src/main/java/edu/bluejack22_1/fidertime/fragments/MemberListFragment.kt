package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import edu.bluejack22_1.fidertime.adapters.MediaListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.MemberRecycleViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMediaListBinding
import edu.bluejack22_1.fidertime.databinding.FragmentMemberListBinding

class MemberListFragment (
    private val memberListRef : Query
) : Fragment() {

    private var _binding: FragmentMemberListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var adapter : MemberRecycleViewAdapter;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemberListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewMember
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))

        attachRecyclerViewAdapter(memberListRef)
        return binding.root
    }

    private fun attachRecyclerViewAdapter(query: Query) {
        adapter = MemberRecycleViewAdapter(query)
        recyclerViewMessages.adapter = adapter
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