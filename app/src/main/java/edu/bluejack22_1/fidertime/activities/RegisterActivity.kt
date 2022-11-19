package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.databinding.ActivityRegisterBinding
import edu.bluejack22_1.fidertime.models.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvToLogin.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this , LoginActivity::class.java))
        })

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener{
            val email = binding.email.text.toString()
            val name = binding.name.text.toString()
            val phoneNumber = binding.phoneNumber.text.toString()
            val password = binding.password.text.toString()

            // EMAIL VALIDATION
            if(email.isEmpty()){
                binding.email.error = "Email cannot be null"
                binding.email.requestFocus()
                return@setOnClickListener
            }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.email.error = "Email not valid"
                binding.email.requestFocus()
                return@setOnClickListener
            }

            // NAMEf VALIDATION
            if(name.isEmpty()){
                binding.name.error = "Name cannot be null"
                binding.name.requestFocus()
                return@setOnClickListener
            }else if(name.length < 5){
                binding.name.error = "Name must be more than 5 characters"
                binding.name.requestFocus()
                return@setOnClickListener
            }

            // PHONE NUMBER VALIDATION
            if(phoneNumber.isEmpty()){
                binding.phoneNumber.error = "Phone Number cannot be null"
                binding.phoneNumber.requestFocus()
                return@setOnClickListener
            }else if(!phoneNumber.startsWith("+62")){
                binding.phoneNumber.error = "Phone Number must starts with +62"
                binding.phoneNumber.requestFocus()
                return@setOnClickListener
            }
            // Password Validation
            if(password.isEmpty()){
                binding.password.error = "Password cannot be null"
                binding.password.requestFocus()
                return@setOnClickListener
            }else if(password.length < 5){
                binding.password.error = "Password must be more than 5 characters"
                binding.password.requestFocus()
                return@setOnClickListener
            }

            checkDataToFirebase(email , name , phoneNumber , password)
        }
    }

    private fun checkDataToFirebase(email : String, name : String, phoneNumber : String, password : String){
        val db = Firebase.firestore
        db.collection("users")
            .get()
            .addOnSuccessListener{ result ->
                var checkName = false
                var checkPhoneNumber = false
                Log.d("Test" , "test1234")
                for(doc in result){
                    val user = doc.toObject<User>()
                    user.id = doc.id
                    Log.d("Check = " , user.phoneNumber + " | " + phoneNumber)
                    if(user.phoneNumber == phoneNumber){
                        checkPhoneNumber = true
                    }else if(user.name == name){
                        checkName = true
                    }
                }

                if(checkPhoneNumber){
                    Toast.makeText(this , "Phone Number has already taken" , Toast.LENGTH_SHORT).show()
                }else if(checkName){
                    Toast.makeText(this , "Name has already taken" , Toast.LENGTH_SHORT).show()
                }else{
                    registerUser(email , name , phoneNumber , password)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ERROR USER", "Error getting documents: ", exception)
            }
        }

    private fun registerUser(email : String, name : String, phoneNumber : String, password : String){
        val db = Firebase.firestore
        auth.createUserWithEmailAndPassword(email , password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("USER DATA", auth.currentUser.toString())
                    val id = auth.currentUser?.uid

                    val data = hashMapOf(
                        "email" to email,
                        "name" to name,
                        "phoneNumber" to phoneNumber,
                    )

                    db.collection("users").document(id.toString())
                        .set(data)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this , "Success Register" , Toast.LENGTH_SHORT).show()
                            val intent = Intent(this , LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this , e.toString() , Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FAILED", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}