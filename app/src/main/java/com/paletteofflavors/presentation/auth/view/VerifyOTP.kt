package com.paletteofflavors.presentation.auth.view

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.R
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.data.remote.api.googleScript.GoogleScriptService
import com.paletteofflavors.databinding.FragmentVerifyOtpBinding
import com.paletteofflavors.domain.utils.maskHideChars
import com.paletteofflavors.domain.utils.verifyCode
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.presentation.auth.viewmodel.RegistrationViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class VerifyOTP : Fragment() {

    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!

    private val args: VerifyOTPArgs by navArgs()

    private var email: String = ""
    private var phone: String = ""
    private var type: String = ""
    private var typeOper: String = ""

    private lateinit var sessionManager: SessionManager
    private var timer: CountDownTimer? = null

    private var _vm: LoginViewModel? = null
    private val vm get() = _vm!!

    private var _vmRegister: RegistrationViewModel? = null
    private val vmRegister get() = _vmRegister!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        binding.tvResendCode.isVisible = false
        binding.tvResendCode.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTypeOfConnection()
        setUpCodeSession()
        setOnClickListeners()
        observeRegistrationResult()
    }

    private fun observeRegistrationResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vmRegister.registrationResult.collect { result ->
                    if (result == null) return@collect
                    
                    binding.btnVerifyCode.isEnabled = true
                    result.onSuccess { success ->
                        if (success) {
                            handleRegistrationSuccess()
                        } else {
                            Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Registration error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleRegistrationSuccess() {
        val activity = requireActivity() as MainActivity
        if (activity.sessionManager.checkLogin()) {
            findNavController().navigate(R.id.action_verifyOTP_to_loginFragment)
            activity.binding.appContent.visibility = View.VISIBLE
            activity.navBottomViewModel.setIsContentVisible(true)
            Log.d("Registration", "New account successful registered")
        } else {
            Log.d("Registration", "Registration successful")
            findNavController().navigate(R.id.action_verifyOTP_to_loginFragment)
        }
        vmRegister.clearResults()
    }

    private fun getData() {
        val activity = requireActivity() as MainActivity
        _vm = activity.loginViewModel
        _vmRegister = activity.viewModelRegistration

        try {
            email = args.email
            phone = args.phone
            type = args.typeOfConnection
            typeOper = args.typeOfOperation
            if (typeOper == "reset")
                vm.setTypeOfConnection(type)
        } catch (_: Exception) {}
    }

    private fun showTypeOfConnection() {
        if (vm.resetemail.value != null) {
            binding.typeOfVerification.text = when (vm.typeOfVConnection.value) {
                "email" -> vm.resetemail.value
                "phone" -> maskHideChars(vm.resetphone.value!!)
                else -> ""
            }
        } else {
            binding.typeOfVerification.text = vmRegister.email.value.toString()
        }
    }

    private fun setUpCodeSession() {
        sessionManager = SessionManager(requireContext(), SessionManager.SESSION_CODE)
        if (vm.typeOfVConnection.value == "email" || vmRegister.email.value != null) {
            sendVerificationCodeOnEMail()
        }
    }

    private fun setOnClickListeners() {
        binding.btnVerifyCode.setOnClickListener {
            val savedCode = sessionManager.getVerificationCodeSessionDetails() ?: ""
            if (verifyCode(binding.pinView.text.toString(), savedCode)) {
                onVerificationSuccess()
            } else {
                onVerificationFailed()
            }
        }

        binding.tvResendCode.setOnClickListener {
            if (!sessionManager.isVerificationCodeTimerRunning()) {
                timer?.cancel()
                sendVerificationCodeOnEMail()
            }
        }

        binding.backButtonVerifyOtp.setOnClickListener {
            if (vm.resetemail.value != null) {
                vm.setTypeOfConnection("")
                findNavController().navigate(R.id.action_verifyOTP_to_makeSelection)
            } else {
                findNavController().navigate(R.id.action_verifyOTP_to_registrationFragment)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun startTimer() {
        binding.tvResendCode.isVisible = true
        binding.tvResendCode.isEnabled = false
        binding.tvResendCode.setTextColor(requireContext().getColor(R.color.gray))

        timer = object : CountDownTimer(
            sessionManager.getVerificationCodeTimer() - System.currentTimeMillis(), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt().toString()
                binding.tvResendCode.text = getString(R.string.Post_code_again_after_seconds, secondsRemaining)
            }

            override fun onFinish() {
                binding.tvResendCode.text = getString(R.string.Post_code_again)
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.setTextColor(requireContext().getColor(R.color.blue))
                sessionManager.clearVerificationCodeTimer()
            }
        }.start()
    }

    private fun sendVerificationCodeOnEMail() {
        val baseUrl = "https://script.google.com/macros/s/AKfycbyMdw5WXBcXs13Igoi3wE5PTR3OszGlqsyUH4F3n4c5w0Ntqm2heBVx3n9L2L6rS2Hw/"
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val googleScriptService = retrofit.create(GoogleScriptService::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = googleScriptService.executeScript(email).execute()
                    if (response.isSuccessful) {
                        val verificationCode = response.body()
                        sessionManager.createCodeVerificationSession(verificationCode!!)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, getString(R.string.Code_post_on_email), Toast.LENGTH_SHORT).show()
                            startTimer()
                        }
                    } else {
                        response.errorBody()?.string()?.let { Log.d("VerifyOTP", it) }
                        withContext(Dispatchers.Main) {
                            binding.typeOfVerification.text = response.errorBody()?.string()
                            Toast.makeText(context, getString(R.string.Error_when_send_code, email), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, getString(R.string.Connection_error, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun onVerificationSuccess() {
        Log.d("VerifyOTP", "onVerificationSuccess called. email=${vmRegister.email.value}")
        Toast.makeText(context, getString(R.string.success_verification), Toast.LENGTH_SHORT).show()
        if (vm.resetemail.value != null) {
            Log.d("VerifyOTP", "Navigating to setNewPassword")
            val direction = VerifyOTPDirections.actionVerifyOTPToSetNewPassword(email, phone)
            findNavController().navigate(direction)
        } else if (vmRegister.email.value != null) {
            Log.d("VerifyOTP", "Triggering vmRegister.register()")
            binding.btnVerifyCode.isEnabled = false
            vmRegister.register()
        } else {
            Log.e("VerifyOTP", "onVerificationSuccess: email is null, cannot register!")
        }
    }

    private fun onVerificationFailed() {
        Toast.makeText(context, getString(R.string.invalid_verification_code), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
        _vm = null
        _vmRegister = null
    }
}
