package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.bluejack22_1.fidertime.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logo.alpha = 0f
        binding.logo.animate().setDuration(2000).alpha(1f).withEndAction({
            val i = Intent(this , LoginActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in , android.R.anim.fade_out)
            finish()
        })
    }
}