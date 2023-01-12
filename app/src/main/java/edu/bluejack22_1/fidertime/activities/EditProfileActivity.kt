package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.FirebaseQueries.Companion.updateUserData
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityEditProfileBinding
import edu.bluejack22_1.fidertime.models.User

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private lateinit var filePath : Uri
    private var checkChangeImageUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableButton(binding.emailInput)
        disableButton(binding.phoneNumberInput)

        val userId : String? = intent.getStringExtra("userId")

        if (userId != null) {
            FirebaseQueries.getUserById(userId){ user ->
                attachUserData(user)

                binding.toolbarEditProfile.cancelButton.setOnClickListener{
                    val intent = Intent(this , MainActivity::class.java)
                    intent.putExtra("FragmentOption" , "profile")
                    startActivity(intent)
                }

                binding.imageViewProfile.setOnClickListener{
                    selectImage()
                }

                binding.toolbarEditProfile.editButton.setOnClickListener{
                    updateUserData(user)
                }
            }
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
            binding.imageViewProfile.setImageBitmap(bitmap)
            checkChangeImageUrl = true
        }
    }

    private fun disableButton(editText : EditText){
        editText.showSoftInputOnFocus = false
    }

    private fun updateUserData(user : User){
        var email = binding.emailInput.text.toString()
        var phoneNumber = binding.phoneNumberInput.text.toString()
        var name = binding.nameInput.text.toString()
        var bio = binding.bioInput.text.toString()
        var uid = Utilities.getAuthFirebase().uid

        if(bio.length > 200){
            binding.bioInput.error = getString(R.string.bio_length_validation)
            binding.bioInput.requestFocus()
            return
        }

        FirebaseQueries.getUsers{

            var checkName = false
            for(userFromDB in it){
                if(userFromDB.name == name && user.name != name){
                    checkName = true
                    break
                }
            }

            if(!checkName){
                user.email = email
                user.phoneNumber = phoneNumber
                user.name = name
                user.bio = bio
                if(checkChangeImageUrl){
                    FirebaseQueries.uploadImage(user , filePath , this) { imageUrl ->
                        user.profileImageUrl = imageUrl

                        updateUserData(uid.toString() , user) {
                            val intent = Intent(this , MainActivity::class.java)
                            intent.putExtra("FragmentOption" , "profile")
                            startActivity(intent)
                        }
                    }
                }else{
                    updateUserData(uid.toString() , user) {
                        val intent = Intent(this , MainActivity::class.java)
                        intent.putExtra("FragmentOption" , "profile")
                        startActivity(intent)
                    }
                }
            }else{
                Toast.makeText(this , getString(R.string.name_unique_validation) , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachUserData(user : User){
        if(user.profileImageUrl.isNotEmpty()){
            binding.imageViewProfile.load(user.profileImageUrl) {
                crossfade(true)
                crossfade(300)
                placeholder(R.drawable.image_placeholder)
            }
        }
        binding.emailInput.setText(user.email)
        binding.phoneNumberInput.setText(user.phoneNumber)
        binding.nameInput.setText(user.name)
        binding.bioInput.setText(user.bio)
    }
}