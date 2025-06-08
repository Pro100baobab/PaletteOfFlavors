package com.paletteofflavors

import DataSource.Local.AppDatabase
import DataSource.Local.SessionManager
import DataSource.model.CreateRecipeViewModelFactory
import DataSource.model.FavoritesViewModel
import DataSource.model.FavoritesViewModelFactory
import DataSource.model.RecipeSharedViewModel
import ViewModels.CreateRecipeViewModel
import ViewModels.NavBottomViewModel
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.paletteofflavors.databinding.ActivityMainBinding
import com.paletteofflavors.logIn.AuthorizationFragment
import com.paletteofflavors.logIn.LoginFragment
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import com.paletteofflavors.logIn.viewmodels.RegistrationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    val TURSO_AUTH_TOKEN =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhIjoicnciLCJpYXQiOjE3NDQ5MDcyMjMsImlkIjoiZTU4ZTQ5MGEtZmVhYi00MzRiLTgxYTYtNjU1NGM2YjJlZGEwIiwicmlkIjoiMzY2OWJlZTYtYmE4Zi00ODc3LTk4MmItNjYxYzAwMDM5ZGNhIn0.af-7zDFU8XyhiIRP21CGehtSK-00AJGgnuX1y9lXAY_OtEtYn0yervXX31zFzuZGqiEDCO8VACfvjXUi3eyoAg"
    val TURSO_DATABASE_URL = "libsql://vkr-baobab2049.aws-us-east-1.turso.io"
    //TODO: replace location of variables


    lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navBottomViewModel: NavBottomViewModel
    private var isFromUserInteraction = true // Флаг для определения источника изменения

    lateinit var sessionManager: SessionManager;
    lateinit var sessionManagerRememberMe: SessionManager;

    lateinit var viewModel: LoginViewModel
    lateinit var viewModelRegistration: RegistrationViewModel


    val database by lazy { AppDatabase.getInstance(this) }
    val createRecipeViewModel: CreateRecipeViewModel by viewModels {
        CreateRecipeViewModelFactory(database.recipeDao())
    }
    val favoritesViewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(database.recipeDao())
    }
    val sharedViewModel: RecipeSharedViewModel by viewModels()

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

        //setSupportActionBar(binding.appBarMain.toolbar)

        navBottomViewModel = ViewModelProvider(this)[NavBottomViewModel::class.java]
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        val drawerLayout: DrawerLayout = binding.drawerLayout
        //val navView: NavigationView = binding.navView
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        //val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow //поменять на будущие разделы
            ), drawerLayout
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        //navView.setupWithNavController(navController)


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isFromUserInteraction) {
                when (item.itemId) {
                    R.id.navigation_home -> replaceMainFragment(HomeFragment())
                    R.id.navigation_search -> replaceMainFragment(SearchFragment())
                    R.id.navigation_favorites -> replaceMainFragment(FavoritesFragment())
                    R.id.navigation_pantry -> replaceMainFragment(FridgeFragment())
                    R.id.navigation_profile -> replaceMainFragment(ProfileFragment())
                }
                navBottomViewModel.setSelectedNavItem(item.itemId)
            }

            true
        }


    }



    override fun onStart() {
        super.onStart()


        navBottomViewModel.selectedNavItem.observe(this) { itemId ->

            isFromUserInteraction = false // Говорим, что изменение программное
            binding.bottomNavigation.selectedItemId = itemId ?: R.id.navigation_home
            isFromUserInteraction = true // Возвращаем флаг в исходное состояние


        }

        if (supportFragmentManager.findFragmentById(R.id.frame_layout) == null
            && intent?.getBooleanExtra("SHOW_PROFILE_FRAGMENT", false) != true)
        {
            replaceMainFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }

        initDatabase()


        // Проверка авторизации
        sessionManager = SessionManager(this, SessionManager.SESSION_USERSESSION)
        if (sessionManager.checkLogin()) {
            binding.appContent.isVisible = true
            val rememberMeDetails: HashMap<String, String?> =
                sessionManager.getUsersDetailFromSession()
        } else {
            //showFullscreenFragment(LoginFragment())
            showFullscreenFragment(AuthorizationFragment())
        }
    }


    // Settings
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /*
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }*/

    fun replaceMainFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

    }


    fun returnNavigation(){
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
    // Для показа полноэкранного фрагмента (авторизация/регистрация)
    fun showFullscreenFragment(fragment: Fragment) {
        binding.bottomNavigation.visibility = View.GONE
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Возвращение нижнего меню
    fun showNormalFragment(fragment: Fragment) {
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        replaceMainFragment(fragment)
    }


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
        val lang = sharedPref.getString("app_language", Locale.getDefault().language) ?: Locale.getDefault().language
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