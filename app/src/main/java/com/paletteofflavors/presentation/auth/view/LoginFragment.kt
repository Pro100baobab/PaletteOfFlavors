package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
import android.util.Log
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
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.databinding.FragmentLoginBinding
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.presentation.feature.main.view.SearchFragment
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: LoginViewModel
    private var isUpdatingFromViewModel = false

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
        vm = activity.loginViewModel

        checkRememberMeSessionAndBind(activity)
        setUpLoginViewModelObservers()
        setUpListeners()
        observeLoginResult()
    }

    private fun setUpListeners() {
        binding.etLoginUsername.doAfterTextChanged { editable ->
            if (!isUpdatingFromViewModel) {
                editable?.toString()?.let { vm.setUserName(it) }
            }
        }

        binding.etLoginPassword.doAfterTextChanged { editable ->
            if (!isUpdatingFromViewModel) {
                editable?.toString()?.let { vm.setPassword(it) }
            }
        }

        binding.forgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPassword)
        }

        binding.signupBackButtonLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_authorizationFragment)
        }

        binding.tvRegistration.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etLoginUsername.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            Log.d("LoginFragment", "Login button clicked for username: $username")

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            vm.login(username, password.hashCode())
        }
    }

    private fun observeLoginResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.loginResult.collect { result ->
                    Log.d("LoginFragment", "loginResult collected: $result")
                    val user = result.getOrNull()
                    if (user != null) {
                        Log.d("LoginFragment", "Login success for ${user.username}")
                        binding.btnLogin.isEnabled = true
                        val activity = requireActivity() as MainActivity
                        handleSuccessfulLogin(activity, user)
                    } else if (result.isSuccess) {
                        // Initial state or user not found
                        if (result.getOrNull() == null && binding.btnLogin.isEnabled == false) {
                            Log.d("LoginFragment", "Login failed: Invalid credentials")
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    } else if (result.isFailure) {
                        val error = result.exceptionOrNull()
                        Log.e("LoginFragment", "Login failed: ${error?.message}", error)
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(requireContext(), "Login failed: ${error?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessfulLogin(activity: MainActivity, user: com.paletteofflavors.domain.model.User) {
        // LogIn Session
        activity.sessionManager = SessionManager(requireContext(), SessionManager.SESSION_USERSESSION)
        activity.sessionManager.createLoginSession(
            userId = user.id ?: -1,
            fullName = user.fullName,
            username = user.username,
            email = user.email,
            phoneNumber = user.phoneNumber,
            password = user.passwordHash.toString()
        )

        Log.d("Login", "Login successful: ${user.username}")

        rememberMe(user.username, binding.etLoginPassword.text.toString())

        activity.replaceMainFragment(SearchFragment())
        activity.binding.fragmentContainerView.visibility = View.VISIBLE
        activity.returnNavigation()
        
        vm.clearResults()
    }

    private fun rememberMe(username: String, password: String) {
        val activity = requireActivity() as MainActivity
        if (binding.rememberMe.isChecked) {
            activity.sessionManagerRememberMe = SessionManager(requireContext(), SessionManager.SESSION_REMEMBERME)
            activity.sessionManagerRememberMe.createRememberMeSession(username, password)
        } else {
            activity.sessionManagerRememberMe = SessionManager(requireContext(), SessionManager.SESSION_REMEMBERME)
            if (activity.sessionManagerRememberMe.checkRememberMe()) {
                activity.sessionManagerRememberMe.logoutUserSession()
            }
        }
    }

    private fun setUpLoginViewModelObservers() {
        vm.username.observe(viewLifecycleOwner) { newText ->
            if (binding.etLoginUsername.text.toString() != newText) {
                isUpdatingFromViewModel = true
                binding.etLoginUsername.setText(newText)
                isUpdatingFromViewModel = false
            }
        }

        vm.password.observe(viewLifecycleOwner) { newText ->
            if (binding.etLoginPassword.text.toString() != newText) {
                isUpdatingFromViewModel = true
                binding.etLoginPassword.setText(newText)
                isUpdatingFromViewModel = false
            }
        }
    }

    private fun checkRememberMeSessionAndBind(activity: MainActivity) {
        activity.sessionManagerRememberMe = SessionManager(requireContext(), SessionManager.SESSION_REMEMBERME)
        if (activity.sessionManagerRememberMe.checkRememberMe()) {
            val rememberMeDetails = activity.sessionManagerRememberMe.getRememberMeDetailsFromSession()
            val savedPassword = rememberMeDetails[SessionManager.KEY_SESSION_PASSWORD]
            val savedUsername = rememberMeDetails[SessionManager.KEY_SESSION_USERNAME]
            
            binding.etLoginPassword.setText(savedPassword)
            vm.setPassword(savedPassword ?: "")
            
            binding.etLoginUsername.setText(savedUsername)
            vm.setUserName(savedUsername ?: "")
            
            binding.rememberMe.isChecked = true
        } else {
            binding.rememberMe.isChecked = false
            // If RememberMe is false, ensure fields are empty (unless user already typed something)
            if (vm.username.value.isNullOrEmpty()) {
                binding.etLoginUsername.setText("")
            }
            if (vm.password.value.isNullOrEmpty()) {
                binding.etLoginPassword.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
