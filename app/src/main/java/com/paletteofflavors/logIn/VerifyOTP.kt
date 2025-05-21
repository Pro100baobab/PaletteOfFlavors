package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentVerifyOtpBinding


class VerifyOTP : Fragment() {

    private lateinit var _binding: FragmentVerifyOtpBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVerifyCode.setOnClickListener {
            findNavController().navigate(R.id.action_verifyOTP_to_setNewPassword)
        }
    }

}