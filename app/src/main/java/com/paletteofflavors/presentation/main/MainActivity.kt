package com.paletteofflavors.presentation.main

import com.paletteofflavors.data.local.database.AppDatabase
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.data.remote.API.Turso.Turso
import com.paletteofflavors.presentation.feature.recipes.di.CreateRecipeViewModelFactory
import com.paletteofflavors.presentation.feature.main.viewmodel.FavoritesViewModel
import com.paletteofflavors.presentation.feature.main.di.FavoritesViewModelFactory
import com.paletteofflavors.data.local.repository.RecipeRepository
import com.paletteofflavors.presentation.feature.recipes.viewmodel.CreateRecipeViewModel
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.databinding.ActivityMainBinding
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel
import com.paletteofflavors.presentation.auth.viewmodel.RegistrationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.lifecycleScope
import com.paletteofflavors.R
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment
import com.paletteofflavors.presentation.feature.main.view.FridgeFragment
import com.paletteofflavors.presentation.feature.main.view.ProfileFragment
import com.paletteofflavors.presentation.feature.main.view.SearchFragment
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    val TURSO_AUTH_TOKEN =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhIjoicnciLCJpYXQiOjE3NDQ5MDcyMjMsImlkIjoiZTU4ZTQ5MGEtZmVhYi00MzRiLTgxYTYtNjU1NGM2YjJlZGEwIiwicmlkIjoiMzY2OWJlZTYtYmE4Zi00ODc3LTk4MmItNjYxYzAwMDM5ZGNhIn0.af-7zDFU8XyhiIRP21CGehtSK-00AJGgnuX1y9lXAY_OtEtYn0yervXX31zFzuZGqiEDCO8VACfvjXUi3eyoAg"
    val TURSO_DATABASE_URL = "libsql://vkr-baobab2049.aws-us-east-1.turso.io"
    //TODO: replace location of variables


    lateinit var binding: ActivityMainBinding
    lateinit var navBottomViewModel: NavBottomViewModel

    lateinit var sessionManager: SessionManager;
    lateinit var sessionManagerRememberMe: SessionManager;
    lateinit var sessionManagerBaseSettings: SessionManager;

    lateinit var viewModel: LoginViewModel
    lateinit var viewModelRegistration: RegistrationViewModel


    private val database by lazy { AppDatabase.getInstance(this) }
    val createRecipeViewModel: CreateRecipeViewModel by viewModels {
        CreateRecipeViewModelFactory(database.recipeDao())
    }
    val favoritesViewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(
            RecipeRepository(
                database.recipeDao(),
                database.savedRecipeDao(),
                database.cashDao()
            )
        )
    }

    private var isFromUserInteraction = true // Флаг для определения источника изменения
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent?.getBooleanExtra("SHOW_PROFILE_FRAGMENT", false) == true) {
            Handler(Looper.getMainLooper()).postDelayed({
                replaceMainFragment(ProfileFragment())
                binding.bottomNavigation.selectedItemId = R.id.navigation_profile
            }, 100)
        }


        navBottomViewModel = ViewModelProvider(this)[NavBottomViewModel::class.java]
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isFromUserInteraction) {
                when (item.itemId) {
                    //R.id.navigation_home -> replaceMainFragment(HomeFragment())
                    R.id.navigation_search -> replaceMainFragment(SearchFragment())
                    R.id.navigation_favorites -> replaceMainFragment(FavoritesFragment())
                    R.id.navigation_pantry -> replaceMainFragment(FridgeFragment())
                    R.id.navigation_profile -> replaceMainFragment(ProfileFragment())
                }
                navBottomViewModel.setSelectedNavItem(item.itemId)
            }

            true
        }

        SetUpBaseSettingsSession()
    }

    override fun onStart() {
        super.onStart()


        navBottomViewModel.selectedNavItem.observe(this) { itemId ->
            isFromUserInteraction = false // Говорим, что изменение программное
            binding.bottomNavigation.selectedItemId = itemId ?: R.id.navigation_search
            isFromUserInteraction = true // Возвращаем флаг в исходное состояние
        }

        if (supportFragmentManager.findFragmentById(R.id.frame_layout) == null) {
            replaceMainFragment(SearchFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_search
        }

        initDatabase()
        checkIsLogin()  // Проверка авторизации
        cashReсipesData()
    }

    override fun onResume() {
        super.onResume()

        if (navBottomViewModel.isContentVisible.value == false) {
            binding.appContent.visibility = View.GONE
        }

    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }


    // Инициализация сессии базовых настроек
    private fun SetUpBaseSettingsSession() {

        sessionManagerBaseSettings = SessionManager(this, SessionManager.SESSION_BASESETTINGS)

        sessionManagerBaseSettings.let {
            if (!it.checkBaseSettings()) {
                sessionManagerBaseSettings.createBaseSettingSession(true)
                //Log.d("dddd", "true")
            }
        }
    }

    // Проверка авторизации
    private fun checkIsLogin() {
        sessionManager = SessionManager(this, SessionManager.SESSION_USERSESSION)
        if (sessionManager.checkLogin()) {
            binding.appContent.isVisible = true
        } else {
            showFullScreenContainer()
        }
    }

    // Кеширование рецептов
    private fun cashReсipesData() {
        val tursoConnection = Turso(this@MainActivity, this@MainActivity)
        if (tursoConnection.checkInternetConnection(this)) {

            job = lifecycleScope.launch(Dispatchers.IO) {

                if (sessionManagerBaseSettings.usersSession.getBoolean(
                        SessionManager.KEY_CASH,
                        true
                    )
                ) {
                    favoritesViewModel.deleteCashRecipes()
                    try {
                        tursoConnection.getAllNetworkRecipesFlow()
                            .collect { networkRecipes ->
                                favoritesViewModel.addCashedRecipe(networkRecipes)
                            }
                    } catch (e: Exception) {
                        Log.e("FavoritesViewModel", "Flow collection error", e)
                        job?.cancel()
                    }
                }
            }
        }
    }

    // Показ полноэкранных фрагментов
    fun showFullScreenContainer() {
        binding.run {
            bottomNavigation.visibility = View.GONE
            appContent.visibility = View.GONE
            fragmentContainerView.visibility = View.VISIBLE
        }
    }

    // Скрыть полноэкранные фрагменты
    fun hideFullScreenContainer(){
        binding.run {
            appContent.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            fragmentContainerView.visibility = View.GONE
        }
    }

    // Смена фрагментов в центральном контейнере
    fun replaceMainFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

    }

    // Показ основного контента после авторизации
    fun returnNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.appContent.isVisible = true
    }


    // Безопасная инициализация баы данных - не доделано
    private fun initDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val dbFileBasePath = this@MainActivity.filesDir.path
            val dbUrl = TURSO_DATABASE_URL // Должно быть определено в buildConfigField
            val dbAuthToken = TURSO_AUTH_TOKEN // Должно быть определено в buildConfigField
        }
    }


    // Перезапуск активности с новыми языковыми настройками
    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("app_language", Locale.getDefault().language)
            ?: Locale.getDefault().language
        super.attachBaseContext(updateBaseContextLocale(newBase, lang))
    }

    private fun updateBaseContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }


}