package edu.bluejack22_1.fidertime.adapters

import FirestoreAdapter
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.common.TypeEnum
import edu.bluejack22_1.fidertime.databinding.FragmentChatFileItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatFileItemOutBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatImageItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatImageItemOutBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatItemOutBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatVideoItemInBinding
import edu.bluejack22_1.fidertime.databinding.FragmentChatVideoItemOutBinding
import edu.bluejack22_1.fidertime.models.*
import java.text.DecimalFormat
import java.util.Calendar


class ChatListRecyclerViewAdapter(query: Query, private val messageType: String, private val context: Context) : FirestoreAdapter<ChatListRecyclerViewAdapter.ViewHolder>(query) {

    private val userId = Firebase.auth.currentUser!!.uid
    private val CHAT_IN = 1
    private val CHAT_OUT = 2
    private val CHAT_IN_IMAGE = 3
    private val CHAT_OUT_IMAGE = 4
    private val CHAT_IN_VIDEO = 5
    private val CHAT_OUT_VIDEO = 6
    private val CHAT_IN_FILE = 7
    private val CHAT_OUT_FILE = 8

    override fun onDocumentAdded(change: DocumentChange) {
        super.onDocumentAdded(change)
        createNotification(change)
    }

    private fun createNotification(change: DocumentChange) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val chat = change.document.toObject<Chat>()
            val intent = Intent(context.applicationContext, Notification::class.java)
            val title = "You have a new chat"
            val message = chat.chatText
            intent.putExtra(titleExtra, title)
            intent.putExtra(messageExtra, message)

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = Calendar.getInstance().timeInMillis
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New chat channel"
            val description = "Fire when new chat received"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        createNotificationChannel()
        if (viewType == CHAT_OUT) {
            val binding = FragmentChatItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatOutViewHolder(binding, binding.root, messageType)
        }
        if (viewType == CHAT_IN) {
            val binding = FragmentChatItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatInViewHolder(binding, binding.root)
        }
        if (viewType == CHAT_OUT_IMAGE) {
            val binding = FragmentChatImageItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatImageOutViewHolder(binding, binding.root, messageType)
        }
        if (viewType == CHAT_IN_IMAGE) {
            val binding = FragmentChatImageItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatImageInViewHolder(binding, binding.root)
        }
        if (viewType == CHAT_OUT_VIDEO) {
            val binding = FragmentChatVideoItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatVideoOutViewHolder(binding, binding.root, messageType)
        }
        if (viewType == CHAT_IN_VIDEO) {
            val binding = FragmentChatVideoItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatVideoInViewHolder(binding, binding.root)
        }
        if (viewType == CHAT_OUT_FILE) {
            val binding = FragmentChatFileItemOutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatFileOutViewHolder(binding, binding.root, messageType)
        }
        if (viewType == CHAT_IN_FILE) {
            val binding = FragmentChatFileItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ChatFileInViewHolder(binding, binding.root)
        }
        val binding = FragmentChatImageItemInBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ChatImageInViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == CHAT_OUT) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatOutViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_IN) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatInViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_OUT_IMAGE) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatImageOutViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_IN_IMAGE) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatImageInViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_OUT_VIDEO) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatVideoOutViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_IN_VIDEO) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatVideoInViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_OUT_FILE) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatFileOutViewHolder).bind(snapshot) }
        }
        else if (getItemViewType(position) == CHAT_IN_FILE) {
            getSnapshot(position)?.let { snapshot -> (holder as ChatFileInViewHolder).bind(snapshot) }
        }
    }

    private fun getChatType(position: Int): TypeEnum {
        return when(getSnapshot(position)?.get("chatType")) {
            "text" -> TypeEnum.TEXT
            "image" -> TypeEnum.IMAGE
            "video" -> TypeEnum.VIDEO
            "file" -> TypeEnum.FILE
            else -> TypeEnum.TEXT
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getSnapshot(position)?.get("senderUserId") == userId && getChatType(position) == TypeEnum.FILE) {
            return CHAT_OUT_FILE
        }
        if (getSnapshot(position)?.get("senderUserId") == userId && getChatType(position) == TypeEnum.VIDEO) {
            return CHAT_OUT_VIDEO
        }
        if (getSnapshot(position)?.get("senderUserId") == userId && getChatType(position) == TypeEnum.IMAGE) {
            return CHAT_OUT_IMAGE
        }
        if (getSnapshot(position)?.get("senderUserId") == userId && getChatType(position) == TypeEnum.TEXT) {
            return CHAT_OUT
        }
        if (getSnapshot(position)?.get("senderUserId") != userId && getChatType(position) == TypeEnum.FILE) {
            return CHAT_IN_FILE
        }
        if (getSnapshot(position)?.get("senderUserId") != userId && getChatType(position) == TypeEnum.VIDEO) {
            return CHAT_IN_VIDEO
        }
        if (getSnapshot(position)?.get("senderUserId") != userId && getChatType(position) == TypeEnum.IMAGE) {
            return CHAT_IN_IMAGE
        }
        if (getSnapshot(position)?.get("senderUserId") != userId && getChatType(position) == TypeEnum.TEXT) {
            return CHAT_IN
        }
        return CHAT_IN
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(chat: DocumentSnapshot) {}
    }

    class ChatInViewHolder(private val binding: FragmentChatItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            chat.id = snapshot.id
            binding.textViewChat.text = chat.chatText
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.textViewName.text = it.name
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatOutViewHolder(private val binding: FragmentChatItemOutBinding, itemView: View, private val messageType: String) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            binding.textViewChat.text = chat.chatText
            FirebaseQueries.subscribeToMemberLastVisit(chat.messageId, chat.timestamp!!) { totalReadBy ->
                if (totalReadBy == 0) {
                    binding.textViewReadBy.text = ""
                }
                else {
                    if (messageType == "personal") {
                        binding.textViewReadBy.text = "Read"
                    }
                    else {
                        binding.textViewReadBy.text = "Read By $totalReadBy"
                    }
                }
            }
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatImageInViewHolder(private val binding: FragmentChatImageItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            chat.id = snapshot.id
            binding.imageViewChat.load(chat.mediaUrl) {
                crossfade(true)
                crossfade(300)
                placeholder(R.drawable.image_placeholder)
            }
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.textViewName.text = it.name
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatImageOutViewHolder(private val binding: FragmentChatImageItemOutBinding, itemView: View, private val messageType: String) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            binding.imageViewChat.load(chat.mediaUrl) {
                crossfade(true)
                crossfade(300)
                placeholder(R.drawable.image_placeholder)
            }
            FirebaseQueries.subscribeToMemberLastVisit(chat.messageId, chat.timestamp!!) { totalReadBy ->
                if (totalReadBy == 0) {
                    binding.textViewReadBy.text = ""
                }
                else {
                    if (messageType == "personal") {
                        binding.textViewReadBy.text = "Read"
                    }
                    else {
                        binding.textViewReadBy.text = "Read By $totalReadBy"
                    }
                }
            }
            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatVideoInViewHolder(private val binding: FragmentChatVideoItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            chat.id = snapshot.id
            val uri = Uri.parse(chat.mediaUrl)
            Glide.with(itemView.context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        binding.imageViewChat.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.textViewName.text = it.name
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatVideoOutViewHolder(private val binding: FragmentChatVideoItemOutBinding, itemView: View, private val messageType: String) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            val uri = Uri.parse(chat.mediaUrl)
            Glide.with(itemView.context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        binding.imageViewChat.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            FirebaseQueries.subscribeToMemberLastVisit(chat.messageId, chat.timestamp!!) { totalReadBy ->
                if (totalReadBy == 0) {
                    binding.textViewReadBy.text = ""
                }
                else {
                    if (messageType == "personal") {
                        binding.textViewReadBy.text = "Read"
                    }
                    else {
                        binding.textViewReadBy.text = "Read By $totalReadBy"
                    }
                }
            }

            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatFileInViewHolder(private val binding: FragmentChatFileItemInBinding, itemView: View) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!
            chat.id = snapshot.id

            val size : Double? = chat.mediaSize.toDouble()?.div(1024.0)
            val covertSizeToDecimal = DecimalFormat("#.#").format(size)
            val convertToDate = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getDateFormat() }
            val convertToTime = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getTimeFormat() }

            val resultDate = "$convertToDate at $convertToTime"
            binding.name.text = chat.mediaName
            binding.fileDescription.text = covertSizeToDecimal.toString() + " KB, " + resultDate

            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                binding.textViewName.text = it.name
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class ChatFileOutViewHolder(private val binding: FragmentChatFileItemOutBinding, itemView: View, private val messageType: String) : ViewHolder(
        itemView
    ) {
        override fun bind(snapshot: DocumentSnapshot) {
            val chat = snapshot.toObject<Chat>()!!

            FirebaseQueries.subscribeToMemberLastVisit(chat.messageId, chat.timestamp!!) { totalReadBy ->
                if (totalReadBy == 0) {
                    binding.textViewReadBy.text = ""
                }
                else {
                    if (messageType == "personal") {
                        binding.textViewReadBy.text = "Read"
                    }
                    else {
                        binding.textViewReadBy.text = "Read By $totalReadBy"
                    }
                }
            }

            val size : Double? = chat.mediaSize.toDouble()?.div(1024.0)
            val covertSizeToDecimal = DecimalFormat("#.#").format(size)
            val convertToDate = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getDateFormat() }
            val convertToTime = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getTimeFormat() }

            val resultDate = "$convertToDate at $convertToTime"
            binding.name.text = chat.mediaName
            binding.fileDescription.text = covertSizeToDecimal.toString() + " KB, " + resultDate

            binding.textViewTimestamp.text = chat.timestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getHourMinuteFormat() }
            FirebaseQueries.subscribeToUser(chat.senderUserId) {
                if(it.profileImageUrl != ""){
                    binding.imageViewProfile.load(it.profileImageUrl)
                }else{
                    binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
                }
            }
        }
    }
}