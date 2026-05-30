package com.paletteofflavors.presentation.feature.recipes.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.paletteofflavors.R
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.databinding.FragmentNetworkRecipeDetailsBinding
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment
import com.paletteofflavors.presentation.feature.recipes.view.adapter.CommentAdapter
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeDetailsViewModel
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch
import android.content.Intent

class NetworkRecipeDetailsFragment(val fragment: String) : Fragment() {

    private var _binding: FragmentNetworkRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()
    private val viewModel: RecipeDetailsViewModel by lazy {
        (requireActivity() as MainActivity).recipeDetailsViewModel
    }
    
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkRecipeDetailsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setUpListeners()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.commentsRecyclerView.adapter = commentAdapter
    }

    private fun setUpListeners() {
        binding.backButtonNetworkRecipeDetails.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
        }

        val sessionManager = (requireActivity() as MainActivity).sessionManager
        val userId = sessionManager.getUsersDetailFromSession()[SessionManager.KEY_USER_ID]?.toIntOrNull() ?: -1

        binding.likesCount.setOnClickListener {
            viewModel.likeRecipe(userId)
        }

        binding.sendCommentButton.setOnClickListener {
            val text = binding.commentInput.text.toString()
            if (text.isNotBlank()) {
                viewModel.addComment(userId, text)
                binding.commentInput.text.clear()
            }
        }

        binding.followButton.setOnClickListener {
            viewModel.toggleFollow(userId)
        }

        binding.shareButtonNetworkRecipeDetails.setOnClickListener {
            val recipe = sharedViewModel.selectedRecipe.value
            recipe?.let { shareRecipeAsText(it) }
        }
    }

    private fun shareRecipeAsText(recipe: NetworkRecipe) {
        val ingredientsText = recipe.ingredients.joinToString("\n") { "• $it" }
        
        val shareBody = """
            🍳 ${recipe.title}
            
            ⏱ Время приготовления: ${recipe.cookTime} мин
            ⭐ Сложность: ${recipe.complexity}/5
            
            📝 Ингредиенты:
            $ingredientsText
            
            👨‍🍳 Способ приготовления:
            ${recipe.instruction}
            
            Отправлено из приложения "Палитра Вкусов"
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipe.title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        startActivity(Intent.createChooser(intent, "Поделиться рецептом"))
    }

    private fun observeData() {
        val sessionManager = (requireActivity() as MainActivity).sessionManager
        val userId = sessionManager.getUsersDetailFromSession()[SessionManager.KEY_USER_ID]?.toIntOrNull() ?: -1

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.selectedRecipe.collect { recipe ->
                        recipe?.let { 
                            bindNetworkRecipeData(it)
                            viewModel.setRecipe(it, userId)
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { comments ->
                        commentAdapter.setComments(comments)
                    }
                }
                launch {
                    viewModel.likesCount.collect { count ->
                        binding.likesCount.text = count.toString()
                    }
                }
                launch {
                    viewModel.isLiked.collect { isLiked ->
                        val icon = if (isLiked) R.drawable.favorites_icon else R.drawable.icon_unsaved
                        binding.likesCount.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
                    }
                }
                launch {
                    viewModel.isFollowing.collect { isFollowing ->
                        binding.followButton.text = if (isFollowing) "Отписаться" else "Подписаться"
                    }
                }
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
            commentsCount.text = networkRecipe.commentsCount.toString()
            recipeTags.text = ": ${networkRecipe.mainCategory}, ${networkRecipe.secondaryCategory}"
            
            // Hidden if owner
            val currentUserId = (requireActivity() as MainActivity).sessionManager.getUsersDetailFromSession()[SessionManager.KEY_USER_ID]?.toIntOrNull() ?: -1
            followButton.visibility = if (networkRecipe.ownerId == currentUserId) View.GONE else View.VISIBLE
        }
    }
}
