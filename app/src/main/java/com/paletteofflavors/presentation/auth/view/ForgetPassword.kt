package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentForgetPasswordBinding
import com.paletteofflavors.domain.utils.validationData.isValidEmail
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch

class ForgetPassword : Fragment() {

    private var _binding: FragmentForgetPasswordBinding? = null
    private val binding get() = _binding!!

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
        vm = (requireActivity() as MainActivity).loginViewModel

        setUpLoginViewModelObservers()
        setUpListeners()
        observeFindUserResult()
    }

    private fun observeFindUserResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.findUserResult.collect { result ->

                    val phoneNumber = result.getOrNull()
                    if (phoneNumber != null) {
                        binding.forgetPasswordContinue.isEnabled = true
                        val email = binding.emailForReset.text.toString().trim()
                        val direction = ForgetPasswordDirections.actionForgetPasswordToMakeSelection(email, phoneNumber)
                        findNavController().navigate(direction)
                        vm.clearResults()
                    }
                    else if (result.isSuccess && binding.forgetPasswordContinue.isEnabled == false) {

                         if (vm.findUserResult.value.getOrNull() == null) {
                             binding.forgetPasswordContinue.isEnabled = true
                             if (binding.emailForReset.text?.isNotEmpty() == true) {
                                 Toast.makeText(requireContext(), "Email is not registered", Toast.LENGTH_SHORT).show()
                             }
                         }
                    } else if (result.isFailure) {
                        binding.forgetPasswordContinue.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.forgetPasswordContinue.setOnClickListener {
            if (isValidEmail(binding.emailForReset)) {
                val email = binding.emailForReset.text.toString().trim()
                binding.forgetPasswordContinue.isEnabled = false
                vm.findUserByEmail(email)
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

    private fun setUpLoginViewModelObservers() {
        vm.curemail.observe(viewLifecycleOwner) { newText ->
            if (binding.emailForReset.text.toString() != newText) {
                isUpdatingFromViewModel = true
                binding.emailForReset.setText(newText)
                isUpdatingFromViewModel = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
