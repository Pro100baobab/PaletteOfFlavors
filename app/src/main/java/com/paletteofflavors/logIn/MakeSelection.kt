package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentMakeSelectionBinding


class MakeSelection : Fragment() {

    private lateinit var _binding: FragmentMakeSelectionBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
           _binding = FragmentMakeSelectionBinding.inflate(inflater, container, false)
           return _binding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Add *** for phone_number on the next layout
        binding.viaEmail.setOnClickListener {
            findNavController().navigate(R.id.action_makeSelection_to_verifyOTP)
        }

        binding.viaPhoneNumber.setOnClickListener {
            findNavController().navigate(R.id.action_makeSelection_to_verifyOTP)
        }
    }

}