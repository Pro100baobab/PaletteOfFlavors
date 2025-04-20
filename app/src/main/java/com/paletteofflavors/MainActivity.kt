package com.paletteofflavors

import ViewModels.NavBottomViewModel
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.paletteofflavors.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql

class MainActivity : AppCompatActivity() {

    val TURSO_AUTH_TOKEN = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhIjoicnciLCJpYXQiOjE3NDQ5MDcyMjMsImlkIjoiZTU4ZTQ5MGEtZmVhYi00MzRiLTgxYTYtNjU1NGM2YjJlZGEwIiwicmlkIjoiMzY2OWJlZTYtYmE4Zi00ODc3LTk4MmItNjYxYzAwMDM5ZGNhIn0.af-7zDFU8XyhiIRP21CGehtSK-00AJGgnuX1y9lXAY_OtEtYn0yervXX31zFzuZGqiEDCO8VACfvjXUi3eyoAg"
    val TURSO_DATABASE_URL = "libsql://vkr-baobab2049.aws-us-east-1.turso.io"
    //TODO: replace location of variables


    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navBottomViewModel: NavBottomViewModel
    private var isFromUserInteraction = true // Флаг для определения источника изменения

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        navBottomViewModel = ViewModelProvider(this)[NavBottomViewModel::class.java]


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow //поменять на будущие разделы
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if(isFromUserInteraction){
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

        navBottomViewModel.selectedNavItem.observe(this){
            itemId ->
            isFromUserInteraction = false // Говорим, что изменение программное
            binding.bottomNavigation.selectedItemId = itemId ?: R.id.navigation_home
            isFromUserInteraction = true // Возвращаем флаг в исходное состояние
        }

        if (supportFragmentManager.findFragmentById(R.id.frame_layout) == null) {
            replaceMainFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }

        initDatabase()
        replaceMainFragment(HomeFragment())
        showFullscreenFragment(RegistrationFragment())
    }


    // Settings
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun replaceMainFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

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
}