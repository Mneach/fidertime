package edu.bluejack22_1.fidertime.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.activities.EditProfileActivity
import edu.bluejack22_1.fidertime.activities.LoginActivity
import edu.bluejack22_1.fidertime.activities.MainActivity
import edu.bluejack22_1.fidertime.adapters.ProfileMediaListPagerAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.FragmentProfileBinding
import edu.bluejack22_1.fidertime.models.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    lateinit var binding : FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initializeTabs()
        binding.logoutButton.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            FirebaseQueries.updateUserStatus(Firebase.auth.currentUser!!.uid, mapOf(Pair("status", "offline"), Pair("lastSeenTimestamp", Timestamp.now())))
            val mainActivity : MainActivity = activity as MainActivity

            if(GoogleSignIn.getLastSignedInAccount(mainActivity) != null){
                // SIGN OUT IF USER LOGIN WITH GOOGLE
                Utilities.getGoogleSignInClient(mainActivity).signOut().addOnCompleteListener{
                    Utilities.getAuthFirebase().signOut()
                    startActivity(intent)
                }
            }else{
                // SIGN OUT IF USER LOGIN WITH FIREBASE
                Utilities.getAuthFirebase().signOut()
                startActivity(intent)
            }

        }

        FirebaseQueries.subscribeToUser(Firebase.auth.currentUser!!.uid.toString()) {

            attachUserData(it)

            val user : User = it
            binding.editButton.setOnClickListener(View.OnClickListener {
                val intent = Intent(context , EditProfileActivity::class.java)
                intent.putExtra("UserData" , user)
                startActivity(intent)

            })
        }

        return binding.root
    }

    private fun initializeTabs() {
        val messageListPagerAdapter = ProfileMediaListPagerAdapter(parentFragmentManager, lifecycle)
        binding.pagerMessageList.adapter = messageListPagerAdapter
        Log.d("tabs", "masuk")

        TabLayoutMediator(binding.tabLayoutMessageList, binding.pagerMessageList) {tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Media"
                }
                1 -> {
                    tab.text = "Files"
                }
                2 -> {
                    tab.text = "Links"
                }
            }
        }.attach()
    }

    private fun attachUserData(it : User){
        binding.name.text = it.name
        binding.email.text = it.email
        binding.phoneNumber.text = it.phoneNumber
        binding.bio.text = it.bio
        if(it.media.size == 0){
            binding.mediaCount.text = "0"
        }else{
            binding.mediaCount.text = it.media.size.toString()
        }

        if(it.files.size == 0){
            binding.fileCount.text = "0"
        }else{
            binding.fileCount.text = it.media.size.toString()
        }

        if(it.links.size == 0){
            binding.linkCount.text = "0"
        }else{
            binding.linkCount.text = it.media.size.toString()
        }

        if(it.profileImageUrl != "" && it.profileImageUrl.isNotEmpty()){
            binding.imageViewProfile.load(it.profileImageUrl)
        }else{
            binding.imageViewProfile.setBackgroundResource(R.drawable.default_avatar)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}