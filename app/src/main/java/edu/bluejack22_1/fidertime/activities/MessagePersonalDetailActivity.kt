package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import coil.load
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.MessagePersonalMediaPagerAdapter
import edu.bluejack22_1.fidertime.adapters.ProfileMediaListPagerAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import edu.bluejack22_1.fidertime.common.RichTextHelper
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityMessagePersonalDetailBinding
import edu.bluejack22_1.fidertime.databinding.FragmentProfileBinding
import edu.bluejack22_1.fidertime.models.User

class MessagePersonalDetailActivity : AppCompatActivity() {

    lateinit var binding : ActivityMessagePersonalDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagePersonalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userData = intent.getStringExtra("userId")
        val messageId = intent.getStringExtra("messageId")
        val username = intent.getStringExtra("username")

        if (username != null) {
            FirebaseQueries.subscribeToUserByUsername(username) {
                setUserData(it)
            }
        }
        else if (userData != null && messageId != null) {
            initializeTabs(messageId)
            FirebaseQueries.subscribeToUser(userData) {
                setUserData(it)
            }
            setSwitchNotification(messageId)
        }

    }

    private fun initializeTabs(messageId : String) {
        val messageListPagerAdapter = MessagePersonalMediaPagerAdapter(supportFragmentManager, lifecycle , messageId)
        binding.pagerMessageList.adapter = messageListPagerAdapter

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Media"
                }
                1 -> {
                    tab.text = "Files"
                }
                2 -> {
                    tab.text = "Links"
                }
            }
        }.attach()
    }

    private fun setUserData(user : User){
        binding.name.text = user.name
        binding.phoneNumber.text = user.phoneNumber


        RichTextHelper.linkAndMentionRecognizer(this, binding.bio, user.bio)
        if(user.profileImageUrl != "" && user.profileImageUrl.isNotEmpty()){
            binding.imageViewProfile.load(user.profileImageUrl)
        }else{
            binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
        }
        binding.status.text = if (user.status == "offline") {
            getString(R.string.last_seen).plus(" ") + user.lastSeenTimestamp?.toDate()
                ?.let { RelativeDateAdapter(it).getRelativeString() }
        } else {
            user.status
        }
        setActionBar()
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarPersonalDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setSwitchNotification(messageId : String){
        val userId = Utilities.getAuthFirebase().uid.toString()
        FirebaseQueries.getMessageNotificationStatus(userId , messageId){ notificationStatus ->
            val switchNotification = binding.switchNotification
            switchNotification.isChecked = notificationStatus.toBoolean()
            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                FirebaseQueries.updateMessageNotificationStatus(userId, messageId , isChecked) {
                    var message = "";
                    if(isChecked) message = getString(R.string.On)
                    else message = getString(R.string.Off)
                    Toast.makeText(this , getString(R.string.notification).plus(" ").plus(message) , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}