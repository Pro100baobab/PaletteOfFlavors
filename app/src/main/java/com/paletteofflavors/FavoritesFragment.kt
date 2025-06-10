package com.paletteofflavors

import DataSource.model.FavoritesViewModel
import DataSource.model.FavoritesViewModelFactory
import DataSource.model.RecipeSharedViewModel
import ViewModels.CreateRecipeViewModel
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.databinding.ActivityMainBinding
import com.paletteofflavors.databinding.FragmentFavoritesBinding
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import domain.Recipe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class FavoritesFragment() : Fragment() {


    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()

    private val resipesViewModel: CreateRecipeViewModel by lazy {
        ((requireActivity() as MainActivity).createRecipeViewModel)
    }

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        hintRecipe = binding.favoritesFragmentMissingItemHint
        hintuserRecipe = binding.favoritesFragmentMissingItemHint2
        radioGroup = binding.favoritesFragmentRadioGroup
        //savedRadioButton = _binding.favoritesFragmentSavedRecipes
        //userRadioButton = _binding.favoritesFragmentMyRecipes

        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        hintRecipe.visibility = View.INVISIBLE
        hintuserRecipe.visibility = View.INVISIBLE

        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        when (checkedRadioButtonId) {
            R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
            R.id.favorites_fragment_myRecipes -> updateMyRecipes()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            viewModel.setRadioButtonId(checkedId)

            when (checkedId) {
                R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
                R.id.favorites_fragment_myRecipes -> updateMyRecipes()
            }
        }

        viewModel.radioButton.observe(viewLifecycleOwner) { id ->
            binding.favoritesFragmentRadioGroup.check(id)
        }

        binding.createRecipe.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(CreateRecipeFragment())
        }

    }


    private fun updateMyRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        viewModel.myRecipes.onEach { recipes ->
            if (recipes.isEmpty()) {
                hintuserRecipe.visibility = View.VISIBLE
            }

            recipeAdapter = RecipeAdapter(
                recipeList = recipes,
                onItemClick = { recipe ->
                    sharedViewModel.selectRecipe(recipe)
                    (requireActivity() as MainActivity).replaceMainFragment(RecipeDetailsFragment())
                },
                removeItem = { recipe ->
                    showDeleteRecipeConfirmDialog(recipe)
                }
            )
            recipesRecyclerView.adapter = recipeAdapter

        }.launchIn(lifecycleScope)
    }



    private fun updateSavedRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        val recipelist = getSavedRecipeList()
        if (recipelist.isEmpty()) {
            hintRecipe.visibility = View.VISIBLE
        }

        recipeAdapter = RecipeAdapter(
            recipeList = recipelist,
            onItemClick = { recipe ->
                sharedViewModel.selectRecipe(recipe)
                (requireActivity() as MainActivity).showFullscreenFragment(RecipeDetailsFragment())
            },
            removeItem = { recipe ->
                //viewModel.deleteRecipe(recipe) не та база
            }
        )
        recipesRecyclerView.adapter = recipeAdapter
    }


    private fun getSavedRecipeList(): ArrayList<Recipe> {
        // TODO(): Inclement logic of getting data from local database(saved recipes)
        val recipelist = arrayListOf<Recipe>(
            Recipe(
                id = 1,
                title = "Пример 1",
                ingredients = List<String>(2, { "биба"; "боба" }),
                instruction = "Инструкция",
                cookTime = 60,
                //comments = 4,
                //likes = 5,
                imageUrl = null
            ),
            Recipe(
                2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120, null,/* 7, likes = 3*/
            ),
            Recipe(
                2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120, null /*,7, likes = 3*/
            ),
            Recipe(
                2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                120, null /*,7, likes = 3*/
            )
        )

        return recipelist
    }


    private fun showDeleteRecipeConfirmDialog(recipe: Recipe) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить рецепт ${recipe.title}")

        builder.setPositiveButton("Удалить") { dialog: DialogInterface, _: Int ->
            viewModel.deleteRecipe(recipe)
        }

        builder.setNegativeButton("Отменить") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}