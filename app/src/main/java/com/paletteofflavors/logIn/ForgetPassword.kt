package com.paletteofflavors.logIn

import DataSource.Network.Turso
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentForgetPasswordBinding
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ForgetPassword : Fragment() {

    private lateinit var _binding: FragmentForgetPasswordBinding
    private val binding get() = _binding

    private lateinit var vm: LoginViewModel
    private var isUpdatingFromViewModel = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLoginViewModelAndObservers()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.forgetPasswordContinue.setOnClickListener {

            if (isValidEmail(binding.emailForReset)) {

                val email = binding.emailForReset.text.toString().trim()

                // Check that this email is registered
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    Turso(
                        requireActivity() as MainActivity,
                        requireContext()
                    ).FindUserByEmail(email) { phoneNumber ->
                        if (phoneNumber.isNotEmpty()) {
                            //Toast.makeText(requireContext(), "phone:$phoneNumber", Toast.LENGTH_SHORT).show()
                            val direction =
                                ForgetPasswordDirections.actionForgetPasswordToMakeSelection(
                                    email,
                                    phoneNumber
                                )
                            findNavController().navigate(direction)
                        } else {
                            lifecycleScope.launch(Dispatchers.Main){
                                Toast.makeText(
                                    requireContext(),
                                    "Email is not registered",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
            }

        }

        binding.signupBackButtonForgetPassword.setOnClickListener {
            vm.setEmail("")
            findNavController().navigate(R.id.action_forgetPassword_to_loginFragment)
        }

        binding.emailForReset.doAfterTextChanged { editable ->
            if (!isUpdatingFromViewModel) editable?.toString()?.let { vm.setEmail(it) }
        }
    }

    private fun setUpLoginViewModelAndObservers() {
        vm = (requireActivity() as MainActivity).viewModel
        vm.curemail.observe(viewLifecycleOwner) { newText ->
            if (binding.emailForReset.text.toString() != newText) {
                isUpdatingFromViewModel = true
                binding.emailForReset.setText(newText)
                isUpdatingFromViewModel = false
            }
        }
    }
}