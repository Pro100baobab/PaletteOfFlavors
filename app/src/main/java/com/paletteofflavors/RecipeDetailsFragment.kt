package com.paletteofflavors

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
import com.paletteofflavors.databinding.FragmentRecipeDetailsBinding
import domain.Recipe
import kotlinx.coroutines.launch

class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedRecipe.collect { recipe ->
                    recipe?.let { bindRecipeData(it) }
                }
            }
        }


        binding.backButtonRecipeDetails.setOnClickListener {
            //parentFragmentManager.popBackStack()
            //(requireActivity() as MainActivity).returnNavigation()
            (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
        }
    }


    private fun bindRecipeData(recipe: Recipe) {
        binding.recipeDetailsTitle.text = recipe.title
        binding.recipeDetailsCookingTime.text = "${recipe.cookTime} мин"
        binding.recipeDetailsIngredientsList.text = recipe.ingredients.joinToString("\n") { "• $it" }
        binding.instructionsText.text = recipe.instruction
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}