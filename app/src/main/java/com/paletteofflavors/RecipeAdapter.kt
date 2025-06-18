package com.paletteofflavors


import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import domain.Recipe

// For user's recipes
class RecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val removeItem: (Recipe) -> Unit
): RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {

    class RecipeHolder(view: View): RecyclerView.ViewHolder(view){
        val titleOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_name)
        val likesOfRecipeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val commentsOfRecipeIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val timeOfRecipe: TextView = itemView.findViewById(R.id.favorite_fragment_item_time)
        val savedImageView: ImageView = itemView.findViewById(R.id.favorites_fragment_item_saved_icon)
        val complexity: RatingBar = itemView.findViewById(R.id.complexityRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {

        val recipe = recipeList[position]

        editHolderVisualization(holder, recipe)
        setUpHolderListener(holder, recipe)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }




    private fun editHolderVisualization(holder: RecipeHolder, recipe: Recipe){

        holder.run {
            savedImageView.setImageResource(R.drawable.delete_24px)
            savedImageView.imageTintList = ColorStateList.valueOf(Color.BLACK)
            likesOfRecipeIcon.isVisible = false
            commentsOfRecipeIcon.isVisible = false

            titleOfRecipe.text = recipe.title

            timeOfRecipe.text = recipe.cookTime.toString()
            complexity.rating = recipe.complexity.toFloat()
        }

    }

    private fun setUpHolderListener(holder: RecipeHolder, recipe: Recipe){
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }

        holder.savedImageView.setOnClickListener {
            removeItem(recipe)
        }
    }
}