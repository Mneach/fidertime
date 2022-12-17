package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityCreateGroupBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User
import kotlinx.coroutines.DEBUG_PROPERTY_NAME

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupBinding
    private var participants : ArrayList<User> = arrayListOf()
    private lateinit var filePath : Uri
    private var checkChangeImageUrl = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initArrayOfParticipant()
        setBackButton()
        setCreateGroupButton()
        handleGroupImage()
    }

    private fun setBackButton(){
        binding.toolbarCreateGroup.backButton.setOnClickListener {
            val intent = Intent(this , AddParticipantActivity::class.java)
            intent.putExtra("ParticipantData" , participants)
            startActivity(intent)
        }
    }

    private fun setCreateGroupButton(){
        binding.toolbarCreateGroup.createButton.setOnClickListener {
            val groupName = binding.groupNameInput.text.toString()
            val groupDescription = binding.groupDescriptionInput.text.toString()

            if(!checkChangeImageUrl) {
                Toast.makeText(this, getString(R.string.Group_Image_Validation), Toast.LENGTH_SHORT).show()
            }else if(groupName.isEmpty()){
                Toast.makeText(this , getString(R.string.Group_Name_Validation) , Toast.LENGTH_SHORT).show();
            }else if(groupDescription.isEmpty()){
                Toast.makeText(this , getString(R.string.Group_Description_Validation) , Toast.LENGTH_SHORT).show()
            }else{
                val message = Message()
                message.messageType = "group"
                message.groupName = binding.groupNameInput.text.toString()
                message.groupDescription = binding.groupDescriptionInput.text.toString()
                message.members = participants.map { it.id } as ArrayList<String>
                message.members.add(Utilities.getAuthFirebase().uid.toString())
                Log.d("Members" , message.members.toString())
                FirebaseQueries.addMessage(message){ messageId ->
                    FirebaseQueries.uploadMedia(filePath , "image" , this){ imageUrl ->
                        FirebaseQueries.updateGroupPhoto(messageId , imageUrl){
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra("messageId", messageId)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun handleGroupImage(){
        binding.imageViewGroup.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        chooseImageFromGallery.launch(intent)
    }

    private var chooseImageFromGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK && result.data != null){
            filePath = result.data!!.data!!
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver , filePath)
            binding.imageViewGroup.setImageBitmap(bitmap)
            checkChangeImageUrl = true

            Log.d("filepath" , filePath.toString())
        }
    }


    private fun initArrayOfParticipant(){
        if(intent.getSerializableExtra("ParticipantData") != null){
            val participantFromIntent : ArrayList<User> = intent.getSerializableExtra("ParticipantData") as ArrayList<User>
            participants.addAll(participantFromIntent)
        }
    }
}