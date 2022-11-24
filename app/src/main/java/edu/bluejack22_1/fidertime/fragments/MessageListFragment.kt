package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.MessageListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMessageListBinding

class MessageListFragment(
    private val userMessagesRef: Query
) : Fragment() {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var adapter: MessageListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewMessages
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))

        attachRecyclerViewAdapter()
        return binding.root
    }

    private fun attachRecyclerViewAdapter() {
        adapter = MessageListRecyclerViewAdapter(userMessagesRef)
        adapter.onItemClick = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("messageId", it)
            startActivity(intent)
        }
        recyclerViewMessages.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        recyclerViewMessages.recycledViewPool.clear()
        adapter.startListening()
        recyclerViewMessages.recycledViewPool.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}