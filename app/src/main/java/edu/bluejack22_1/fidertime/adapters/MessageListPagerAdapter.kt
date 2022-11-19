package edu.bluejack22_1.fidertime.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.fragments.MessageListFragment

class MessageListPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        val db = Firebase.firestore
        val userId = "Km69GgIsRZhgKUsb0aIq0YSZWVX2"

        return when (position) {
            0 -> {
                MessageListFragment(
                    db.collection("messages").whereArrayContains("members", userId).orderBy("lastChatTimestamp", Direction.DESCENDING)
                )
            }
            1 -> {
                MessageListFragment(
                    db.collection("messages").whereArrayContains("members", userId).orderBy("lastChatTimestamp", Direction.DESCENDING)
                        .whereEqualTo("messageType", "personal")
                )
            }
            2 -> {
                MessageListFragment(
                    db.collection("messages").whereArrayContains("members", userId).orderBy("lastChatTimestamp", Direction.DESCENDING)
                        .whereEqualTo("messageType", "group")
                )
            }
            else -> {
                MessageListFragment(
                    db.collection("messages").whereArrayContains("members", userId).orderBy("lastChatTimestamp", Direction.DESCENDING)
                )
            }
        }
    }

}