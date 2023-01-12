package edu.bluejack22_1.fidertime.adapters

import android.provider.MediaStore.Audio.Media
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

class MessagePersonalMediaPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle , var messageId : String): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val db = Firebase.firestore

        return when (position) {
            0 -> {
                MediaListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereIn("type" , listOf("image" , "video")).orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            1 -> {
                FileListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereEqualTo("type" , "file").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            2 -> {
                LinkListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereEqualTo("type" , "link").orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            else -> {
                MediaListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereIn("type" , listOf("image" , "video")).orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
        }
    }
}