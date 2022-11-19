package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack22_1.fidertime.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvToRegister.setOnClickListener(View.OnClickListener{
            startActivity(
                Intent(this , RegisterActivity::class.java)
            )
        })

        binding.buttonLogin.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if(email.isEmpty()){
                binding.email.error = "Email / Phone number cannot be null"
                binding.email.requestFocus()
                return@setOnClickListener
            }else if(password.isEmpty()){
                binding.password.error = "Password cannot be null"
                binding.password.requestFocus()
                return@setOnClickListener
            }
            login(email , password)

        }
    }

    private fun login(email: String , password : String){
        auth.signInWithEmailAndPassword(email , password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    val intent = Intent(this , MainActivity::class.java)
                    resetForm()
                    startActivity(intent)
                }else{
                    Toast.makeText(this , "INVALID CREDENTIAL" , Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetForm(){
        binding.email.text.clear()
        binding.password.text.clear()
    }

}