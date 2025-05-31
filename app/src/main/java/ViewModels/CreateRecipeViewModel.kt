package ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateRecipeViewModel: ViewModel() {

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

}