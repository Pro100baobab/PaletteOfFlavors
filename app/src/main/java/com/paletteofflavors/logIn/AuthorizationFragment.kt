package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentAuthorizationBinding


class AuthorizationFragment : Fragment() {

    private var _binding: FragmentAuthorizationBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
 }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_authorizationFragment_to_registrationFragment)
        }


        binding.startLogin.setOnClickListener{
            findNavController().navigate(R.id.action_authorizationFragment_to_loginFragment)
        }

        binding.aboutUs.setOnClickListener {
            //TODO:
            //findNavController().navigate(R.id.)
        }
    }

    override fun onResume() {
        super.onResume()

        if((requireActivity() as MainActivity).sessionManager.checkLogin()){
            findNavController().navigate(R.id.action_authorizationFragment_to_registrationFragment)
        }
    }
}