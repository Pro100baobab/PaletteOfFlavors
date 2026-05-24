package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hbb20.CountryCodePicker
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentRegistrationBinding
import com.paletteofflavors.domain.utils.validationData.isValidEmail
import com.paletteofflavors.domain.utils.validationData.isValidFullName
import com.paletteofflavors.domain.utils.validationData.isValidPassword
import com.paletteofflavors.domain.utils.validationData.isValidPhone
import com.paletteofflavors.domain.utils.validationData.isValidUsername
import com.paletteofflavors.presentation.auth.viewmodel.RegistrationViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        vm = activity.viewModelRegistration

        if (activity.sessionManager.checkLogin()) {
            binding.tvLogin.isVisible = false
        }

        setUpOnClickListeners()
        observeUniqueCheck()
    }

    private fun observeUniqueCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isUniqueResult.collect { result ->
                    Log.d("RegistrationFragment", "isUniqueResult collected: $result")
                    if (result == null) return@collect
                    
                    result.onSuccess { (isUnique, errorMsg) ->
                        Log.d("RegistrationFragment", "isUniqueResult success: isUnique=$isUnique, errorMsg=$errorMsg")
                        if (isUnique) {
                            navigateToOTP()
                        } else {
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(requireContext(), errorMsg ?: "User already exists", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure { e ->
                        Log.e("RegistrationFragment", "isUniqueResult failure: ${e.message}", e)
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToOTP() {
        Log.d("RegistrationFragment", "navigateToOTP called")
        val fullName = binding.etFullname.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.countryCodePiker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        vm.run {
            setFullName(fullName)
            setUserName(username)
            setEmail(email)
            setPhone(phone)
            setPassword(password)
        }

        Log.d("RegistrationFragment", "Data set in VM, email=$email, phone=$phone")

        try {
            // Clearing unique result so it doesn't trigger again on back navigation
            vm.clearUniqueResult()
            
            val destination = RegistrationFragmentDirections.actionRegistrationFragmentToVerifyOTP(
                typeOfOperation = "registration",
                email = email,
                phone = phone,
                typeOfConnection = "email"
            )
            Log.d("RegistrationFragment", "Navigating to destination: $destination")
            findNavController().navigate(destination)
        } catch (e: Exception) {
            Log.e("RegistrationFragment", "Navigation failed: ${e.message}", e)
            binding.btnRegister.isEnabled = true
            Toast.makeText(requireContext(), "Navigation error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpOnClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (!isFillsValid(
                    fullName = binding.etFullname,
                    username = binding.etUsername,
                    email = binding.etEmail,
                    phoneNumber = binding.etPhoneNumber,
                    ccp = binding.countryCodePiker,
                    password = binding.etPassword
                )
            ) {
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            Log.d("RegistrationFragment", "Register button clicked, checking uniqueness for: ${binding.etUsername.text}, ${binding.etEmail.text}")
            vm.checkUnique(
                binding.etUsername.text.toString().trim(),
                binding.etEmail.text.toString().trim()
            )
        }

        binding.tvLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }

        binding.signupBackButtonRegistration.setOnClickListener {
            val activity = requireActivity() as MainActivity
            if (activity.sessionManager.checkLogin()) {
                activity.run {
                    findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                    navBottomViewModel.setIsContentVisible(true)
                    hideFullScreenContainer()
                }
            } else {
                requireActivity().viewModelStore.clear()
                findNavController().navigate(R.id.action_registrationFragment_to_authorizationFragment)
            }
        }
    }

    private fun isFillsValid(
        fullName: EditText,
        username: EditText,
        email: EditText,
        phoneNumber: EditText,
        password: EditText,
        ccp: CountryCodePicker
    ): Boolean {
        return isValidFullName(fullName) && isValidUsername(username) && isValidEmail(email)
                && isValidPhone(phoneEditText = phoneNumber, ccp = ccp) && isValidPassword(password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
