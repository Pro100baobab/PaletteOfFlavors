package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentPasswordSuccessUpdatedBinding
import com.paletteofflavors.databinding.FragmentSetNewPasswordBinding


class PasswordSuccessUpdated : Fragment() {

    private lateinit var _binding: FragmentPasswordSuccessUpdatedBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentPasswordSuccessUpdatedBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginAfterResetPassword.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_passwordSuccessUpdated_to_loginFragment)
        }
    }


}