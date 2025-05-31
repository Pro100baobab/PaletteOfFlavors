package ViewModels

import DataSource.Local.RecipeDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.launch

class CreateRecipeViewModel(private val recipeDao: RecipeDao): ViewModel() {

    private val _title = MutableLiveData<String>()
    private val _ingredients = MutableLiveData<String>()
    private val _instruction = MutableLiveData<String>()
    private val _timeInMinutes = MutableLiveData<Int>()

    val title: LiveData<String> = _title
    val ingredients: LiveData<String> = _ingredients
    val instruction: LiveData<String> = _instruction
    val timeInMinutes: LiveData<Int> = _timeInMinutes

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
        _timeInMinutes.value = timeInMintues
    }


    fun saveRecipe() {
        viewModelScope.launch {
            val ingredientsList = _ingredients.value?.split("\n")?.filter {
                it.isNotBlank() } ?: emptyList()

            val recipe = Recipe(
                title = _title.value ?: "",
                ingredients = ingredientsList,
                instruction = _instruction.value ?: "",
                cookTime = _timeInMinutes.value ?: 0
            )

            recipeDao.insert(recipe)
        }
    }

    fun cleanRecipeData(){
        _title.value = ""
        _ingredients.value = ""
        _instruction.value = ""
        _timeInMinutes.value = null
    }
}