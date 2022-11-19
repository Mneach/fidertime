package edu.bluejack22_1.fidertime.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.MessageListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMessageListBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.UserMessage

class MessageListFragment(
    private val userMessagesRef: Query
) : Fragment() {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var userMessagesListener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewMessages
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, null))

        subscribeToUserMessages()
        return binding.root
    }

    private fun subscribeToUserMessages() {
        userMessagesListener = userMessagesRef.addSnapshotListener { value, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (value != null && !value.isEmpty) {
                val userMessages = arrayListOf<Message>()
                for (doc in value) {
                    val userMessage = doc.toObject<Message>()
                    userMessage.id = doc.id
                    userMessages.add(userMessage)
                }
                attachRecyclerViewAdapter(userMessages)
            }
        }
    }

    private fun attachRecyclerViewAdapter(userMessages: ArrayList<Message>) {
        val adapter = MessageListRecyclerViewAdapter(userMessages)
        adapter.onItemClick = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("messageId", it)
            startActivity(intent)
        }
        recyclerViewMessages.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Intent.ACTION_TIME_TICK)
        context?.registerReceiver(RelativeTimeBroadCastReceiver(), filter)
    }

    class RelativeTimeBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}