package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import edu.bluejack22_1.fidertime.adapters.MemberRecycleViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.FragmentMemberListBinding
import edu.bluejack22_1.fidertime.models.MessageMember
import edu.bluejack22_1.fidertime.models.User

class MemberListFragment (
    private val memberListRef : Query,
    private val messageId : String,
    private val memberGroupIds : ArrayList<String>
) : Fragment() {

    private var _binding: FragmentMemberListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMember: RecyclerView
//    private lateinit var adapter : MemberRecycleViewAdapter
    private lateinit var adapter : MemberRecycleViewAdapter
    private lateinit var currentUserRole : MessageMember
    private lateinit var userData: ArrayList<User>
    private var isLoading = false
    private var LIMIT: Long = 7
    private var prevSize: Long = 0;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemberListBinding.inflate(inflater, container, false)
        recyclerViewMember = binding.recyclerViewMember
        recyclerViewMember.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMember.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        userData = arrayListOf()
        attachRecyclerViewAdapter(memberListRef)
        return binding.root
    }

    private fun attachRecyclerViewAdapter(query: Query) {

        val userId = Utilities.getAuthFirebase().uid.toString()

        FirebaseQueries.getUserRole(messageId , userId){ messageMember ->
            adapter = MemberRecycleViewAdapter(userData , messageMember , this , messageId , memberGroupIds)
            recyclerViewMember.adapter = adapter
            recyclerViewMember.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(dy > 0){
                        var itemCount = adapter.itemCount
                        if((recyclerViewMember.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1 && !isLoading){
                            loadMoreData(query)
                        }
                    }
                }
            })

            currentUserRole = messageMember
            query.limit(LIMIT).addSnapshotListener { snapshot , e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val users = ArrayList<User>()
                    for(doc in snapshot){
                        val user = doc.toObject<User>()
                        user.id = doc.id
                        users.add(user)
                    }
                    if(adapter.getData().isEmpty()){
                        userData.addAll(users)
                        adapter = MemberRecycleViewAdapter(userData , messageMember , this , messageId , memberGroupIds)
                        recyclerViewMember.adapter = adapter
//                        adapter.updateData(userData)
                    }else{
                        userData.clear()
                        adapter = MemberRecycleViewAdapter(userData , messageMember , this , messageId , memberGroupIds)
                        recyclerViewMember.adapter = adapter
                    }
                }
            }
        }
    }

    private fun loadMoreData(query: Query){
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        LIMIT += LIMIT;
        Log.d("Limit : " , LIMIT.toString())
        query.limit(LIMIT).get().addOnSuccessListener { documentSnapshots ->
            val users = ArrayList<User>()
            for (doc in documentSnapshots) {
                val user = doc.toObject<User>()
                user.id = doc.id
                users.add(user)
            }
            if(users != adapter.getData()){
                Handler(Looper.getMainLooper()).postDelayed({
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    userData.clear()
                    userData.addAll(users)
                    adapter = MemberRecycleViewAdapter(userData , currentUserRole , this , messageId , memberGroupIds)
                    recyclerViewMember.adapter = adapter
                    setScroll()
                    }, 500)
            }else{
                Handler(Looper.getMainLooper()).postDelayed({
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    }, 500)
            }
        }.addOnFailureListener {
                Log.d("query error : " , it.toString())
        }
    }

    private fun setScroll(){
        var size = if(prevSize.toInt() == 0) 2 else prevSize
        recyclerViewMember.scrollToPosition(size.toInt())
        prevSize = (adapter.itemCount - 5).toLong()
    }

//    private fun attachRecyclerViewAdapter(query: Query , currentMember : MessageMember) {
//        adapter = MemberRecycleViewAdapter(query , currentMember , this , messageId , memberGroupIds)
//        recyclerViewMember.adapter = adapter
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val userId = Utilities.getAuthFirebase().uid.toString()
//
//        FirebaseQueries.getUserRole(messageId , userId){ messageMember ->
//            attachRecyclerViewAdapter(memberListRef , messageMember)
//            adapter.startListening()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }
}