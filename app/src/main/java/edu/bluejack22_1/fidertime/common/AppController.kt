package edu.bluejack22_1.fidertime.common

import android.app.Application
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AppController : Application(), LifecycleObserver {

    private var userId: String? = null
    private val TAG = "AppController"

    override fun onCreate() {
        super.onCreate()
        userId = Firebase.auth.currentUser?.uid
        Log.d(TAG, userId.toString())
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        Log.d(TAG, "Foreground")
        if (userId != null) {
            FirebaseQueries.updateUserStatus(userId!!, mapOf(Pair("status", "online")))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        Log.d(TAG, "Background")
        if (userId != null) {
            FirebaseQueries.updateUserStatus(userId!!, mapOf(Pair("status", "offline"), Pair("lastSeenTimestamp", Timestamp.now())))
        }
    }


}