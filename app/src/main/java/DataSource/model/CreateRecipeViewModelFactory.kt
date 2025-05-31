package DataSource.model

import DataSource.Local.RecipeDao
import ViewModels.CreateRecipeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateRecipeViewModelFactory(private val recipeDao: RecipeDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRecipeViewModel(recipeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}