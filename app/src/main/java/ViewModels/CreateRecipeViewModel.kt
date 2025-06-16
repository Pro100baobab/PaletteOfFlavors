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
    private val _ratingBarCount = MutableLiveData<String>()

    private val _mainCategory = MutableLiveData<String>()
    private val _secondaryCategory = MutableLiveData<String>()

    private var _mainPos = MutableLiveData<String>()
    private val _secondaryPos = MutableLiveData<String>()

    val title: LiveData<String> = _title
    val ingredients: LiveData<String> = _ingredients
    val instruction: LiveData<String> = _instruction
    val timeInMinutes: LiveData<String> = _timeInMinutes
    val ratingBarCount: LiveData<String> = _ratingBarCount
    val mainCategory: LiveData<String> = _mainCategory
    val secondaryCategory: LiveData<String> = _secondaryCategory
    val mainPos: LiveData<String> = _mainPos
    val secondaryPos: LiveData<String> = _secondaryPos

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

    fun setRatingBarCount(starsCount: Float){
        _ratingBarCount.value = when(starsCount){
            0f -> ""
            else -> starsCount.toString()
        }
    }

    fun setMainCategory(category: String, position: String){
        _mainCategory.value = category
        _mainPos.value = position
    }

    fun setSecondaryCategory(category: String, position: String){
        _secondaryCategory.value = category
        _secondaryPos.value = position
    }


    fun saveRecipe() {
        viewModelScope.launch {
            val ingredientsList = _ingredients.value?.split("\n")?.filter {
                it.isNotBlank() } ?: emptyList()

            val recipe = Recipe(
                title = _title.value ?: "",
                ingredients = ingredientsList,
                instruction = _instruction.value ?: "",
                cookTime = _timeInMinutes.value?.toInt() ?: 0,
                complexity = _ratingBarCount.value?.toFloat()?.toInt()?:1,
                mainCategory = _mainCategory.value.toString(),
                secondaryCategory = _secondaryCategory.value.toString()
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
        _ratingBarCount.value = "0f"
        _mainCategory.value = ""
        _secondaryCategory.value = ""
        _mainPos.value = ""
        _secondaryPos.value = ""
    }
}