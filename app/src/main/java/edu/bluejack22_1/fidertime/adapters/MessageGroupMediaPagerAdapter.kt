package edu.bluejack22_1.fidertime.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.fragments.FileListFragment
import edu.bluejack22_1.fidertime.fragments.LinkListFragment
import edu.bluejack22_1.fidertime.fragments.MediaListFragment
import edu.bluejack22_1.fidertime.fragments.MemberListFragment

class MessageGroupMediaPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle, var messageId : String, var memberGroupIds : ArrayList<String>): FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val db = Firebase.firestore
        Log.d("message id = " , messageId)

        return when (position) {
            0 -> {
                MemberListFragment(
                    db.collection("users").whereIn(FieldPath.documentId() , memberGroupIds).orderBy(FieldPath.documentId()),
                    messageId,
                    memberGroupIds
                )
            }
            1 -> {
                MediaListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereIn("type" , listOf("image" , "video")).orderBy("timestamp", Query.Direction.DESCENDING)
                )
            }
            2 -> {
                FileListFragment(
                    db.collection("media").whereEqualTo("messageId", messageId).whereEqualTo("type" , "file").orderBy("timestamp", Query.Direction.DESCENDING)
                )
//                FileListFragment()
            }
            3 -> {
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