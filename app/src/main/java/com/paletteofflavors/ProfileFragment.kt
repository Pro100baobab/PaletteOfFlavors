package com.paletteofflavors

import DataSource.Local.SessionManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat.recreate
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.databinding.FragmentProfileBinding
import domain.Recipe
import kotlinx.coroutines.launch

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.paletteofflavors.logIn.AuthorizationFragment
import com.paletteofflavors.logIn.LoginFragment
import com.paletteofflavors.logIn.RegistrationFragment
import java.util.*
import androidx.navigation.findNavController
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first


class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        (requireActivity() as MainActivity).profileViewModel.recipesCount.observe(viewLifecycleOwner){
            count -> binding.recipesCount.text = count.toString()
        }*/
        recipesCountVisualization()

        // Получение данных сессии
        val usersDetails: HashMap<String, String?> =
            (activity as MainActivity).sessionManager.getUsersDetailFromSession()

        binding.profileName.text = usersDetails[SessionManager.KEY_USERNAME]
        binding.profileEmail.text = usersDetails[SessionManager.KEY_EMAIL]

        //imageViewProfile?.setImageResource(R.drawable.favorites_icon)

        setObservers()
    }

    private fun setObservers() {

        binding.changeAvatarButton.setOnClickListener {
            changeAvatar()
        }

        binding.changeThemeButton.setOnClickListener {
            changeTheme()
        }

        binding.changeLanguageButton.setOnClickListener {
            try {
                changeLanguage()
            } catch (e: Exception){
                Log.e("ChangeLang", "Error: $e")
            }
        }

        binding.logoutButton.setOnClickListener {
            showConfirmDialog()
        }

        binding.addAccountButton.setOnClickListener {
            addAccount()
        }
    }

    private fun addAccount() {
        val activity = requireActivity() as MainActivity

        activity.findNavController(R.id.fragmentContainerView).popBackStack()
        activity.navBottomViewModel.setIsContentVisible(false)
        activity.binding.appContent.visibility = View.GONE
    }




    // Функции для управления языковыми настройками

    private fun changeLanguage() {
        val languageCode = if (getCurrentLanguageCode() == "ru") "en" else "ru"
        setAppLocale(requireContext(), languageCode)
        restartActivity()
    }

    private fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        context.createConfigurationContext(config)

        resources.updateConfiguration(config, resources.displayMetrics)

        // Сохраняем выбранный язык в SharedPreferences
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPref.edit().putString("app_language", languageCode).apply()
    }

    private fun getCurrentLanguageCode(): String {
        // Получаем сохраненный язык или системный по умолчанию
        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPref?.getString("app_language", Locale.getDefault().language) ?: Locale.getDefault().language
    }

    private fun restartActivity() {
        val intent = Intent(requireActivity(), requireActivity().javaClass).apply {
            putExtra("SHOW_PROFILE_FRAGMENT", true)
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        requireActivity().finish()
        startActivity(intent)
    }





    //

    private fun changeAvatar() {
        //TODO("Not yet implemented")
        Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show()

    }

    private fun changeTheme() {
        //TODO("Not yet implemented")
        Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show()
    }


    // Функции для выхода из аккаунта

    private fun showConfirmDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выход из аккаунта")
        builder.setMessage("Вы уверены, что хотите выйти из аккаунта" + " ${binding.profileName.text}?")

        builder.setPositiveButton("Выйти") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            logOut() // Вызываем функцию выхода из аккаунта
        }

        builder.setNegativeButton("Отмена") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logOut() {
        (requireActivity() as MainActivity).navBottomViewModel.setSelectedNavItem(R.id.navigation_search)
        (requireActivity() as MainActivity).sessionManager.logoutUserSession() // userSession
        //(requireActivity() as MainActivity).showFullscreenFragment(LoginFragment())
        (requireActivity() as MainActivity).binding.appContent.visibility = View.GONE
        //activity.navBottomViewModel.setIsContentVisible(false)

    }


    private fun recipesCountVisualization() {

        lifecycleScope.launch {
            val count = (requireActivity() as MainActivity)
                .favoritesViewModel.myRecipes
                .first()
                .size

            binding.recipesCount.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}