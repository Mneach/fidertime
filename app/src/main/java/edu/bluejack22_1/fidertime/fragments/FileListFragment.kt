package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.adapters.FileListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.common.MarginItemDecoration
import edu.bluejack22_1.fidertime.common.Utilities
import edu.bluejack22_1.fidertime.databinding.FragmentFileListBinding
import edu.bluejack22_1.fidertime.models.Media

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FileListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FileListFragment (private val query : Query) : Fragment() {

    private var _binding: FragmentFileListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var adapter : FileListRecyclerViewAdapter
    private var lastVisible : String? = null
    private lateinit var mediasData : ArrayList<Media>
    var isLoading = false
    val LIMIT : Long = 5
    var prevSize : Long = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFileListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewImage
        recyclerViewMessages.layoutManager = LinearLayoutManager(this.context)
        recyclerViewMessages.addItemDecoration(MarginItemDecoration(40, LinearLayoutManager.VERTICAL))
        mediasData = arrayListOf()

        loadMoreData()

        recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0){
                    if((recyclerViewMessages.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == mediasData.size-1 && !isLoading){
                        loadMoreData()
                    }
                }
            }
        })
        return binding.root
    }

    private fun loadMoreData(){
        if(lastVisible == null){
            query.limit(LIMIT)
                .get().addOnSuccessListener { documentSnapshots ->
                    val medias = ArrayList<Media>()
                    for (doc in documentSnapshots) {
                        val media = doc.toObject<Media>()
                        media.id = doc.id
                        medias.add(media)

                    }
                    if(medias.isNotEmpty()){
                        lastVisible = medias[medias.size - 1].id
                        mediasData.addAll(medias)
                        setDataToRecyclerView()
                    }
                }.addOnFailureListener{
                    Log.d("Error query" , it.toString())
                }
        }else{
            isLoading = true
            binding.progressBar.visibility = View.VISIBLE
            Firebase.firestore.collection("media").document(lastVisible!!).get().addOnSuccessListener{ documentSnapshot ->
                query.startAfter(documentSnapshot)
                    .limit(LIMIT)
                    .get().addOnSuccessListener { documentSnapshots ->
                        val medias = ArrayList<Media>()
                        for (doc in documentSnapshots) {
                            val media = doc.toObject<Media>()
                            media.id = doc.id
                            medias.add(media)
                        }
                        if(medias.isNotEmpty()){
                            lastVisible = medias[medias.size - 1].id
                            mediasData.addAll(medias)
                            Handler(Looper.getMainLooper()).postDelayed({
                                isLoading = false
                                binding.progressBar.visibility = View.GONE
                                setDataToRecyclerView()
                            }, 500)
                        }else{
                            Handler(Looper.getMainLooper()).postDelayed({
                                isLoading = false
                                binding.progressBar.visibility = View.GONE
                            }, 500)
                        }

                    }.addOnFailureListener{
                        Log.d("Error query" , it.toString())
                    }
            }

        }
    }

    private fun setDataToRecyclerView(){
        val size = prevSize
        adapter = FileListRecyclerViewAdapter(mediasData)
        recyclerViewMessages.adapter = adapter
        recyclerViewMessages.scrollToPosition(size.toInt())
        prevSize = (adapter.itemCount - 4).toLong()
    }
}