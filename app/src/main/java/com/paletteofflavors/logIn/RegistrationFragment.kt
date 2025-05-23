package com.paletteofflavors.logIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.paletteofflavors.databinding.FragmentRegistrationBinding
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

//TODO: Add ViewModel with liveData
class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnRegister.setOnClickListener {


            // Check validation for all fills
            if (!isFillsValid(fullName =  binding.etFullname, username = binding.etUsername, email = binding.etEmail,
                phoneNumber = binding.etPhoneNumber, password = binding.etPassword)) {
                return@setOnClickListener
            }

            // If success start registration process
            val fullname = binding.etFullname.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone_number = binding.etPhoneNumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            registerUser(fullname, username, phone_number, email, password)
        }

        binding.tvLogin.setOnClickListener {
            //Toast.makeText(requireContext(), "VALUES('$username', '${password.hashCode()}')", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }


        binding.signupBackButtonRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_authorizationFragment)
        }

    }


    private fun isFillsValid(fullName: EditText, username: EditText, email: EditText, phoneNumber: EditText, password: EditText): Boolean {

        return isValidFullName(fullName) && isValidUsername(username) && isValidEmail(email)
                && isValidPhone(phoneNumber) && isValidPassword(password)
    }



    //TODO: Change datatype of phone_number and ui for this view.

    //TODO: Check that email and phone number are unique.
    private fun registerUser(fullname: String, username: String, phone_number: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        // Проверяем, существует ли пользователь
                        conn.query("SELECT username FROM users WHERE username = '$username'").use { rows ->
                            if (rows.nextRow() != null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "Username already exists", Toast.LENGTH_SHORT).show()
                                }
                                return@use
                            }
                        }

                        // TODO: Alter table users in turso for default CURRENT_TIMESTAMP
                        // Регистрируем нового пользователя
                        conn.query(
                            "INSERT INTO users (fullname, username, email, phone_number, password, created_at) VALUES('$fullname','$username', '$email', '$phone_number', '${password.hashCode()}', CURRENT_TIMESTAMP)")


                        //TODO: Add progress bar for connection time.
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()

                            // If success go to the next fragment
                            val destination = RegistrationFragmentDirections.actionRegistrationFragmentToVerifyOTP(email, phone_number, "")
                            findNavController().navigate(destination)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Registration", "Error during registration", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}