package com.paletteofflavors

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.transition.Visibility
import com.paletteofflavors.databinding.FragmentFridgeBinding


class FridgeFragment : Fragment() {

    var _binding: FragmentFridgeBinding? = null
    val binding get() = _binding!!

    class Direction(){
        val forward = "forward"
        val downward = "downward"
        var direction = forward
    }


    val mostHaveIngredients = listOf<String>(
        "Спагетти", "Бекон", "Яйца", "Пармезан", "Молоко",
        "Тыква", "Куриная грудка", "Шоколад", "Сливки"
    ).sorted()

    val mostHaveDirection = Direction()
    val fruitsDirection = Direction()
    val vegetablesDirection = Direction()
    val dairyDirection = Direction()
    val berriesDirection = Direction()
    val mushroomsDirection = Direction()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        mostHaveDirection.direction = Direction().downward
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMostHaveIngredient()
        setListeners()
    }

    private fun setMostHaveIngredient() {
    }

    private fun setListeners() {

        binding.mostHave.setOnClickListener {
            mostHaveVisible(mostHaveDirection, binding.mostHaveArrow)
        }


        binding.fruitsButton.setOnClickListener {
            changeArrowRotation(binding.fruitsButton, fruitsDirection)
        }
        binding.vegetablesButton.setOnClickListener {
            changeArrowRotation(binding.vegetablesButton, vegetablesDirection)
        }
        binding.dairyButton.setOnClickListener {
            changeArrowRotation(binding.dairyButton, dairyDirection)
        }
        binding.berriesButton.setOnClickListener {
            changeArrowRotation(binding.berriesButton, berriesDirection)
        }
        binding.mushroomsButton.setOnClickListener {
            changeArrowRotation(binding.mushroomsButton, mushroomsDirection)
        }
    }

    private fun mostHaveVisible(Direction: Direction, imageView: ImageView) {

        val arrowForward = R.drawable.arrow_forward_24px
        val arrowDownward = R.drawable.arrow_downward_24px


        val newIcon: Int = when(Direction.direction) {
            "forward" -> {
                Direction.direction = Direction().downward
                binding.mostHaveContent.visibility = View.VISIBLE
                arrowDownward
            }
            "downward" -> {
                Direction.direction = Direction().forward
                binding.mostHaveContent.visibility = View.GONE
                arrowForward
            }
            else -> {
                Direction.direction = Direction().downward
                binding.mostHaveContent.visibility = View.GONE
                arrowForward
            }
        }

        imageView.setImageResource(newIcon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeArrowRotation(button: Button, Direction: Direction) {

        val currentDrawables = button.compoundDrawablesRelative
        val arrowForward = ContextCompat.getDrawable(button.context, R.drawable.arrow_forward_24px)
        val arrowDownward = ContextCompat.getDrawable(button.context, R.drawable.arrow_downward_24px)

        val newIcon: Drawable? = when(Direction.direction) {
            "forward" -> {
                Direction.direction = Direction().downward
                arrowDownward
            }
            "downward" -> {
                Direction.direction = Direction().forward
                arrowForward
            }
            else -> {
                Direction.direction = Direction().downward
                arrowForward
            }
        }

        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            currentDrawables[0],
            currentDrawables[1],
            newIcon,
            currentDrawables[3]
        )
    }
}