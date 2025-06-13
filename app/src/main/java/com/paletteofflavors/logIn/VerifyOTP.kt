package com.paletteofflavors.logIn

import DataSource.API.GoogleScriptService
import DataSource.Local.SessionManager
import DataSource.Network.Turso
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
//import com.google.firebase.Firebase
//import com.google.firebase.FirebaseException
//import com.google.firebase.FirebaseTooManyRequestsException
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.PhoneAuthCredential
//import com.google.firebase.auth.PhoneAuthOptions
//import com.google.firebase.auth.PhoneAuthProvider
//import com.google.firebase.auth.auth
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentVerifyOtpBinding
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import com.paletteofflavors.logIn.viewmodels.RegistrationViewModel
import com.paletteofflavors.utils.maskHideChars
import java.util.concurrent.TimeUnit
import com.paletteofflavors.utils.verifyCode
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import tech.turso.libsql.Libsql


class VerifyOTP : Fragment() {

    private lateinit var _binding: FragmentVerifyOtpBinding
    private val binding get() = _binding

    private val args: VerifyOTPArgs by navArgs()

    var email: String = ""
    var phone: String = ""
    var type: String = ""
    var typeOper: String = ""


    private lateinit var sessionManager: SessionManager
    private var verificationCode = ""
    private var timer: CountDownTimer? = null

    private lateinit var vm: LoginViewModel
    private lateinit var vmRegister: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        vm = (requireActivity() as MainActivity).viewModel
        vmRegister = (requireActivity() as MainActivity).viewModelRegistration


        try {
            email = args.email
            phone = args.phone
            type = args.typeOfConnection
            typeOper = args.typeOfOperation
            if(typeOper == "reset")
                vm.setTypeOfVerification(type)

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
        binding.tvResendCode.isVisible = false
        binding.tvResendCode.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Null", "3")

        //Toast.makeText(requireContext(), "${vmRegister.email.value}", Toast.LENGTH_SHORT).show()
        if(vm?.resetemail?.value != ""){
            vm.typeOfVerification.observe(viewLifecycleOwner){
                binding.typeOfVerification.text = when (vm.typeOfVerification.value) {
                    "email" -> vm.resetemail.value
                    "phone" -> maskHideChars(vm.resetphone.value!!)
                    else -> {""}
                }
            }
        }

        else{
            vmRegister!!.email.observe(viewLifecycleOwner){
                binding.typeOfVerification.text = it
            }
        }

        Log.d("Null", "4")



        sessionManager = SessionManager(requireContext(), SessionManager.SESSION_CODE)

        if (vm?.typeOfVerification?.value == "email" || vmRegister?.email?.value != null) {
            sendVerificationCodeOnEMail()
        } else {
            //binding.typeOfVerification.text = maskHideChars(phone)
            //phone
        }

        Log.d("Null", "5")


        binding.btnVerifyCode.setOnClickListener {

            val savedCode = sessionManager.getVerificationCodeSessionDetails() ?: ""
            when (verifyCode(binding.pinView.text.toString(), savedCode)) {
                true -> onVerificationSuccess()
                false -> onVerificationFailed(exception = null)
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
            if (vmRegister?.email?.value == null) {
                vm.setTypeOfVerification("")
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
        binding.tvResendCode.setTextColor(R.color.gray)

        timer = object : CountDownTimer(
            sessionManager.getVerificationCodeTimer()-System.currentTimeMillis(), 1000
        ) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt().toString()
                binding.tvResendCode.text = "Отправить код повторно через: $secondsRemaining с"
            }

            override fun onFinish() {
                binding.tvResendCode.text = "Отправить код повторно"
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.setTextColor(R.color.blue) //TODO: is not working
                sessionManager.clearVerificationCodeTimer()
            }
        }.start()
    }

    private fun sendVerificationCodeOnEMail() {

        val baseUrl =
            "https://script.google.com/macros/s/AKfycbyMdw5WXBcXs13Igoi3wE5PTR3OszGlqsyUH4F3n4c5w0Ntqm2heBVx3n9L2L6rS2Hw/" // Базовый URL для Google Scripts

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val googleScriptService = retrofit.create(GoogleScriptService::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { // Выполняем сетевой запрос в IO потоке
                try {
                    val response = googleScriptService.executeScript(email).execute()
                    if (response.isSuccessful) {
                        val verificationCode = response.body()

                        sessionManager.createCodeVerificationSession(verificationCode!!)

                        withContext(Dispatchers.Main) { // Возвращаемся в главный поток для UI
                            Toast.makeText(context, "Код отправлен на почту", Toast.LENGTH_SHORT).show()
                            startTimer()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.typeOfVerification.text = response.errorBody()?.string()
                            Toast.makeText(
                                context,
                                "Ошибка отправки кода: $email",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("TAG", "Ошибка отправки кода: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Ошибка подключения: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private fun onVerificationSuccess() {
        Toast.makeText(context, "Верификация успешна!", Toast.LENGTH_SHORT).show()

        if(vmRegister?.email?.value == null){
            val direction = VerifyOTPDirections.actionVerifyOTPToSetNewPassword(email, phone)
            findNavController().navigate(direction)
        }
        else{
            val TursoConnection = Turso(requireActivity() as MainActivity, requireContext())
            TursoConnection.registerUser(vmRegister!!.fullName.value!!, vmRegister!!.userName.value!!, vmRegister!!.phone.value!!,
                vmRegister!!.email.value!!, vmRegister!!.password.value!!, findNavController())
        }
    }

    private fun onVerificationFailed(exception: Exception?) {
        Toast.makeText(context, "Неверный код подтверждения", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        timer?.cancel()
        super.onDestroyView()
    }




    // FireBase Realization
    /*

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

        sendVerificationCodeOnPhone(phone)



    private fun sendVerificationCodeOnPhone(phone: String) {

        verificationCode = generateVerificationCode()

        val auth = Firebase.auth



        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)

        resendToken?.let { optionsBuilder.setForceResendingToken(it) }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            storedVerificationId = verificationId
            resendToken = token

            // Можно добавить уведомление пользователя:
            Toast.makeText(context, "Код отправлен", Toast.LENGTH_SHORT).show()
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            // Автоматическая верификация через SMS или Play Services
            credential.smsCode?.let { code ->
                binding.pinView.setText(code) // Автозаполнение поля ввода
                verifyCode(code)          // Автоматическая проверка кода
            } ?: run {
                signInWithPhoneAuthCredential(credential)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Неверный формат номера"
                is FirebaseTooManyRequestsException -> "Слишком много запросов"
                else -> "Ошибка верификации: ${e.localizedMessage}"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyCode(userEnteredCode: String) {

        val verificationId = storedVerificationId ?: run {
            Toast.makeText(context, "Сначала запросите код подтверждения", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(verificationId, userEnteredCode)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Успешная аутентификация
                    onVerificationSuccess()
                } else {
                    // Обработка ошибок
                    onVerificationFailed(task.exception)
                }
            }
    }

    private fun onVerificationSuccess() {
        Toast.makeText(context, "Верификация успешна!", Toast.LENGTH_SHORT).show()

        val direction = VerifyOTPDirections.actionVerifyOTPToSetNewPassword(email, phone)
        findNavController().navigate(direction)
    }

    private fun onVerificationFailed(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Неверный код подтверждения"
            else -> "Ошибка аутентификации: ${exception?.localizedMessage ?: "Неизвестная ошибка"}"
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    */
}