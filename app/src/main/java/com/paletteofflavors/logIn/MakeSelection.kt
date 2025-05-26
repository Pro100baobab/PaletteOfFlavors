package com.paletteofflavors.logIn

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentMakeSelectionBinding

// For sending password on email
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Patterns
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MakeSelection : Fragment() {

    //Save Arg navigation
    private val args: MakeSelectionArgs by navArgs()

    private var _binding: FragmentMakeSelectionBinding? = null
    private val binding get() = _binding!!

    //private lateinit var viewModel: AuthViewModel // ViewModel



    var email: String = ""
    var phone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            email = args.email
            phone = args.phone
        } catch (_: Exception) {

        }

        super.onCreate(savedInstanceState)

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

        // Инициализация ViewModel
        //viewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        binding.currentEmail.text = email
        binding.currentPhone.text = maskHideChars(phone)


        //TODO: make sending of verification on email

        binding.viaEmail.setOnClickListener {
            //sendVerificationCode(email)

            val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "email")
            findNavController().navigate(destination)
        }


        binding.viaPhoneNumber.setOnClickListener {
            //sendVerificationCodeOnPhone(phone)

            val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "phone")
            findNavController().navigate(destination)
        }

        binding.backButtonMakeSelection.setOnClickListener {
            findNavController().navigate(R.id.action_makeSelection_to_forgetPassword)
        }
    }


    fun maskHideChars(input: String): String {
        return when {
            input.length <= 7 -> input
            else -> input.take(5) + "*".repeat(input.length - 6) + input.takeLast(2)
        }
    }


    // TODO: This is a Local realization of sending verify code. Adding of server is required.


    /*
    private fun saveCodeLocally(email: String, code: String) {
        val prefs = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        prefs.edit() {
            putString("verification_email", email)
                .putString("verification_code", code)
                .putLong("code_timestamp", System.currentTimeMillis())
        }
    }*/

    private fun sendVerificationCode(email: String) {

    }
    /*
    private fun sendVerificationCode(email: String) {

        // Запуск корутины через lifecycleScope (привязан к жизненному циклу фрагмента)
        lifecycleScope.launch {

            verificationCode = generateVerificationCode()


            viewModel.sendVerificationCode(email, verificationCode)


            viewModel.sendVerificationCode(email, verificationCode).collect { response ->

                if (response.success) {
                    Toast.makeText(requireContext(), "Код отправлен на $email", Toast.LENGTH_SHORT).show()

                    val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "phone")
                    findNavController().navigate(destination)
                } else {
                    Toast.makeText(requireContext(), "Ошибка: ${response.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}