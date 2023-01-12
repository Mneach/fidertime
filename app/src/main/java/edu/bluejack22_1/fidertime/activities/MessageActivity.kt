package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.devlomi.record_view.OnRecordListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.ChatListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.*
import edu.bluejack22_1.fidertime.databinding.ActivityMessageBinding
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Media
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import java.io.File
import java.io.IOException


class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageId: String
    private lateinit var adapter: ChatListRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var type: String
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var recordingPath: String
    private var pinned = false
    private val userId = Firebase.auth.currentUser!!.uid
    var isLoading = false
    val LIMIT : Long = 5
    var prevSize : Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageId = intent.getStringExtra("messageId").toString()

        initializeActionBar()
        initializeAttachmentBox()
        initializeChatBox()
//        initializeRecyclerView()
        initializeRecorderView()
        initializeEditTextChat()
    }

    private fun initializeVoiceRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        MediaStore.Audio.Media.DISPLAY_NAME

        val file = File(getExternalFilesDir("")!!.absolutePath, "")

        if (!file.exists()) {
            file.mkdirs()
        }

        recordingPath = file.absolutePath + File.separator + System.currentTimeMillis() + ".3gp"

        mediaRecorder.setOutputFile(recordingPath)
    }

    private fun initializeEditTextChat() {
        binding.editTextChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isEmpty()) {
                    binding.parentRecord.visibility = View.VISIBLE
                }
                else {
                    binding.parentRecord.visibility = View.GONE
                }
            }
        })
    }

    private fun initializeRecorderView() {
        val recordView = binding.recordView
        val recordButton = binding.recordButton

        recordButton.setRecordView(recordView)
        recordButton.isListenForRecord = false

        recordButton.setOnClickListener {
            if (Permissions.isRecordingGranted(this)) {
                recordButton.isListenForRecord = true
            }
            else {
                Permissions.requestRecordingPermission(this)
            }
        }

        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                hideEditTextBox()
                initializeVoiceRecorder()

                mediaRecorder.prepare()
                mediaRecorder.start()
            }

            override fun onCancel() {
                mediaRecorder.reset()
                mediaRecorder.release()
                val file = File(recordingPath)
                if (file.exists()) {
                    file.delete()
                }
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                showEditTextBox()
                mediaRecorder.stop()
                mediaRecorder.reset()
                mediaRecorder.release()

                val recordingUri = Uri.fromFile(File(recordingPath))
                FirebaseQueries.uploadMedia(recordingUri, "voice", this@MessageActivity) {
                    val chat = Chat("", "", "voice", messageId, arrayListOf(), userId, Timestamp.now(), it, System.currentTimeMillis().toString(), 1024)
                    FirebaseQueries.sendChatMedia(chat, System.currentTimeMillis().toString(), 1024) {
                        scrollToBottom()
                        FirebaseQueries.updateMemberLastVisit(messageId, userId)
                    }
                }

            }

            override fun onLessThanSecond() {
                showEditTextBox()
                mediaRecorder.reset()
                mediaRecorder.release()
                val file = File(recordingPath)
                if (file.exists()) {
                    file.delete()
                }
            }
        })

        recordView.setOnBasketAnimationEndListener {
            showEditTextBox()
        }
    }

    private fun hideEditTextBox() {
        binding.editTextChat.visibility = View.GONE
        binding.buttonAttachment.visibility = View.GONE
        binding.buttonSend.visibility = View.GONE
    }

    private fun showEditTextBox() {
        binding.editTextChat.visibility = View.VISIBLE
        binding.buttonAttachment.visibility = View.VISIBLE
        binding.buttonSend.visibility = View.VISIBLE
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

        val fileButton = binding.buttonFile
        fileButton.setOnClickListener {
            attachmentLayout.visibility = View.GONE
            selectMedia("*")
        }
    }


    private fun selectMedia(type: String) {
        this.type = if (type == "*") {
            "file"
        } else {
            type
        }
        val intent = Intent()
        intent.type = "$type/*"
        intent.action = Intent.ACTION_GET_CONTENT

        chooseMediaFromGallery.launch(intent)
    }

    private var chooseMediaFromGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK && result.data != null){
            result.data?.data?.let { returnUri ->
                contentResolver.query(returnUri, null, null, null, null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                val fileSize = cursor.getLong(sizeIndex)

                val filePath = result.data!!.data!!

                FirebaseQueries.uploadMedia(filePath, type, this) { imageUrl ->
                    val chat = Chat("", "",
                        type, messageId, arrayListOf(), userId, Timestamp.now(), imageUrl, fileName, fileSize)
                    FirebaseQueries.sendChatMedia(chat, fileName, fileSize) {
                        scrollToBottom()
                        FirebaseQueries.updateMemberLastVisit(messageId, userId)
                    }
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

        editTextChat.setOnClickListener {
            scrollToBottom()
        }

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
                sendLink(editTextChat.text.toString())
                editTextChat.text.clear()
                FirebaseQueries.sendChatText(chat) {
                    scrollToBottom()
                    FirebaseQueries.updateMemberLastVisit(messageId, userId)
                }
            }
        }
    }

    private fun sendLink(text: String){
        val linkMatcher = Patterns.WEB_URL.matcher(text)
        var matchStart: Int
        var matchEnd: Int
        var links : ArrayList<Media> = arrayListOf()
        while(linkMatcher.find()){
            matchStart = linkMatcher.start(1)
            matchEnd = linkMatcher.end()

            var url = text.substring(matchStart, matchEnd)
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            var media = Media()
            media.messageId = messageId
            media.senderUserId = Utilities.getAuthFirebase().uid.toString()
            media.type = "link"
            media.timestamp = Timestamp.now()
            media.name = url
            media.url = url

            links.add(media)
        }

        if(links.isNotEmpty()){
            FirebaseQueries.addLinks(links)
        }
    }

    private fun scrollToBottom() {
        if(adapter.itemCount != 0){
            recyclerView.postDelayed({
                recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            }, 500)
        }
    }

    private fun initializeRecyclerView() {
        NotificationHelper.createNotificationChannel(this)
        recyclerView = binding.recyclerViewChats
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        val query = Firebase.firestore.collection("chats").whereEqualTo("messageId", messageId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d("dy = " , dy.toString())
                if(dy < 0){
                    Log.d("current child" , (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition().toString())
                    if((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0){
                        Log.d("udah sampe" , "udah sampe beneran")
                        // LOAD MORE
                    }
                }
            }
        })
        FirebaseQueries.getMessageNotificationStatus(Utilities.getAuthFirebase().uid.toString(), messageId) { notificationStatus ->
            adapter = ChatListRecyclerViewAdapter(query.limitToLast(12), "personal", this , notificationStatus.toBoolean())
            recyclerView.adapter = adapter
            adapter.startListening()
            adapter.notifyDataSetChanged()
        }
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
            binding.toolbarMessage.actionBarProfile.setOnClickListener{
                val intent = Intent(this , MessageGroupDetailActivity::class.java)
                intent.putExtra("messageId" , messageId);
                startActivity(intent)
            }
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
        binding.toolbarMessage.textViewSubtitle.text = if (user.status == "offline") {
            "Last seen " + user.lastSeenTimestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getRelativeString() }
        } else {
            user.status
        }
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
        initializeRecyclerView()
        FirebaseQueries.updateMemberLastVisit(messageId, userId)
        if (!Permissions.isStorageGranted(this)) {
            Permissions.requestStoragePermission(this)
        }
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