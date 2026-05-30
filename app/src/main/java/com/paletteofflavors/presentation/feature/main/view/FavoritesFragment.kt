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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.presentation.feature.recipes.view.CreateRecipeFragment
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.presentation.feature.recipes.view.adapter.NetworkRecipeAdapter
import com.paletteofflavors.presentation.feature.recipes.view.NetworkRecipeDetailsFragment
import com.paletteofflavors.R
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

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
        return binding.root
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
        val userDetails = (requireActivity() as MainActivity).sessionManager.getUsersDetailFromSession()
        val userId = userDetails[SessionManager.KEY_USER_ID]?.toIntOrNull() ?: -1
        
        viewModel.fetchMyRecipes(userId)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.myRecipes.collect { recipes ->
                    if (recipes.isEmpty()) {
                        hintUserRecipe.visibility = View.VISIBLE
                    } else {
                        hintUserRecipe.visibility = View.INVISIBLE
                    }

                    savedRecipeAdapter = NetworkRecipeAdapter(
                        onItemClick = { networkRecipe ->
                            sharedViewModel.selectNetworkRecipe(networkRecipe)
                            (requireActivity() as MainActivity).replaceMainFragment(
                                NetworkRecipeDetailsFragment("Favorites")
                            )
                        },
                        onSaveOrDeleteButtonClick = { networkRecipe, _ ->
                            showDeleteOwnRecipeConfirmDialog(networkRecipe)
                        },
                        isSaved = { _ ->
                            flow { emit(false) }
                        },
                        useDeleteIcon = true
                    ).apply {
                        addRecipes(recipes)
                    }
                    recipesRecyclerView.adapter = savedRecipeAdapter
                    hintRecipe.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun updateSavedRecipes() {
        hintUserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedRecipes.collect { savedRecipes ->
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
                            showDeleteSavedRecipeConfirmDialog(networkRecipe)
                        },
                        isSaved = { id -> 
                            viewModel.isRecipeSaved(id)
                        },
                        useDeleteIcon = false
                    ).apply {
                        addRecipes(savedRecipes)
                    }
                    recipesRecyclerView.adapter = savedRecipeAdapter
                    hintUserRecipe.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showDeleteSavedRecipeConfirmDialog(recipe: NetworkRecipe) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить рецепт ${recipe.title} из избранного?")
        builder.setPositiveButton("Удалить") { _, _ ->
            viewModel.deleteSavedRecipe(recipe)
        }
        builder.setNegativeButton("Отменить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showDeleteOwnRecipeConfirmDialog(recipe: NetworkRecipe) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление собственного рецепта")
        builder.setMessage("Вы уверены, что хотите полностью удалить рецепт ${recipe.title}?")
        builder.setPositiveButton("Удалить") { _, _ ->
            viewModel.deleteOwnRecipe(recipe)
        }
        builder.setNegativeButton("Отменить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}
