package com.paletteofflavors.presentation.auth.view

import com.paletteofflavors.data.remote.API.Turso.Turso
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paletteofflavors.databinding.FragmentRegistrationBinding
import android.util.Log
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.hbb20.CountryCodePicker
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.presentation.auth.viewmodel.RegistrationViewModel
import com.paletteofflavors.domain.utils.validationData.isValidEmail
import com.paletteofflavors.domain.utils.validationData.isValidFullName
import com.paletteofflavors.domain.utils.validationData.isValidPassword
import com.paletteofflavors.domain.utils.validationData.isValidPhone
import com.paletteofflavors.domain.utils.validationData.isValidUsername
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

        (requireActivity() as MainActivity).viewModelRegistration = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        vm = (requireActivity() as MainActivity).viewModelRegistration

        if((requireActivity() as MainActivity).sessionManager.checkLogin()){
            binding.tvLogin.isVisible = false
        }

        setUpOnClickListeners()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setUpOnClickListeners() {

        binding.btnRegister.setOnClickListener {

            // Check validation for all fills
            if (!isFillsValid(fullName =  binding.etFullname, username = binding.etUsername, email = binding.etEmail,
                    phoneNumber = binding.etPhoneNumber, ccp = binding.countryCodePiker,
                    password = binding.etPassword)) {
                return@setOnClickListener
            }

            val tursoConnection = Turso(requireActivity() as MainActivity, requireContext())

            lifecycleScope.launch {

                // CHeck Unique username and email address
                val isUnique = tursoConnection.checkUniqueUsernameAndEmail(
                    binding.etUsername.text.toString().trim(),
                    binding.etEmail.text.toString().trim()
                )
                if (!isUnique) {
                    return@launch
                }

                // If unique
                try {

                    // Заполняем ViewModel
                    vm.run {
                        setFullName(binding.etFullname.text.toString().trim())
                        setUserName(binding.etUsername.text.toString().trim())
                        setEmail(binding.etEmail.text.toString().trim())
                        setPhone(binding.countryCodePiker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim())
                        setPassword(binding.etPassword.text.toString().trim())
                    }

                    // Переходим к следующему экрану
                    val destination = RegistrationFragmentDirections.actionRegistrationFragmentToVerifyOTP(
                        "registration",
                        email = vm.email.value!!,
                        phone = vm.phone.value!!,
                        "email"
                    )
                    findNavController().navigate(destination)
                } catch (e: Error) {
                    Log.e("Registration", "Navigation error", e)
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }

        binding.signupBackButtonRegistration.setOnClickListener {

            val activity = requireActivity() as MainActivity

            if(activity.sessionManager.checkLogin()){

                activity.run{
                    findNavController(R.id.fragmentContainerView).navigate(R.id.action_registrationFragment_to_loginFragment)
                    navBottomViewModel.setIsContentVisible(true)
                    hideFullScreenContainer()
                }
            }
            else{
                requireActivity().viewModelStore.clear()
                findNavController().navigate(R.id.action_registrationFragment_to_authorizationFragment)
            }

        }
    }

    // Check validation
    fun isFillsValid(fullName: EditText, username: EditText, email: EditText, phoneNumber: EditText, password: EditText, ccp: CountryCodePicker): Boolean {

        return isValidFullName(fullName) && isValidUsername(username) && isValidEmail(email)
                && isValidPhone(phoneEditText = phoneNumber, ccp = ccp) && isValidPassword(password)
    }

}