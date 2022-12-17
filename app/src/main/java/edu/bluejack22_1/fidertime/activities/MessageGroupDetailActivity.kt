package edu.bluejack22_1.fidertime.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.MessageGroupMediaPagerAdapter
import edu.bluejack22_1.fidertime.adapters.MessagePersonalMediaPagerAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.databinding.ActivityMessageGroupDetailBinding
import edu.bluejack22_1.fidertime.models.Message

class MessageGroupDetailActivity : AppCompatActivity() {

    lateinit var binding : ActivityMessageGroupDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val messageId = intent.getStringExtra("messageId").toString()

        FirebaseQueries.subscribeToMessage(messageId){ message ->
            FirebaseQueries.subscribeToOnlineMember(message.members){ onlineMember ->
                setGroupData(message , onlineMember.size)
                initializeTabs(messageId, message.members)
            }
        }
    }

    private fun initializeTabs(messageId : String, groupMemberIds : ArrayList<String>) {
        val messageListPagerAdapter = MessageGroupMediaPagerAdapter(supportFragmentManager, lifecycle , messageId, groupMemberIds)
        binding.pagerMessageList.adapter = messageListPagerAdapter

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Members"
                }
                1 -> {
                    tab.text = "Media"
                }
                2 -> {
                    tab.text = "Files"
                }
                3 -> {
                    tab.text = "Links"
                }
            }
        }.attach()
    }

    private fun setGroupData(message : Message , onlineMember : Int){
        binding.imageViewGroup.load(message.groupImageUrl) {
            crossfade(true)
            crossfade(300)
            placeholder(R.drawable.image_placeholder)
        }
        binding.countMember.text = message.members.size.toString().plus(" Members, ")
        binding.countOnline.text = onlineMember.toString().plus(" Online")
        binding.groupName.text = message.groupName
        binding.groupDescription.text = message.groupDescription
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