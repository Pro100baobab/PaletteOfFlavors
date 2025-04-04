package com.paletteofflavors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import domain.Recipe

//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"


class FavoritesFragment : Fragment() {

    private lateinit var recipesRececlerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

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
        recipesRececlerView = view.findViewById(R.id.recipesRecyclerView)
        recipesRececlerView.layoutManager = LinearLayoutManager(context)

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

        recipeAdapter = RecipeAdapter(recipelist)
        recipesRececlerView.adapter = recipeAdapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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