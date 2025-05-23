package com.paletteofflavors.logIn

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentVerifyOtpBinding


class VerifyOTP : Fragment() {

    private lateinit var _binding: FragmentVerifyOtpBinding
    private val binding get() = _binding

    private val args: VerifyOTPArgs by navArgs()

    var email: String = ""
    var phone: String = ""
    var type: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            email = args.email
            phone = args.phone
            type = args.typeOfConnection


        } catch (_: Exception) {

        }

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.typeOfVerification.text = if (type == "email") email else maskHideChars(phone)

        binding.btnVerifyCode.setOnClickListener {

            val enteredCode = binding.pinView.text.toString()
            val prefs = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

            val savedCode = prefs.getString("verification_code", null)
            val timestamp = prefs.getLong("code_timestamp", 0)

            if (isCodeValid(enteredCode, savedCode, timestamp)) {

                Toast.makeText(context, "$phone ; $email", Toast.LENGTH_LONG).show()
                val direction = VerifyOTPDirections.actionVerifyOTPToSetNewPassword(email, phone)
                findNavController().navigate(direction)
            } else {
                binding.pinView.error = "Неверный код"
            }
        }

        binding.backButtonVerifyOtp.setOnClickListener {
            if(type != ""){
                findNavController().navigate(R.id.action_verifyOTP_to_makeSelection)
            }
            else{
                findNavController().navigate(R.id.action_verifyOTP_to_registrationFragment)
            }
        }
    }

    fun maskHideChars(input: String): String {
        return when {
            input.length <= 7 -> input
            else -> input.take(5) + "*".repeat(input.length - 6) + input.takeLast(2)
        }
    }

    private fun isCodeValid(
        enteredCode: String,
        savedCode: String?,
        timestamp: Long
    ): Boolean {
        // Проверяем что код не пустой и совпадает
        if (enteredCode.isBlank() || savedCode == null) return false

        // Время действия (3 минуты)
        val isExpired = System.currentTimeMillis() > timestamp + 3 * 60 * 1000
        return enteredCode == savedCode && !isExpired
    }

}