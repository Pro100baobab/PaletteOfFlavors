package com.paletteofflavors.presentation.feature.recipes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.paletteofflavors.domain.model.NetworkRecipe

class CreateRecipeViewModel(): ViewModel() {

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
        _isPublic.value = false
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
            imageUrl = null, // нужно реализовать загрузку фотографии и получение url из FireBase
            dateTime = "", // будет присвоено сервером
            ownerId = null // нужно передавать id авторизованного пользователя
        )
    }

    // TODO: Отправка на сервер должна быть реализована отдельно (заглушка)

    /*
    // Функция сохранения рецепта в локальную БД
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
*/
}

// TODO: Добавить сохранение рецепта во временную локальную БД собственных рецептов после
//  успешного создания на сервере или синхронизацию и отправку на сервер при первом подключении