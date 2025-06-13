package com.paletteofflavors

import DataSource.Network.Turso
import DataSource.model.FavoritesViewModel
import DataSource.model.RecipeSharedViewModel
import Repositories.toSavedRecipe
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.unit.DpSize
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.paletteofflavors.databinding.FragmentFridgeBinding
import domain.Recipe
import domain.SavedRecipe
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64


class FridgeFragment : Fragment() {

    var _binding: FragmentFridgeBinding? = null
    val binding get() = _binding!!

    class Direction(){
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

    private val fridgeViewModel by lazy { FridgeViewModel() }
    private val viewModel: FavoritesViewModel by lazy {
        (requireActivity() as MainActivity).favoritesViewModel
    }

    private lateinit var networkRecipeAdapter: NetworkRecipeAdapter
    private lateinit var recipesRecyclerView: RecyclerView
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()


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

        recipesRecyclerView = binding.recipesRecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)

        setupMostHaveIngredients()
        setListeners()

        fridgeViewModel.listOfSelectedIngredients.observe(viewLifecycleOwner){ list ->
            binding.selectedButton.text = "Выбрано " + list.size.toString()
        }
    }

    private fun setupMostHaveIngredients() {
        val mostHaveIngredients = listOf(
            "Спагетти", "Бекон", "Яйца", "Пармезан", "Молоко",
            "Тыква", "Куриная грудка", "Шоколад", "Сливки", "Гречка"
        ).sorted()

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
                text = ingredient
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                buttonTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.dark_red))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }

            checkBox.setOnClickListener {
                when (checkBox.isChecked) {
                    true -> fridgeViewModel.addIngredient(ingredient)
                    false -> fridgeViewModel.removeIngredient(ingredient)
                }
            }
            container1.addView(checkBox)
        }

        // Второй столбец
        secondColumnItems.forEach { ingredient ->
            val checkBox = CheckBox(requireContext()).apply {
                text = ingredient
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                buttonTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.dark_red))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            checkBox.setOnClickListener {
                when(checkBox.isChecked){
                    true -> fridgeViewModel.addIngredient(ingredient)
                    false ->fridgeViewModel.removeIngredient(ingredient)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showDeleteRecipeConfirmDialog(savedRecipe: SavedRecipe, holder: NetworkRecipeAdapter.RecipeHolder) {
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


class FridgeViewModel : ViewModel() {

    private val _listOfingredients = MutableLiveData<MutableList<String>>(mutableListOf())
    val listOfSelectedIngredients: LiveData<MutableList<String>> = _listOfingredients


    fun addIngredient(name: String) {
        val currentList = _listOfingredients.value ?: mutableListOf()
        val newList = currentList.toMutableList().apply { add(name) }
        _listOfingredients.value = newList
    }

    fun removeIngredient(name: String) {
        val currentList = _listOfingredients.value ?: mutableListOf()
        val newList = currentList.toMutableList().apply { remove(name) }
        _listOfingredients.value = newList
    }

    fun getselectedIngredientsCount(): Int {
        return _listOfingredients.value?.size ?: 0
    }

}