package com.paletteofflavors

import DataSource.Network.NetworkRecipe
import DataSource.model.RecipeSharedViewModel
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
                sharedViewModel.selectedNetworkRecipe.collect { recipe ->
                    recipe?.let { bindNetworkRecipeData(it) }
                }
            }
        }


        binding.backButtonNetworkRecipeDetails.setOnClickListener {
            when(fragment){
                "Favorites" ->
                    (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
                "Fridge" ->
                    (requireActivity() as MainActivity).replaceMainFragment(FridgeFragment())
                else ->
                    (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
            }
        }
    }


    private fun bindNetworkRecipeData(networkRecipe: NetworkRecipe) {
        binding.recipeDetailsTitle.text = networkRecipe.title
        binding.recipeDetailsCookingTime.text = "${networkRecipe.cookTime} мин"
        binding.recipeDetailsIngredientsList.text = networkRecipe.ingredients.joinToString("\n") { "• $it" }
        binding.instructionsText.text = networkRecipe.instruction
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}