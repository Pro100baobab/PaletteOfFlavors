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
import android.util.Patterns
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import androidx.core.content.edit


object EmailSender {
    fun sendVerificationEmail(context: Context, email: String, code: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Код подтверждения")
                putExtra(Intent.EXTRA_TEXT, "Ваш код подтверждения: $code")
            }

            context.startActivity(Intent.createChooser(intent, "Отправить код через"))
            true
        } catch (e: Exception) {
            false
        }
    }
}


class MakeSelection : Fragment() {

    //Save Arg navigation
    private val args: MakeSelectionArgs by navArgs()

    private lateinit var _binding: FragmentMakeSelectionBinding
    private val binding get() = _binding


    private var verificationCode = ""

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
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Add *** for phone_number on the next layout

        binding.currentEmail.text = email
        binding.currentPhone.text = maskHideChars(phone)

        binding.viaEmail.setOnClickListener {

            verificationCode = generateVerificationCode()

            if (EmailSender.sendVerificationEmail(requireContext(), email, verificationCode)) {
                saveCodeLocally(email, verificationCode)

                val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "email")
                findNavController().navigate(destination)
            }

        }

        //TODO: make sending of verification on phone_number

        binding.viaPhoneNumber.setOnClickListener {

            verificationCode = generateVerificationCode()

            if (EmailSender.sendVerificationEmail(requireContext(), email, verificationCode)) {
                saveCodeLocally(email, verificationCode)

                val destination = MakeSelectionDirections.actionMakeSelectionToVerifyOTP(email, phone, "phone")
                findNavController().navigate(destination)
            }
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
    fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }

    private fun saveCodeLocally(email: String, code: String) {
        val prefs = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        prefs.edit() {
            putString("verification_email", email)
                .putString("verification_code", code)
                .putLong("code_timestamp", System.currentTimeMillis())
        }
    }

}