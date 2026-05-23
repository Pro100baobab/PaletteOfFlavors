package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentSetNewPasswordBinding
import com.paletteofflavors.domain.utils.validationData.isValidPassword
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch

class SetNewPassword : Fragment() {

    private var _binding: FragmentSetNewPasswordBinding? = null
    private val binding get() = _binding!!

    private val args: SetNewPasswordArgs by navArgs()

    private var email = ""
    private var phone = ""
    
    private lateinit var vm: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (requireActivity() as MainActivity).loginViewModel
        getNavArgsData()
        setUpListeners()
        observeResetPasswordResult()
    }

    private fun getNavArgsData() {
        try {
            email = args.email
            phone = args.phone
        } catch (_: Exception) {}
    }

    private fun observeResetPasswordResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.resetPasswordResult.collect { result ->
                    result.onSuccess { success ->
                        if (success) {
                            binding.confirmChangeInPassword.isEnabled = true
                            findNavController().navigate(R.id.action_setNewPassword_to_passwordSuccessUpdated)
                            vm.clearResults()
                        } else if (vm.resetPasswordResult.value.getOrNull() == false && binding.confirmChangeInPassword.isEnabled == false) {
                            binding.confirmChangeInPassword.isEnabled = true
                            Toast.makeText(requireContext(), "Password update failed", Toast.LENGTH_LONG).show()
                        }
                    }.onFailure { e ->
                        binding.confirmChangeInPassword.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.confirmChangeInPassword.setOnClickListener {
            val password = binding.newPassword.text.toString().trim()
            if (password == binding.confirmNewPassword.text.toString().trim() && isValidPassword(binding.newPassword)) {
                binding.confirmChangeInPassword.isEnabled = false
                vm.setNewPassword(email, phone, password.hashCode())
            } else if (password != binding.confirmNewPassword.text.toString().trim()) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButtonSetNewPassword.setOnClickListener {
            findNavController().navigate(R.id.action_setNewPassword_to_verifyOTP)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
