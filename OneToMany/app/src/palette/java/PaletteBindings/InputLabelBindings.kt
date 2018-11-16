package com.engageft.feature.PaletteBindings

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.engageft.apptoolbox.view.BaseInputWithLabel
import com.engageft.feature.Palette
import com.engageft.onetomany.R

/**
 * TODO: Class Name
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 11/16/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

@BindingAdapter("setThemeWithPalette", requireAll = true)
fun BaseInputWithLabel.setThemeWithPalette(shouldTheme: Boolean){
    if (shouldTheme) {
        val structureThree = ContextCompat.getColor(context, R.color.structure3)
        this.setLineTint(
                getInputLabelStateList(
                        enabledColor = Palette.primaryColor,
                        disabledColor = structureThree,
                        pressedColor = Palette.primaryColor,
                        focusedColor = structureThree,
                        notFocused = structureThree))
    }
}