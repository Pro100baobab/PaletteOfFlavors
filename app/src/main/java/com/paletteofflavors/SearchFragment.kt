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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.paletteofflavors.databinding.FragmentSearchBinding
import domain.SavedRecipe
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomsheetScrollView: NestedScrollView
    private lateinit var networkRecipeAdapter: NetworkRecipeAdapter
    private lateinit var recipesRecyclerView: RecyclerView


    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()
    private val viewModel: FavoritesViewModel by lazy {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





    private fun setUpRecyclerView(){
        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupOnClickListeners() {

        // Main category button
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
                formatAndSearch(binding.searchFragmentSearchRecipeString.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
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
    // Получение слов по id для составления запроса к бд
    fun getRussianString(@StringRes resId: Int): String {
        val configuration = android.content.res.Configuration(context?.resources?.configuration)
        configuration.setLocale(Locale("ru"))
        Log.d("Name", context?.createConfigurationContext(configuration)?.getString(resId) ?: "")
        return context?.createConfigurationContext(configuration)?.getString(resId) ?: ""
    }





    // Преобразование строки поиска и создание запроса
    fun formatAndSearch(searchText: String){
        val searchWords = searchText.split("\\s+".toRegex())

        val formattedWords = searchWords.map { word ->
            if (word.isNotEmpty()) {
                word.substring(0, 1).uppercase() + word.substring(1) // Первая буква в верхний регистр, остальное - как есть
            } else {
                ""
            }
        }

        // Создание строки для IN(...)
        val words = formattedWords.joinToString(separator = "', '", prefix = "'", postfix = "'")

        //
        val query = """
        SELECT * FROM Recipes WHERE title IN ($words)
        UNION ALL
        SELECT r.*
        FROM Recipes r
        WHERE EXISTS (
            SELECT 1
            FROM RecipeIngredients ri
            JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
            WHERE ri.recipe_id = r.recipe_id AND id.name IN ($words)
        )
    """

        Log.d("Query", query)

        OnCategoryButtonClick(requireActivity() as MainActivity, requireContext(), query)
    }

    // Создание запросов по категориям
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

    // Поиск по категориям
    private fun OnCategoryButtonClick(activity: MainActivity, context: Context, query: String) {

        val TursoConnection = Turso(activity, context)

        // Если подключения нет
        if (!TursoConnection.checkInternetConnection(requireContext())) {
            Toast.makeText(requireContext(), "Используем кешированные рецепты", Toast.LENGTH_LONG)
                .show()

            searchFromCache()
            binding.CoordinatorLayout.visibility = View.GONE
            binding.filteredContent.visibility = View.VISIBLE
            return
        }

        // Если подключение есть
        createRecyclerViewAdapter(activity)
        executeQuery(TursoConnection, query)


        binding.CoordinatorLayout.visibility = View.GONE
        binding.filteredContent.visibility = View.VISIBLE
    }


    // Поиск в кеше
    private fun searchFromCache(){
        viewModel.cashedRecipes.onEach { networkRecipes ->

            networkRecipeAdapter = NetworkRecipeAdapter(
                onItemClick = { networkRecipe ->
                    sharedViewModel.selectNetworkRecipe(networkRecipe)
                    (requireActivity() as MainActivity).replaceMainFragment(
                        NetworkRecipeDetailsFragment("Search")
                    )
                },
                onSaveOrDeleteButtonClick = { recipe, holder ->
                    if (holder.savedOrDeletedImageView.drawable.constantState ==
                        ContextCompat.getDrawable(
                            holder.itemView.context,
                            R.drawable.icon_saved
                        )?.constantState
                    ) {
                        showDeleteRecipeConfirmDialog(recipe.toSavedRecipe(), holder)
                    } else {
                        viewModel.addSavedRecipe(recipe.toSavedRecipe())
                        holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_saved)
                    }
                },
                isSaved = { recipeId ->
                    viewModel.isRecipeSaved(recipeId)  // Возвращем сохранен или нет рецепт
                }
            ).apply {
                // Добавляем все рецепты сразу
                addRecipes(networkRecipes)
            }
            recipesRecyclerView.adapter = networkRecipeAdapter
        }.launchIn(lifecycleScope)

    }

    // Создание адаптера
    private fun createRecyclerViewAdapter(activity: MainActivity){
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
    }


    // Выполнение сетевого запроса
    private fun executeQuery(TursoConnection: Turso, query: String){
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




    // Окно подтверждения для удаления рецепта
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
}