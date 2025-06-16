package com.paletteofflavors

import DataSource.Network.NetworkRecipe
import DataSource.model.RecipeSharedViewModel
import Repositories.toNetworkRecipe
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
import androidx.navigation.fragment.navArgs
import com.paletteofflavors.databinding.FragmentNetworkRecipeDetailsBinding
import com.paletteofflavors.databinding.FragmentRecipeDetailsBinding
import domain.Recipe
import kotlinx.coroutines.flow.collect
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


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                if (fragment == "Fridge")
                    sharedViewModel.selectedNetworkRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it) }
                    }

                else if(fragment == "Favorites")
                    sharedViewModel.selectedSavedRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it.toNetworkRecipe())}
                    }

                else if(fragment == "Search")
                    sharedViewModel.selectedNetworkRecipe.collect { recipe ->
                        recipe?.let { bindNetworkRecipeData(it)}
                    }
            }
        }


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
        binding.recipeDetailsTitle.text = networkRecipe.title
        binding.recipeDetailsCookingTime.text = "${networkRecipe.cookTime} мин"
        binding.recipeDetailsIngredientsList.text =
            networkRecipe.ingredients.joinToString("\n") { "• $it" }
        binding.instructionsText.text = networkRecipe.instruction
        binding.complexityRating.rating = networkRecipe.complexity.toFloat()
        binding.likesCount.text = networkRecipe.likesCount.toString()
        binding.commentsCount.text = networkRecipe.commentsCount.toString()
        binding.recipeTags.text = ": ${networkRecipe.mainCategory}, ${networkRecipe.secondaryCategory}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}