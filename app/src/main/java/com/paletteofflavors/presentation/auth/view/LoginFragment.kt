package com.paletteofflavors.presentation.auth.view

import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.data.remote.API.Turso.Turso
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentLoginBinding
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    lateinit var vm: LoginViewModel
    private lateinit var rememberMe: CheckBox

    private var isUpdatingFromViewModel = false

    private lateinit var username: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        // Check phone_number and password are saved already in Shared Preferences
        checkRememberMeSessionAndBind(activity)

        // ViewModel interaction
        setUpLoginViewModelObservers(activity)

        // Set up all listeners
        setUpListeners()
    }

    private fun setUpListeners() {
        // ViewModel interaction
        binding.etLoginUsername.doAfterTextChanged{
                editable ->
            if(!isUpdatingFromViewModel)
                editable?.toString()?.let { vm.setUserName(it) }
        }

        binding.etLoginPassword.doAfterTextChanged{
                editable -> if(!isUpdatingFromViewModel) editable?.toString()?.let { vm.setPassword(it) }
        }

        // Buttons onClick
        binding.forgetPassword.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_forgetPassword)
        }

        binding.signupBackButtonLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_authorizationFragment)
        }

        binding.tvRegistration.setOnClickListener{
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        binding.btnLogin.setOnClickListener {

            it.isEnabled = false

            username = binding.etLoginUsername.text.toString().trim()
            password = binding.etLoginPassword.text.toString().trim()
            rememberMe = binding.rememberMe


            // Check fields valid
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                it.isEnabled = true

                return@setOnClickListener
            }

            // Check internet connection
            val tursoConection = Turso(requireActivity() as MainActivity, requireContext())
            if(!tursoConection.checkInternetConnection(requireContext())){
                it.isEnabled = true
                return@setOnClickListener
            }


            // loginUser(username, password) if valid and internet connection is on
            val TursoConnection = Turso(requireActivity() as MainActivity, requireContext(), rememberMe)
            TursoConnection.loginUser(username, password, binding.rememberMe.isChecked)
            //TODO: progress bar

            it.isEnabled = true
        }
    }

    private fun setUpLoginViewModelObservers(activity: MainActivity) {
        vm = activity.viewModel

        vm.username.observe(viewLifecycleOwner){
                newText ->
            if(binding.etLoginUsername.text.toString() != newText){
                isUpdatingFromViewModel = true
                binding.etLoginUsername.setText(newText)
                isUpdatingFromViewModel = false
            }
        }

        vm.password.observe(viewLifecycleOwner){
                newText ->
            if(binding.etLoginPassword.text.toString() != newText){
                isUpdatingFromViewModel = true
                binding.etLoginPassword.setText(newText)
                isUpdatingFromViewModel = false
            }
        }
    }

    private fun checkRememberMeSessionAndBind(activity: MainActivity) {
        activity.sessionManagerRememberMe = SessionManager(requireContext(), SessionManager.SESSION_REMEMBERME)
        if(activity.sessionManagerRememberMe.checkRememberMe()){
            val rememberMeDetails: HashMap<String, String?> = activity.sessionManagerRememberMe.getRememberMeDetailsFromSession()

            binding.run {
                etLoginPassword.setText(rememberMeDetails[SessionManager.KEY_SESSION_PASSWORD])
                etLoginUsername.setText(rememberMeDetails[SessionManager.KEY_SESSION_USERNAME])
                rememberMe.isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
