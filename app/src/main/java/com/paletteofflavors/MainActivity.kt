package com.paletteofflavors

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.paletteofflavors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceMainFragment(HomeFragment())


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home -> replaceMainFragment(HomeFragment())
                R.id.navigation_search -> replaceMainFragment(SearchFragment())
                R.id.navigation_favorites -> replaceMainFragment(FavoritesFragment())
                R.id.navigation_pantry -> replaceMainFragment(FridgeFragment())
                R.id.navigation_profile -> replaceMainFragment(ProfileFragment())

                else -> {

                }
            }
            true
        }
    }



    private fun replaceMainFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }
}