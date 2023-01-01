package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import edu.bluejack22_1.fidertime.adapters.MemberRecycleViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.FragmentMemberListBinding
import edu.bluejack22_1.fidertime.models.MessageMember

class MemberListFragment (
    private val memberListRef : Query,
    private val messageId : String,
    private val memberGroupIds : ArrayList<String>
) : Fragment() {

    private var _binding: FragmentMemberListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMember: RecyclerView
    private lateinit var adapter : MemberRecycleViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemberListBinding.inflate(inflater, container, false)
        recyclerViewMember = binding.recyclerViewMember
        recyclerViewMember.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMember.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        return binding.root
    }

    private fun attachRecyclerViewAdapter(query: Query , currentMember : MessageMember) {
        adapter = MemberRecycleViewAdapter(query , currentMember , this , messageId , memberGroupIds)
        recyclerViewMember.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        val userId = Utilities.getAuthFirebase().uid.toString()

        FirebaseQueries.getUserRole(messageId , userId){ messageMember ->
            attachRecyclerViewAdapter(memberListRef , messageMember)
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}