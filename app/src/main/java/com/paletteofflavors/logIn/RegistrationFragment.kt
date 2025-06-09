package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.paletteofflavors.databinding.FragmentRegistrationBinding
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.hbb20.CountryCodePicker
import com.paletteofflavors.MainActivity
import com.paletteofflavors.ProfileFragment
import com.paletteofflavors.R
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import com.paletteofflavors.logIn.viewmodels.RegistrationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

//TODO: Add ViewModel with liveData
class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    lateinit var vm: RegistrationViewModel


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


        binding.btnRegister.setOnClickListener {

            // Check validation for all fills
            if (!isFillsValid(fullName =  binding.etFullname, username = binding.etUsername, email = binding.etEmail,
                phoneNumber = binding.etPhoneNumber, ccp = binding.countryCodePiker,
                    password = binding.etPassword)) {
                return@setOnClickListener
            }

            // If success start registration process

            //val fullname = binding.etFullname.text.toString().trim()
            //val username = binding.etUsername.text.toString().trim()
            //val email = binding.etEmail.text.toString().trim()
            //val phone_number = binding.countryCodePiker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim()
            ///val password = binding.etPassword.text.toString().trim()
            //registerUser(fullname, username, phone_number, email, password)

            try{
                vm.setFullName(binding.etFullname.text.toString().trim())
                vm.setUserName(binding.etUsername.text.toString().trim())
                vm.setEmail(binding.etEmail.text.toString().trim())
                vm.setPhone(binding.countryCodePiker.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim())
                vm.setPassword(binding.etPassword.text.toString().trim())
            }
            catch (e: Error){
                Log.d("setError", "val Error: $e")
            }


            try {
                val destination = RegistrationFragmentDirections.actionRegistrationFragmentToVerifyOTP("registration", email = vm.email.value!!, phone = vm.phone.value!!,"email")
                findNavController().navigate(destination)
            } catch (e: Error){
                Log.d("setError", "Nav Erorr: $e")
            }

        }

        binding.tvLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }


        binding.signupBackButtonRegistration.setOnClickListener {

            val activity = requireActivity() as MainActivity

            if(activity.sessionManager.checkLogin()){
                activity.findNavController(R.id.fragmentContainerView).navigate(R.id.action_registrationFragment_to_loginFragment)
                activity.binding.appContent.visibility = View.VISIBLE
                activity.navBottomViewModel.setIsContentVisible(true)
            }
            else{
                requireActivity().viewModelStore.clear()
                findNavController().navigate(R.id.action_registrationFragment_to_authorizationFragment)
            }

        }

    }


    private fun isFillsValid(fullName: EditText, username: EditText, email: EditText, phoneNumber: EditText, password: EditText, ccp: CountryCodePicker): Boolean {

        return isValidFullName(fullName) && isValidUsername(username) && isValidEmail(email)
                && isValidPhone(phoneEditText = phoneNumber, ccp = ccp) && isValidPassword(password)
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}