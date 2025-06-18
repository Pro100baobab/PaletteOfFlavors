package com.paletteofflavors

import DataSource.Network.NetworkRecipe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// For network/cached and saved recipes
class NetworkRecipeAdapter(
    private val onItemClick: (NetworkRecipe) -> Unit,
    private val onSaveOrDeleteButtonClick: (NetworkRecipe, RecipeHolder) -> Unit,
    private val isSaved: (Int) -> Flow<Boolean>
): RecyclerView.Adapter<NetworkRecipeAdapter.RecipeHolder>() {


    class RecipeHolder(view: View): RecyclerView.ViewHolder(view){
        val titleOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_name)
        val likesOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_likes)
        val commentsOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_comments)
        val timeOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_time)
        val savedOrDeletedImageView: ImageView = itemView.findViewById(R.id.favorites_fragment_item_saved_icon)
        val complexity: RatingBar = itemView.findViewById(R.id.complexityRatingBar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(itemView)
    }

    private val recipes = mutableListOf<NetworkRecipe>()
    private val adapterScope = CoroutineScope(Dispatchers.Main) // Create a CoroutineScope for the adapter

    fun addRecipe(recipe: NetworkRecipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }

    fun addRecipes(newRecipes: List<NetworkRecipe>) {
        val startPosition = recipes.size
        recipes.addAll(newRecipes)
        notifyItemRangeInserted(startPosition, newRecipes.size)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {

        val networkRecipe = recipes[position]

        bindHolder(holder, networkRecipe)
        setUpHolderListener(holder, networkRecipe)
    }





    private fun bindHolder(holder: RecipeHolder, networkRecipe: NetworkRecipe){
        // Запускаем корутину для наблюдения за Flow
        adapterScope.launch {
            isSaved(networkRecipe.recipeId).collect { isSaved ->
                // Обновляем изображение, когда значение Flow меняется
                if (isSaved) {
                    holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_saved)
                } else {
                    holder.savedOrDeletedImageView.setImageResource(R.drawable.icon_unsaved)
                }
            }
        }

        with(holder) {
            titleOfRecipe.text = networkRecipe.title
            likesOfRecipe.text = networkRecipe.likesCount.toString()
            commentsOfRecipe.text = networkRecipe.commentsCount.toString()
            timeOfRecipe.text = networkRecipe.cookTime.toString()
            complexity.rating = networkRecipe.complexity.toFloat()
        }
    }
    private fun setUpHolderListener(holder: RecipeHolder, networkRecipe: NetworkRecipe){
        holder.itemView.setOnClickListener {
            onItemClick(networkRecipe)
        }

        // Для удаления или сохранения в локальную бд сетевых рецептов
        holder.savedOrDeletedImageView.setOnClickListener {
            onSaveOrDeleteButtonClick(networkRecipe, holder)
        }
    }
}



