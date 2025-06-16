package com.paletteofflavors

import DataSource.Network.Turso
import DataSource.model.FavoritesViewModel
import DataSource.model.RecipeSharedViewModel
import Repositories.toSavedRecipe
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Configuration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.paletteofflavors.databinding.BottomSheetLayoutBinding
import com.paletteofflavors.databinding.FragmentSearchBinding
import domain.SavedRecipe
import kotlinx.coroutines.launch
import java.util.Locale


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomsheetScrollView: NestedScrollView
    private lateinit var networkRecipeAdapter: NetworkRecipeAdapter
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()
    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }
    private lateinit var recipesRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        setupBottomSheetBehavior()
        setupCategories()

        setupOnClickListeners()


    }

    private fun setupOnClickListeners() {
        binding.searchFragmentDinnerButton.setOnClickListener {
            createCategoryQuery(mainCategory = getRussianString(R.string.dinner))
            Toast.makeText(requireContext(), "Selected: Dinner", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentLaunchButton.setOnClickListener {
            createCategoryQuery(mainCategory = getRussianString(R.string.launch))
            Toast.makeText(requireContext(), "Selected: Launch", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentDessertsButton.setOnClickListener {
            createCategoryQuery(mainCategory = getRussianString(R.string.desserts))
            Toast.makeText(requireContext(), "Selected: Desert", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentBreakfastButton.setOnClickListener {
            createCategoryQuery(mainCategory = getRussianString(R.string.breakfast))
            Toast.makeText(requireContext(), "Selected: Breakfast", Toast.LENGTH_SHORT).show()
        }

        binding.searchFragmentFilterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selected: Show ALL Filters", Toast.LENGTH_SHORT)
                .show()
        }


        binding.backToSearchButton.setOnClickListener {
            binding.CoordinatorLayout.visibility = View.VISIBLE
            binding.filteredContent.visibility = View.GONE
        }

    }

    private fun setupBottomSheetBehavior() {
        bottomsheetScrollView = binding.bottomSheetInclude.bottomSheet
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetScrollView)
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

            createCategoryQuery(secondaryCategory = getRussianString(category.nameResId))
            Toast.makeText(requireContext(), "Selected: ${getString(category.nameResId)}", Toast.LENGTH_SHORT).show()
        }

        binding.bottomSheetInclude.categoriesRecyclerView.adapter = adapter
        binding.bottomSheetInclude.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(context)
    }

    private fun createCategoryQuery(
        mainCategory: String? = null,
        secondaryCategory: String? = null
    ) {

        lateinit var marginName: String
        lateinit var category: String

        if (!mainCategory.isNullOrEmpty()) {
            marginName = "main_category"
            category = mainCategory
        } else if (!secondaryCategory.isNullOrEmpty()) {
            marginName = "secondary_category"
            category = secondaryCategory
        }

        var query =
            """
            SELECT r.*
            FROM Recipes r
            WHERE r.$marginName == '$category'
        """

        Log.d("Recipe", query)

        OnCategoryButtonClick(requireActivity() as MainActivity, requireContext(), query)
    }

    private fun OnCategoryButtonClick(activity: MainActivity, context: Context, query: String) {


        val TursoConnection = Turso(activity, context)


        networkRecipeAdapter = NetworkRecipeAdapter(
            onItemClick = { networkRecipe ->
                sharedViewModel.selectNetworkRecipe(networkRecipe)
                activity.replaceMainFragment(
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
                    showDeleteRecipeConfirmDialog(recipe.toSavedRecipe(), holder)
                } else {
                    // Если не сохранено - сохраняем
                    viewModel.addSavedRecipe(recipe.toSavedRecipe())
                    holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_saved)
                }
            },
            isSaved = { recipeId ->
                viewModel.isRecipeSaved(recipeId)  // Возвращем сохранен или нет рецепт
            }
        )

        recipesRecyclerView.adapter = networkRecipeAdapter

        lifecycleScope.launch {
            try {
                TursoConnection.getAllNetworkRecipesFlow(query)
                    .collect { networkRecipes ->
                        networkRecipeAdapter.addRecipe(networkRecipes)
                    }
            } catch (e: Exception) {
                Log.d("NetworkProblem", "$e")
            }
        }


        /*
        lifecycleScope.launch {
            try {
                TursoConnection.getAllNetworkRecipesFlow(query)
                    .collect { networkRecipes ->

                        val strike = "${networkRecipes.recipeId} - ${networkRecipes.title} -" +
                                "${networkRecipes.mainCategory} - ${networkRecipes.secondaryCategory}"

                        Log.d("Recipe", strike)

                    }
            } catch (e: Exception) {
                Log.d("NetworkProblem", "$e")
            }
        }*/

        binding.CoordinatorLayout.visibility = View.GONE
        binding.filteredContent.visibility = View.VISIBLE
    }

    private fun showDeleteRecipeConfirmDialog(
        savedRecipe: SavedRecipe,
        holder: NetworkRecipeAdapter.RecipeHolder
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить рецепт ${savedRecipe.title}")

        builder.setPositiveButton("Удалить") { dialog: DialogInterface, _: Int ->
            viewModel.deleteSavedRecipe(savedRecipe)
            holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_unsaved)
        }

        builder.setNegativeButton("Отменить") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    /*
    fun postQuery(activity: MainActivity, context: Context) {

        val TursoConnection = Turso(activity, context)

        networkRecipeAdapter = NetworkRecipeAdapter(
            onItemClick = { networkRecipe ->
                sharedViewModel.selectNetworkRecipe(networkRecipe)
                activity.replaceMainFragment(
                    NetworkRecipeDetailsFragment("Fridge")
                )
            },
            onSaveOrDeleteButtonClick = { recipe, holder -> // TODO: Убрать зависимость от UI
                if (holder.savedOrDeletedImageView.drawable.constantState ==
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.icon_saved)?.constantState) {
                    // Если иконка показывает "сохранено" - удаляем
                    showDeleteRecipeConfirmDialog(recipe.toSavedRecipe(), holder)
                } else {
                    // Если не сохранено - сохраняем
                    viewModel.addSavedRecipe(recipe.toSavedRecipe())
                    holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_saved)
                }
            },
            isSaved = { recipeId ->
                viewModel.isRecipeSaved(recipeId)  // Возвращем сохранен или нет рецепт
            }
        )

        recipesRecyclerView.adapter = networkRecipeAdapter

        if (fridgeViewModel.getselectedIngredientsCount() == 0) {

            lifecycleScope.launch {
                try {
                    TursoConnection.getAllNetworkRecipesFlow()
                        .collect { networkRecipes ->
                            networkRecipeAdapter.addRecipe(networkRecipes)
                        }
                } catch (e: Exception) {
                    Log.d("NetworkProblem", "$e")
                }
            }


        } else {
            val query = createQuery()

            lifecycleScope.launch {
                try {
                    TursoConnection.getAllNetworkRecipesFlow(query)
                        .collect { networkRecipes ->
                            networkRecipeAdapter.addRecipe(networkRecipes)
                        }
                } catch (e: Exception) {
                    Log.d("NetworkProblem", "$e")
                }
            }
        }

        binding.fridgeIngredientsContent.visibility = View.GONE

    }
    */

    fun getRussianString(@StringRes resId: Int): String {
        val configuration = android.content.res.Configuration(context?.resources?.configuration)
        configuration.setLocale(Locale("ru"))
        Log.d("Name", context?.createConfigurationContext(configuration)?.getString(resId) ?: "")
        return context?.createConfigurationContext(configuration)?.getString(resId) ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}