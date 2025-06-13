package ViewModels

import DataSource.Local.RecipeDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class CreateRecipeViewModel(private val recipeDao: RecipeDao): ViewModel() {

    private val _title = MutableLiveData<String>()
    private val _ingredients = MutableLiveData<String>()
    private val _instruction = MutableLiveData<String>()
    private val _timeInMinutes = MutableLiveData<String>()

    val title: LiveData<String> = _title
    val ingredients: LiveData<String> = _ingredients
    val instruction: LiveData<String> = _instruction
    val timeInMinutes: LiveData<String> = _timeInMinutes

    fun setTitle(title: String){
        _title.value = title
    }

    fun setIngredients(ingredients: String){
        _ingredients.value = ingredients
    }

    fun setInstruction(instruction: String){
        _instruction.value = instruction
    }

    fun setTimeInMinutes(timeInMintues: Int){
        _timeInMinutes.value = when(timeInMintues){
            0 -> ""
            else -> timeInMintues.toString()
        }
    }


    fun saveRecipe() {
        viewModelScope.launch {
            val ingredientsList = _ingredients.value?.split("\n")?.filter {
                it.isNotBlank() } ?: emptyList()

            val recipe = Recipe(
                title = _title.value ?: "",
                ingredients = ingredientsList,
                instruction = _instruction.value ?: "",
                cookTime = _timeInMinutes.value?.toInt() ?: 0
            )

            recipeDao.insert(recipe)
        }
    }

    // Получение всех рецептов (getAllRecipes() возвращает List<Recipe>)
    suspend fun getRecipes(): List<Recipe> {
        return recipeDao.getAllRecipes().first()
    }

    fun cleanRecipeData(){
        _title.value = ""
        _ingredients.value = ""
        _instruction.value = ""
        _timeInMinutes.value = ""
    }
}