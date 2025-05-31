package com.paletteofflavors

import DataSource.model.FavoritesViewModel
import DataSource.model.FavoritesViewModelFactory
import android.os.Bundle
import android.text.Layout.Directions
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.databinding.ActivityMainBinding
import com.paletteofflavors.databinding.FragmentFavoritesBinding
import domain.Recipe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class FavoritesFragment() : Fragment() {

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory((requireContext() as MainActivity).database.recipeDao())
    }

    private lateinit var _binding: FragmentFavoritesBinding
    private val binding get() = _binding

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var hintRecipe: TextView
    private lateinit var hintuserRecipe: TextView

    private lateinit var savedRadioButton: RadioButton
    private lateinit var userRadioButton: RadioButton
    private lateinit var radioGroup: RadioGroup


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hintRecipe = _binding.favoritesFragmentMissingItemHint
        hintuserRecipe = _binding.favoritesFragmentMissingItemHint2
        radioGroup = _binding.favoritesFragmentRadioGroup
        //savedRadioButton = _binding.favoritesFragmentSavedRecipes
        //userRadioButton = _binding.favoritesFragmentMyRecipes

        recipesRecyclerView = _binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        hintRecipe.visibility = View.INVISIBLE
        hintuserRecipe.visibility = View.INVISIBLE

        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        when (checkedRadioButtonId) {
            R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
            R.id.favorites_fragment_myRecipes -> updateMyRecipes()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            when(checkedId){
                R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
                R.id.favorites_fragment_myRecipes -> updateMyRecipes()
            }
        }

        binding.createRecipe.setOnClickListener {
            (requireActivity() as MainActivity).showFullscreenFragment(CreateRecipeFragment())
        }

    }


    private fun updateMyRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        // Подписываемся на Flow из ViewModel
        viewModel.myRecipes.onEach { recipes ->
            if (recipes.isEmpty()) {
                hintuserRecipe.visibility = View.VISIBLE
            } else {
                recipeAdapter = RecipeAdapter(recipes)
                recipesRecyclerView.adapter = recipeAdapter
            }
        }.launchIn(lifecycleScope)  // Автоматически отменяется при уничтожении фрагмента
    }

    private fun updateSavedRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        val recipelist = getSavedRecipeList()
        recipeAdapter = RecipeAdapter(recipelist)
        recipesRecyclerView.adapter = recipeAdapter

        if(recipelist.size == 0){
            hintRecipe.visibility = View.VISIBLE
        }
    }


    private fun getSavedRecipeList(): ArrayList<Recipe> {
        // TODO(): Inclement logic of getting data from local database(saved recipes)
        val recipelist = arrayListOf<Recipe>(
            Recipe(
                id = 1,
                title = "Пример 1",
                ingredients = List<String>(2, {"биба"; "боба"}),
                instruction = "Инструкция",
                cookTime = 60,
                //comments = 4,
                //likes = 5,
                imageUrl = null
            ),
            Recipe(2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120,null,/* 7, likes = 3*/),
            Recipe(2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120, null /*,7, likes = 3*/),
            Recipe(2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120, null /*,7, likes = 3*/)
        )

        return recipelist
    }

    private fun getUserRecipeList(): ArrayList<Recipe> {
        // TODO(): Inclement logic of getting data from local database(user recipes)
        return ArrayList<Recipe>()
    }


    /*private suspend fun testRoom() {
        val db = (requireContext() as MainActivity).database
        val recipeDao = db.recipeDao()


        // Тестовая вставка
        val testRecipe = Recipe(
            title = "Тестовый рецепт",
            ingredients = listOf("Яйцо", "Молоко"),
            instruction = "Смешать и пожарить.",
            cookTime = 10
        )
        recipeDao.insert(testRecipe)


        // Получение всех рецептов
        val recipes = recipeDao.getAllRecipes().first()
        Log.d("RoomTest", "Recipes: $recipes")
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Favorite", "FavoriteView умер")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Favorite", "Favorite Fragment умер")

    }
}