package com.paletteofflavors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.name.text = category.name
        holder.itemView.setOnClickListener { onItemClick(category) }

        val bgColor = if (position % 2 == 0) {
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.light_pink)
        }
        holder.itemView.setBackgroundColor(bgColor)

        /*
        category.icon?.let { iconRes ->
            holder.icon.setImageResource(iconRes)
            holder.icon.visibility = View.VISIBLE
        } ?: run {
            holder.icon.visibility = View.GONE
        }*/
    }

    override fun getItemCount() = categories.size
}


data class Category(
    val id: Int,
    val name: String,
    @DrawableRes val icon: Int? = null
)