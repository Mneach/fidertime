package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.MessageListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.FragmentMessageListBinding
import edu.bluejack22_1.fidertime.models.Message

class MessageListFragment(
    private val userMessagesRef: Query
) : Fragment() {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
//    private lateinit var adapter: MessageListRecyclerViewAdapter
    private lateinit var adapter: MessageListRecyclerViewAdapter
    private var lastVisible : String? = null
    private lateinit var messageData : ArrayList<Message>
    var isLoading = false
    var LIMIT : Long = 7
    var prevSize : Long = 0;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewMessages
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        messageData = arrayListOf()
        adapter = MessageListRecyclerViewAdapter(messageData)
        recyclerViewMessages.adapter = adapter
        attachRecyclerViewAdapter()

        recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val itemCount = adapter.itemCount
                if(dy > 0){
                    if((recyclerViewMessages.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1 && !isLoading){
                        loadMoreData()

                    }
                }
            }
        })
        return binding.root
    }

    private fun loadMoreData(){
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        LIMIT += LIMIT;
        Log.d("LIMIT 2 : " , LIMIT.toString())
        userMessagesRef.limit(LIMIT).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val messages = ArrayList<Message>()
                for(doc in snapshot){
                    val message = doc.toObject<Message>()
                    message.id = doc.id
                    messages.add(message)
                }
                if(messages != adapter.getData()){
                    Handler(Looper.getMainLooper()).postDelayed({
                        isLoading = false
                        binding.progressBar.visibility = View.GONE
                        messageData.clear()
                        messageData.addAll(messages)
                        updateAdapter()
                        setScroll()
                    }, 500)
                }else{
                    Handler(Looper.getMainLooper()).postDelayed({
                        isLoading = false
                        binding.progressBar.visibility = View.GONE
                    }, 500)
                }
            }
        }
    }


    private fun attachRecyclerViewAdapter() {
        userMessagesRef.limit(LIMIT).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val messages = ArrayList<Message>()
                for(doc in snapshot){
                    val message = doc.toObject<Message>()
                    message.id = doc.id
                    messages.add(message)
                }
                if(adapter.getData().isEmpty()){
                    messageData.addAll(messages)
                    updateAdapter()
//                        adapter.updateData(userData)
                }else{
                    messageData.clear()
                    updateAdapter()
                }
            }
        }
    }

    private fun updateAdapter(){
        adapter = MessageListRecyclerViewAdapter(messageData)
        adapter.onItemClick = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("messageId", it)
            startActivity(intent)
        }
        recyclerViewMessages.adapter = adapter
    }

    private fun setScroll(){
        var size = if(prevSize.toInt() == 0) 2 else prevSize
        recyclerViewMessages.scrollToPosition(size.toInt())
        prevSize = (adapter.itemCount - 5).toLong()
    }

//    override fun onStart() {
//        super.onStart()
//        recyclerViewMessages.recycledViewPool.clear()
//        adapter.startListening()
//        recyclerViewMessages.recycledViewPool.clear()
//        adapter.notifyDataSetChanged()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

}