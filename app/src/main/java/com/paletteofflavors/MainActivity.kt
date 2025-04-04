package com.paletteofflavors

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.paletteofflavors.databinding.ActivityMainBinding
import domain.Recipe


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

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
        replaceMainFragment(HomeFragment())


        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> replaceMainFragment(HomeFragment())
                R.id.navigation_search -> replaceMainFragment(SearchFragment())
                R.id.navigation_favorites -> {
                    replaceMainFragment(FavoritesFragment())
                    /*
                    val recipelist = arrayListOf<Recipe>(
                        Recipe(
                            id = 1,
                            title = "Пример 1",
                            ingredients = List<String>(2, {"биба"; "боба"}),
                            instruction = "Инструкция",
                            cookTime = 60,
                            comments = 4,
                            likes = 5,
                            imageUrl = null
                        ),
                        Recipe(
                            id = 2,
                            title = "Пример 2",
                            ingredients = List<String>(2, {"бибаF"; "бобаF"}),
                            instruction = "Инструкция 2",
                            cookTime = 120,
                            comments = 7,
                            likes = 3,
                            imageUrl = null
                        )

                    )

                    recyclerView = findViewById(R.id.recipesRecyclerView)

                    recyclerView?.adapter = RecipeAdapter(recipelist)
                    recyclerView?.layoutManager = LinearLayoutManager(this)*/
                }
                R.id.navigation_pantry -> replaceMainFragment(FridgeFragment())
                R.id.navigation_profile -> replaceMainFragment(ProfileFragment())

                else -> {

                }
            }
            true
        }
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

    private fun replaceMainFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }
}