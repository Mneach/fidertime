package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.devlomi.record_view.OnRecordListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.ChatListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.MemberRecycleViewAdapter
import edu.bluejack22_1.fidertime.adapters.TestAdapter
import edu.bluejack22_1.fidertime.common.*
import edu.bluejack22_1.fidertime.databinding.ActivityMessageBinding
import edu.bluejack22_1.fidertime.models.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageId: String
//    private lateinit var adapter: ChatListRecyclerViewAdapter
    private lateinit var adapter : TestAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var type: String
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var recordingPath: String
    private var pinned = false
    private val userId = Firebase.auth.currentUser!!.uid
    private lateinit var chatData : ArrayList<Chat>
    var isLoading = false
    var LIMIT : Long = 12
    var prevSize : Long = 0;

    var messageContainsBot = false

    private lateinit var currentEditReminder: Reminder
    private var state = "default"
    private lateinit var reminders: ArrayList<Reminder>
    private lateinit var preferences: SharedPreferences

    private fun <E> putArrayList(key: String, list: ArrayList<E>) {
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    private inline fun <reified E> getArrayList(key: String): ArrayList<E> {
        val gson = Gson()
        val json = preferences.getString(key, null) ?: return arrayListOf()
        val type = TypeToken.getParameterized(ArrayList::class.java, E::class.java).type
        return gson.fromJson(json, type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getPreferences(Context.MODE_PRIVATE)
        reminders = getArrayList(REMINDERS_KEY)
        messageId = intent.getStringExtra("messageId").toString()
        chatData = arrayListOf()
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
                FirebaseQueries.sendChatText(chat) {
                    scrollToBottom()
                    FirebaseQueries.updateMemberLastVisit(messageId, userId)
                }
                if (messageContainsBot && editTextChat.text.toString().startsWith("/")) {
                    botReply(editTextChat.text.toString())
                }
                editTextChat.text.clear()
            }
        }
    }

    private fun botReply(text: String) {

        val reply = if (state == "default") {
            getReply(text)
        } else {
            getEditReply(text)
        }
        val chat = Chat(
            "",
            reply,
            "text",
            messageId,
            arrayListOf(),
            "bot",
            Timestamp.now()
        )
        FirebaseQueries.sendChatText(chat) {
            scrollToBottom()
        }
//        chats.add(chat)
    }

    private fun getEditReply(text: String): String {

        if (state == "edit_choose") {

            if (!text.isDigitsOnly()) {
                return "Please only send me number from 1 to ${reminders.size} or 0 to cancel"
            }

            val index = text.toInt() - 1

            if (index < 0 || index > reminders.size - 1) {
                if (index ==  0 - 1) {
                    state = "default"
                    return "Edit reminder canceled"
                }
                return "Please only send me number from 1 to ${reminders.size} or 0 to cancel"
            }

            currentEditReminder = reminders[index]
            reminders.removeAt(index)

            state = "edit_title"

            return "Send me the new title for this reminder"
        }

        if (state == "edit_title") {

            if (text.isBlank()) {
                return "The title can not be blank"
            }

            currentEditReminder.title = text
            state = "edit_date_time"

            return "Send me the new date and time for this reminder"
        }

        if (state == "edit_date_time") {

            val date: Date
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)

            try {
                date = simpleDateFormat.parse(text) as Date
            } catch (e: Exception) {
                return "Your date and time is not correctly formatted\n\n" + "Try this following format:\n" + "yyyy-MM-dd HH:mm"
            }

            currentEditReminder.date = date
            state = "edit_remind_before"

            return "Send me the new remind before for this reminder"
        }

        if (state == "edit_remind_before") {
            val splitted = text.split(", ")
            val remindBefore = arrayListOf<Date>()

            for (i in splitted.indices) {
                if (splitted[i].length < 3 || (splitted[i][0] != 'D' && splitted[i][0] != 'H' && splitted[i][0] != 'M') || splitted[i][1] != '-' || !splitted[i].substring(
                        splitted[i].indexOf("-") + 1
                    ).isDigitsOnly()
                ) {
                    return "One of your remind before is not correctly formatted\n\n" + "Try this following format:\n" + "D-1: 1 day before\n" + "H-2: 2 hours before\n" + "M-15: 15 Minutes before"
                }

                val value = splitted[i].substring(
                    splitted[i].indexOf("-") + 1
                ).toInt()
                val calendar = Calendar.getInstance()
                calendar.time = currentEditReminder.date
                if (splitted[i].startsWith("D")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -value)
                }
                if (splitted[i].startsWith("H")) {
                    calendar.add(Calendar.HOUR, -value)
                }
                if (splitted[i].startsWith("M")) {
                    calendar.add(Calendar.MINUTE, -value)
                }

                remindBefore.add(calendar.time)
            }

            currentEditReminder.remindBefore = remindBefore
            state = "default"

            reminders.add(currentEditReminder)
            putArrayList(REMINDERS_KEY, reminders)
            NotificationHelper.createNotification(this, currentEditReminder.title, "Deadline now", currentEditReminder.date.time)
            currentEditReminder.remindBefore.forEach {
                NotificationHelper.createNotification(this, currentEditReminder.title, "Deadline ${RelativeDateAdapter(currentEditReminder.date).getFullRelativeString(it)}", it.time )
            }

            return "Successfully edit reminder"
        }


        return ""
    }

    private fun getReply(text: String): String {

        if (text.startsWith("/Reminder")) {
            val splitted = text.substring(text.indexOf(" ") + 1).split(", ")

            if (splitted.size < 3) {
                return "I'm sorry, I don't understand, your command is not correctly formatted\n\n" + "Try this following format:\n" + "/Reminder [title], [date] [time], [remindBefore ...]"
            }

            val title = splitted[0]
            val dateText = splitted[1]
            val remindBefore = arrayListOf<Date>()

            if (title.isBlank()) {
                return "Your title can not be blank"
            }

            val date: Date
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)

            try {
                date = simpleDateFormat.parse(dateText) as Date
            } catch (e: Exception) {
                return "Your date and time is not correctly formatted\n\n" + "Try this following format:\n" + "yyyy-MM-dd HH:mm"
            }

            for (i in 2 until splitted.size) {
                if (splitted[i].length < 3 || (splitted[i][0] != 'D' && splitted[i][0] != 'H' && splitted[i][0] != 'M') || splitted[i][1] != '-' || !splitted[i].substring(
                        splitted[i].indexOf("-") + 1
                    ).isDigitsOnly()
                ) {
                    return "One of your remind before is not correctly formatted\n\n" + "Try this following format:\n" + "D-1: 1 day before\n" + "H-2: 2 hours before\n" + "M-15: 15 Minutes before"
                }

                val value = splitted[i].substring(
                    splitted[i].indexOf("-") + 1
                ).toInt()
                val calendar = Calendar.getInstance()
                calendar.time = date
                if (splitted[i].startsWith("D")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -value)
                }
                if (splitted[i].startsWith("H")) {
                    calendar.add(Calendar.HOUR, -value)
                }
                if (splitted[i].startsWith("M")) {
                    calendar.add(Calendar.MINUTE, -value)
                }

                remindBefore.add(calendar.time)
            }

            val reminder = Reminder(title, date, remindBefore)

            reminders.add(reminder)
            putArrayList(REMINDERS_KEY, reminders)
            NotificationHelper.createNotification(this, reminder.title, "Deadline now", reminder.date.time)
            reminder.remindBefore.forEach {
                NotificationHelper.createNotification(this, reminder.title, "Deadline ${RelativeDateAdapter(reminder.date).getFullRelativeString(it)}", it.time )
            }

            return "Successfully added new reminder\n\n/ViewReminder to view reminders"
        } else if (text.startsWith("/ViewReminder")) {

            if (reminders.isEmpty()) {
                return "Alhamdulillah you don't have any reminder"
            }

            val splitted = text.split(" ")
            if (splitted.size == 2) {
                when (splitted[1]) {
                    "title" -> reminders.sortBy { reminder -> reminder.title.lowercase(Locale.ENGLISH) }
                    "date" -> reminders.sortBy { reminder -> reminder.date }
                }
            }

            var view = "You have ${reminders.size} reminders:"

            for (i in 0 until reminders.size) {
                view += "\n\n" + "${i + 1}. ${reminders[i]}"
            }

            return view
        } else if (text.startsWith("/EditReminder")) {

            state = "edit_choose"

            var view = "Choose reminder 1 to ${reminders.size} (0 to cancel):"

            for (i in 0 until reminders.size) {
                view += "\n\n" + "${i + 1}. ${reminders[i]}"
            }

            return view
        }

        return "Command unrecognized, please send me only with one of these command (/Reminder [task], /ViewReminder, /EditReminder)"
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
                if(dy < 0){
                   if((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 1 && !isLoading){
                        loadMoreData(query)
                    }
                }
            }
        })
//        FirebaseQueries.getMessageNotificationStatus(Utilities.getAuthFirebase().uid.toString(), messageId) { notificationStatus ->
//            adapter = ChatListRecyclerViewAdapter(query.limitToLast(12), "personal", this , notificationStatus.toBoolean())
//            recyclerView.adapter = adapter
//            adapter.startListening()
//            adapter.notifyDataSetChanged()
//        }
        FirebaseQueries.getMessageNotificationStatus(Utilities.getAuthFirebase().uid.toString(), messageId) { notificationStatus ->
            adapter = TestAdapter("personal",chatData, this , notificationStatus.toBoolean())
            recyclerView.adapter = adapter
            query.limitToLast(LIMIT).addSnapshotListener { snapshot , e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val chats = ArrayList<Chat>()
                    for(doc in snapshot){
                        val chat = doc.toObject<Chat>()
                        chat.id = doc.id
                        chats.add(chat)
                    }

                    if(adapter.getData().isEmpty()){
                        chatData.addAll(chats)
                        adapter = TestAdapter("personal",chatData, this , notificationStatus.toBoolean())
                        recyclerView.adapter = adapter
//                        adapter.updateData(userData)
                    }else{
                        chatData.clear()
                        chatData.addAll(chats)
                        adapter = TestAdapter("personal",chatData, this , notificationStatus.toBoolean())
                        recyclerView.adapter = adapter
                    }
                }
            }
            }
        }

    private fun loadMoreData(query : Query){
        isLoading = true
        LIMIT += LIMIT;
        query.limitToLast(LIMIT).addSnapshotListener { snapshot , e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val chats = ArrayList<Chat>()
                for (doc in snapshot) {
                    val chat = doc.toObject<Chat>()
                    chat.id = doc.id
                    chats.add(chat)
                }
                if(chats != adapter.getData()){
                    Handler(Looper.getMainLooper()).postDelayed({
                        isLoading = false
                        chatData.clear()
                        chatData.addAll(chats)
                        adapter = TestAdapter("personal",chatData, this , false)
                        recyclerView.adapter = adapter
                        setScroll()
                    }, 500)
                }else{
                    Handler(Looper.getMainLooper()).postDelayed({
                        isLoading = false
                    }, 500)
                }
            }

        }
    }

    private fun setScroll(){
        var size = 0
        if(prevSize.toInt() == 0){
            size = 12
//            prevSize = size
        }else {
            size = 10
        }

        recyclerView.scrollToPosition(size.toInt())
        prevSize += LIMIT
    }

    private fun initializeActionBar() {
        FirebaseQueries.subscribeToMessage(messageId) {
            setNameAndProfile(it)
            if (it.messageType.equals("group") && it.members.contains("bot")) {
                messageContainsBot = true
            }
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
            binding.toolbarMessage.textViewSubtitle.text = "${message.members.size}".plus(" ").plus(getString(R.string.members))
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
        val last_seen = getString(R.string.last_seen)
        binding.toolbarMessage.textViewTitle.text = user.name
        binding.toolbarMessage.textViewSubtitle.text = if (user.status == "offline") {
            "$last_seen " + user.lastSeenTimestamp?.toDate()
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
//        adapter.stopListening()
    }

    override fun onPause() {
        super.onPause()
        FirebaseQueries.updateMemberLastVisit(messageId, userId)
    }

}