package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.adapters.ChatListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityMessageBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageId: String
    private val userId = "Km69GgIsRZhgKUsb0aIq0YSZWVX2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageId = intent.getStringExtra("messageId").toString()

        initializeActionBar()
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        val recyclerView = binding.recyclerViewChats
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        FirebaseQueries.subscribeToChats(messageId, null) {
            val adapter = ChatListRecyclerViewAdapter(it)
            recyclerView.adapter = adapter
        }
    }

    private fun initializeActionBar() {
        FirebaseQueries.subscribeToMessage(messageId) {
            setNameAndProfile(it)
        }
    }

    private fun setNameAndProfile(message: Message) {
        if (message.messageType == "group") {
            binding.toolbarMessage.textViewTitle.text = message.groupName
            binding.toolbarMessage.textViewSubtitle.text = "${message.members.size} members"
            binding.toolbarMessage.imageViewProfile.load(message.groupImageUrl)
            setActionBar()
        }
        else {
            val withUserId = message.members.find { memberId -> memberId != userId }
            FirebaseQueries.subscribeToUser(withUserId!!) {
                setNameAndProfile(it)
            }

            binding.toolbarMessage.actionBarProfile.setOnClickListener{
                val intent = Intent(this , MessagePersonalDetailActivity::class.java)
                intent.putExtra("userId" , withUserId)
                startActivity(intent)
            }
        }
    }

    private fun setNameAndProfile(user: User) {
        binding.toolbarMessage.textViewTitle.text = user.name
        binding.toolbarMessage.textViewSubtitle.text = user.status
        binding.toolbarMessage.imageViewProfile.load(user.profileImageUrl)
        setActionBar()
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarMessage.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}