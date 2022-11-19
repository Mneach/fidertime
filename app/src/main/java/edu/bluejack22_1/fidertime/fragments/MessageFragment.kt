package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ListenerRegistration
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.MessageListPagerAdapter
import edu.bluejack22_1.fidertime.adapters.PinnedMessageListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMessageBinding
import edu.bluejack22_1.fidertime.models.UserMessage

class MessageFragment : Fragment() {

    private var _binding : FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var userMessagesListener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        initializeTabs()
        initializeRecyclerView()
        return binding.root
    }

    private fun initializeRecyclerView() {
        binding.recyclerViewPinnedMessages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewPinnedMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.HORIZONTAL))
        val userId = "Km69GgIsRZhgKUsb0aIq0YSZWVX2"
        userMessagesListener = FirebaseQueries.subscribeToUserPinnedMessages(userId) {
            attachRecyclerViewAdapter(it)
        }
    }

    private fun attachRecyclerViewAdapter(userMessages: ArrayList<UserMessage>) {
        Log.d("recycler", "masuk")
        val adapter = PinnedMessageListRecyclerViewAdapter(userMessages)
        adapter.onItemClick = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("messageId", it)
            startActivity(intent)
        }
        binding.recyclerViewPinnedMessages.adapter = adapter
    }

    private fun initializeTabs() {
        val messageListPagerAdapter = MessageListPagerAdapter(parentFragmentManager, lifecycle)
        binding.pagerMessageList.adapter = messageListPagerAdapter
        Log.d("tabs", "masuk")

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = "All"
                }
                1 -> {
                    tab.text = "Personal"
                }
                2 -> {
                    tab.text = "Group"
                }
            }
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        userMessagesListener.remove()
    }
}