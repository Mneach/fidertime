package edu.bluejack22_1.fidertime.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.compose.ui.text.toLowerCase
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.BotChatListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.NotificationHelper
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.databinding.ActivityBotMessageBinding
import edu.bluejack22_1.fidertime.models.Chat
import edu.bluejack22_1.fidertime.models.Reminder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val CHATS_KEY = "CHAT"
const val REMINDERS_KEY = "REMINDER"
const val BOT_ID = "bot"

class BotMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBotMessageBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var messageId: String
    private lateinit var chats: ArrayList<Chat>
    private lateinit var reminders: ArrayList<Reminder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BotChatListRecyclerViewAdapter
    private lateinit var currentEditReminder: Reminder
    private var state = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBotMessageBinding.inflate(layoutInflater)
        preferences = getPreferences(Context.MODE_PRIVATE)
        userId = Firebase.auth.currentUser!!.uid
        messageId = "bot"
        chats = getArrayList(CHATS_KEY)
        reminders = getArrayList(REMINDERS_KEY)
        recyclerView = binding.recyclerViewChats
        adapter = BotChatListRecyclerViewAdapter(chats)
        setContentView(binding.root)
        NotificationHelper.createNotificationChannel(this)

        setActionBar()
        setChatBox()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        recyclerView = binding.recyclerViewChats
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        adapter = BotChatListRecyclerViewAdapter(chats)
        recyclerView.adapter = adapter
        scrollToBottom()
    }

    private fun setChatBox() {
        val sendButton = binding.buttonSend
        val editTextChat = binding.editTextChat

        sendButton.setOnClickListener {
            if (editTextChat.text.isNotBlank()) {
                val chat = Chat(
                    "",
                    editTextChat.text.toString(),
                    "text",
                    messageId,
                    arrayListOf(),
                    userId,
                    Timestamp.now()
                )
                chats.add(chat)
                adapter.notifyItemInserted(chats.size - 1)
                botReply(editTextChat.text.toString())
                putArrayList(CHATS_KEY, chats)
                scrollToBottom()
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

        chats.add(chat)
        adapter.notifyItemInserted(chats.size - 1)

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

    private fun <E> clearArrayList(key: String) {
        putArrayList<E>(key, arrayListOf())
    }

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

    private fun setActionBar() {
        binding.toolbarMessage.textViewTitle.text = "Bot"
        binding.toolbarMessage.textViewSubtitle.text = "Online"
        binding.toolbarMessage.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
        binding.toolbarMessage.imageButtonPinned.visibility = View.GONE
        setSupportActionBar(binding.toolbarMessage.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun scrollToBottom() {
        if (chats.isNotEmpty()) {
            recyclerView.postDelayed({
                recyclerView.smoothScrollToPosition(chats.size - 1)
            }, 500)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}