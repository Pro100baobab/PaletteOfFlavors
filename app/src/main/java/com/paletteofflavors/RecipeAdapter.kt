package com.paletteofflavors

import DataSource.Network.NetworkRecipe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import domain.Recipe


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
    private val removeItem: (NetworkRecipe) -> Unit
): RecyclerView.Adapter<NetworkRecipeAdapter.RecipeHolder>() {

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



    private val recipes = mutableListOf<NetworkRecipe>()

    fun addRecipe(recipe: NetworkRecipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
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

        with(holder) {
            titleOfRecipe.text = networkRecipe.title
            likesOfRecipe.text = networkRecipe.likesCount.toString()
            commentsOfRecipe.text = networkRecipe.commentsCount.toString()
            timeOfRecipe.text = networkRecipe.cookTime.toString()
        }


        holder.itemView.setOnClickListener {
            onItemClick(networkRecipe)
        }

        holder.savedImageView.setOnClickListener {
            //removeItem(networkRecipe)
        }
    }
}