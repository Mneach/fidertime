package edu.bluejack22_1.fidertime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack22_1.fidertime.adapters.ParticipantListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.adapters.SelectedPartcipantListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.databinding.ActivityAddParticipantBinding
import edu.bluejack22_1.fidertime.databinding.ActivityCreateGroupBinding
import edu.bluejack22_1.fidertime.models.User

class AddParticipantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddParticipantBinding
    private lateinit var adapter: ParticipantListRecyclerViewAdapter
    private lateinit var adapterSelectedParticipant : SelectedPartcipantListRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewSelectedParticipant : RecyclerView
    private lateinit var auth : FirebaseAuth
    private var participants : ArrayList<User> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddParticipantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeRecyclerView()
        setToolbarAction()
    }

    private fun initializeRecyclerView(){
        recyclerView = binding.recyclerViewList
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))


        FirebaseQueries.subscribeToContacts() {
            attachRecyclerViewAdapterParticipantList(it)
            setNumberOfParticipant(it.size)
            initArrayOfParticipant()
            initializeRecyclerViewSelectedParticipant()
            initializeFilter(it)
        }

    }

    private fun initializeRecyclerViewSelectedParticipant(){
        recyclerViewSelectedParticipant = binding.recyclerViewSelectedParticipant
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerViewSelectedParticipant.layoutManager = layoutManager
        recyclerViewSelectedParticipant.addItemDecoration(MarginItemDecoration(100, LinearLayoutManager.HORIZONTAL))
        attachRecyclerViewAdapterSelectedParticipant()
    }

    private fun attachRecyclerViewAdapterSelectedParticipant(){
        adapterSelectedParticipant = SelectedPartcipantListRecyclerViewAdapter(participants)
        recyclerViewSelectedParticipant.adapter = adapterSelectedParticipant
        adapterSelectedParticipant.onItemClick = { user ->
            handleSelectedParticipant(user , "remove")
        }
    }

    private fun attachRecyclerViewAdapterParticipantList(participants : ArrayList<User>) {
        adapter = ParticipantListRecyclerViewAdapter(participants)
        recyclerView.adapter = adapter
        adapter.onItemClick = { user ->
            handleSelectedParticipant(user , "add")
        }
    }

    private fun setToolbarAction(){
        binding.toolbarCreateGroup.cancelButton.setOnClickListener {
            val intent = Intent(this , MainActivity::class.java)
            intent.putExtra("FragmentOption" , "contact")
            startActivity(intent)
        }

        binding.toolbarCreateGroup.nextButton.setOnClickListener {
            Log.d("participants : " , participants.toString())
            val intent = Intent(this , CreateGroupActivity::class.java)
            val participantIds : ArrayList<String> = participants.map { it.id } as ArrayList
            intent.putStringArrayListExtra("ParticipantIds" , participantIds)
            startActivity(intent)
        }
    }

    private fun handleSelectedParticipant(participant : User , actionType : String){

        if(actionType == "add"){
            if(!participants.contains(participant)){
                participants.add(participant)
                adapterSelectedParticipant.setNewSelectedParticipant(participants)
                binding.toolbarCreateGroup.currentNumberOfParticipant.text = participants.size.toString()
            }
        }else if(actionType == "remove"){
            participants.remove(participant)
            adapterSelectedParticipant.setNewSelectedParticipant(participants)
            binding.toolbarCreateGroup.currentNumberOfParticipant.text = participants.size.toString()
        }

        binding.toolbarCreateGroup.nextButton.isEnabled = participants.size > 1
    }

    private fun setNumberOfParticipant(numberOfParticipant : Int){
        binding.toolbarCreateGroup.numberOfParticipant.text = numberOfParticipant.toString()
    }

    private fun initArrayOfParticipant(){
        val participantIdsFromIntent = intent.getStringArrayListExtra("ParticipantIds")
        if (participantIdsFromIntent != null) {
            val participantData = adapter.getParticipantByIds(participantIdsFromIntent)
            participants.addAll(participantData)
            binding.toolbarCreateGroup.currentNumberOfParticipant.text = participants.size.toString()
        }
    }

    private fun initializeFilter(userContacts : ArrayList<User>){
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterParticipant(newText , userContacts)
                return true
            }
        })
    }

    private fun filterParticipant(text : String , userContacts : ArrayList<User>){
        var filteredList = ArrayList<User>()

        for(user in userContacts){
            if(user.name.lowercase().contains(text.lowercase()) || user.phoneNumber.contains(text.lowercase())){
                filteredList.add(user)
            }
        }

        adapter.setFilteredList(filteredList)
    }
}