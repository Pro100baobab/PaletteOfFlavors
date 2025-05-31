package com.paletteofflavors

import ViewModels.CreateRecipeViewModel
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.paletteofflavors.databinding.FragmentCreateRecipeBinding


class CreateRecipeFragment : Fragment() {

    private lateinit var _binding: FragmentCreateRecipeBinding
    private val binding get() = _binding

    private val viewModel = (requireActivity() as MainActivity).createRecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add Listeners
        binding.recipeNameEdit.doAfterTextChanged {
            editable ->
            editable?.toString()?.let {
                viewModel.setTitle(it)
            }
        }
        binding.ingredientsEdit.doAfterTextChanged {
            editable ->
            editable?.toString()?.let {
                viewModel.setIngredients(it)
            }
        }
        binding.instructionsEdit.doAfterTextChanged {
            editable ->
            editable?.toString()?.let {
                viewModel.setIngredients(it)
            }
        }
        binding.recipeCookingTimeEdit.doAfterTextChanged {
            text: Editable? ->
            text?.toString()?.toInt()?.let {
                viewModel.setTimeInMinutes(text.toString().toInt())
            }
        }


        // Add Observer
    }
}