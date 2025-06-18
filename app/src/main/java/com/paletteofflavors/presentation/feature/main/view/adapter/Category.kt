package com.paletteofflavors.presentation.feature.main.view.adapter

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Category(
    val id: Int,
    @StringRes val nameResId: Int,
    val name: String,
    @DrawableRes val icon: Int? = null
) {
    constructor(context: Context, id: Int, @StringRes nameResId: Int, @DrawableRes icon: Int? = null) :
            this(id, nameResId, context.getString(nameResId), icon)
}