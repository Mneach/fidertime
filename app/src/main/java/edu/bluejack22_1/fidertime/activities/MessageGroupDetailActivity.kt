package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import coil.load
import com.google.android.material.tabs.TabLayoutMediator
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.adapters.MessageGroupMediaPagerAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.RichTextHelper
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityMessageGroupDetailBinding
import edu.bluejack22_1.fidertime.fragments.ChooseAdminFragment
import edu.bluejack22_1.fidertime.models.Message

class MessageGroupDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMessageGroupDetailBinding
    private lateinit var messageId : String
    private lateinit var memberGroupIds : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageId = intent.getStringExtra("messageId").toString()

        FirebaseQueries.subscribeToMessage(messageId){ message ->
            FirebaseQueries.subscribeToOnlineMember(message.members){ onlineMember ->
                setGroupData(message , onlineMember.size)
                initializeTabs(messageId, message.members)
                memberGroupIds = message.members
            }
        }
        setSwitchNotification(messageId)
    }

    private fun initializeTabs(messageId : String, groupMemberIds : ArrayList<String>) {
        val messageListPagerAdapter = MessageGroupMediaPagerAdapter(supportFragmentManager, lifecycle , messageId, groupMemberIds)
        binding.pagerMessageList.adapter = messageListPagerAdapter

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.members)
                }
                1 -> {
                    tab.text = getString(R.string.media)
                }
                2 -> {
                    tab.text = getString(R.string.file)
                }
                3 -> {
                    tab.text = getString(R.string.link)
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
        binding.countMember.text = message.members.size.toString().plus(" ").plus(getString(R.string.members)).plus(" , ")
        binding.countOnline.text = onlineMember.toString().plus(" ").plus(getString(R.string.online))
        binding.groupName.text = message.groupName
        RichTextHelper.linkAndMentionRecognizer(this, binding.groupDescription, message.groupDescription)
        setActionBar()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.message_group_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.leave_group) {
            val userId = Utilities.getAuthFirebase().uid.toString()
            FirebaseQueries.getUserRole(messageId , userId){ messageMember ->
                if(messageMember.admin && memberGroupIds.size > 1){
                    // LEAVE GROUP AND ASSIGN ADMIN ROLE
                    val bottomSheetDialog = ChooseAdminFragment(messageId,memberGroupIds)
                    bottomSheetDialog.show(supportFragmentManager, bottomSheetDialog.tag)
                }else if(messageMember.admin){
                    // REMOVE GROUP
                    FirebaseQueries.deleteGroupMessage(messageId , userId){
                        Toast.makeText(this , R.string.success_leave_group , Toast.LENGTH_SHORT).show()
                        val intent = Intent(this , MainActivity::class.java)
                        intent.putExtra("FragmentOption" , "message")
                        startActivity(intent)
                    }
                }else{
                    // LEAVE GROUP MEMBER
                    FirebaseQueries.deleteMember(messageId , userId , memberGroupIds){
                        Toast.makeText(this , R.string.success_leave_group , Toast.LENGTH_SHORT).show()
                        val intent = Intent(this , MainActivity::class.java)
                        intent.putExtra("FragmentOption" , "message")
                        startActivity(intent)
                    }
                }
            }
            return true
        }

        return super.onOptionsItemSelected(item)

    }
}