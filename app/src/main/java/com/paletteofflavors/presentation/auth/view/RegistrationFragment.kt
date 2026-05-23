package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
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
                    if (result == null) return@collect
                    
                    result.onSuccess { (isUnique, errorMsg) ->
                        if (isUnique) {
                            navigateToOTP()
                        } else {
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(requireContext(), errorMsg ?: "User already exists", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure { e ->
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToOTP() {
        vm.run {
            setFullName(binding.etFullname.text.toString().trim())
            setUserName(binding.etUsername.text.toString().trim())
            setEmail(binding.etEmail.text.toString().trim())
            setPhone(binding.countryCodePiker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim())
            setPassword(binding.etPassword.text.toString().trim())
        }

        val destination = RegistrationFragmentDirections.actionRegistrationFragmentToVerifyOTP(
            "registration",
            email = vm.email.value!!,
            phone = vm.phone.value!!,
            "email"
        )
        findNavController().navigate(destination)
        vm.clearResults()
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
