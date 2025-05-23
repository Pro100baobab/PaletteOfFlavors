package com.paletteofflavors.logIn

import DataSource.Local.SessionManager
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.HomeFragment
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentSetNewPasswordBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.turso.libsql.Libsql
import java.util.regex.Matcher


class SetNewPassword : Fragment() {

    private lateinit var _binding: FragmentSetNewPasswordBinding
    private val binding get() = _binding

    private val args: SetNewPasswordArgs by navArgs()

    private var email = ""
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            email = args.email
            phone = args.phone
        } catch (_: Exception) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetNewPasswordBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmChangeInPassword.setOnClickListener {

            val password = binding.newPassword.text.toString().trim()

            if (password == binding.confirmNewPassword.text.toString().trim()
                && isValidPassword(binding.newPassword)) {

                setNewPasswordUser(email, phone, password)
                //findNavController().navigate(R.id.action_setNewPassword_to_passwordSuccessUpdated)
            }
        }

        binding.backButtonSetNewPassword.setOnClickListener {
            findNavController().navigate(R.id.action_setNewPassword_to_verifyOTP)
        }
    }


    private fun setNewPasswordUser(email: String, phone: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->

                        conn.execute("BEGIN TRANSACTION") // Начало транзакции

                        try {

                            val hash = password.hashCode()
                            val query = """
                            UPDATE users 
                            SET password = '$hash'
                            WHERE email = '$email' 
                            AND phone_number = '$phone'
                        """.trimIndent()

                            conn.execute(query)

                            val check = """
                            SELECT 1 FROM users 
                            WHERE password = '$hash'
                            AND email = '$email' 
                            AND phone_number = '$phone'
                            LIMIT 1
                        """.trimIndent()

                            val isUpdated = conn.query(check).use { rows ->
                                rows.nextRow() != null
                            }

                            conn.execute("COMMIT") // Подтверждение изменений

                            withContext(Dispatchers.Main) {
                                if (isUpdated) {
                                    findNavController().navigate(R.id.action_setNewPassword_to_passwordSuccessUpdated)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Password update failed: no matching user found",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        } catch (e: Exception) {
                            conn.execute("ROLLBACK") // Откат при ошибке
                            throw e
                        }

                    }
                }
            } catch (e: Exception) {

                Log.e("PasswordUpdate", "Error during password update", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Password update failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}