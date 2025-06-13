package com.paletteofflavors

import DataSource.Network.NetworkRecipe
import DataSource.Network.Turso
import DataSource.model.FavoritesViewModel
import DataSource.model.FavoritesViewModelFactory
import DataSource.model.RecipeSharedViewModel
import Repositories.toNetworkRecipe
import Repositories.toSavedRecipe
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
import domain.SavedRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavoritesFragment() : Fragment() {


    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var savedRecipeAdapter: NetworkRecipeAdapter   // Такой же, потому что сохраняем сетевые рецепты

    private lateinit var hintRecipe: TextView
    private lateinit var hintuserRecipe: TextView
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


        hintRecipe = binding.favoritesFragmentMissingItemHint
        hintuserRecipe = binding.favoritesFragmentMissingItemHint2
        radioGroup = binding.favoritesFragmentRadioGroup


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
    }


    private fun updateSavedRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE


        viewModel.savedRecipes.onEach { savedRecipes ->
            if (savedRecipes.isEmpty()) {
                hintRecipe.visibility = View.VISIBLE
            } else {
                hintRecipe.visibility = View.INVISIBLE
            }

            // Конвертируем SavedRecipe в NetworkRecipe
            val networkRecipes = savedRecipes.map { it.toNetworkRecipe() }


            savedRecipeAdapter = NetworkRecipeAdapter(
                onItemClick = { networkRecipe ->
                    // Конвертируем обратно при клике, если нужно
                    sharedViewModel.selectSavedRecipe(networkRecipe.toSavedRecipe())
                    (requireActivity() as MainActivity).replaceMainFragment(
                        NetworkRecipeDetailsFragment("Favorites")
                    )
                },
                onSaveOrDeleteButtonClick = { networkRecipe, _ ->
                    showDeleteRecipeConfirmDialog(savedRecipe = networkRecipe.toSavedRecipe())
                },
                isSaved = { _ ->
                    flow { emit(true) } // Всегда возвращаем Flow<Boolean>, который излучает true
                }
            ).apply {
                // Добавляем все рецепты сразу
                addRecipes(networkRecipes)
            }
            recipesRecyclerView.adapter = savedRecipeAdapter

            hintuserRecipe.visibility = View.INVISIBLE
        }.launchIn(lifecycleScope)
    }


    /*
    private fun getSavedRecipeList() {
        val TursoConnection = Turso(requireActivity() as MainActivity, requireContext())

        savedRecipeAdapter = NetworkRecipeAdapter(
            onItemClick = { networkRecipe ->
                sharedViewModel.selectNetworkRecipe(networkRecipe)
                (requireActivity() as MainActivity).replaceMainFragment(
                    NetworkRecipeDetailsFragment("Favorites")
                )
            }
        )
        recipesRecyclerView.adapter = savedRecipeAdapter

        lifecycleScope.launch {
            try {

                hintRecipe.visibility = View.INVISIBLE

                TursoConnection.getAllNetworkRecipesFlow()
                    .collect { networkRecipes ->
                        savedRecipeAdapter.addRecipe(networkRecipes)
                    }
            } catch (e:Exception){
                Log.d("NetworkProblem", "$e")
                hintRecipe.visibility = View.VISIBLE
            }
        }
    }*/


    private fun showDeleteRecipeConfirmDialog(recipe: Recipe? = null, savedRecipe: SavedRecipe? = null) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        if (recipe != null) builder.setMessage("Вы уверены, что хотите удалить рецепт ${recipe.title}")
        if(savedRecipe!=null) builder.setMessage("Вы уверены, что хотите удалить рецепт ${savedRecipe.title}")

        builder.setPositiveButton("Удалить") { dialog: DialogInterface, _: Int ->
            if (recipe != null) viewModel.deleteRecipe(recipe)
            if(savedRecipe!=null) viewModel.deleteSavedRecipe(savedRecipe)

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