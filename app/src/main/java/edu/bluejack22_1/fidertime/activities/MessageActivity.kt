package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.ChatListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.ActivityMessageBinding
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageId: String
    private lateinit var adapter: ChatListRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var type: String
    private var pinned = false
    private val userId = Firebase.auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageId = intent.getStringExtra("messageId").toString()

        initializeActionBar()
        initializeAttachmentBox()
        initializeChatBox()
        initializeRecyclerView()
    }

    private fun initializeAttachmentBox() {
        val attachmentLayout = binding.layoutAttachment
        attachmentLayout.visibility = View.GONE

        val attachmentButton = binding.buttonAttachment
        attachmentButton.setOnClickListener {
            toggleAttachmentLayout(attachmentLayout)
        }

        val imageButton = binding.buttonImage
        imageButton.setOnClickListener {
            attachmentLayout.visibility = View.GONE
            selectMedia("image")
        }

        val videoButton = binding.buttonVideo
        videoButton.setOnClickListener {
            attachmentLayout.visibility = View.GONE
            selectMedia("video")
        }
    }

    private fun selectMedia(type: String) {
        this.type = type
        val intent = Intent()
        intent.type = "$type/*"
        intent.action = Intent.ACTION_GET_CONTENT

        chooseMediaFromGallery.launch(intent)
    }

    private var chooseMediaFromGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK && result.data != null){
            val filePath = result.data!!.data!!
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle(getString(R.string.please_wait))
            progressDialog.show()

            FirebaseQueries.uploadMedia(filePath, type, this) { imageUrl ->
                val chat = Chat("", "",
                    type, messageId, arrayListOf(), userId, Timestamp.now(), imageUrl)
                FirebaseQueries.sendChatMedia(chat) {
                    scrollToBottom()
                }
            }
        }
    }

    private fun toggleAttachmentLayout(attachmentLayout: ConstraintLayout) {
        if (attachmentLayout.visibility == View.GONE) {
            attachmentLayout.visibility = View.VISIBLE
        } else {
            attachmentLayout.visibility = View.GONE
        }
    }

    private fun initializeChatBox() {
        val sendButton = binding.buttonSend
        val editTextChat = binding.editTextChat

        sendButton.setOnClickListener {
            if (editTextChat.text.isNotEmpty()) {
                val chat = Chat(
                    "",
                    editTextChat.text.toString(),
                    "text",
                    messageId,
                    arrayListOf(),
                    userId,
                    Timestamp.now()
                )
                editTextChat.text.clear()
                FirebaseQueries.sendChatText(chat) {
                    scrollToBottom()
                }
            }
        }
    }

    private fun scrollToBottom() {
        recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
    }

    private fun initializeRecyclerView() {
        recyclerView = binding.recyclerViewChats
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        val query = Firebase.firestore.collection("chats").whereEqualTo("messageId", messageId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
        adapter = ChatListRecyclerViewAdapter(query, "personal")
        recyclerView.adapter = adapter
    }

    private fun initializeActionBar() {
        FirebaseQueries.subscribeToMessage(messageId) {
            setNameAndProfile(it)
        }
        FirebaseQueries.getMessageIsPinned(userId, messageId) { pinned ->
            setButtonPinned(pinned)
        }
        binding.toolbarMessage.imageButtonPinned.setOnClickListener {
            FirebaseQueries.togglePinnedMessage(userId, messageId, pinned) {
                setButtonPinned(!pinned)
            }
        }
    }

    private fun setButtonPinned(pinned: Boolean) {
        if (pinned) {
            binding.toolbarMessage.imageButtonPinned.setImageResource(R.drawable.ic_baseline_push_pin_24)
        }
        else {
            binding.toolbarMessage.imageButtonPinned.setImageResource(R.drawable.ic_outline_push_pin_24)
        }
        this.pinned = pinned
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
                intent.putExtra("messageId" , messageId);
                startActivity(intent)
            }
        }
    }

    private fun setNameAndProfile(user: User) {
        binding.toolbarMessage.textViewTitle.text = user.name
        binding.toolbarMessage.textViewSubtitle.text = user.status
        if(user.profileImageUrl != ""){
            binding.toolbarMessage.imageViewProfile.load(user.profileImageUrl)
        }else{
            binding.toolbarMessage.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
        }
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

    override fun onStart() {
        super.onStart()
        recyclerView.recycledViewPool.clear()
        adapter.startListening()
        recyclerView.recycledViewPool.clear()
        adapter.notifyDataSetChanged()
        FirebaseQueries.updateMemberLastVisit(messageId, userId)
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onPause() {
        super.onPause()
        FirebaseQueries.updateMemberLastVisit(messageId, userId)
    }

}