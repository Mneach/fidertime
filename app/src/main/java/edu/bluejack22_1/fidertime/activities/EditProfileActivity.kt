package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.bluejack22_1.fidertime.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarEditProfile.cancelButton.setOnClickListener{
            val intent = Intent(this , MainActivity::class.java)
            intent.putExtra("FragmentOption" , "profile")
            startActivity(intent)
        }

        binding.toolbarEditProfile.editButton.setOnClickListener{

        }
    }
}