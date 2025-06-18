package com.paletteofflavors.presentation.auth.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentMakeSelectionBinding
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.domain.utils.maskHideChars


class MakeSelection : Fragment() {

    //Save Arg navigation
    private val args: MakeSelectionArgs by navArgs()

    var email: String = ""
    var phone: String = ""

    private var _binding: FragmentMakeSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMakeSelectionBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservers()
        setUpListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun setUpViewModel() {
        vm = (requireActivity() as MainActivity).viewModel
        try {
            email = args.email
            phone = args.phone
            vm.setResetEmail(email)
            vm.setResetPhone(phone)
        } catch (_: Exception) {

        }
    }

    private fun setUpObservers() {
        vm.resetphone.observe(viewLifecycleOwner){
            binding.currentPhone.text = maskHideChars(it)
        }
        vm.resetemail.observe(viewLifecycleOwner){
            binding.currentEmail.text = it
        }
    }

    private fun setUpListeners() {
        binding.viaEmail.setOnClickListener {
            val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "email", "reset")
            findNavController().navigate(destination)
        }

        binding.viaPhoneNumber.setOnClickListener {
            val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "phone", "reset")
            findNavController().navigate(destination)
        }

        binding.backButtonMakeSelection.setOnClickListener {
            vm.setResetPhone("")
            vm.setResetEmail("")
            findNavController().navigate(R.id.action_makeSelection_to_forgetPassword)
        }
    }

}