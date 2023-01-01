package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.R
import edu.bluejack22_1.fidertime.activities.MainActivity
import edu.bluejack22_1.fidertime.adapters.ChooseAdminRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.PopupChooseAdminBinding

class ChooseAdminFragment (
    private val messageId : String,
    private val memberGroupIds : ArrayList<String>
    ) : BottomSheetDialogFragment() {

    private lateinit var binding : PopupChooseAdminBinding
    private lateinit var recyclerViewMember: RecyclerView
    private lateinit var adapter : ChooseAdminRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PopupChooseAdminBinding.inflate(inflater , container , false)
        recyclerViewMember = binding.recyclerViewMember
        recyclerViewMember.layoutManager = LinearLayoutManager(this.context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.maxHeight = 1000
        bottomSheetBehavior.peekHeight = 1000
    }

    private fun attachRecyclerViewAdapter(query: Query) {
        adapter = ChooseAdminRecyclerViewAdapter(query)
        recyclerViewMember.adapter = adapter
        adapter.onItemClick = { memberId ->
            FirebaseQueries.deleteMember(messageId , Utilities.getAuthFirebase().uid.toString() , memberGroupIds){
                FirebaseQueries.assignAdminRole(messageId , memberId){
                    Toast.makeText(this.context , R.string.success_leave_group , Toast.LENGTH_SHORT).show()
                    val intent = Intent(this.context , MainActivity::class.java)
                    intent.putExtra("FragmentOption" , "message")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val userId = Utilities.getAuthFirebase().uid.toString()
        memberGroupIds.remove(userId)
        val query = Firebase.firestore.collection("users").whereIn(FieldPath.documentId() , memberGroupIds)
        attachRecyclerViewAdapter(query)
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }


}