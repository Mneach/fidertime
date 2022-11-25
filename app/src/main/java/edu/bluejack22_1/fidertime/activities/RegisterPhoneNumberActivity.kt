package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.databinding.ActivityRegisterBinding
import edu.bluejack22_1.fidertime.databinding.ActivityRegisterPhoneNumberBinding
import edu.bluejack22_1.fidertime.models.User

class RegisterPhoneNumberActivity : AppCompatActivity() {

    lateinit var binding : ActivityRegisterPhoneNumberBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.buttonNext.setOnClickListener{
            val phoneNumber = binding.phoneNumber.text.toString()
            val user : User = intent.getSerializableExtra("GoogleLoginData") as User

            // PHONE NUMBER VALIDATION
            if(phoneNumber.isEmpty()){
                binding.phoneNumber.error = getString(R.string.phone_number_null_validation)
                binding.phoneNumber.requestFocus()
                return@setOnClickListener
            }else if(!phoneNumber.startsWith("+62")){
                binding.phoneNumber.error = getString(R.string.phone_number_starts_validation)
                binding.phoneNumber.requestFocus()
                return@setOnClickListener
            }

            user.phoneNumber = phoneNumber
            registerUserData(user)
        }

    }

    private fun registerUserData(user : User){
        val db = Firebase.firestore

        FirebaseQueries.getUserByPhoneNumber(user.phoneNumber) {
            if(it.isEmpty()){
                db.collection("users").document(user.id)
                    .set(user)
                    .addOnSuccessListener { documentReference ->
                        val intent = Intent(this , MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this , e.toString() , Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(this , getString(R.string.phone_number_unique_validation) , Toast.LENGTH_SHORT).show()
            }
        }
    }
}