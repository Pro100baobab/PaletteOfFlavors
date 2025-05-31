package com.paletteofflavors

import ViewModels.CreateRecipeViewModel
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paletteofflavors.databinding.FragmentCreateRecipeBinding


class CreateRecipeFragment : Fragment() {

    private lateinit var _binding: FragmentCreateRecipeBinding
    private val binding get() = _binding

    private lateinit var viewModel: CreateRecipeViewModel
    private var userChangeFlag = true



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            viewModel = (requireActivity() as MainActivity).createRecipeViewModel
        } finally {

        }


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
            if (userChangeFlag) {
                text?.toString()?.toInt()?.let {
                    viewModel.setTimeInMinutes(text.toString().toInt())
                }
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
            if (binding.recipeCookingTimeEdit.text.toString() != newText.toString()) {
                userChangeFlag = false
                binding.recipeCookingTimeEdit.setText(newText)
                userChangeFlag = true
            }

        }

        binding.backToFavoritesButton.setOnClickListener {
            (requireActivity() as MainActivity).showNormalFragment(FavoritesFragment())
        }

        binding.saveButton.setOnClickListener {
            // Проверяем, что все обязательные поля заполнены
            if (binding.recipeNameEdit.text.isNullOrEmpty() ||
                binding.ingredientsEdit.text.isNullOrEmpty() ||
                binding.instructionsEdit.text.isNullOrEmpty() ||
                binding.recipeCookingTimeEdit.text.toString().isEmpty()
            ) {

                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                    Toast.makeText(requireContext(), "Рецепт сохранен!", Toast.LENGTH_SHORT).show()
                    viewModel.cleanRecipeData()
                    (requireActivity() as MainActivity).showNormalFragment(FavoritesFragment())
                    // Возврат назад
                }
                .setNegativeButton("Отмена", null)
                .show()

            //viewModel.saveRecipe()
        }
    }
}