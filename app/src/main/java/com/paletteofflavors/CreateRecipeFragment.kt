package com.paletteofflavors

import DataSource.model.CreateRecipeViewModelFactory
import ViewModels.CreateRecipeViewModel
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paletteofflavors.databinding.FragmentCreateRecipeBinding


class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    //private lateinit var viewModel: CreateRecipeViewModel
    private var userChangeFlag = true


    private val viewModel: CreateRecipeViewModel by lazy {
        ((requireActivity() as MainActivity).createRecipeViewModel)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = (requireActivity() as MainActivity).createRecipeViewModel


        // Add Listeners
        binding.recipeNameEdit.doAfterTextChanged { editable ->
            if (userChangeFlag) {
                editable?.toString()?.let {
                    viewModel.setTitle(it)
                }
            }

        }
        binding.ingredientsEdit.doAfterTextChanged { editable ->
            if (userChangeFlag) {
                editable?.toString()?.let {
                    viewModel.setIngredients(it)
                }
            }
        }
        binding.instructionsEdit.doAfterTextChanged { editable ->
            if (userChangeFlag) {
                editable?.toString()?.let {
                    viewModel.setInstruction(it)
                }
            }
        }
        binding.recipeCookingTimeEdit.doAfterTextChanged { text: Editable? ->
            try {
                if (userChangeFlag) {
                    text?.toString()?.toInt()?.let {
                        viewModel.setTimeInMinutes(text.toString().toInt())
                    }
                }
            } catch (_: Exception) {
                // стерли последний символ
                viewModel.setTimeInMinutes(0)
            }
        }

        //binding.setComplexityRatingBar.rating = recipe.complexity.toFloat()

        binding.setComplexityRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                viewModel.setRatingBarCount(rating)
            }
        }


        // Add Observer
        viewModel.title.observe(viewLifecycleOwner) {

                newText ->
            if (binding.recipeNameEdit.text.toString() != newText) {
                userChangeFlag = false
                binding.recipeNameEdit.setText(newText)
                userChangeFlag = true
            }

        }
        viewModel.ingredients.observe(viewLifecycleOwner) {

                newText ->
            if (binding.ingredientsEdit.text.toString() != newText) {
                userChangeFlag = false
                binding.ingredientsEdit.setText(newText)
                userChangeFlag = true
            }

        }
        viewModel.instruction.observe(viewLifecycleOwner) { newText ->
            if (binding.instructionsEdit.text.toString() != newText) {
                userChangeFlag = false
                binding.instructionsEdit.setText(newText)
                userChangeFlag = true
            }

        }
        viewModel.timeInMinutes.observe(viewLifecycleOwner) { newText ->
            if (binding.recipeCookingTimeEdit.text.toString() != newText) {
                userChangeFlag = false
                newText?.let {
                    binding.recipeCookingTimeEdit.setText(it)
                }
                userChangeFlag = true
            }

        }


        viewModel.ratingBarCount.observe(viewLifecycleOwner) { stars ->
            if (binding.setComplexityRatingBar.rating.toString() != stars) {
                userChangeFlag = false
                binding.setComplexityRatingBar.rating = stars.toFloat()
                userChangeFlag = true
            }
        }

        binding.backToFavoritesButton.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
        }

        binding.saveButton.setOnClickListener {

            binding.run {
                if (recipeNameEdit.text.isNullOrEmpty() ||
                    ingredientsEdit.text.isNullOrEmpty() ||
                    instructionsEdit.text.isNullOrEmpty() ||
                    recipeCookingTimeEdit.text.toString().isEmpty() ||
                    setComplexityRatingBar.rating == 0f
                ) {

                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }



            /*
            // Устанавливаем время приготовления
            val time = try {
                binding.recipeCookingTimeEdit.text.toString().toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Введите корректное время", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.setTimeInMinutes(time)
            */


            // Сохраняем рецепт
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Сохранить рецепт?")
                .setMessage("Вы уверены, что хотите сохранить этот рецепт?")
                .setPositiveButton("Да") { _, _ ->
                    viewModel.saveRecipe()

                    // Показываем уведомление и возвращаемся назад
                    Toast.makeText(requireContext(), "Рецепт сохранен!", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.cleanRecipeData()
                    (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())

                }
                .setNegativeButton("Отмена", null)
                .show()

            //viewModel.saveRecipe()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}