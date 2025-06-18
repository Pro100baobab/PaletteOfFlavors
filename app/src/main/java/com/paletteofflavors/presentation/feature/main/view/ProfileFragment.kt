package com.paletteofflavors.presentation.feature.main.view

import com.paletteofflavors.data.local.SessionManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.paletteofflavors.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import android.content.res.Configuration
import androidx.lifecycle.lifecycleScope
import java.util.*
import androidx.navigation.findNavController
import kotlinx.coroutines.flow.first
import androidx.core.content.edit
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.R


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

        setUpRecipesCountObserver()
        bindData()
        setOnCLickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecipesCountObserver() {

        lifecycleScope.launch {
            val count = (requireActivity() as MainActivity)
                .favoritesViewModel.myRecipes
                .first()
                .size

            binding.recipesCount.text = count.toString()
        }
    }

    private fun bindData() {
        // Получение данных сессии
        val usersDetails: HashMap<String, String?> =
            (activity as MainActivity).sessionManager.getUsersDetailFromSession()

        binding.profileName.text = usersDetails[SessionManager.KEY_USERNAME]
        binding.profileEmail.text = usersDetails[SessionManager.KEY_EMAIL]
        when ((activity as MainActivity).sessionManagerBaseSettings.usersSession.getBoolean(
            SessionManager.KEY_CASH,
            false
        )) {
            true -> binding.cashLabel.text = getString(R.string.do_not_cash_network_recipes)
            false -> binding.cashLabel.text = getString(R.string.do_cash_network_recipes)
        }
    }

    private fun setOnCLickListeners() {

        // Смена аватара
        binding.changeAvatarButton.setOnClickListener {
            changeAvatar()
        }

        // Регулирование флага кеширования
        binding.changeCashFlagButton.setOnClickListener {
            changeCashSettings()
        }

        // Смена языковых настроек
        binding.changeLanguageButton.setOnClickListener {
            changeLanguage()
        }

        // Выход из аккаунта
        binding.logoutButton.setOnClickListener {
            showConfirmDialog()
        }

        // Создание аккаунта
        binding.addAccountButton.setOnClickListener {
            addAccount()
        }
    }

    private fun addAccount() {
        val activity = requireActivity() as MainActivity

        activity.run {
            findNavController(R.id.fragmentContainerView).popBackStack()
            navBottomViewModel.setIsContentVisible(false)
            showFullScreenContainer()
        }
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
        sharedPref.edit { putString("app_language", languageCode) }
    }

    // Получаем сохраненный язык или системный по умолчанию
    private fun getCurrentLanguageCode(): String {
        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPref?.getString("app_language", Locale.getDefault().language)
            ?: Locale.getDefault().language
    }

    private fun restartActivity() {
        val intent = Intent(requireActivity(), requireActivity().javaClass).apply {
            putExtra("SHOW_PROFILE_FRAGMENT", true)
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        requireActivity().finish()
        startActivity(intent)
    }


    // Изменение автара (в процессе) - требует масштабирования бд
    private fun changeAvatar() {
        Toast.makeText(context, "В разработке", Toast.LENGTH_LONG).show()
    }

    // Регулирования флага кеширования
    private fun changeCashSettings() {
        val session = (requireActivity() as MainActivity).sessionManagerBaseSettings.usersSession
        val newFlag = !session.getBoolean(SessionManager.KEY_CASH, true)
        session.edit {
            putBoolean(SessionManager.KEY_CASH, newFlag)
        }

        when (newFlag) {
            true -> binding.cashLabel.text = getString(R.string.do_not_cash_network_recipes)
            false -> binding.cashLabel.text = getString(R.string.do_cash_network_recipes)
        }
        //Log.d("IsCashed", (requireActivity() as MainActivity).sessionManagerBaseSettings.usersSession.getBoolean(SessionManager.KEY_CASH, false).toString())
    }


    // Функции для выхода из аккаунта
    private fun showConfirmDialog() {
        val builder = AlertDialog.Builder(context)
        builder
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта" + " ${binding.profileName.text}?")
            .setPositiveButton("Выйти") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                logOut() // Вызываем функцию выхода из аккаунта
            }
            .setNegativeButton("Отмена") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logOut() {

        val activity = requireActivity() as MainActivity

        activity.run {
            navBottomViewModel.setSelectedNavItem(R.id.navigation_search)
            sessionManager.logoutUserSession() // Logout from LogIn session
            navBottomViewModel.setIsContentVisible(false)
            showFullScreenContainer()
        }

    }

}