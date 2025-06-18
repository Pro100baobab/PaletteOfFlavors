package com.paletteofflavors.logIn

import DataSource.API.GoogleScriptService
import DataSource.Local.SessionManager
import DataSource.Network.Turso
import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentVerifyOtpBinding
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import com.paletteofflavors.logIn.viewmodels.RegistrationViewModel
import com.paletteofflavors.utils.maskHideChars
import com.paletteofflavors.utils.verifyCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


class VerifyOTP : Fragment() {

    private lateinit var _binding: FragmentVerifyOtpBinding
    private val binding get() = _binding

    private val args: VerifyOTPArgs by navArgs()

    var email: String = ""
    var phone: String = ""
    var type: String = ""   // Connection
    var typeOper: String = ""


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
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //logCatCheck()
        showTypeOfConnection()
        setUpCodeSession()
        setOnClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _vm = null
        _vmRegister = null
    }

    private fun showTypeOfConnection(){
        if(vm.resetemail.value != null){

            binding.typeOfVerification.text = when (vm.typeOfVConnection.value) {
                "email" -> vm.resetemail.value
                "phone" -> maskHideChars(vm.resetphone.value!!)
                else -> {
                    ""
                }
            }
        }

        else{
            binding.typeOfVerification.text = vmRegister?.email?.value.toString()
        }
    }

    private fun setUpCodeSession(){
        sessionManager = SessionManager(requireContext(), SessionManager.SESSION_CODE)

        if (vm.typeOfVConnection.value == "email" || vmRegister?.email?.value != null) {
            sendVerificationCodeOnEMail()
        } else {
            //binding.typeOfVerification.text = maskHideChars(phone)
            //phone
        }
    }

    private fun setOnClickListeners(){
        binding.btnVerifyCode.setOnClickListener {

            val savedCode = sessionManager.getVerificationCodeSessionDetails() ?: ""
            when (verifyCode(binding.pinView.text.toString(), savedCode)) {
                true -> onVerificationSuccess()
                false -> onVerificationFailed()
            }
        }

        binding.tvResendCode.setOnClickListener {
            if(!sessionManager.isVerificationCodeTimerRunning()){
                timer?.cancel()

                sendVerificationCodeOnEMail()

                /*if (vm.typeOfVerification.value == "email") {
                    sendVerificationCodeOnEMail()
                } else {
                    //phone
                }*/
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


    private fun getData(){
        _vm = (requireActivity() as MainActivity).viewModel
        try{
            _vmRegister = (requireActivity() as MainActivity).viewModelRegistration
        } catch (_: Exception){

        }


        try {
            email = args.email
            phone = args.phone
            type = args.typeOfConnection
            typeOper = args.typeOfOperation
            if(typeOper == "reset")
                vm.setTypeOfConnection(type)

        } catch (_: Exception) {

        }
    }


    @SuppressLint("ResourceAsColor")
    private fun startTimer() {

        binding.tvResendCode.isVisible = true
        binding.tvResendCode.isEnabled = false
        binding.tvResendCode.setTextColor(R.color.gray)

        timer = object : CountDownTimer(
            sessionManager.getVerificationCodeTimer()-System.currentTimeMillis(), 1000
        ) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt().toString()
                binding.tvResendCode.text =
                    getString(R.string.Post_code_again_after_seconds, secondsRemaining)
            }

            override fun onFinish() {
                binding.tvResendCode.text = getString(R.string.Post_code_again)
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.setTextColor(R.color.blue)
                sessionManager.clearVerificationCodeTimer()
            }
        }.start()
    }

    // Functions for sending code and show result of matching
    private fun sendVerificationCodeOnEMail() {

        val baseUrl =
            "https://script.google.com/macros/s/AKfycbyMdw5WXBcXs13Igoi3wE5PTR3OszGlqsyUH4F3n4c5w0Ntqm2heBVx3n9L2L6rS2Hw/"

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
                            Toast.makeText(context,
                                getString(R.string.Code_post_on_email), Toast.LENGTH_SHORT).show()
                            startTimer()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.typeOfVerification.text = response.errorBody()?.string()
                            Toast.makeText(
                                context,
                                getString(R.string.Error_when_send_code, email),
                                Toast.LENGTH_SHORT
                            ).show()
                            //Log.d("TAG", "Ошибка отправки кода: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            getString(R.string.Connection_error, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private fun onVerificationSuccess() {
        Toast.makeText(context, getString(R.string.success_verification), Toast.LENGTH_SHORT).show()

        if(vm?.resetemail?.value != null){
            val direction = VerifyOTPDirections.actionVerifyOTPToSetNewPassword(email, phone)
            findNavController().navigate(direction)
        }
        else if(vmRegister?.email?.value != null){
            val TursoConnection = Turso(requireActivity() as MainActivity, requireContext())
            TursoConnection.registerUser(vmRegister.fullName.value!!, vmRegister.userName.value!!, vmRegister.phone.value!!,
                vmRegister.email.value!!, vmRegister.password.value!!, findNavController())
        }
    }

    private fun onVerificationFailed() {
        Toast.makeText(context, getString(R.string.invalid_verification_code), Toast.LENGTH_SHORT).show()
    }



    // For debugging
    @Suppress("unused")
    private fun logCatCheck() {
        Log.d("ViewModel", "NavArgs Email: ${args.email}")
        Log.d("ViewModel", "NavArgs Phone: ${args.phone}")
        Log.d("ViewModel", "NavArgs TypeOfOperation: ${args.typeOfOperation}")
        Log.d("ViewModel", "NavArgs TypeOfConnection: ${args.typeOfConnection}\n\n")

        Log.d("ViewModel", "")
        Log.d("ViewModel", "")


        vm?.apply {
            Log.d("ViewModel", "LoginView Username: ${vm.username.value}")
            Log.d("ViewModel", "LoginView CurrentEmail: ${vm.curemail.value}")
            Log.d("ViewModel", "LoginView Password: ${vm.password.value}")
            Log.d("ViewModel", "LoginView ResetEmail: ${vm.resetemail.value}")
            Log.d("ViewModel", "LoginView ResetPhone: ${vm.resetphone.value}")
            Log.d("ViewModel", "LoginView typeOfVerification: ${vm.typeOfVConnection.value}")
            Log.d("ViewModel", "LoginView TypeOfConnection: нет")

            Log.d("ViewModel", "")
            Log.d("ViewModel", "")
        }

        vmRegister?.apply {
            Log.d("ViewModel", "Register FullName: ${vmRegister.fullName.value}")
            Log.d("ViewModel", "Register Username: ${vmRegister.userName.value}")
            Log.d("ViewModel", "Register Password: ${vmRegister.password.value}")
            Log.d("ViewModel", "Register Email: ${vmRegister.email.value}")
            Log.d("ViewModel", "Register Phone: ${vmRegister.phone.value}")
            Log.d("ViewModel", "Register TypeOfConnection: нет")

            Log.d("ViewModel", "")
            Log.d("ViewModel", "")

        }
    }
}