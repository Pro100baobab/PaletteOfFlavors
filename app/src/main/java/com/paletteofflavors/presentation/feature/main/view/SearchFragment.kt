package com.paletteofflavors.presentation.feature.main.view

import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.paletteofflavors.presentation.feature.main.view.adapter.CategoriesAdapter
import com.paletteofflavors.presentation.feature.main.view.adapter.Category
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.presentation.feature.recipes.view.adapter.NetworkRecipeAdapter
import com.paletteofflavors.presentation.feature.recipes.view.NetworkRecipeDetailsFragment
import com.paletteofflavors.R
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.databinding.FragmentSearchBinding
import com.paletteofflavors.presentation.feature.main.viewmodel.FavoritesViewModel
import com.paletteofflavors.presentation.feature.main.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetScrollView: NestedScrollView
    private lateinit var networkRecipeAdapter: NetworkRecipeAdapter
    private lateinit var recipesRecyclerView: RecyclerView


    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()

    private val searchViewModel: SearchViewModel by viewModels {
        (requireActivity() as MainActivity).searchViewModelFactory
    }

    // Нужно для кешированных рецептов (пока используется FavoritesViewModel и не вынесено из нее)
    private val cachedRecipeViewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setupBottomSheetBehavior()
        setupCategories()
        setupOnClickListeners()

        observeSearchResults()
        observeNetworkError()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView(){
        binding.CoordinatorLayout.visibility = View.VISIBLE
        binding.filteredContent.visibility = View.GONE

        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        networkRecipeAdapter = createNetworkRecipeAdapter()
        recipesRecyclerView.adapter = networkRecipeAdapter
    }

    private fun setupOnClickListeners() {

        // Main category button
        binding.searchFragmentDinnerButton.setOnClickListener {
            onMainCategoryButtonClick(mainCategory = getRussianString(R.string.dinner))
            Toast.makeText(requireContext(), "Selected: Dinner", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentLaunchButton.setOnClickListener {
            onMainCategoryButtonClick(mainCategory = getRussianString(R.string.launch))
            Toast.makeText(requireContext(), "Selected: Launch", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentDessertsButton.setOnClickListener {
            onMainCategoryButtonClick(mainCategory = getRussianString(R.string.desserts))
            Toast.makeText(requireContext(), "Selected: Desert", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentBreakfastButton.setOnClickListener {
            onMainCategoryButtonClick(mainCategory = getRussianString(R.string.breakfast))
            Toast.makeText(requireContext(), "Selected: Breakfast", Toast.LENGTH_SHORT).show()
        }

        // Filter button
        binding.searchFragmentFilterButton.setOnClickListener {
            Toast.makeText(requireContext(), "В разработке", Toast.LENGTH_SHORT)
                .show()
        }

        // Back button
        binding.backToSearchButton.setOnClickListener {
            binding.CoordinatorLayout.visibility = View.VISIBLE
            binding.filteredContent.visibility = View.GONE
        }

        // Search button
        binding.searchFragmentSearchRecipeString.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                Log.d("SearchEnter", "Нажат Enter")
                findRecipesByWords(binding.searchFragmentSearchRecipeString.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

    }

    private fun setupBottomSheetBehavior() {
        bottomSheetScrollView = binding.bottomSheetInclude.bottomSheet
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetScrollView)
        bottomSheetBehavior.peekHeight = 250
        bottomSheetBehavior.maxHeight = 1000
    }
    private fun setupCategories() {
        val categories = listOf(
            Category(requireContext(),1, R.string.cold_eat, R.drawable.icon_saved),
            Category(requireContext(),2, R.string.hot_eat),
            Category(requireContext(),3, R.string.vegetable_salads),
            Category(requireContext(),4, R.string.meat_salads),
            Category(requireContext(),5, R.string.poultry_salads),
            Category(requireContext(),6, R.string.fish_salads),
            Category(requireContext(),7, R.string.soups),
            Category(requireContext(),8, R.string.vegetable_dishes),
            Category(requireContext(),9, R.string.meat_dishes),
            Category(requireContext(),10, R.string.poulry_dishes),
            Category(requireContext(),11, R.string.fish_dishes),
            Category(requireContext(),12, R.string.side_dishes),
            Category(requireContext(),13, R.string.bakery),
            Category(requireContext(),14, R.string.drinks),
            Category(requireContext(),15, R.string.first_courses),
            Category(requireContext(),16, R.string.second_courses)
        )

        val adapter = CategoriesAdapter(categories) { category ->

            onSecondaryCategoryButtonClick(getRussianString(category.nameResId))
            Toast.makeText(requireContext(), "Selected: ${getString(category.nameResId)}", Toast.LENGTH_SHORT).show()
        }

        binding.bottomSheetInclude.categoriesRecyclerView.adapter = adapter
        binding.bottomSheetInclude.categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun getRussianString(@StringRes resId: Int): String {
        val configuration = android.content.res.Configuration(context?.resources?.configuration)
        configuration.setLocale(Locale("ru"))
        Log.d("Name", context?.createConfigurationContext(configuration)?.getString(resId) ?: "")
        return context?.createConfigurationContext(configuration)?.getString(resId) ?: ""
    }

    // Поиск по словам в названии и ингредиентах
    private fun findRecipesByWords(searchText: String){
        val searchWords = searchText.split("\\s+".toRegex())

        val formattedWords = searchWords.map { word ->
            if (word.isNotEmpty()) {
                word.substring(0, 1).uppercase() + word.substring(1)
            } else {
                ""
            }
        }

        binding.CoordinatorLayout.visibility = View.GONE
        binding.filteredContent.visibility = View.VISIBLE

        if(formattedWords.isEmpty())
            searchViewModel.searchAll()
        else
            searchViewModel.searchByTitleOrIngredient(formattedWords)
    }

    private fun onMainCategoryButtonClick(mainCategory: String) {
        binding.CoordinatorLayout.visibility = View.GONE
        binding.filteredContent.visibility = View.VISIBLE

        searchViewModel.searchByMainCategory(mainCategory)
    }

    // Поиск по категориям
    private fun onSecondaryCategoryButtonClick(secondaryCategory: String) {
        binding.CoordinatorLayout.visibility = View.GONE
        binding.filteredContent.visibility = View.VISIBLE

        searchViewModel.searchBySecondaryCategory(secondaryCategory)
    }

    private fun searchFromCache() {
        lifecycleScope.launch {
            val cachedRecipes = try {
                cachedRecipeViewModel.cashedRecipes.first()
            } catch (e: Exception) {
                emptyList()
            }

            networkRecipeAdapter.clearRecipes()

            if (cachedRecipes.isNotEmpty()) {
                networkRecipeAdapter.addRecipes(cachedRecipes)
            } else {
                Toast.makeText(requireContext(), "Кешированных рецептов нет", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Создание адаптера
    private fun createNetworkRecipeAdapter(): NetworkRecipeAdapter {
        return NetworkRecipeAdapter(
            onItemClick = { networkRecipe ->
                sharedViewModel.selectNetworkRecipe(networkRecipe)
                (requireActivity() as MainActivity).replaceMainFragment(
                    NetworkRecipeDetailsFragment("Search")
                )
            },
            onSaveOrDeleteButtonClick = { recipe, holder -> // TODO: Убрать зависимость от UI
                if (holder.savedOrDeletedImageView.drawable.constantState ==
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.icon_saved
                    )?.constantState
                ) {
                    // Если иконка показывает "сохранено" - удаляем
                    showDeleteRecipeConfirmDialog(recipe, holder)
                } else {
                    // Если не сохранено - сохраняем
                    cachedRecipeViewModel.addSavedRecipe(recipe)
                    holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_saved)
                }
            },
            isSaved = { recipeId ->
                cachedRecipeViewModel.isRecipeSaved(recipeId)  // Возвращем сохранен или нет рецепт
            }
        )
    }

    private fun observeSearchResults() {
        lifecycleScope.launch {
            searchViewModel.searchResults.collect { recipes ->
                networkRecipeAdapter.clearRecipes()
                networkRecipeAdapter.addRecipes(recipes)
            }
        }
    }

    private fun observeNetworkError() {
        lifecycleScope.launch {
            searchViewModel.isNetworkError.collect { isError ->
                if (isError) {
                    Toast.makeText(requireContext(), "Используем кешированные рецепты", Toast.LENGTH_LONG).show()
                    searchFromCache()
                }
            }
        }
    }

    private fun showDeleteRecipeConfirmDialog(
        savedRecipe: NetworkRecipe,
        holder: NetworkRecipeAdapter.RecipeHolder
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить рецепт ${savedRecipe.title}")

        builder.setPositiveButton("Удалить") { dialog: DialogInterface, _: Int ->
            cachedRecipeViewModel.deleteSavedRecipe(savedRecipe)
            holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_unsaved)
        }

        builder.setNegativeButton("Отменить") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}