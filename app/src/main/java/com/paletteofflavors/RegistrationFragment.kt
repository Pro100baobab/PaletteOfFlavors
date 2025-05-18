package com.paletteofflavors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.paletteofflavors.databinding.FragmentRegistrationBinding
import android.util.Log
import androidx.core.text.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fullname: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var phone_number: String
    private lateinit var email: String

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

            fullname = binding.etFullname.text.toString().trim()
            username = binding.etUsername.text.toString().trim()
            email = binding.etEmail.text.toString().trim()
            phone_number = binding.etPhoneNumber.text.toString().trim()
            password = binding.etPassword.text.toString().trim()



            if (fullname.isEmpty() || username.isEmpty() || email.isEmpty() || phone_number.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(fullname, username, phone_number, email, password)
        }

        binding.tvLogin.setOnClickListener {
            //Toast.makeText(requireContext(), "VALUES('$username', '${password.hashCode()}')", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.showFullscreenFragment(LoginFragment())
        }

    }

    //TODO: make a validation check for the input values.
    //TODO: change datatype of phone_number and ui for this view.
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
                            // После успешной регистрации переходим на главный экран или экран логина
                            (activity as? MainActivity)?.showFullscreenFragment(LoginFragment())
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}