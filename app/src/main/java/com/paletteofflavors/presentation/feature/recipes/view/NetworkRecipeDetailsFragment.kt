package com.paletteofflavors.presentation.feature.recipes.view

import com.paletteofflavors.data.local.database.model.NetworkRecipe
import com.paletteofflavors.data.local.database.converters.toNetworkRecipe
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.databinding.FragmentNetworkRecipeDetailsBinding
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment
import com.paletteofflavors.presentation.feature.main.view.FridgeFragment
import com.paletteofflavors.presentation.feature.main.view.SearchFragment
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import kotlinx.coroutines.launch

class NetworkRecipeDetailsFragment(val fragment: String) : Fragment() {

    private var _binding: FragmentNetworkRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkRecipeDetailsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindCurrentNetworkRecipe()
        setUpBackButtonListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindCurrentNetworkRecipe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                when (fragment) {
                    "Fridge" -> sharedViewModel.selectedNetworkRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it) }
                    }

                    "Favorites" -> sharedViewModel.selectedSavedRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it.toNetworkRecipe()) }
                    }

                    "Search" -> sharedViewModel.selectedNetworkRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it) }
                    }
                }
            }
        }
    }

    private fun setUpBackButtonListener() {
        binding.backButtonNetworkRecipeDetails.setOnClickListener {
            when (fragment) {
                "Favorites" ->
                    (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())

                "Fridge" ->
                    (requireActivity() as MainActivity).replaceMainFragment(FridgeFragment())

                "Search" ->
                    (requireActivity() as MainActivity).replaceMainFragment(SearchFragment())

                else ->
                    (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun bindNetworkRecipeData(networkRecipe: NetworkRecipe) {

        binding.run {
            recipeDetailsTitle.text = networkRecipe.title
            recipeDetailsCookingTime.text = "${networkRecipe.cookTime} мин"
            recipeDetailsIngredientsList.text =
                networkRecipe.ingredients.joinToString("\n") { "• $it" }
            instructionsText.text = networkRecipe.instruction
            complexityRating.rating = networkRecipe.complexity.toFloat()
            likesCount.text = networkRecipe.likesCount.toString()
            commentsCount.text = networkRecipe.commentsCount.toString()
            recipeTags.text = ": ${networkRecipe.mainCategory}, ${networkRecipe.secondaryCategory}"
        }
    }
}