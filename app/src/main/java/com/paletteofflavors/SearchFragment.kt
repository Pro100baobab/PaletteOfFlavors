package com.paletteofflavors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.paletteofflavors.databinding.BottomSheetLayoutBinding
import com.paletteofflavors.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomsheetScrollView: NestedScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheetBehavior()
        setupCategories()

        setupOnClickListeners()


    }

    private fun setupOnClickListeners() {
        binding.searchFragmentDinnerButton.setOnClickListener{
            Toast.makeText(requireContext(), "Selected: Dinner", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentLaunchButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selected: Launch", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentDessertsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selected: Desert", Toast.LENGTH_SHORT).show()
        }
        binding.searchFragmentBreakfastButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selected: Breakfast", Toast.LENGTH_SHORT).show()
        }

        binding.searchFragmentFilterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selected: Show ALL Filters", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomSheetBehavior() {
        bottomsheetScrollView = binding.bottomSheetInclude.bottomSheet
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetScrollView)
        bottomSheetBehavior.peekHeight = 250
        bottomSheetBehavior.maxHeight = 1000
    }

    // В вашем Activity/Fragment:
    private fun setupCategories() {
        val categories = listOf(
            Category(1, getString(R.string.cold_eat), R.drawable.icon_saved),
            Category(2, getString(R.string.hot_eat)),
            Category(3, getString(R.string.vegetable_salads)),
            Category(4, getString(R.string.meat_salads)),
            Category(5, getString(R.string.poultry_salads)),
            Category(6, getString(R.string.fish_salads)),
            Category(7, getString(R.string.soups)),
            Category(8, getString(R.string.vegetable_dishes)),
            Category(9, getString(R.string.meat_dishes)),
            Category(10, getString(R.string.poulry_dishes)),
            Category(11, getString(R.string.fish_dishes)),
            Category(12, getString(R.string.side_dishes)),
            Category(13, getString(R.string.bakery)),
            Category(14, getString(R.string.drinks)),
            Category(15, getString(R.string.first_courses)),
            Category(16, getString(R.string.second_courses))
        )

        val adapter = CategoriesAdapter(categories) { category ->
            Toast.makeText(requireContext(), "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
        }

        binding.bottomSheetInclude.categoriesRecyclerView.adapter = adapter
        binding.bottomSheetInclude.categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}