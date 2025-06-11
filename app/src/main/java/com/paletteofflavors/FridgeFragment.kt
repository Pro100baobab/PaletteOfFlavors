package com.paletteofflavors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paletteofflavors.databinding.FragmentFridgeBinding


class FridgeFragment : Fragment() {

    var _binding: FragmentFridgeBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() {

        //val drawables = button.compoundDrawablesRelative // Получаем массив drawables [start, top, end, bottom]
        //val currentDrawableEnd = drawables[2] // Текущий drawableEnd (индекс 2)
        binding.fruitsButton.setOnClickListener {

        }

        //binding.fruitsButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, end = R.drawable.arrow_forward_24px)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}