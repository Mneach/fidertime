package edu.bluejack22_1.fidertime.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_1.fidertime.activities.AddParticipantActivity
import edu.bluejack22_1.fidertime.activities.MessageActivity
import edu.bluejack22_1.fidertime.adapters.ContactListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.FirebaseQueries
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.FragmentContactListBinding
import edu.bluejack22_1.fidertime.models.Message
import edu.bluejack22_1.fidertime.models.User

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!
    private var userId = Utilities.getAuthFirebase().uid
    private lateinit var adapter: ContactListRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentContactListBinding.inflate(inflater , container , false)
        createGroupAction()
        initializeRecyclerView()
        return binding.root
    }

    private fun initializeRecyclerView() {
        recyclerView = binding.recyclerViewList
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))

        FirebaseQueries.subscribeToContacts() {
            attachRecyclerViewAdapter(it)
            initializeFilter(it)
        }

    }

    private fun attachRecyclerViewAdapter(userContacts : ArrayList<User>) {
        adapter = ContactListRecyclerViewAdapter(userContacts)
        recyclerView.adapter = adapter
        adapter.onItemClick = { userContactId ->
            FirebaseQueries.getMessageByUserAndMessageType(userId.toString() , "personal") { messages ->
                var messageId = "";

                for (message in messages){
                    if(message.members.contains(userContactId) && message.members.contains(userId)){
                        messageId = message.id
                    }
                }
                if(messageId != ""){
                    // if user already has message room , then goto message room
                    val intent = Intent(context, MessageActivity::class.java)
                    intent.putExtra("messageId", messageId)
                    startActivity(intent)
                }else{
                    // create message room
                    val message = Message()
                    message.members.addAll(listOf(userContactId , userId.toString()))
                    message.messageType = "personal"
                    FirebaseQueries.addMessage(message){
                        val intent = Intent(context, MessageActivity::class.java)
                        intent.putExtra("messageId", it)
                        startActivity(intent)
                    }
                }
            }

        }
    }

    private fun initializeFilter(userContacts : ArrayList<User>){
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterContact(newText , userContacts)
                return true
            }
        })
    }

    private fun filterContact(text : String , userContacts : ArrayList<User>){
        var filteredList = ArrayList<User>()

        for(user in userContacts){
            if(user.name.lowercase().contains(text.lowercase()) || user.phoneNumber.contains(text.lowercase())){
                filteredList.add(user)
            }
        }

        adapter.setFilteredList(filteredList)
    }

    private fun createGroupAction(){
        binding.createGroupIcon.setOnClickListener{
            val intent = Intent(context , AddParticipantActivity::class.java)
            startActivity(intent)
        }
    }
}