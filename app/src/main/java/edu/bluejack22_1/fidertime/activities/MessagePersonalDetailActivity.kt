package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import coil.load
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.MessagePersonalMediaPagerAdapter
import edu.bluejack22_1.fidertime.adapters.ProfileMediaListPagerAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
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
        val messageId = intent.getStringExtra("messageId").toString()


        initializeTabs(messageId)
        FirebaseQueries.subscribeToUser(userData!!) {
            setUserData(it)
        }
    }

    private fun initializeTabs(messageId : String) {
        val messageListPagerAdapter = MessagePersonalMediaPagerAdapter(supportFragmentManager, lifecycle , messageId)
        binding.pagerMessageList.adapter = messageListPagerAdapter
        Log.d("tabs", "masuk")

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

    private fun setUserData(it : User){
        binding.name.text = it.name
        binding.phoneNumber.text = it.phoneNumber
        binding.bio.text = it.bio
        if(it.profileImageUrl != "" && it.profileImageUrl.isNotEmpty()){
            binding.imageViewProfile.load(it.profileImageUrl)
        }else{
            binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
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
}