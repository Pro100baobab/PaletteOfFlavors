package com.paletteofflavors.presentation.feature.recipes.view

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
import com.paletteofflavors.databinding.FragmentRecipeDetailsBinding
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import com.paletteofflavors.data.local.database.model.Recipe
import kotlinx.coroutines.launch

class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAndBindCurrentRecipe()

        binding.backButtonNetworkRecipeDetails.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




    private fun getAndBindCurrentRecipe(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedRecipe.collect { recipe ->
                    recipe?.let { bindRecipeData(it) }
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun bindRecipeData(recipe: Recipe) {
        binding.recipeDetailsTitle.text = recipe.title
        binding.recipeDetailsCookingTime.text = "${recipe.cookTime} мин"
        binding.recipeDetailsIngredientsList.text = recipe.ingredients.joinToString("\n") { "• $it" }
        binding.instructionsText.text = recipe.instruction
        binding.likesCount.text = "0"
        binding.commentsCount.text = "0"
        binding.complexityRating.rating = recipe.complexity.toFloat()
        binding.recipeTags.text = ": ${recipe.mainCategory}, ${recipe.secondaryCategory}"

    }



}