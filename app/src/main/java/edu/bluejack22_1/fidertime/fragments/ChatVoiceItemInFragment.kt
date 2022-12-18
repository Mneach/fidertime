package edu.bluejack22_1.fidertime.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.bluejack22_1.fidertime.databinding.FragmentChatVoiceItemInBinding

class ChatVoiceItemInFragment : Fragment() {

    private var _binding : FragmentChatVoiceItemInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatVoiceItemInBinding.inflate(inflater, container, false)
        return binding.root
    }

}