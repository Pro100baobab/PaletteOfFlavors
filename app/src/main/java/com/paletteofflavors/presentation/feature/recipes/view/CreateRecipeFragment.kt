package com.paletteofflavors.presentation.feature.recipes.view

import com.paletteofflavors.presentation.feature.recipes.viewmodel.CreateRecipeViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.databinding.FragmentCreateRecipeBinding
import com.paletteofflavors.presentation.feature.main.view.FavoritesFragment

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    private var userChangeFlag = true


    private val viewModel: CreateRecipeViewModel by lazy {
        ((requireActivity() as MainActivity).createRecipeViewModel)
    }


    val mainCategories = listOf("завтрак", "обед", "ужин", "десерт")
    val secondaryCategories = listOf(
        "Холодные закуски", "Горячие закуски", "Овощные салаты",
        "Мясные салаты", "Салаты с птицей", "Рыбные салаты", "Супы",
        "Овощные блюда", "Блюда с мясом", "Блюда с птицей",
        "Блюда с рыбой", "Гарниры", "Выпечка", "Напитки",
        "Первые блюда", "Вторые блюда"
    )

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSpinners()
        setUpListeners()
        setUpObservers()
    }


    override fun onResume() {
        super.onResume()
        restoreСategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // Восстановить выбор в черновике
    private fun restoreСategories(){
        viewModel.mainPos.value?.let { pos ->
            if (pos.isNotEmpty()) {
                binding.spinner1.setSelection(pos.toInt())
            }
        }
        viewModel.secondaryPos.value?.let { pos ->
            if (pos.isNotEmpty()) {
                binding.spinner2.setSelection(pos.toInt())
            }
        }
    }

    private fun saveConfirmDialog(){
        // Сохраняем рецепт
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.Do_save_recipe))
            .setMessage(getString(R.string.sure_to_save))
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                viewModel.saveRecipe()

                Toast.makeText(requireContext(),
                    getString(R.string.Save_successful), Toast.LENGTH_SHORT)
                    .show()
                viewModel.cleanRecipeData()
                (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())

            }
            .setNegativeButton(getString(R.string.Cancellation), null)
            .show()
    }

    private fun setUpObservers() {
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
    }

    private fun setUpListeners() {
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

        binding.setComplexityRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                viewModel.setRatingBarCount(rating)
            }
        }

        binding.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position) as? String
                viewModel.setMainCategory(selectedItem.toString(), position.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position) as? String
                viewModel.setSecondaryCategory(selectedItem.toString(), position.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Обработка кнопок
        binding.backToFavoritesButton.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(FavoritesFragment())
        }

        binding.saveButton.setOnClickListener {

            binding.run {
                // Check validation
                if (recipeNameEdit.text.isNullOrEmpty() ||
                    ingredientsEdit.text.isNullOrEmpty() ||
                    instructionsEdit.text.isNullOrEmpty() ||
                    recipeCookingTimeEdit.text.toString().isEmpty() ||
                    setComplexityRatingBar.rating == 0f
                ) {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.Fill_in_all_fields), Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                saveConfirmDialog()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpSpinners() {

        spinner1 = binding.spinner1
        spinner2 = binding.spinner2


        val adapter1 = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mainCategories
        )
        val adapter2 = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            secondaryCategories
        )

        spinner1.adapter = adapter1
        spinner2.adapter = adapter2

        val popup = ListPopupWindow(requireContext())
        popup.setAdapter(spinner2.adapter as ArrayAdapter<*>)
        popup.anchorView = spinner2
        popup.height =
            (300 * resources.displayMetrics.density).toInt() // Максимальная высота в dp, преобразованная в пиксели
        popup.setOnItemClickListener { _, _, position, _ ->
            spinner2.setSelection(position)
            popup.dismiss()
        }
        spinner2.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                popup.show()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

    }
}