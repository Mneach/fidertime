package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.fidertime.adapters.MediaListRecyclerViewAdapter
import edu.bluejack22_1.fidertime.databinding.FragmentMediaListBinding
import edu.bluejack22_1.fidertime.models.Media

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MediaListFragment (private val query : Query): Fragment() {

    private var _binding: FragmentMediaListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var adapter : MediaListRecyclerViewAdapter;
    private var lastVisible : String? = null
    private lateinit var mediasData : ArrayList<Media>
    var isLoading = false
    val LIMIT : Long = 12
    var prevSize : Long = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMediaListBinding.inflate(inflater, container, false)
        recyclerViewMessages = binding.recyclerViewMedia
        recyclerViewMessages.layoutManager = GridLayoutManager(this.context , 3)

//        attachRecyclerViewAdapter(query)

        mediasData = arrayListOf()
        loadMoreData()
        recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0){
                    val visibleThreshold = 3
                    var lastItem = (recyclerViewMessages.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                    var totalCount = (recyclerViewMessages.layoutManager as GridLayoutManager).itemCount
                    if(totalCount <= lastItem + visibleThreshold){
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
                        adapter = MediaListRecyclerViewAdapter(mediasData)
                        recyclerViewMessages.adapter = adapter
                        setScroll()
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
                                setScroll()
                                adapter = MediaListRecyclerViewAdapter(mediasData)
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

    private fun setScroll(){
        val size = prevSize
        recyclerViewMessages.scrollToPosition(size.toInt())
        if(prevSize.toInt() == 0){
            prevSize = 3;
        }else{
            prevSize += LIMIT - 3;
        }
    }

//    private fun attachRecyclerViewAdapter(query: Query) {
//        adapter = MediaListRecyclerViewAdapter(query)
//        recyclerViewMessages.adapter = adapter
//    }
}