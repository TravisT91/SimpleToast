package com.engageft.fis.pscu.feature.palettebindings

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.engageft.apptoolbox.R
import com.engageft.apptoolbox.view.PillButton
import com.engageft.fis.pscu.feature.branding.Palette

/**
 * PillButtonBindings
 * </p>
 * Contains the bindings that are used to style pill buttons using the Paris
 * </p>
 * Created by Travis Tkachuk 11/14/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

//Pill Button Bindings
@BindingAdapter("PillButton.setThemeFilled", requireAll = true)
fun PillButton.setThemeFilled(isFilled: Boolean){
    if (isFilled){
        this.setColorStateList(
                fillPressed = getColor40PercentBlacker(Palette.primaryColor),
                strokePressed = getColor40PercentBlacker(Palette.primaryColor),
                fillEnabled = Palette.primaryColor,
                strokeEnabled = Palette.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, R.color.structure2),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = ContextCompat.getColor(this.context, R.color.white),
                        enabledColor = ContextCompat.getColor(this.context, R.color.white),
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    }
}

@BindingAdapter("PillButton.setThemeOutlined", requireAll = true)
fun PillButton.setThemeOutlined(isOutlined: Boolean){
    if (isOutlined){
        this.setColorStateList(
                fillPressed = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokePressed = getColor40PercentBlacker(Palette.primaryColor),
                fillEnabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeEnabled = Palette.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = getColor40PercentBlacker(Palette.primaryColor),
                        enabledColor = Palette.primaryColor,
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    }
}