package com.paletteofflavors.presentation.feature.main.view

import com.paletteofflavors.presentation.feature.main.viewmodel.FavoritesViewModel
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.presentation.feature.recipes.view.CreateRecipeFragment
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.presentation.feature.recipes.view.adapter.NetworkRecipeAdapter
import com.paletteofflavors.presentation.feature.recipes.view.NetworkRecipeDetailsFragment
import com.paletteofflavors.R
import com.paletteofflavors.data.local.database.model.NetworkRecipe
import com.paletteofflavors.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class FavoritesFragment() : Fragment() {

    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var savedRecipeAdapter: NetworkRecipeAdapter
    private lateinit var hintRecipe: TextView
    private lateinit var hintUserRecipe: TextView
    private lateinit var radioGroup: RadioGroup


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindData()
        setUpListenersAndObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun bindData(){
        hintRecipe = binding.favoritesFragmentMissingItemHint
        hintUserRecipe = binding.favoritesFragmentMissingItemHint2
        radioGroup = binding.favoritesFragmentRadioGroup

        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        hintRecipe.visibility = View.INVISIBLE
        hintUserRecipe.visibility = View.INVISIBLE

        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        when (checkedRadioButtonId) {
            R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
            R.id.favorites_fragment_myRecipes -> updateMyRecipes()
        }
    }

    private fun setUpListenersAndObservers(){

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


    private fun updateMyRecipes(){
        // TODO:
        //  1) Реалзиовать получение данных о собственных рецептов с сервера.
        //  2) Реализовать фолбэк: получение рецептов из локальной бд, которая синхронизируется
        //     с серверной при первом подключении
    }
/*
    // For show recipe's lists
    private fun updateMyRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE

        viewModel.myRecipes.onEach { recipes ->
            if (recipes.isEmpty()) {
                hintuserRecipe.visibility = View.VISIBLE
            }
            else{
                hintuserRecipe.visibility = View.INVISIBLE
            }

            recipeAdapter = RecipeAdapter(
                recipeList = recipes,
                onItemClick = { recipe ->
                    sharedViewModel.selectRecipe(recipe)    // для актуального отображение
                    (requireActivity() as MainActivity).replaceMainFragment(RecipeDetailsFragment())
                },
                removeItem = { recipe ->
                    showDeleteRecipeConfirmDialog(recipe)
                }
            )
            recipesRecyclerView.adapter = recipeAdapter

            hintRecipe.visibility = View.INVISIBLE
        }.launchIn(lifecycleScope)
    }*/

    private fun updateSavedRecipes() {
        hintUserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        viewModel.savedRecipes.onEach { savedRecipes ->
            if (savedRecipes.isEmpty()) {
                hintRecipe.visibility = View.VISIBLE
            } else {
                hintRecipe.visibility = View.INVISIBLE
            }


            savedRecipeAdapter = NetworkRecipeAdapter(
                onItemClick = { networkRecipe ->
                    sharedViewModel.selectNetworkRecipe(networkRecipe)
                    (requireActivity() as MainActivity).replaceMainFragment(
                        NetworkRecipeDetailsFragment("Favorites")
                    )
                },
                onSaveOrDeleteButtonClick = { networkRecipe, _ ->
                    showDeleteRecipeConfirmDialog(networkRecipe)
                },
                isSaved = { _ ->
                    flow { emit(true) } // все элементы в избранном уже сохранены
                }
            ).apply {
                addRecipes(savedRecipes)
            }
            recipesRecyclerView.adapter = savedRecipeAdapter

            hintUserRecipe.visibility = View.INVISIBLE
        }.launchIn(lifecycleScope)
    }


    private fun showDeleteRecipeConfirmDialog(recipe: NetworkRecipe) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить рецепт ${recipe.title} из избранного?")

        builder.setPositiveButton("Удалить") { dialog: DialogInterface, _: Int ->
            viewModel.deleteSavedRecipe(recipe)
        }

        builder.setNegativeButton("Отменить") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}