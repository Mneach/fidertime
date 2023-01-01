package edu.bluejack22_1.fidertime.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.activities.BotMessageActivity
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.MessageListPagerAdapter
import edu.bluejack22_1.fidertime.adapters.PinnedMessageListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMessageBinding
import edu.bluejack22_1.fidertime.models.UserMessage

class MessageFragment() : Fragment() {

    private var _binding : FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PinnedMessageListRecyclerViewAdapter
    private lateinit var recyclerViewMessages: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        initializeTabs()
        initializeRecyclerView()
        initializeMessageBotButton()
        return binding.root
    }

    private fun initializeMessageBotButton() {
        binding.messageBotButton.setOnClickListener {
            val intent = Intent(context, BotMessageActivity::class.java)
            intent.putExtra("messageId", "bot")
            startActivity(intent)
        }
    }

    private fun initializeRecyclerView() {
        recyclerViewMessages =binding.recyclerViewPinnedMessages
        recyclerViewMessages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.HORIZONTAL))
        val userId = Firebase.auth.currentUser!!.uid
        val query = Firebase.firestore.collection("users").document(userId).collection("messages")
            .whereEqualTo("pinned", true)
        attachRecyclerViewAdapter(query)
    }

    private fun attachRecyclerViewAdapter(query: Query) {
        adapter = PinnedMessageListRecyclerViewAdapter(query)
        adapter.onItemClick = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("messageId", it)
            startActivity(intent)
        }
        recyclerViewMessages.adapter = adapter
    }

    private fun initializeTabs() {
        val messageListPagerAdapter = MessageListPagerAdapter(parentFragmentManager, lifecycle)
        binding.pagerMessageList.adapter = messageListPagerAdapter

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.all)
                }
                1 -> {
                    tab.text = getString(R.string.personal)
                }
                2 -> {
                    tab.text = getString(R.string.group)
                }
            }
        }.attach()
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