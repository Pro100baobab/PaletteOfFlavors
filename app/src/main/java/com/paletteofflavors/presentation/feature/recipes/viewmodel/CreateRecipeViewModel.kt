package com.paletteofflavors.presentation.feature.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.domain.model.NetworkRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateRecipeViewModel(
    private val remoteRepository: RecipeRemoteRepository? = null
): ViewModel() {

    // region <LiveData initialization>
    private val _title = MutableLiveData<String>()
    private val _ingredients = MutableLiveData<String>()
    private val _instruction = MutableLiveData<String>()
    private val _timeInMinutes = MutableLiveData<String>()
    private val _ratingBarCount = MutableLiveData<String>()
    private val _mainCategory = MutableLiveData<String>()
    private val _secondaryCategory = MutableLiveData<String>()
    private var _mainPos = MutableLiveData<String>()
    private val _secondaryPos = MutableLiveData<String>()
    private val _isPublic = MutableLiveData<Boolean>(true)

    val title: LiveData<String> = _title
    val ingredients: LiveData<String> = _ingredients
    val instruction: LiveData<String> = _instruction
    val timeInMinutes: LiveData<String> = _timeInMinutes
    val ratingBarCount: LiveData<String> = _ratingBarCount
    val mainCategory: LiveData<String> = _mainCategory
    val secondaryCategory: LiveData<String> = _secondaryCategory
    val mainPos: LiveData<String> = _mainPos
    val secondaryPos: LiveData<String> = _secondaryPos
    val isPublic: LiveData<Boolean> = _isPublic
    // endregion

    private val _saveResult = MutableStateFlow<Result<Boolean>?>(null)
    val saveResult: StateFlow<Result<Boolean>?> = _saveResult

    // region <LiveData functions>
    fun setTitle(title: String) { _title.value = title }

    fun setIngredients(ingredients: String){ _ingredients.value = ingredients }

    fun setInstruction(instruction: String){ _instruction.value = instruction }

    fun setTimeInMinutes(timeInMinutes: Int){
        _timeInMinutes.value = if (timeInMinutes == 0) "" else timeInMinutes.toString()
    }

    fun setRatingBarCount(starsCount: Float){
        _ratingBarCount.value = if (starsCount == 0f) "" else starsCount.toString()
    }

    fun setMainCategory(category: String, position: String){
        _mainCategory.value = category
        _mainPos.value = position
    }

    fun setSecondaryCategory(category: String, position: String){
        _secondaryCategory.value = category
        _secondaryPos.value = position
    }

    fun setIsPublic(public: Boolean) { _isPublic.value = public }
    // endregion

    // Чистка черновика
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
        _isPublic.value = true
        _saveResult.value = null
    }

    /** Формирует объект NetworkRecipe без сохранения в локальную БД. */
    fun buildRecipe(): NetworkRecipe {
        val ingredientsList = _ingredients.value?.split("\n")?.filter {
            it.isNotBlank()
        } ?: emptyList()

        return NetworkRecipe(
            recipeId = 0, // будет присвоено сервером
            title = _title.value ?: "",
            ingredients = ingredientsList,
            instruction = _instruction.value ?: "",
            cookTime = _timeInMinutes.value?.toIntOrNull() ?: 0,
            complexity = _ratingBarCount.value?.toFloat()?.toInt() ?: 1,
            mainCategory = _mainCategory.value ?: "",
            secondaryCategory = _secondaryCategory.value ?: "",
            isPublic = _isPublic.value ?: true,
            likedListOfUsers = emptyList(),
            savedListOfUsers = emptyList(),
            commentsCount = 0,
            likesCount = 0,
            imageUrl = null,
            dateTime = "",
            ownerId = null
        )
    }

    fun saveRecipeToServer(ownerId: Int) {
        val recipe = buildRecipe()
        viewModelScope.launch {
            try {
                val success = remoteRepository?.saveRecipe(recipe, ownerId) ?: false
                _saveResult.value = Result.success(success)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }
}
