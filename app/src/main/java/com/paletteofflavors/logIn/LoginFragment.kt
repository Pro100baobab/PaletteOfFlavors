package com.paletteofflavors.logIn

import DataSource.Local.SessionManager
import DataSource.Network.Turso
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.transition.Visibility
import com.paletteofflavors.HomeFragment
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentLoginBinding
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
//import com.paletteofflavors.logIn.viewmodels.MyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

//TODO: Add ViewModel with liveData
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    lateinit var vm: LoginViewModel
    private var isUpdatingFromViewModel = false
    //val id0 = 0L

    private lateinit var username: String
    private lateinit var password: String


    private lateinit var rememberMe: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check phone_number and password are saved already in Shared Preferences
        (activity as MainActivity).sessionManagerRememberMe = SessionManager(requireContext(), SessionManager.SESSION_REMEMBERME)
        if((activity as MainActivity).sessionManagerRememberMe.checkRememberMe()){
            val rememberMeDetails: HashMap<String, String?> = (activity as MainActivity).sessionManagerRememberMe.getRememberMeDetailsFromSession()

            binding.etLoginPassword.setText(rememberMeDetails[SessionManager.KEY_SESSION_PASSWORD])
            binding.etLoginUsername.setText(rememberMeDetails[SessionManager.KEY_SESSION_USERNAME])
            binding.rememberMe.isChecked = true
        }

        //viewModel = ViewModelProvider(requireActivity(), MyViewModelFactory(id0)).get("id_$id0", LoginViewModel::class.java)
        //(requireActivity() as MainActivity).viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        vm = (requireActivity() as MainActivity).viewModel
        vm.username.observe(viewLifecycleOwner){
            newText ->
            if(binding.etLoginUsername.text.toString() != newText){
                isUpdatingFromViewModel = true
                binding.etLoginUsername.setText(newText)
                isUpdatingFromViewModel = false
            }
        }
        vm.password.observe(viewLifecycleOwner){
                newText ->
            if(binding.etLoginPassword.text.toString() != newText){
                isUpdatingFromViewModel = true
                binding.etLoginPassword.setText(newText)
                isUpdatingFromViewModel = false
            }
        }

        binding.etLoginUsername.doAfterTextChanged{
            editable ->
            if(!isUpdatingFromViewModel)
                editable?.toString()?.let { vm.setUserName(it) }
        }
        binding.etLoginPassword.doAfterTextChanged{
                editable -> if(!isUpdatingFromViewModel) editable?.toString()?.let { vm.setPassword(it) }
        }


        //
        binding.forgetPassword.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_forgetPassword)
        }

        binding.tvRegistration.setOnClickListener{
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        binding.btnLogin.setOnClickListener {

            it.isEnabled = false

            username = binding.etLoginUsername.text.toString().trim()
            password = binding.etLoginPassword.text.toString().trim()
            rememberMe = binding.rememberMe

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                it.isEnabled = true

                return@setOnClickListener
            }

            if(!checkInternetConnection(requireContext())){
                it.isEnabled = true
                return@setOnClickListener
            }


            //loginUser(username, password)
            val TursoConnection = Turso(requireActivity() as MainActivity, requireContext(), rememberMe)
            TursoConnection.loginUser(username, password)
            //TODO: progress bar

            it.isEnabled = true
        }


        binding.signupBackButtonLogin.setOnClickListener {
            requireActivity().viewModelStore.clear()
            findNavController().navigate(R.id.action_loginFragment_to_authorizationFragment)
        }

    }
/*
    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        val query = """
                            SELECT * FROM users 
                            WHERE username = '$username' 
                            AND password = '${password.hashCode()}'
                        """.trimIndent()

                        conn.query(query).use { rows ->
                            val nextRow = rows.nextRow()
                            if ( nextRow != null) {

                                //val _id = nextRow[0].toString()
                                //val _username = nextRow[1].toString()
                                //val _password = nextRow[2].toString() // вернет хершированный пароль
                                val _fullName = nextRow[3].toString()
                                val _email = nextRow[4].toString()
                                val _phoneNumber = nextRow[5].toString()

                                activity?.runOnUiThread {

                                    // Create a Session by SessionManager
                                    (activity as MainActivity).sessionManager = SessionManager(requireContext(), SessionManager.SESSION_USERSESSION)
                                    (activity as MainActivity).sessionManager.createLoginSession(fullName = _fullName, username = username, email = _email, phoneNumber = _phoneNumber, password = password) //password, а не _password, потому что в бд хранится хешированный пароль

                                    Toast.makeText(requireContext(),
                                        "Login successful: $username $_fullName $_phoneNumber", Toast.LENGTH_SHORT).show()

                                    // Save LogIn Settings if checked
                                    rememberMe(username, password)

                                    (activity as? MainActivity)?.binding?.appContent?.isVisible = true
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
*/
    private fun checkInternetConnection(requireContext: Context): Boolean {
        if (isInternetAvailable(requireContext)) {
            Toast.makeText(context, "Internet is available", Toast.LENGTH_SHORT).show()
            return true
        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //requireActivity().viewModelStore.get("id_$id0")
        _binding = null
    }

}
