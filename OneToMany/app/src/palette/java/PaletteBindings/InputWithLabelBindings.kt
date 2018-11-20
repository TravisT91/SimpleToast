package com.engageft.feature.PaletteBindings

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.engageft.apptoolbox.view.BaseInputWithLabel
import com.engageft.feature.Palette
import com.engageft.onetomany.R

/**
 * InputWithLabelBinding
 * </p>
 * Contains the bindings for InputWithLabel
 * </p>
 * Created by Travis Tkachuk 11/16/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

@BindingAdapter("inputWithLabel.setThemeWithPalette", requireAll = true)
fun BaseInputWithLabel.setThemeWithPalette(shouldTheme: Boolean){
    if (shouldTheme) {

        setLineTint(
                getInputStateList(
                        enabledColor = ContextCompat.getColor(context, R.color.structure3),
                        disabledColor = ContextCompat.getColor(context, R.color.structure3),
                        pressedColor = Palette.primaryColor,
                        focusedColor = Palette.primaryColor,
                        notFocused = ContextCompat.getColor(context, R.color.structure3)))

        getTextSizeAndTypefaceFromParisStyle(context,Palette.Caption1).let{
            errorTypeface = it.second
            errorTextSize = it.first
        }

        labelTextColor = getInputStateList(
                        enabledColor = ContextCompat.getColor(context, R.color.structure5),
                        disabledColor = ContextCompat.getColor(context, R.color.structure4),
                        pressedColor = Palette.primaryColor,
                        focusedColor = Palette.primaryColor,
                        notFocused = ContextCompat.getColor(context, R.color.structure5))

        setInputTextColor(
                getTextStateList(
                        pressedColor = ContextCompat.getColor(context, R.color.structure6),
                        disabledColor = ContextCompat.getColor(context, R.color.structure4),
                        enabledColor = ContextCompat.getColor(context, R.color.structure6)))

        setInputAndLabelTypeface(getTextSizeAndTypefaceFromParisStyle(context,Palette.Body).second!!)
    }
}