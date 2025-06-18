package com.paletteofflavors.presentation.feature.main.view

import com.paletteofflavors.data.remote.API.Turso.Turso
import com.paletteofflavors.presentation.feature.main.viewmodel.FavoritesViewModel
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import com.paletteofflavors.data.local.database.converters.toSavedRecipe
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.presentation.feature.main.viewmodel.FridgeViewModel
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.presentation.feature.recipes.view.adapter.NetworkRecipeAdapter
import com.paletteofflavors.presentation.feature.recipes.view.NetworkRecipeDetailsFragment
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentFridgeBinding
import com.paletteofflavors.data.local.database.model.SavedRecipe
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale


class FridgeFragment : Fragment() {

    class Direction() {
        val forward = "forward"
        val downward = "downward"
        var direction = forward
    }

    val mostHaveDirection = Direction()
    val fruitsDirection = Direction()
    val vegetablesDirection = Direction()
    val dairyDirection = Direction()
    val berriesDirection = Direction()
    val mushroomsDirection = Direction()


    var _binding: FragmentFridgeBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()
    private val fridgeViewModel by lazy { FridgeViewModel() }
    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }

    private lateinit var networkRecipeAdapter: NetworkRecipeAdapter
    private lateinit var recipesRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        mostHaveDirection.direction = Direction().downward
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setupMostHaveIngredients()
        setListeners()
        setUpRecipesCountObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupMostHaveIngredients() {
        val mostHaveIngredients = listOf(
            MostHave(1, R.string.spaghetti),
            MostHave(2, R.string.bacon),
            MostHave(3, R.string.eggs),
            MostHave(4, R.string.parmesan),
            MostHave(5, R.string.milk),
            MostHave(6, R.string.pumpkin),
            MostHave(7, R.string.chicken_breast),
            MostHave(8, R.string.chocolate),
            MostHave(9, R.string.cream),
            MostHave(10, R.string.buckwheat)
        )

        val container1 = binding.mostHaveContent1
        val container2 = binding.mostHaveContent2

        container1.removeAllViews()
        container2.removeAllViews()

        val half = (mostHaveIngredients.size + 1) / 2
        val firstColumnItems = mostHaveIngredients.take(half)
        val secondColumnItems = mostHaveIngredients.drop(half)

        // Первый столбец
        firstColumnItems.forEach { ingredient ->
            val checkBox = CheckBox(requireContext()).apply {
                text = getString(ingredient.nameResId)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                buttonTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.dark_red)
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }

            checkBox.setOnClickListener {
                when (checkBox.isChecked) {
                    true -> fridgeViewModel.addIngredient(getRussianString(ingredient.nameResId))
                    false -> fridgeViewModel.removeIngredient(getRussianString(ingredient.nameResId))
                }
            }
            container1.addView(checkBox)
        }

        // Второй столбец
        secondColumnItems.forEach { ingredient ->
            val checkBox = CheckBox(requireContext()).apply {
                text = getString(ingredient.nameResId)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                buttonTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.dark_red)
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            checkBox.setOnClickListener {
                when (checkBox.isChecked) {
                    true -> fridgeViewModel.addIngredient(getRussianString(ingredient.nameResId))
                    false -> fridgeViewModel.removeIngredient(getRussianString(ingredient.nameResId))
                }
            }
            container2.addView(checkBox)
        }
    }

    private fun setListeners() {

        binding.mostHave.setOnClickListener {
            mostHaveVisible(mostHaveDirection, binding.mostHaveArrow)
        }
        binding.fruitsButton.setOnClickListener {
            changeArrowRotation(binding.fruitsButton, fruitsDirection)
        }
        binding.vegetablesButton.setOnClickListener {
            changeArrowRotation(binding.vegetablesButton, vegetablesDirection)
        }
        binding.dairyButton.setOnClickListener {
            changeArrowRotation(binding.dairyButton, dairyDirection)
        }
        binding.berriesButton.setOnClickListener {
            changeArrowRotation(binding.berriesButton, berriesDirection)
        }
        binding.mushroomsButton.setOnClickListener {
            changeArrowRotation(binding.mushroomsButton, mushroomsDirection)
        }


        binding.findButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Ищем рецепты с ${fridgeViewModel.getselectedIngredientsCount()} ингредиентами",
                Toast.LENGTH_SHORT
            ).show()
            postQuery(requireActivity() as MainActivity, requireContext())
        }

        binding.backToFridgeButton.setOnClickListener {
            binding.fridgeIngredientsContent.visibility = View.VISIBLE
        }

    }


    @SuppressLint("SetTextI18n")
    private fun setUpRecipesCountObserver() {
        fridgeViewModel.listOfSelectedIngredients.observe(viewLifecycleOwner) { list ->
            binding.selectedButton.text = getString(R.string.Selected) + list.size.toString()
        }
    }

    private fun getRussianString(@StringRes resId: Int): String {
        val configuration = android.content.res.Configuration(context?.resources?.configuration)
        configuration.setLocale(Locale("ru"))
        Log.d("Name", context?.createConfigurationContext(configuration)?.getString(resId) ?: "")
        return context?.createConfigurationContext(configuration)?.getString(resId) ?: ""
    }

    // Open and close drop list
    private fun mostHaveVisible(Direction: Direction, imageView: ImageView) {

        val arrowForward = R.drawable.arrow_forward_24px
        val arrowDownward = R.drawable.arrow_downward_24px


        val newIcon: Int = when (Direction.direction) {
            "forward" -> {
                Direction.direction = Direction().downward
                binding.mostHaveContent.visibility = View.VISIBLE
                arrowDownward
            }

            "downward" -> {
                Direction.direction = Direction().forward
                binding.mostHaveContent.visibility = View.GONE
                arrowForward
            }

            else -> {
                Direction.direction = Direction().downward
                binding.mostHaveContent.visibility = View.GONE
                arrowForward
            }
        }

        imageView.setImageResource(newIcon)
    }

    private fun changeArrowRotation(button: Button, Direction: Direction) {

        val currentDrawables = button.compoundDrawablesRelative
        val arrowForward = ContextCompat.getDrawable(button.context, R.drawable.arrow_forward_24px)
        val arrowDownward =
            ContextCompat.getDrawable(button.context, R.drawable.arrow_downward_24px)

        val newIcon: Drawable? = when (Direction.direction) {
            "forward" -> {
                Direction.direction = Direction().downward
                arrowDownward
            }

            "downward" -> {
                Direction.direction = Direction().forward
                arrowForward
            }

            else -> {
                Direction.direction = Direction().downward
                arrowForward
            }
        }

        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            currentDrawables[0],
            currentDrawables[1],
            newIcon,
            currentDrawables[3]
        )
    }


    private fun searchFromCachedRecipes() {
        viewModel.cashedRecipes.onEach { networkRecipes ->

            networkRecipeAdapter = NetworkRecipeAdapter(
                onItemClick = { networkRecipe ->
                    sharedViewModel.selectNetworkRecipe(networkRecipe)
                    (requireActivity() as MainActivity).replaceMainFragment(
                        NetworkRecipeDetailsFragment("Fridge")
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

    private fun recyclerViewForNetwork(activity: MainActivity) {
        networkRecipeAdapter = NetworkRecipeAdapter(
            onItemClick = { networkRecipe ->
                sharedViewModel.selectNetworkRecipe(networkRecipe)
                activity.replaceMainFragment(
                    NetworkRecipeDetailsFragment("Fridge")
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

    fun postQuery(activity: MainActivity, context: Context) {

        val TursoConnection = Turso(activity, context)

        if (!TursoConnection.checkInternetConnection(requireContext())) {
            Toast.makeText(requireContext(), "Используем кешированные рецепты", Toast.LENGTH_LONG)
                .show()

            searchFromCachedRecipes()

            binding.fridgeIngredientsContent.visibility = View.GONE
            return
        }


        // Если есть подключение к интернету
        recyclerViewForNetwork(activity)
        executeQuery(TursoConnection)

        binding.fridgeIngredientsContent.visibility = View.GONE

    }

    private fun executeQuery(TursoConnection: Turso) {
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
                            try {
                                networkRecipeAdapter.addRecipe(networkRecipes)
                            } catch (e: Exception) {
                                Log.d("NetworkProblem", "$e")
                            }
                        }
                } catch (e: Exception) {
                    Log.d("NetworkProblem", "$e")
                }
            }
        }
    }

    private fun createQuery(): String {

        var queryBuilder = StringBuilder(
            """
            SELECT r.*
            FROM Recipes r
            WHERE r.recipe_id IN (
                SELECT ri.recipe_id 
                FROM RecipeIngredients ri
                JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
                WHERE id.name IN (
        """
        )

        fridgeViewModel.listOfSelectedIngredients.value?.forEach { ingredient ->
            queryBuilder.append("'$ingredient',")
        }

        queryBuilder.deleteCharAt(queryBuilder.length - 1)
        queryBuilder.append("))")

        val query = queryBuilder.toString()

        Log.d("Query", query)
        return query
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

    data class MostHave(
        val id: Int,
        @StringRes val nameResId: Int,
    )
}