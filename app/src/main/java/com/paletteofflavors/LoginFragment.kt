package com.paletteofflavors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.paletteofflavors.databinding.FragmentLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            username = binding.etLoginUsername.text.toString().trim()
            password = binding.etLoginPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        binding.tvRegistration.setOnClickListener {
            (activity as? MainActivity)?.showFullscreenFragment(RegistrationFragment())
        }
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        val query = """
                            SELECT id FROM users 
                            WHERE username = '$username' 
                            AND password = '${password.hashCode()}'
                        """.trimIndent()

                        conn.query(query).use { rows ->
                            val nextRow = rows.nextRow()
                            if ( nextRow != null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                                    (activity as? MainActivity)?.showNormalFragment(HomeFragment())
                                }
                            } else {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Login", "Error during login", e)
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}