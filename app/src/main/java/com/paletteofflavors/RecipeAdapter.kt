package com.paletteofflavors

import DataSource.Local.SavedRecipeDao
import DataSource.Network.NetworkRecipe
import Repositories.toSavedRecipe
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import domain.Recipe
import domain.SavedRecipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class RecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val removeItem: (Recipe) -> Unit
): RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {

    class RecipeHolder(view: View): RecyclerView.ViewHolder(view){
        val titleOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_name)
        val likesOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_likes)
        val commentsOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_comments)
        val timeOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_time)
        val savedImageView: ImageView = itemView.findViewById(R.id.favorites_fragment_item_saved_icon)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(itemView)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }



    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {

        holder.savedImageView.setImageResource(R.drawable.delete_24px)
        holder.savedImageView.imageTintList = ColorStateList.valueOf(Color.BLACK)

        val recipe = recipeList[position]

        holder.titleOfRecipe.text = recipe.title
        //holder.likesOfRecipe.text = recipeList[position].likes.toString()
        //holder.commentsOfRecipe.text = recipeList[position].comments.toString()
        holder.timeOfRecipe.text = recipe.cookTime.toString()

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }

        holder.savedImageView.setOnClickListener {
            removeItem(recipe)
        }
    }
}



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

    fun clearRecipes() {
        recipes.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {

        val networkRecipe = recipes[position]

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
        }


        holder.itemView.setOnClickListener {
            onItemClick(networkRecipe)
        }

        // Для удаления или сохранения в локальную бд сетевых рецептов
        holder.savedOrDeletedImageView.setOnClickListener {
            onSaveOrDeleteButtonClick(networkRecipe, holder)
        }
    }
}



