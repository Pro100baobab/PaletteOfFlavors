package com.paletteofflavors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paletteofflavors.databinding.ActivityMainBinding
import domain.Recipe

//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"


class FavoritesFragment() : Fragment() {

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var hintRecipe: TextView
    private lateinit var hintuserRecipe: TextView

    private lateinit var savedRadioButton: RadioButton
    private lateinit var userRadioButton: RadioButton
    private lateinit var radioGroup: RadioGroup


    //private var param1: String? = null
    //private var param2: String? = null

    //private var cardView: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        hintRecipe = view.findViewById(R.id.favorites_fragment_missing_item_hint)
        hintuserRecipe = view.findViewById(R.id.favorites_fragment_missing_item_hint2)
        radioGroup = view.findViewById(R.id.favorites_fragment_radioGroup)
        savedRadioButton = view.findViewById(R.id.favorites_fragment_savedRecipes)
        userRadioButton = view.findViewById(R.id.favorites_fragment_myRecipes)

        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView)
        recipesRecyclerView.layoutManager = LinearLayoutManager(context)


        return view
    }

    override fun onStart() {
        super.onStart()

        hintRecipe.visibility = View.INVISIBLE
        hintuserRecipe.visibility = View.INVISIBLE

        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        when (checkedRadioButtonId) {
            R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
            R.id.favorites_fragment_myRecipes -> updateMyRecipes()
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->

            when(i){
                R.id.favorites_fragment_savedRecipes -> updateSavedRecipes()
                R.id.favorites_fragment_myRecipes -> updateMyRecipes()
            }
        }
    }

    private fun updateMyRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        val recipelist = getUserRecipeList()
        recipeAdapter = RecipeAdapter(recipelist)
        recipesRecyclerView.adapter = recipeAdapter

        if(recipelist.size == 0){
            hintuserRecipe.visibility = View.VISIBLE
        }
    }

    private fun updateSavedRecipes() {
        hintuserRecipe.visibility = View.INVISIBLE
        hintRecipe.visibility = View.INVISIBLE

        val recipelist = getSavedRecipeList()
        recipeAdapter = RecipeAdapter(recipelist)
        recipesRecyclerView.adapter = recipeAdapter

        if(recipelist.size == 0){
            hintRecipe.visibility = View.VISIBLE
        }
    }


    private fun getSavedRecipeList(): ArrayList<Recipe> {
        // TODO(): Inclement logic of getting data from local database(saved recipes)
        val recipelist = arrayListOf<Recipe>(
            Recipe(
                id = 1,
                title = "Пример 1",
                ingredients = List<String>(2, {"биба"; "боба"}),
                instruction = "Инструкция",
                cookTime = 60,
                comments = 4,
                likes = 5,
                imageUrl = null
            ),
            Recipe(2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                null,120, 7, likes = 3),
            Recipe(2, "Пример 2", listOf("бибаF", "бобаF"), "Инструкция 2",
                null,120, 7, likes = 3)
        )

        return recipelist
    }

    private fun getUserRecipeList(): ArrayList<Recipe> {
        // TODO(): Inclement logic of getting data from local database(user recipes)
        return ArrayList<Recipe>()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoritesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoritesFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                    //putString(ARG_PARAM2, param2)
                }
            }
    }
}