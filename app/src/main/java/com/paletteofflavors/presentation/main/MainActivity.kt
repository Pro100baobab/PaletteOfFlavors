package com.paletteofflavors.presentation.main

import com.paletteofflavors.data.local.database.AppDatabase
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.data.remote.api.turso.Turso
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
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.presentation.feature.main.di.SearchViewModelFactory
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment
import com.paletteofflavors.presentation.feature.main.view.FridgeFragment
import com.paletteofflavors.presentation.feature.main.view.ProfileFragment
import com.paletteofflavors.presentation.feature.main.view.SearchFragment
import com.paletteofflavors.presentation.feature.main.view.RestaurantsMapFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import com.paletteofflavors.BuildConfig
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.data.remote.utils.AndroidInternetChecker
import com.paletteofflavors.data.remote.api.imgBB.ImgBBService
import com.paletteofflavors.presentation.auth.di.LoginViewModelFactory
import com.paletteofflavors.presentation.auth.di.RegistrationViewModelFactory
import com.paletteofflavors.presentation.feature.main.di.ProfileViewModelFactory
import com.paletteofflavors.presentation.feature.recipes.di.RecipeDetailsViewModelFactory
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeDetailsViewModel
import com.paletteofflavors.presentation.feature.main.viewmodel.ProfileViewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navBottomViewModel: NavBottomViewModel

    lateinit var sessionManager: SessionManager;
    lateinit var sessionManagerRememberMe: SessionManager;
    lateinit var sessionManagerBaseSettings: SessionManager;

    lateinit var loginViewModel: LoginViewModel
    lateinit var viewModelRegistration: RegistrationViewModel

    lateinit var remoteRepository: RecipeRemoteRepository
    lateinit var userRemoteRepository: UserRemoteRepository

    private val database by lazy { AppDatabase.getInstance(this) }
    
    lateinit var createRecipeViewModel: CreateRecipeViewModel
    lateinit var favoritesViewModel: FavoritesViewModel
    lateinit var recipeDetailsViewModel: RecipeDetailsViewModel
    lateinit var profileViewModel: ProfileViewModel
    
    lateinit var searchViewModelFactory: SearchViewModelFactory

    private var isFromUserInteraction = true 
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

        setUpRepositories()
        setUpViewModels()
        SetUpBaseSettingsSession()
    }

    private fun setUpRepositories(){
        val turso = Turso()
        val internetChecker = AndroidInternetChecker(applicationContext)

        val imgBBRetrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val imgBBService = imgBBRetrofit.create(ImgBBService::class.java)

        remoteRepository = RecipeRemoteRepository(turso, internetChecker)
        userRemoteRepository = UserRemoteRepository(turso, internetChecker, imgBBService)
    }

    private fun setUpViewModels(){

        navBottomViewModel = ViewModelProvider(this)[NavBottomViewModel::class.java]

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isFromUserInteraction) {
                when (item.itemId) {
                    R.id.navigation_search -> replaceMainFragment(SearchFragment())
                    R.id.navigation_favorites -> replaceMainFragment(FavoritesFragment())
                    R.id.navigation_pantry -> replaceMainFragment(FridgeFragment())
                    R.id.navigation_profile -> replaceMainFragment(ProfileFragment())
                    R.id.navigation_restaurantsMap -> replaceMainFragment(RestaurantsMapFragment())
                }
                navBottomViewModel.setSelectedNavItem(item.itemId)
            }
            true
        }

        searchViewModelFactory = SearchViewModelFactory(remoteRepository)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(userRemoteRepository))[LoginViewModel::class.java]
        viewModelRegistration = ViewModelProvider(this, RegistrationViewModelFactory(userRemoteRepository))[RegistrationViewModel::class.java]
        createRecipeViewModel = ViewModelProvider(this, CreateRecipeViewModelFactory(remoteRepository))[CreateRecipeViewModel::class.java]

        favoritesViewModel = ViewModelProvider(this, FavoritesViewModelFactory(
            RecipeRepository(database.savedRecipeDao(), database.cachedRecipeDao()),
            remoteRepository
        ))[FavoritesViewModel::class.java]

        recipeDetailsViewModel = ViewModelProvider(this, RecipeDetailsViewModelFactory(remoteRepository, userRemoteRepository))[RecipeDetailsViewModel::class.java]
        profileViewModel = ViewModelProvider(this, ProfileViewModelFactory(remoteRepository, userRemoteRepository))[ProfileViewModel::class.java]
    }


    override fun onStart() {
        super.onStart()

        navBottomViewModel.selectedNavItem.observe(this) { itemId ->
            isFromUserInteraction = false 
            binding.bottomNavigation.selectedItemId = itemId ?: R.id.navigation_search
            isFromUserInteraction = true 
        }

        if (supportFragmentManager.findFragmentById(R.id.frame_layout) == null) {
            replaceMainFragment(SearchFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_search
        }

        checkIsLogin()
        cacheRecipesData()
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

    private fun SetUpBaseSettingsSession() {
        sessionManagerBaseSettings = SessionManager(this, SessionManager.SESSION_BASESETTINGS)
        sessionManagerBaseSettings.let {
            if (!it.checkBaseSettings()) {
                sessionManagerBaseSettings.createBaseSettingSession(true)
            }
        }
    }

    private fun checkIsLogin() {
        sessionManager = SessionManager(this, SessionManager.SESSION_USERSESSION)
        if (sessionManager.checkLogin()) {
            binding.appContent.isVisible = true
        } else {
            showFullScreenContainer()
        }
    }

    private fun cacheRecipesData() {
        if (true/*turso.checkInternetConnection(this)*/) {
            job = lifecycleScope.launch(Dispatchers.IO) {
                if (sessionManagerBaseSettings.usersSession.getBoolean(
                        SessionManager.KEY_CASH,
                        true
                    )
                ) {
                    favoritesViewModel.deleteCashRecipes()
                    try {
                        val recipes = remoteRepository.getAllRecipes()
                        withContext(Dispatchers.Main) {
                            recipes.map { recipe ->
                                favoritesViewModel.addCashedRecipe(recipe)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FavoritesViewModel", "Flow collection error", e)
                        job?.cancel()
                    }
                }
            }
        }
    }

    fun showFullScreenContainer() {
        binding.run {
            bottomNavigation.visibility = View.GONE
            appContent.visibility = View.GONE
            fragmentContainerView.visibility = View.VISIBLE
        }
    }

    fun hideFullScreenContainer(){
        binding.run {
            appContent.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            fragmentContainerView.visibility = View.GONE
        }
    }

    fun replaceMainFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    fun returnNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.appContent.isVisible = true
    }

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
