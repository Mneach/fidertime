package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.bluejack22_1.fidertime.databinding.FragmentChatFileItemInBinding

class ChatFileItemInFragment : Fragment() {
    private var _binding : FragmentChatFileItemInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatFileItemInBinding.inflate(inflater, container, false)
        return binding.root
    }

}