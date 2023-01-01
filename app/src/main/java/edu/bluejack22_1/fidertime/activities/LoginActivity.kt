package edu.bluejack22_1.fidertime.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.ActivityLoginBinding
import edu.bluejack22_1.fidertime.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (Utilities.getAuthFirebase().uid != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.tvToRegister.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        })

        binding.buttonLogin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty()) {
                binding.email.error = getString(R.string.email_null_validation)
                binding.email.requestFocus()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                binding.password.error = getString(R.string.password_null_validation)
                binding.password.requestFocus()
                return@setOnClickListener
            }
            login(email, password)

        }

        binding.buttonLoginGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent = Utilities.getGoogleSignInClient(this).signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
        }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                var intent = Intent(this, MainActivity::class.java)
                val userId = Utilities.getAuthFirebase().uid.toString()

                FirebaseQueries.getUserById(userId) { userFromDB ->
                    if (userFromDB.id != "") {
                        // goto main page if user already have account
                        startActivity(intent)
                    } else {
                        // register phone number if user didn't have account
                        intent = Intent(this, RegisterPhoneNumberActivity::class.java)
                        val user: User = userFromDB
                        user.id = Utilities.getAuthFirebase().uid.toString()
                        user.email = account.email.toString()
                        user.name = account.displayName.toString()
                        user.profileImageUrl = account.photoUrl.toString()
                        intent.putExtra("GoogleLoginData", user)
                        startActivity(intent)
                    }
                }

            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    resetForm()
                    FirebaseQueries.updateUserStatus(
                        task.result.user!!.uid,
                        mapOf(Pair("status", "online"))
                    ) {

                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.invalid_credential), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun resetForm() {
        binding.email.text.clear()
        binding.password.text.clear()
    }

}