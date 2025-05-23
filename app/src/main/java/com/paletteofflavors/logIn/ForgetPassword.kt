package com.paletteofflavors.logIn

import DataSource.Local.SessionManager
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompatSideChannelService
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.HomeFragment
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentForgetPasswordBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.turso.libsql.Libsql


class ForgetPassword : Fragment() {


    private lateinit var _binding: FragmentForgetPasswordBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.forgetPasswotdContinue.setOnClickListener {


            if (isValidEmail(binding.emailForReset)){

                val email = binding.emailForReset.text.toString().trim()

                FindUserByEmail(email) { phoneNumber ->
                    if (phoneNumber.isNotEmpty()) {
                        Toast.makeText(requireContext(), "phone:$phoneNumber", Toast.LENGTH_SHORT).show()


                        //TODO: Add Bound to transmit data
                        val direction = ForgetPasswordDirections.actionForgetPasswordToMakeSelection(email, phoneNumber)
                        findNavController().navigate(direction)

                        //findNavController().navigate(R.id.action_forgetPassword_to_makeSelection)

                    } else {
                        Toast.makeText(requireContext(),"Email is not registered",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupBackButtonForgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_forgetPassword_to_loginFragment)
        }
    }


    private fun FindUserByEmail(email: String, callback: (String) -> Unit) {

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                var phoneNumber = ""

                withContext(Dispatchers.IO) {
                    Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                        db.connect().use { conn ->
                            val query = """
                                SELECT * FROM users 
                                WHERE email = '$email'
                            """.trimIndent()

                            conn.query(query).use { rows ->
                                val nextRow = rows.nextRow()
                                if ( nextRow != null) {
                                    phoneNumber = nextRow[5].toString()

                                    activity?.runOnUiThread {
                                        Toast.makeText(requireContext(), "phone: $phoneNumber", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    activity?.runOnUiThread {
                                        Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                        }
                    }
                }

                callback(phoneNumber) // return

            } catch (e: Exception) {
                Log.e("Login", "Error during sending email", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                callback("")
            }
        }
    }

}