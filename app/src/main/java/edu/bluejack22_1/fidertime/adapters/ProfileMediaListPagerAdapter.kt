package edu.bluejack22_1.fidertime.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.fragments.FileListFragment
import edu.bluejack22_1.fidertime.fragments.LinkListFragment
import edu.bluejack22_1.fidertime.fragments.MediaListFragment

class ProfileMediaListPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        val db = Firebase.firestore
        val userId = Utilities.getAuthFirebase().currentUser!!.uid
        Log.d("User id = " , userId)

        return when (position) {
            0 -> {
                MediaListFragment(
                    db.collection("media").whereEqualTo("senderUserId", userId).whereEqualTo("type" , "image").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            1 -> {
                FileListFragment(
                    db.collection("media").whereEqualTo("senderUserId", userId).whereEqualTo("type" , "file").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            2 -> {
                LinkListFragment(
                    db.collection("media").whereEqualTo("senderUserId", userId).whereEqualTo("type" , "link").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            else -> {
                MediaListFragment(
                    db.collection("media").whereEqualTo("senderUserId", userId).whereEqualTo("type" , "link").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
        }
    }
}